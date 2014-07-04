package ads.common.sessions

import org.mapdb.{DBMaker, BTreeMap}
import ads.common.model.Config
import java.io.File
import ads.common.rpc.{ProxyGen, ThriftClient, Rpc}
import ads.common.Syntaxs._
import scala.concurrent.ops._
import java.util.concurrent.atomic.AtomicInteger

import java.util.concurrent._
import java.util.concurrent.ConcurrentHashMap

class Session extends java.io.Serializable{
    var logs: Array[Int] = new Array[Int](0)
    var version = new AtomicInteger(0)

    def put (banner: Int, time: Int) = {
        this.synchronized{
            val temp = new Array[Int](logs.length + 2)
            Array.copy(logs, 0, temp, 0, logs.length)
            temp(logs.length) = banner
            temp(logs.length + 1) = time
            logs = temp
        }
    }
}

trait IStore{
    def put(key: String, value: Session) : Boolean
    def get(key: String): Session
    def getOrAdd(key: String) : Session
}

class MapStore(name: String, dir: String) extends IStore{

    val db = DBMaker.newFileDB(new File(dir)).
        closeOnJvmShutdown().
        cacheLRUEnable().
        cacheSize(100 *1000).
        make()

    spawn{
        while(Config.storeDB) {
            Thread.sleep(1*1000)
            db.commit()
        }
    }
    val map = db.getTreeMap[String,Session](name)
    val versions = db.getTreeMap[String,Int](name + "_version")

    override def getOrAdd(key: String) : Session = {
        var record = get(key)
        if(record == null) {
            this.synchronized{
                record = get(key)
                if(record == null) {
                    record = new Session()
//                    record.version = 0
                    record.version = new AtomicInteger(0)
                    put(key, record)
                }
            }
        }
        record
    }

    override def get (k: String): Session = map.get(k)

    override def put(k: String, v: Session) : Boolean = {
        val replaced =
//            if (v.version == 0){ //new born
            if (v.version.get() == 0){ //new born
                if(versions.containsKey(k)) false
                else {
                    versions.put(k, 1)
                    true
                }

            }
            else{
                if(versions.containsKey(k)) versions.replace(k, v.version.get(), v.version.get() + 1)
                else {
                    versions.put(k, v.version.get() + 1)
                    true
                }
            }

        if (replaced){
            v.version.incrementAndGet()
//            v.version = v.version + 1
            map.put(k, v)
        }

        replaced
    }
}

class TempStore() extends IStore{

    val map = new ConcurrentHashMap[String, Session]
    val versions = new ConcurrentHashMap[String, Int]()

    override def put(k: String, v: Session): Boolean = {
        val replaced =
            if (v.version.get() == 0){ //new born
                if(versions.containsKey(k)) false
                else {
                    versions.put(k, 1)
                    true
                }

            }
            else{
                versions.replace(k, v.version.get(), v.version.get() + 1)
            }
        if (replaced){
            v.version.incrementAndGet()
            map.put(k, v)
        }
        replaced
    }

    override def get(key: String): Session = map.get(key)



    override def getOrAdd(key: String) : Session = {
        var record = get(key)
        if(record == null) {
            record = new Session()
            record.version = new AtomicInteger(0)
            put(key, record)
        }
        record
    }
}



class ComplexPooledResource[T >: Null] (max: Int, factory: () => (T, ()=>Boolean)) {

    val queue = new ConcurrentLinkedQueue[(T, () => Boolean)]()
    val active = new AtomicInteger(0)

    private def get() = {
        var r : (T, () => Boolean) = null
        while (r == null){
            r = queue.poll()
            if (r == null) {
                if (active.getAndIncrement() <= max){
                    r = factory()
                }
                else{
                    active.decrementAndGet()
                    Thread.sleep(1)
                }
            }
        }
        r
    }

    private def release(r: (T, () => Boolean)) = queue.add(r)

    def use[R](f: T => R) = {
        val t = get()
        var ex = false
        try{
            f(t._1)
        }
        catch{
            case e: Throwable =>{
                ex = true
                throw e
            }
        }
        finally {
            if (t._2() && !ex) release(t)
            else active.decrementAndGet()
        }
    }
}

class PooledStore (pool: ComplexPooledResource[IStore]) extends IStore{

    def put(key: String, value: Session): Boolean = pool.use(s => s.put(key, value))

    def get(key: String): Session = pool.use(s => s.get(key))

    def getOrAdd(key: String): Session = pool.use(s => s.getOrAdd(key))
}

class SimpleStore (stores: Array[IStore]) extends IStore{

    override def get(key: String) = {
        val p = Math.abs(key.hashCode()%stores.length)
        val store = stores(p)
        store.get(key)
    }

    override def put(key: String, value: Session): Boolean = {
        val p = Math.abs(key.hashCode()%stores.length)
        val store = stores(p)
        store.put(key, value)
    }

    def getOrAdd(key: String) = {
        val p = Math.abs(key.hashCode()%stores.length)
        val store = stores(p)
        store.getOrAdd(key)
    }
}

object Session{
    var store : IStore = null

    def start(){

        val urls = Config.frequencyRemotes.get().split(";")
        val localStore = new MapStore("_freq", Config.frequencyDir.getValue)
        Rpc.register("localStore", localStore)
        println(s"isStore ${Config.storeDB} isMaster ${Config.isMaster}")
        if(Config.storeDB && !Config.isMaster) {
            val server = new Thread(new Runnable{
                def run(){
                    println(s"Service store freq is listening port ${Config.masterPort.getValue}")
                    Rpc.startServer(Config.masterPort.getValue)
                }
            })
            server.start()
        }
        Thread.sleep(10)
        val setup = loopUntilZeroException[(IStore, () => Boolean),String]((ip: String) =>{
            var result:(IStore, () => Boolean) = null
            val client = new ThriftClient(ip, Config.masterPort.getValue)
            val remote = ProxyGen.createProxy[IStore]("localStore", client.process)
            result = (remote, () => client.transport.isOpen)
            result
        }, 0, (null, ()=>false), 1*1000)

        val remotes = urls.map(
            s => new PooledStore(
                new ComplexPooledResource[IStore](
                    10,
                    () => setup(s))).asInstanceOf[IStore])
        store = new SimpleStore(remotes)
    }
}

object SiteZoneFreqStore {
    var store : IStore = null

    def start(){

        val urls = Config.frequencyRemotes.get().split(";")
        val localStore = new TempStore
        Rpc.register("tempStore", localStore)
        Thread.sleep(10)
        val setup = loopUntilZeroException[(IStore, () => Boolean),String]((ip: String) =>{
            var result:(IStore, () => Boolean) = null
            val client = new ThriftClient(ip, Config.masterPort.getValue)
            val remote = ProxyGen.createProxy[IStore]("tempStore", client.process)
            result = (remote, () => client.transport.isOpen)
            result
        }, 0, (null, ()=>false), 1*1000)

        val remotes = urls.map(
            s => new PooledStore(
                new ComplexPooledResource[IStore](
                    10,
                    () => setup(s))).asInstanceOf[IStore])
        store = new SimpleStore(remotes)
    }
}

object RetargetingStore{

    var store : IStore = null

    def start(){
        val urls = Config.frequencyRemotes.get().split(";")
        val localStore = new MapStore("_retargeting", Config.retargetingPrDir.getValue)
        Rpc.register("retargetingStore", localStore)
        Thread.sleep(10)
        val setup = loopUntilZeroException[(IStore, () => Boolean),String]((ip: String) =>{
            var result:(IStore, () => Boolean) = null
            val client = new ThriftClient(ip, Config.masterPort.getValue)
            val remote = ProxyGen.createProxy[IStore]("retargetingStore", client.process)
            result = (remote, () => client.transport.isOpen)
            result
        }, 0, (null, ()=>false), 1*1000)

        val remotes = urls.map(
            s => new PooledStore(
                new ComplexPooledResource[IStore](
                    10,
                    () => setup(s))).asInstanceOf[IStore])
        store = new SimpleStore(remotes)
    }
}
