package ads.common.services

import ads.common.database.{IDataDriver, PagingResult, InMemoryDataService, IDataService}
import ads.common.model._
import scala.collection.JavaConversions._
import java.util.Date
import scala.concurrent.Future
import ads.common.Syntaxs._
import ads.common.{UserContext, JsonExt, IItem}
import scala.reflect.ClassTag
import ads.common.Reflect._

trait IActionLogService extends  IDataService[ActionLog] {
    def listBy(key: String, userId : Int, action : String, objectType : String, objectId : Int,
               fromTime : Long, toTime : Long) : Array[ActionLog]
    def listBy(key: String, userId : Int, action : String, objectType : String, objectId : Int,
               fromTime : Long, toTime : Long, from : Int = 0, count : Int = Int.MaxValue) : PagingResult[ActionLog]
//    def log(userId : Int, action : String, objectType : String, objectId : Int ) : Boolean
    def log[T](userId : Int, action : String, objectType : Class[T], oldVal : IItem, newVal : IItem)(implicit tag : ClassTag[T]) : Boolean

}

class ActionLogService(env : {
    val actionLogDataDriver : IDataDriver[ActionLog]
    val actionLogServiceRef : Future[IActionLogService]
    val sessionService : ISessionService

}) extends InMemoryDataService[ActionLog](a=>a.userId,env.actionLogDataDriver,env.actionLogServiceRef) with IActionLogService {

    val gson = JsonExt.gson

    val ignoredModels = Array(
        classOf[ActionLog],
        classOf[CachedLog],
        classOf[ZoneToZoneGroup],
        classOf[ConversionType]
    )

    def listBy(key: String, userId : Int, action : String, objectType : String, objectId : Int,
               fromTime : Long, toTime : Long) : Array[ActionLog] = {
        var ret : Array[ActionLog] = if(userId < 0) all.values().toIterator.toArray
        else all.values().toIterator.toArray.filter(a => a.userId == userId)
        ret = if(action != null && action.length > 0) ret.filter(a => a.action.toLowerCase.equals(action.toLowerCase)) else ret
        if(objectId > 0) {
            objectType match {
                case "Banner" => {
                    ret = ret.filter(a => (a.objectId == objectId && a.objectType.toLowerCase.equals(objectType.toLowerCase)) ||
                                           (a.objectType.toLowerCase.equals("newbookrecord") &&
                                            (a.oldVal.contains("\"itemId\":"+objectId+"\"") || a.newVal.contains("\"itemId\":"+objectId+"\""))
                                           )
                                    )
                }
                case "Zone" => {
                    ret = ret.filter(a => (a.objectId == objectId && a.objectType.toLowerCase.equals(objectType.toLowerCase)) ||
                        (a.objectType.toLowerCase.equals("newbookrecord") &&
                            (a.oldVal.contains("\"zoneId\":"+objectId+"\"") || a.newVal.contains("\"zoneId\":"+objectId+"\""))
                            )
                    )
                }
                case _ => ret = if(objectType != null && objectType.length > 0) ret.filter(a => a.objectType.toLowerCase.equals(objectType.toLowerCase)) else ret
            }
        } else {
            ret = if(objectType != null && objectType.length > 0) ret.filter(a => a.objectType.toLowerCase.equals(objectType.toLowerCase)) else ret
        }
        ret = if(fromTime > 0) ret.filter(a => a.time >= fromTime) else ret
        ret = if(toTime > 0) ret.filter(a=> a.time <= toTime) else ret
        ret = if (key != null && key.length > 0) ret.filter(a=> a.objectName.contains(key) || a.newVal.contains(key)) else ret
        ret.sortWith((a,b)=> a.time > b.time)
    }
    def listBy(key: String, userId : Int, action : String, objectType : String, objectId : Int,
               fromTime : Long, toTime : Long, from : Int = 0, count : Int = Int.MaxValue) : PagingResult[ActionLog] = {
        val data = listBy(key,userId,action,objectType,objectId,fromTime,toTime)
        new PagingResult[ActionLog](data.slice(from,from+count),data.size)
    }
    def log[T](userId : Int, action : String, objectType : Class[T], oldVal : IItem, newVal : IItem)(implicit tag : ClassTag[T]) : Boolean = {
//        val objType = objectType.toLowerCase
        var ret = false
        if(!ignoredModels.contains(objectType)){
//            val user = env.sessionService.getCurrentSession() match {
//                case Success(s)=> {
//                    s.user
//                }
//                case Failure(cause) => {
//                    UserContext.get()
//                }
//            }

            val userId = {
                val user = UserContext.get()
                if(user == null) 0
                else user.id
            }

            val oldJson = gson.toJson(oldVal)
            val newJson = gson.toJson(newVal)
            if (oldJson.equals(newJson) && action != ActionType.DISABLE && action != ActionType.ENABLE) return false
            var objName = null.asInstanceOf[String]
            if(newVal == null) objName = ""
            else objName = if(newVal.getV("name") != null) newVal.getV("name").asInstanceOf[String] else ""

            val id = if(newVal != null) newVal.id else 0
            val al = new ActionLog(0, userId, action,objectType.getSimpleName,id,objName,new Date().getTime,oldJson,newJson)

            action match {
                case ActionType.CREATE => {
                    if(objectType.equals(classOf[NewBookRecord])){
                        al.action = ActionType.LINK
                    }
//                    if(objectType.equals(classOf[ZoneToBanner])){
//                        al.action = ActionType.LINK
//                    }
                    if(objectType.equals(classOf[UserRole])){
                        //al.objectType = classOf[Website].getSimpleName
                        al.action = ActionType.SHARE
                    }
                }
                case ActionType.DELETE => {
                    if(objectType.equals(classOf[NewBookRecord])){
                        al.action = ActionType.UNLINK
                    }
                    if(objectType.equals(classOf[ZoneToBanner])){
                        al.action = ActionType.UNLINK
                    }
                    if(objectType.equals(classOf[UserRole])){
                        //al.objectType = classOf[Website].getSimpleName
                        al.action = ActionType.UNSHARE
                    }
                }
                case ActionType.EDIT => {
                    if(objectType.equals(classOf[Order])){
                        val old : Order = oldVal.asInstanceOf[Order]
                        val n  : Order = newVal.asInstanceOf[Order]
                        if(!old.status.equals(n.status)){
                            n.status match {
                                case OrderStatus.PAUSED => al.action = ActionType.PAUSE
                                case OrderStatus.RUNNING => al.action = ActionType.RESUME
                                case OrderStatus.TERMINATED => al.action = ActionType.TERMINATE
                                case _ => {}
                            }
                        }
                    }
                    if(objectType.equals(classOf[Campaign])){
                        val old : Campaign = oldVal.asInstanceOf[Campaign]
                        val n  : Campaign = newVal.asInstanceOf[Campaign]
                        if(!old.status.equals(n.status)){
                            n.status match {
                                case CampaignStatus.PAUSED => al.action = ActionType.PAUSE
                                case CampaignStatus.RUNNING => al.action = ActionType.RESUME
                                case CampaignStatus.TERMINATED => al.action = ActionType.TERMINATE
                                case _ => {}
                            }
                        }
                    }
                    if(objectType.equals(classOf[UserRole])){
                        val old : UserRole = oldVal.asInstanceOf[UserRole]
                        val n : UserRole = newVal.asInstanceOf[UserRole]
                        if(n.roles != null && old.roles != null) {
                            al.action = ActionType.SHARE
                        }else if(n.roles == null && old.roles == null){

                        }else {
                            al.action = ActionType.SHARE
                        }
                    }
                    if(objectType.equals(classOf[ZoneToBanner])){
                        val n = newVal.asInstanceOf[ZoneToBanner]
                        n.status match {
                            case ZoneToBannerStatus.APPROVED => al.action = ActionType.APPROVE
                            case ZoneToBannerStatus.REJECTED => al.action = ActionType.REJECT
                        }
                    }
                }
                case _ =>

            }
            ret = save(al)==1

        }
        ret
    }
}
