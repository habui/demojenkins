package ads.website.modules

import ads.common.database.{PagingResult, IDataService, AbstractDelegateDataService}
import ads.common.model._
import ads.website.handler.RestHandler
import ads.web.{WebUtils, Invoke, HandlerContainerServlet, BaseServlet}
import ads.web.mvc.{SmartDispatcherHandler, Json, BaseHandler}
import ads.website.{PermissionUtils, Environment}
import ads.common.services.{IUserRoleService, IActionLogService, IUserService}
import ads.common.{SecurityContext}
import javax.servlet.http.HttpServletRequest

/**
 * Created by quangnbh on 11/21/13.
 */
trait IActionLogModelService extends IDataService[ActionLogModel] {
    def detailMessage(item : ActionLog) : String
    def listBy(key: String, userId : Int, action : String, objectType : String, objectId : Int,
               fromTime : Long, toTime : Long, from : Int = 0, count : Int = Int.MaxValue) : PagingResult[ActionLogModel]
}

class ActionLogModelService (env : {
    val actionLogService : IActionLogService
    val zoneGroupService : IDataService[ZoneGroup]
    val campaignService : IDataService[Campaign]
    val categoryService : IDataService[Category]
    val websiteService : IDataService[Website]
    val orderService : IDataService[Order]
//    val zoneToBannerService : IDataService[ZoneToBanner]
    val zoneService : IDataService[Zone]
    val bannerService : IDataService[Banner]
    val userRoleService : IUserRoleService
    val userService : IUserService
    val roleService : IDataService[Role]
    val newBookService: IDataService[NewBookRecord]

}) extends AbstractDelegateDataService[ActionLog,ActionLogModel](env.actionLogService) with IActionLogModelService {

    def toModel(item: ActionLog): ActionLogModel = {
        val user = env.userService.load(item.userId)
        val userName = if(user != null) user.name else ""
        val detail = this.detailMessage(item)
        val m = new ActionLogModel(item.id,item.action,item.userId,userName,item.objectType,item.objectId,item.objectName,item.time,detail)
        if(m.objectType.equals(classOf[Banner].getSimpleName) ||
            m.objectType.equals(classOf[ZoneToBanner].getSimpleName)
        ){
            m.objectType = "Item"
        }
        if(m.action.equals(ActionType.LINK)){
            val ztb = env.newBookService.load(item.objectId)
            if(ztb != null) {

            }
        }
        m
    }
    def fromModel(model: ActionLogModel): ActionLog = {
        new ActionLog(0,0,null,null,0,null,0,null,null)
    }

    def listBy(key: String, userId : Int, action : String, objectType : String,objectId : Int,
               fromTime : Long, toTime : Long, from : Int = 0, count : Int = Int.MaxValue) : PagingResult[ActionLogModel] = {
        convertRecord(env.actionLogService.listBy(key,userId,action,objectType,objectId,fromTime,toTime,from,count))
    }

    def detailMessage(item : ActionLog) : String = {
        item.objectType.toLowerCase match {
            case "website" => {
                val w = env.websiteService.load(item.objectId)
                item.action.capitalize + s" website name '${ if(w!=null)w.name else "unknown"}'"
            }
            case "banner" => {
                val banner = env.bannerService.load(item.objectId)
                item.action.capitalize + s" item name '${if(banner!=null) banner.name else "unknown"}'"
            }
            case "zone" => {
                val z = env.zoneService.load(item.objectId)
                item.action.capitalize + s" zone name '${if(z!=null) z.name else "unknown"}'"
            }
            case "zonegroup" => {
                val z = env.zoneGroupService.load(item.objectId)
                item.action.capitalize + s" zone group name '${if(z!=null) z.name else "unknown"}'"
            }
            case "campaign" => {
                val c = env.campaignService.load(item.objectId)
                val str = item.action.capitalize
                str + s" campaign name '${if(c!=null) c.name else "unknown"}"
            }
            case "order" => {
                val o = env.orderService.load(item.objectId)
                val str = item.action.capitalize
                str + s" order name '${if(o!=null) o.name else "unknown"}'"
            }
            case "category" => {
                val o = env.categoryService.load(item.objectId)
                item.action.capitalize + s" category name '${if(o!=null) o.name else "unknown"}'"
            }
            case "zonetobanner" =>{
                val ztb = env.newBookService.load(item.objectId)
                val zone : Zone = if(ztb!= null) env.zoneService.load(ztb.zoneId) else null
                val web : Website = if(zone!=null) env.websiteService.load(zone.siteId) else null
                val banner : Banner = if(ztb != null) env.bannerService.load(ztb.itemId) else null
                val camp : Campaign = if(banner != null) env.campaignService.load(banner.campaignId) else null
                val to = if(item.action.equals(ActionType.LINK)) "to" else "from"
                val mess = s"${item.action.capitalize} item ${if (banner!=null) banner.name else "unknown"}" +
                    s"(in campaign ${if(camp!=null)camp.name else "unknown"}) ${to} zone ${if(zone!=null)zone.name else "unknown"}" +
                    s"(in website ${if(web!=null) web.name else "unknown"}) with ${if(ztb!=null)ztb.share else 0} percent"
                mess
            }
            case "userrole" => {
                val ur : UserRole = env.userRoleService.load(item.objectId)
                val user : User = if(ur!=null) env.userService.load(ur.userId) else null
                val roles : Array[Role] = if(ur!=null && ur.roles!=null && ur.roles.length > 0) env.roleService.listByIds(ur.roles.toList) else null
                val rString  :String = if(roles != null) roles.map(a=>a.name).reduce((a1,a2) => a1+","+a2) else ""
                if(ur!=null){
                    val str = ur.objName match {
                        case "website" => {
                            val web : Website = if(ur!=null)env.websiteService.load(ur.itemId) else null
                            s"Share website '${if(web!=null) web.name else "unknown"}' " +
                                s"to user '${if(user!=null)user.name else "unknown"}' with right '${rString}'"
                        }
                        case "order"=>{
                            val order : Order = if(ur != null) env.orderService.load(ur.itemId) else null
                            s"Share order '${if(order!=null) order.name else "unknown"}' " +
                                s"to user '${if(user!=null)user.name else "unknown"}' with right '${rString}'"
                        }
                        case _ => ""
                    }
                    str
                }else {
                    val ur : UserRole =WebUtils.fromJson(classOf[UserRole], item.oldVal)
                    val user : User = if(ur!=null) env.userService.load(ur.userId) else null
                    val str = ur.objName match {
                        case "website" => {
                            val web : Website = if(ur!=null)env.websiteService.load(ur.itemId) else null
                            s"UnShare website '${if(web!=null) web.name else "unknown"}' " +
                                s"for user '${if(user!=null)user.name else "unknown"}'"
                        }
                        case "order"=>{
                            val order : Order = if(ur != null) env.orderService.load(ur.itemId) else null
                            s"UnShare order '${if(order!=null) order.name else "unknown"}' " +
                                s"for user '${if(user!=null)user.name else "unknown"}'"
                        }
                        case _ => ""
                    }
                    str
                }
            }
            case "newbookrecord" => {
                val br : NewBookRecord = env.newBookService.load(item.objectId) ;
                val zone : Zone = if(br != null) env.zoneService.load(br.zoneId) else null
                val banner: Banner = if (br != null) env.bannerService.load(br.itemId) else null
                val website  : Website = if(zone != null) env.websiteService.load(zone.siteId) else null
                if(br != null){
                    s"Book a record in zone '${if(zone != null) zone.name else "unknown"}'(of website '${if(website != null) website.name else "unknown"}') " +
                        s" in item '${if(banner != null) banner.name else "unknown"}' with share ${br.share}% from ${WebUtils.timeToString(br.from)} to ${WebUtils.timeToString(br.to)}."
                }else {
                    //unlink
                    val bookrecord: NewBookRecord =  WebUtils.fromJson(classOf[NewBookRecord], item.oldVal)
                    val banner = env.bannerService.load(bookrecord.itemId)
                    val zone = env.zoneService.load(bookrecord.zoneId)
                    s"Unlink item: '${if(banner != null) banner.name else "unknown"}', zone: '${if(zone != null) zone.name else "unknown"}'"
                }
            }
            case _ => ""
        }
    }
}

class ActionLogHandler(env : {
    val actionLogModelService : IActionLogModelService
    val actionLogService : IActionLogService
}) extends SmartDispatcherHandler {
    @Invoke(Parameters = "request,key,userId,objectType,objectId,action,fromTime,toTime,from,count")
    def listBy(request: HttpServletRequest , key: String, userId: Int,objectType: String, objectId: Int, action: String,
               fromTime : Long, toTime : Long, from : Int, count : Int ): Any = {
        val user = PermissionUtils.getUser(request)
        if(user == null || !Config.rootUserName.get().split(",").contains(user.name)) return Json(null)
        val ret = env.actionLogModelService.listBy(key,userId,action,objectType,objectId,fromTime,toTime,from,count)
        Json(ret)
    }

    @Invoke(Parameters = "request")
    def loadAll(request: HttpServletRequest) : Any = {
        val user = PermissionUtils.getUser(request)
        if(user == null || !Config.rootUserName.get().split(",").contains(user.name)) return Json(null)
        Json(env.actionLogModelService.loadAll().toArray)
    }

    class DetailLog(val oldVal: String, val newVal: String)
    @Invoke(Parameters = "request,id")
    def detailMessage(request: HttpServletRequest, id: Int) : Any = {
        val user = PermissionUtils.getUser(request)
        if(user == null || !Config.rootUserName.get().split(",").contains(user.name)) return Json(null)
        val log = env.actionLogService.load(id)
        Json(new DetailLog(log.oldVal, log.newVal))
    }
}

class ActionLogServlet extends HandlerContainerServlet {
    def factory() : BaseHandler = new ActionLogHandler(Environment)
}
