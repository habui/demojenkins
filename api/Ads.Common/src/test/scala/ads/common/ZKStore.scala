package ads.common

import org.scalatest.FunSuite
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.curator.RetryPolicy
import org.apache.zookeeper.{KeeperException, CreateMode}
import org.apache.curator.framework.recipes.cache.{ChildData, PathChildrenCacheEvent, PathChildrenCacheListener, PathChildrenCache}
import ads.common.database._
import scala.reflect.ClassTag
import ads.common.model.Website
import org.apache.curator.framework.recipes.shared.SharedCount
import scala.collection.mutable.ArrayBuffer


class ZKStoreTests extends FunSuite{

    test("driver"){
        val connectionString = "127.0.0.1:2181"
        val retryPolicy = new ExponentialBackoffRetry(1000, 3)
        val client = CuratorFrameworkFactory.newClient(connectionString, retryPolicy)
        client.start()

        ZKStore.client = client

        val driver = new ZKDataService[Website]()
        driver.start()

        val id = driver.save(new Website(1, 1, "Hello world", "desc", "xxxx"))
        val web = driver.load(id)

        assertResult("Hello world")(web.name)
        assertResult("desc")(web.description)
        assertResult("xxxx")(web.reviewType)
        assertResult(1)(web.ownerId)
        assertResult(id)(web.id)

        val list = driver.loadAll().toList
        assertResult(1)(list.size)
    }

    test("simple"){
        val connectionString = "127.0.0.1:2181"
        val retryPolicy = new ExponentialBackoffRetry(1000, 3)
        val client = CuratorFrameworkFactory.newClient(connectionString, retryPolicy)
        client.start()

        val store = new ZKStore(client, "yyy", (id,data)=>println(s"$id -> $data"))
        store.start()

        ZKStore.client = client


//        val cachedPath = new PathChildrenCache(client, "/data/hello", true)
//        cachedPath.getListenable.addListener(new Listener())
//        cachedPath.start()
//
//        client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/data/hello/id")

        Thread.sleep(1000)

        val id = store.save("y")

        Thread.sleep(1000)

        //store.update(id, "hello world")
        //store.save("hello world !!!")

        Thread.sleep(1000)

        for (child <- store.loadAll()){
            println(s"${child._1} -> ${new String(child._2)}")
        }

        Thread.sleep(1000)

        client.close()
    }
}
