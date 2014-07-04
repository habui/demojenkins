package ads.common.database

import org.apache.curator.framework.recipes.cache.{PathChildrenCache, PathChildrenCacheEvent, PathChildrenCacheListener, ChildData}
import org.apache.curator.framework.{CuratorFrameworkFactory, CuratorFramework}
import ads.common.{JsonExt, IItem}
import scala.reflect.ClassTag
import scala.collection.mutable.ArrayBuffer
import org.apache.zookeeper.CreateMode
import org.apache.curator.framework.recipes.shared.SharedCount
import org.apache.curator.retry.ExponentialBackoffRetry
import ads.common.model.Config

class Listener (f : ChildData => Unit)extends PathChildrenCacheListener{
    def childEvent(client: CuratorFramework, e: PathChildrenCacheEvent){
        if (e.getType == PathChildrenCacheEvent.Type.CHILD_ADDED ||
            e.getType == PathChildrenCacheEvent.Type.CHILD_UPDATED
        ){
            f(e.getData)
        }
    }
}

object ZKStore{
    var client: CuratorFramework = null
    def start() {
        val connectionString = Config.zooKeeperAddr.getValue
        val retryPolicy = new ExponentialBackoffRetry(1000, 3)
        client = CuratorFrameworkFactory.newClient(connectionString, retryPolicy)
        client.start()
    }
}

class ZKDataService [T <: IItem](implicit tag: ClassTag[T]) extends IDataDriver[T]{
    lazy val store = new ZKStore(ZKStore.client, tag.runtimeClass.getSimpleName, update)

    var _addOrUpdateHandler: T=>Unit = null

    def update(id: Int, json: String){
        val item = JsonExt.gson.fromJson(json, tag.runtimeClass).asInstanceOf[T]
        item.id = id

        if (_addOrUpdateHandler != null) _addOrUpdateHandler(item)
    }

    def save(item: T): Int = {
        val id = store.save(JsonExt.gson.toJson(item))
        item.id = id
        id
    }

    def load(id: Int): T = {
        val s = store.load(id)
        if (s != null) return JsonExt.gson.fromJson(s, tag.runtimeClass).asInstanceOf[T]
        return null.asInstanceOf[T]
    }

    def loadAll(): Iterator[T] = {
        val list = store.loadAll()
        val r = new ArrayBuffer[T]()

        for ((id,s) <- list){
            val item = JsonExt.gson.fromJson(s, tag.runtimeClass).asInstanceOf[T]
            item.id = id
            r += item
        }

        r.toIterator
    }

    def update(item: T): Boolean = {
        store.update(item.id, JsonExt.gson.toJson(item))
        true
    }

    def remove(id: Int): Unit = {
        val old = load(id)
        if (old != null){
            if (!old.disable){
                old.disable = true
                update(old)
            }
        }
    }

    override def onAddOrUpdate(f: T => Unit) = _addOrUpdateHandler = f

    override def start() = store.start()
}


class ZKStore(client: CuratorFramework, name: String, f: (Int, String)=>Unit){
    lazy val rawPath = s"/data/$name"
    lazy val childPath = s"/data/$name/child/_"
    lazy val idPath = s"/data/$name/id"

    silent{
        client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(rawPath)
    }

    val cached = new PathChildrenCache(client, rawPath + "/child", true)

    cached.getListenable.addListener(new Listener( data => {
        val item = convert(data)
        f(item._1, item._2)
    }))

    def start(){
        cached.start()
    }


    val counter = new SharedCount(client, idPath, 0)
    counter.start()

    def getId(): Int = {
        while(true){
            val id = counter.getCount()
            if (counter.trySetCount(id + 1)) return id + 1
        }
        return 0
    }

    def convert(item: ChildData): (Int,String)={
        val path = item.getPath
        val id = path.substring(path.length-10).toInt
        val data = new String(item.getData, "UTF-8")
        (id,data)
    }

    def save(data: String) =
        silent{
            val id = getId()
            val path = f"$childPath$id%010d"
            client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, data.getBytes("UTF-8"))
            id
        }

    def load(id: Int): String={
        try{
            val bytes = client.getData.forPath(f"$childPath$id%010d")
            return new String(bytes, "UTF-8")
        }
        catch{
            case e: Exception => return null
        }
    }

    def update(id: Int, data: String){
        silent{
            client.setData().forPath(f"$childPath$id%010d", data.getBytes("UTF-8"))
        }
    }

    def reset(){
        silent{
            client.delete().deletingChildrenIfNeeded().forPath(rawPath)
        }
    }

    def silent[T](body: => T): T = {
        try{
            body
        }
        catch{
            case e: Exception => {
                println(s"[Exception]${e.getMessage}")
                println(s"         -> ${e.getStackTraceString}")

                if (e.getCause != null){
                    println(s"          [Exception]${e.getCause.getMessage}")
                    println(s"                   ->${e.getCause.getStackTraceString}")
                }

                null.asInstanceOf[T]
            }
        }
    }

    def loadAll() = {
        val list = cached.getCurrentData
        val count = list.size()
        val r = new ArrayBuffer[(Int,String)]
        for (i <- 0 until count){
            val item = list.get(i)
            r += convert(item)
        }
        r
    }
}

