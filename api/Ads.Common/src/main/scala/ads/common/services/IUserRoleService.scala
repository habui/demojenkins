package ads.common.services

import ads.common.database._
import ads.common.model.UserRole
import java.util.concurrent.ConcurrentHashMap
import ads.common.Syntaxs.{Success, fail, Try, succeed, Failure}
import scala.reflect.ClassTag
import java.util
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import ads.common.Syntaxs._
import scala.util.control.Breaks._
import scala.concurrent.Future
import ads.common.SecurityContext
import ads.common.Syntaxs.Failure
import ads.common.Syntaxs.Success

/**
 * Created by quangnbh on 10/24/13.
 */

trait IUserRoleService extends IDataService[UserRole]{
    def findBy(objName: String, userId : Int, itemId : Int, hasDisable: Boolean = false) : Try[UserRole]
    def findBy(objName : String, itemId : Int) : Try[Array[UserRole]]
    def findBy(objName : String, itemId : Int, from : Int, count: Int) : Try[PagingResult[UserRole]]
    def findByUser(objName :String, userId : Int) : Try[Array[UserRole]]
    def findByUser(objName :String, userId : Int, from :Int, count :Int) : Try[PagingResult[UserRole]]
    def countBy(objName :String, userId : Int) : Int
}

//trait IUserRoleDataService extends IDataService[UserRole] {
//    def findBy(objName: String, userId : Int, itemId : Int) : UserRole
//    def findBy(objName : String, itemId : Int) : Array[UserRole]
//
//}

//class UserRoleDataService(factory : () => UserRole) (implicit tag : ClassTag[UserRole]) extends  SqlDataService[UserRole](factory) with IUserRoleDataService {
//    def findBy(objName: String, userId : Int, itemId : Int) : UserRole = {
//        db.wrap(connection => {
//            val tableName = tag.runtimeClass.getSimpleName.toLowerCase
//            val query : String = s"select * from $tableName where objName = '$objName' and userId=$userId and itemId=$itemId"
//            val statement =  connection.prepareStatement(query)
//            val result =  statement.executeQuery()
//            if(result.next()){
//                println("result.next")
//                db.read(result)
//            }else {
//                null
//            }
//
//        });
//    }
//    def findBy(objName : String, itemId : Int) : Array[UserRole] = {
//        db.wrap(connection => {
//            val tableName = tag.runtimeClass.getSimpleName.toLowerCase
//            val query : String  = s"select * from $tableName where objName='$objName' and itemId=$itemId"
//            val statement = connection.prepareStatement(query)
//            val result = statement.executeQuery()
//            val ret : util.ArrayList[UserRole] = new util.ArrayList[UserRole]()
//            while(result.next()){
//                ret.add(db.read(result))
//            }
//            ret.asScala.toArray[UserRole]
//        })
//    }
//}

class UserRoleService(env : {
    val userRoleDataDriver : IDataDriver[UserRole]
    val actionLogServiceRef : Future[IActionLogService]
}) extends InMemoryDataService[UserRole](u => u.id, env.userRoleDataDriver, env.actionLogServiceRef) with IUserRoleService {

    var initializedCache : Boolean = false
    lazy val userRoleIndex = new ConcurrentHashMap[String,UserRole]() //(objName_UserId_ItemId)
    lazy val listRoleIndex = new ConcurrentHashMap[String,ArrayBuffer[UserRole]]() // Key format : objName_itemId

    override def setup() =  {
        super.setup()
    }

    override def index(item : UserRole){
        super.index(item)

        userRoleIndex.put(item.objName+"_"+item.userId+"_"+item.itemId, item)

        val key = item.objName+"_"+item.itemId
        val list = listRoleIndex.getOrAdd(key,key=> new ArrayBuffer[UserRole]())
        val l = list.updateOrAdd(item)
        listRoleIndex.put(key,l)
    }

    override def beforeSave(item : UserRole) : Unit = {
        super.beforeSave(item)
    }

    override def afterSave(item : UserRole)  = {
        userRoleIndex.put(item.objName.toLowerCase+"_"+item.userId+"_"+item.itemId,item)
        val key = item.objName+"_"+item.itemId
        val list = listRoleIndex.getOrAdd(key,key=> new ArrayBuffer[UserRole]())
        val l = list.updateOrAdd(item)
        listRoleIndex.put(key,l)
        super.afterSave(item)
    }



    override def update(item : UserRole) : Boolean = {
        userRoleIndex.put(item.objName+"_"+item.userId+"_"+item.itemId,item);
        super.update(item) ;
    }

    override def afterUpdate(old: UserRole,newVal : UserRole) = {
        super.afterUpdate(old,newVal)
    }

    def findBy(objName: String, userId : Int, itemId : Int, hasDisable: Boolean = false) : Try[UserRole] = {
        var item = userRoleIndex.get((objName.toLowerCase+"_"+userId+"_"+itemId))
        if(item != null){ // cache hit
            if(item.disable && !hasDisable) fail("not found!")
            else succeed(item)
        }else {
            breakable({
                all.values().filter(p=>{
                    if(!hasDisable && p.disable) false
                    else true
                }).foreach(ur => {
                    if(ur.objName.equals(objName) && ur.itemId == itemId && ur.userId == userId){
                        item = ur
                        break()
                    }
                })
            })
            if(item != null){
                userRoleIndex.put(item.objName+"_"+item.userId+"_"+item.itemId,item)
                succeed(item)
            }else {
                fail("not found!")
            }
        }
    }
    def findBy(objName : String, itemId : Int) : Try[Array[UserRole]] = {
        val data = listRoleIndex.get( objName.toLowerCase+"_"+itemId )
        if(data == null) return fail("not found !")
        val ret = data.filter(p=>(!p.disable) && SecurityContext.get()(p))
        if(ret != null){
            succeed(ret.toArray)
        }else {
            //
            val items : ArrayBuffer[UserRole] = new ArrayBuffer[UserRole]()
            for(item <- all.values().filter(p=>(!p.disable) && SecurityContext.get()(p))){
                if(item.objName.equals(objName) && item.itemId == itemId){
                    items += item ;
                }
            }
            if(items != null && items.length > 0) {
                listRoleIndex.put(objName +"_" + itemId,items)
                succeed(items.toArray)
            }else {
                fail("not found !")
            }
        }
    }
    def findBy(objName : String, itemId : Int, from : Int, count: Int) : Try[PagingResult[UserRole]] = {
        this.findBy(objName,itemId) match {
            case Success(roles ) => {
                val ret : Array[UserRole] = roles.slice(from,from+count) ;
                succeed(new PagingResult[UserRole](ret,roles.length))
            }
            case Failure(cause) => {
                fail("not found")
            }
        }
    }
    def findByUser(objName :String, userId : Int) : Try[Array[UserRole]] = {
        var ret : List[UserRole] = List() ;
        all.values().filter(p=>(!p.disable) && SecurityContext.get()(p)).foreach(usr => {
            if(usr.objName.equals(objName) && usr.userId == userId){
                ret = usr::ret
            }
        })
        succeed(ret.toArray)
    }
    def findByUser(objName :String, userId : Int, from :Int, count :Int) : Try[PagingResult[UserRole]] = {
        this.findByUser(objName,userId) match {
            case Success(roles ) => {
                val ret : Array[UserRole] = roles.slice(from,from+count) ;
                succeed(new PagingResult[UserRole](ret,roles.length))
            }
            case Failure(cause) => {
                fail("not found")
            }
        }
    }
    def countBy(objName :String, userId : Int) : Int = {
        var count = 0
        all.valuesIterator.filter(p=>(!p.disable) && SecurityContext.get()(p)).foreach(va => {
            if(va.objName.equals(objName.toLowerCase) && va.userId == userId){
                count +=1;
            }
        })
        count
    }
}