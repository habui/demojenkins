package ads.common.services

import ads.common.database.{SqlDataService, InMemoryDataService}
import ads.common.model.{ZoneToZoneGroup, ActionType, ZoneToBanner, ActionLog}
import java.util.Date
import ads.common.Syntaxs.{Success,Failure}
import ads.common.IItem

/**
 * Created by quangnbh on 11/22/13.
 */
//object Log {
//    lazy val actionLogService  = CommonEnv.actionLogService
//    lazy val websiteService  = CommonEnv.websiteService
//    lazy val bannerService = CommonEnv.bannerService
//    lazy val zoneService = CommonEnv.zoneService
//    lazy val zoneGroupService = CommonEnv.zoneGroupService
//    lazy val campaignService = CommonEnv.campaignService
//    lazy val orderService = CommonEnv.orderService
//    lazy val categoryService  = CommonEnv.categoryService
//
//    val ignoredModels = Array(
//        classOf[ActionLog].getSimpleName.toLowerCase,
//        classOf[CachedLog].getSimpleName.toLowerCase,
//        classOf[ZoneToZoneGroup].getSimpleName.toLowerCase
//    )
//    def log(userId : Int, action : String, objectType : String, objectId : Int ) = {
//        val objType = objectType.toLowerCase
//        if(!ignoredModels.contains(objType)){
//            if(SessionInfo.session != null){
//                val al = new ActionLog(0, SessionInfo.session.user.id, action,objectType,objectId,new Date().getTime,null )
//                if(action.equals(ActionType.CREATE) &&
//                    objType.equals(classOf[ZoneToBanner].getSimpleName.toLowerCase) ){
//                    al.action = ActionType.LINK
//                }
//                if(action.equals(ActionType.DELETE) &&
//                    objType.equals(classOf[ZoneToBanner].getSimpleName.toLowerCase)){
//                    al.action = ActionType.UNLINK
//                }
//                actionLogService.save(al)
//            }
//        }
//    }
//    def logEdit(userId : Int,objectType: String, oldVal : IItem,newVal : IItem) = {
//
//    }
//
//    def detailMessage(item : ActionLog) : String = {
//        item.objectType.toLowerCase match {
//            case "website" => {
//                val w = websiteService.load(item.id)
//                val str = item.action match {
//                    case ActionType.CREATE => s"create ${item.objectType.toLowerCase} name '${ if(w!=null)w.name else "unknown"}'"
//                    case _ => ""
//                }
//                str
//            }
//            case "banner" => {
//                val banner = bannerService.load(item.id)
//                val str = item.action match {
//                    case ActionType.CREATE => s"create item name ${if(banner!=null) banner.name else "unknown"}"
//                    case _ => ""
//                }
//                str
//            }
//            case "zone" => {
//                val z = zoneService.load(item.id)
//                val str = item.action match {
//                    case ActionType.CREATE => s"create zone name ${if(z!=null) z.name else "unknown"}"
//                    case _ => ""
//                }
//                str
//            }
//            case "zonegroup" => {
//                val z = zoneGroupService.load(item.id)
//                val str = item.action match {
//                    case ActionType.CREATE => s"create zone group name ${if(z!=null) z.name else "unknown"}"
//                    case _ => ""
//                }
//                str
//            }
//            case "campaign" => {
//                val c = campaignService.load(item.id)
//                val str = item.action match {
//                    case ActionType.CREATE => s"create campaign name ${if(c!=null) c.name else "unknown"}"
//                    case _ => ""
//                }
//                str
//            }
//            case "order" => {
//                val o = orderService.load(item.id)
//                val str = item.action match {
//                    case ActionType.CREATE => s"create campaign name ${if(o!=null) o.name else "unknown"}"
//                    case _ => ""
//                }
//                str
//            }
//            case "category" => {
//                val o = categoryService.load(item.id)
//                val str = item.action match {
//                    case ActionType.CREATE => s"create category name ${if(o!=null) o.name else "unknown"}"
//                    case _ => ""
//                }
//                str
//            }
//            case _ => ""
//        }
//
//    }
//}

