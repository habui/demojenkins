package ads.common.database

import ads.common.{JsonExt, IItem}
import scala.reflect.ClassTag
import scala.collection.mutable.ArrayBuffer
import java.util.concurrent.atomic.AtomicBoolean

class SimpleRecord (var id: Int, var content: String, var disable: Boolean = false) extends IItem

class MysqlDriver [T <: IItem](implicit tag: ClassTag[T]) extends IDataDriver[T]{

    val json = JsonExt.gson
    lazy val db = new SimpleDatabase[SimpleRecord](tag.runtimeClass.getSimpleName)(()=>new SimpleRecord(1, ""))

    var started = new AtomicBoolean(false)

    def save(item: T): Int = {
        val record = new SimpleRecord(0, json.toJson(item))
        val id = db.save(record)
        item.id = id
        refresh(item)
        id
    }

    def load(id: Int): T = {
        val record = db.load(id)
        if (record != null){
            val item  = json.fromJson(record.content, tag.runtimeClass).asInstanceOf[T]
            item.id = id
            item
        }
        else null.asInstanceOf[T]
    }

    def loadAll(): Iterator[T] = {
        val list = db.loadAll()
        val result = new ArrayBuffer[T]()
        
        for (r <- list){
            val item = json.fromJson(r.content, tag.runtimeClass).asInstanceOf[T]
            item.id = r.id
            result += item
        }

        result.toIterator
    }

    def update(item: T): Boolean = {
        val s = json.toJson(item)
        val record = new SimpleRecord(item.id, s)
        val r = db.update(record)
        refresh(item)
        r
    }

    def remove(id: Int): Unit = {
        val item = load(id)
        if (item != null && item.disable != true){
            item.disable = true
            update(item)
        }
    }

    override def start(): Unit = {
        if (started.compareAndSet(false, true)){
            for (x <- loadAll()) refresh(x)
        }
    }
}
