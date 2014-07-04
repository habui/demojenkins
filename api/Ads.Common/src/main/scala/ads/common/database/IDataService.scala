package ads.common.database

import ads.common.{SecurityContext, Reflect, IItem}
import scala.reflect.ClassTag
import java.util
import java.util.concurrent.{ConcurrentSkipListSet, ConcurrentHashMap, ConcurrentSkipListMap}
import scala.collection.JavaConversions._
import ads.common.Syntaxs._
import scala.collection.mutable.ArrayBuffer
import java.util.{Date, Comparator}
import ads.common.rpc.IRpcService.process_args._Fields
import ads.common.model.{ActionLog, ActionType, DatabaseEnv}
import ads.common.services.IActionLogService
import scala.concurrent.Future
import scala.concurrent.ops._
import scala.util.control.NonFatal

trait IDataDriver[T <: IItem]{

    var _onAddOrUpdate: (T)=>Unit = null

    def save(item: T): Int
    def load(id: Int): T
    def loadAll(): Iterator[T]
    def update(item: T): Boolean
    def remove(id: Int)

    def onAddOrUpdate(f: T=>Unit): Unit = _onAddOrUpdate = f
    def start() = {}

    def refresh(item: T){
        if (_onAddOrUpdate != null) _onAddOrUpdate(item)
    }
}

trait IDataService[T <: IItem] extends IDataDriver[T]{
    def save(item: T): Int

    def load(id: Int): T

    def update(item: T): Boolean

    def remove(id: Int)

    def list(from: Int, count: Int, sortBy: String = "id", direction: String = "desc"): PagingResult[T]

    def listByIds(ids: List[Int]): Array[T]

    def listByReferenceId(from: Int = 0, count: Int = Int.MaxValue, id: Int, sortBy: String = "id", direction: String = "desc"): PagingResult[T]

    def countByRef(id: Int): Int

    def count(f: (T)=>Boolean): Int

    def list(f: (T)=> Boolean): List[T]

    def list[A](f: (T)=> Boolean, transform: (T)=>A): List[A]

    def loadAll(): Iterator[T]

    def search(query: String, pId: Int, isDisable: Boolean = false, take: Int = 10): List[T]

    def enable(id: Int)

    def disable(id: Int)

    def listDisable(from: Int, count: Int, sortBy: String = "id", direction: String = "desc"): PagingResult[T]
}

class PagingResult[T](var data: Array[T], var total: Int)


abstract class AbstractDelegateDataService[T <: IItem : ClassTag, TModel <: IItem : ClassTag](service: IDataService[T]) extends IDataService[TModel] {

    def toModel(item: T): TModel

    def fromModel(model: TModel): T


    def beforeSave(item : TModel) : Unit = {}

    def internalToModel(item: T): TModel = {
        val m = toModel(item)
        if(m!= null) {
            m.disable = item.disable
            m
        }
        else {
            null.asInstanceOf[TModel]
        }
    }

    def save(item: TModel): Int = {
        beforeSave(item)
        val id = service.save(fromModel(item))
        item.id = id
        afterSave(item)
        id
    }
    def afterSave(model: TModel) = {}

    def load(id: Int): TModel = {
        val item = service.load(id)
        if (item != null) return internalToModel(item)
        return null.asInstanceOf[TModel]
    }

    def beforeUpdate(item : TModel) : Unit =  {}
    def update(item: TModel): Boolean = {
        beforeUpdate(item)
        val r = service.update(fromModel(item))
        afterUpdate(item)
        r
    }
    def afterUpdate(item : TModel) : Unit = {

    }

    def remove(id: Int): Unit = service.remove(id)

    def enable(id: Int): Unit = service.enable(id)

    def disable(id: Int): Unit = service.disable(id)

    def list(from: Int, count: Int, sortBy: String, direction: String): PagingResult[TModel] =
        convertRecord(service.list(from, count, sortBy, direction))

    def listByReferenceId(from: Int, count: Int, id: Int, sortBy: String, direction: String): PagingResult[TModel] =
        convertRecord(service.listByReferenceId(from, count, id, sortBy, direction))

    def countByRef(id: Int): Int = service.countByRef(id)

    def loadAll() = service.loadAll().map(internalToModel)

    def search(query: String, pId: Int, isDisable: Boolean, take: Int = 10): List[TModel] = service.search(query, pId , isDisable, take).map(i=>internalToModel(i)).toList

    def convertRecord(r: PagingResult[T]): PagingResult[TModel] = {
        val list = r.data.map(item => internalToModel(item)).toList.toArray
        new PagingResult[TModel](list, r.total)
    }

    def count(f: (TModel) => Boolean): Int = service.count(item=>f(internalToModel(item)))

    def listByIds(ids: List[Int]): Array[TModel] = service.listByIds(ids).map(internalToModel).toList.toArray

    def list(f: (TModel) => Boolean): List[TModel] =
        service.list(item=>f(internalToModel(item))).map(internalToModel).toList

    def list[A](f: (TModel) => Boolean, transform: (TModel) => A): List[A] = service.list(item=>f(internalToModel(item)), item=>transform(internalToModel(item)))

    def listDisable(from: Int, count: Int, sortBy: String, direction: String): PagingResult[TModel] =
        convertRecord(service.listDisable(from, count, sortBy, direction))
}

class SqlDataService[T <: IItem : ClassTag](factory: ()=>T) extends IDataService[T]{
    val db = new SimpleDatabase[T](null)(factory)

    def save(item: T): Int = db.save(item)

    def load(id: Int): T = db.load(id)

    def update(item: T): Boolean = db.update(item)

    def remove(id: Int): Unit = db.delete(id)


    def list(from: Int, count: Int, sortBy: String, direction: String): PagingResult[T] = {
        val list = new util.ArrayList[T]()
        for (item <- db.loadAll()) list.add(item)

        new PagingResult(list.listIterator().toArray, list.size())
    }

    def loadAll() = db.loadAll()

    def loadWhere(key: String, value: Object): Iterator[T] = db.loadWhere(key, value)

    def listByIds(ids: List[Int]): Array[T] = ???

    def listByReferenceId(from: Int, count: Int, id: Int, sortBy: String, direction: String): PagingResult[T] = ???

    def countByRef(id: Int): Int = 0

    def count(f: (T) => Boolean): Int = ???

    def list(f: (T) => Boolean): List[T] = ???

    def list[A](f: (T) => Boolean, transform: (T) => A): List[A] = ???

    def enable(id: Int): Unit = ???

    def listDisable(from: Int, count: Int, sortBy: String, direction: String): PagingResult[T] = ???

    def disable(id: Int): Unit = ???

    def search(query: String, pId: Int, isDisable: Boolean, take: Int = 10): List[T] = ???
}

class GenericComparator(get: (Any)=>Any) extends Comparator[IItem]{
    def compare(o1: IItem, o2: IItem): Int = {
        val x = get(o1)
        val y = get(o2)

        if(x == null && y == null) return o1.id - o2.id
        if(x == null && y != null) return -1
        if(x != null && y == null) return 1


        val rs = x match{
            case x : Int => x - y.asInstanceOf[Int]
            case x : Long => x - y.asInstanceOf[Long]
            case x : String => x.compareToIgnoreCase(y.asInstanceOf[String])
            case x : Float => x - y.asInstanceOf[Float]
            case x : Double => x - y.asInstanceOf[Double]
            case x : Date => x.compareTo(y.asInstanceOf[Date])
            case x : List[_] => x.length - y.asInstanceOf[List[_]].length
            case _ => 0
        }

        if(rs != 0) rs.toInt
        else o1.id - o2.id
    }
}


class InMemoryDataService[T <: IItem](extract: T => Int, db: IDataDriver[T], logRef : Future[IActionLogService])(implicit tag : ClassTag[T]) extends IDataService[T] {

    val all = new ConcurrentSkipListMap[Int, T]()
    lazy val refIndex = new ConcurrentHashMap[Int, ConcurrentSkipListMap[Int, T]]()
    lazy val typeIndex = new ConcurrentHashMap[String, ConcurrentSkipListSet[T]]()
    lazy val log = logRef.value.get.get
//    for (item <- db.loadAll()) {
//        all.put(item.id, item)
//        index(item)
//    }

    db.onAddOrUpdate(item => {

        if (all.containsKey(item.id)){
            val x = all.get(item.id)
            all.remove(item.id)
            removeIndex(x)
        }

        all.put(item.id, item)
        index(item)
    })

    db.start()

    setup()


    spawn {
        if(tag.runtimeClass != classOf[ActionLog]) {
            while(true) {
                Thread.sleep(30 * 1000)
                try {
//                                    println(s"-------------Update $tag from Database------------")
                    for (item <- db.loadAll()) {
                        if (all.containsKey(item.id)){
                            val x = all.get(item.id)
                            all.remove(item.id)
                            removeIndex(x)
                        }

                        all.put(item.id, item)
                        index(item)
                    }
//                                    println("---------------------End--------------------")
                }
                catch {
                    case NonFatal(e) => printException(e, System.out)
                    case x: Throwable => printException(x, System.out)
                }
            }
        }
    }

    def setup(){}

    def beforeSave(value: T){}

    def save(item: T): Int = {

        beforeSave(item)

        val id = db.save(item)

        afterSave(item)

        //all.put(id, item)
        //index(item)
        item.id = id
        id
    }

    def afterSave(item : T) : Unit =  {
        val cl = tag.runtimeClass
        log.log(0,ActionType.CREATE,cl,null,item)

    }
    def index(item: T) {
        if(item == null)
            return
        val refId = extract(item)
        val map = refIndex.getOrAdd(refId, (id)=>new ConcurrentSkipListMap[Int, T]())
        map.put(item.id, item)

        for(entry <- Reflect.getProperties(item.getClass)) {
            val name = entry._1
            val record = entry._2
            val set = typeIndex.getOrAdd(name, (name) => new ConcurrentSkipListSet[T](new GenericComparator(record.get)))
            set.add(item)
        }
    }

    def removeIndex(item: T){
        val refId = extract(item)
        val map = refIndex.getOrAdd(refId, (id)=>new ConcurrentSkipListMap[Int, T]())
        map.remove(item.id)
        for(entry <- Reflect.getProperties(item.getClass)) {
            val name = entry._1
            val record = entry._2
            val set = typeIndex.getOrAdd(name, (name) => new ConcurrentSkipListSet[T](new GenericComparator(record.get)))
            set.remove(item)
        }
    }

    def load(id: Int): T = {
        val item = all.get(id)
        if(item != null && SecurityContext.get()(item)) item
        else null.asInstanceOf[T]
    }

    def beforeUpdate(oldVal : T, newVal : T) = {} ;

    def update(item: T): Boolean = {
        val old = this.load(item.id)
        //if(old != null) this.removeIndex(old)
        this.beforeUpdate(old,item)
        //this.index(item)
        //all.put(item.id, item)
        val ret = db.update(item)
        this.afterUpdate(old,item)
        ret
    }

    def afterUpdate(oldVal : T,newVal:  T)  = {
        val cl = tag.runtimeClass
        log.log(0,ActionType.EDIT,cl,oldVal,newVal)
    }

    def beforeRemove(id: Int) : Unit = {

    }
    def remove(id: Int) = {
        beforeRemove(id)
        db.remove(id)
        val item = this.load(id)
        item.disable = true
//        val item = all.remove(id)
//        if (item != null) removeIndex(item)
        afterRemove(id)
    }

    def afterRemove(id : Int) : Unit = {
        val cl = tag.runtimeClass
        val item  = this.load(id)
        log.log(0,ActionType.DELETE,cl,item,null)
    }

    def list(from: Int, count: Int, sortBy: String, direction: String): PagingResult[T] = {
        var t = sortBy
        if(!typeIndex.containsKey(sortBy)) t = "id"
        var it : util.Iterator[T] = null

        if(typeIndex.get(t) == null) return new PagingResult[T](new Array[T](0),all.size())
        val data = typeIndex.get(t).toList.filter(p=>(!p.disable)&&SecurityContext.get()(p))
        if (data.size < from ) return new PagingResult[T](new Array[T](0),data.size)

        if(direction == "asc") it = data.toIterator
        else it = data.reverse.toIterator

        for (i <- 0 until from) it.next()

        val maxSize = data.size

        val size = Math.min(count, maxSize - from)
        val arr = new Array[T](size)

        for (i <- 0 until size) arr(i) = it.next()

        new PagingResult(arr, maxSize)
    }

    def listByIds(ids: List[Int]) = ids.map(id => all.get(id)).toArray.filter(t => (t!=null && !t.disable && SecurityContext.get()(t)))

    def listByReferenceId(from: Int, count: Int, id: Int, sortBy: String, direction: String): PagingResult[T] = {
        if(refIndex.get(id) == null) return new PagingResult(new Array[T](0), 0)

        val map = refIndex.get(id).filter(p=>(!p._2.disable) && SecurityContext.get()(p._2))
        if (map == null || map.size <= from) return new PagingResult(new Array[T](0), 0)

        val clazz = refIndex.get(id).firstEntry.getValue.getClass

        var t = sortBy
        if(!typeIndex.keySet().contains(sortBy)) t = "id"

        val c = new GenericComparator(Reflect.getProperties(clazz).get(t).get.get)

        var list = map.values.toList.sortWith((x,y)=>c.compare(x,y)>0).toList

        if(direction == "asc") list = list.reverse

        val size = Math.min(count, list.size - from)
        val arr = list.slice(from, from + size).toArray

        new PagingResult(arr, list.size)
    }

    def countByRef(id: Int): Int = {
        if(refIndex.get(id) == null) 0
        else refIndex.get(id).filter(p=>(!p._2.disable) && SecurityContext.get()(p._2)).toList.size
    }

    def loadAll(): Iterator[T] = all.filter(p=>(!p._2.disable) && SecurityContext.get()(p._2)).map(item=>item._2).toIterator

    def fullTextFilter(p: T, query: String) = {
        val name = Reflect.getProperties(p.getClass).get("name").get.get(p)
        if(name != null) {
            name.toString.toLowerCase.contains(query.toLowerCase)
        } else {
            false
        }

    }

    def search(query: String, pId: Int, isDisable: Boolean, take: Int = 10): List[T] = {
        all.descendingMap().filter(p=>(isDisable == p._2.disable) && SecurityContext.get()(p._2)).map(i=>i._2).filter(i=>fullTextFilter(i, query)).take(take).toList
    }

    def count(f: (T)=>Boolean): Int = all.filter(p=>(!p._2.disable) && SecurityContext.get()(p._2)).map(t=>t._2).count(f)

    def list(f: (T)=> Boolean): List[T] = all.filter(p=>(!p._2.disable) && SecurityContext.get()(p._2)).map(t=>t._2).filter(f).toList

    def list[A](f: (T)=> Boolean, transform: (T)=>A): List[A] = {
        //val temp = all.filter(p=>(!p._2.disable) && SecurityContext.get()(p._2)).map(t=>t._2).filter(f)
        //val temp2 = temp.map(transform).toList
        all.filter(p=>(!p._2.disable) && SecurityContext.get()(p._2)).map(t=>t._2).filter(f).map(transform).toList
    }

    def enable(id: Int): Unit = {
        val item = all.get(id)
        if(item.disable) {
            item.disable = false
            update(item)
        }
        log.log(0,ActionType.ENABLE,tag.runtimeClass,null,item)
    }

    def disable(id: Int): Unit = {
        val item = all.get(id)
        if(!item.disable) {
            item.disable = true
            update(item)
        }
        log.log(0,ActionType.DISABLE,tag.runtimeClass,null,item)
    }

    def listDisable(from: Int, count: Int, sortBy: String, direction: String): PagingResult[T] = {
        var t = sortBy
        if(!typeIndex.containsKey(sortBy)) t = "id"
        var it : util.Iterator[T] = null
        if(typeIndex.get(t) == null) return new PagingResult[T](new Array[T](0),all.size())
        val data = typeIndex.get(t).toList.filter(p=>(p.disable) && SecurityContext.get()(p))
        if (data.size < from ) return new PagingResult[T](new Array[T](0),all.size())
        
        if(direction == "asc") it = data.toIterator
        else it = data.reverse.toIterator

        for (i <- 0 until from) it.next()

        val maxSize = data.size

        val size = Math.min(count, maxSize - from)
        val arr = new Array[T](size)

        for (i <- 0 until size) arr(i) = it.next()

        new PagingResult(arr, maxSize)
    }
}

