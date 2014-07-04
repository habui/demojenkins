package ads.website.modules

import ads.common.{SecurityContext, IItem}
import ads.common.database.{PagingResult, IDataService, AbstractDelegateDataService}
import ads.common.model._
import ads.website.handler.RestHandler
import ads.web.{Invoke, HandlerContainerServlet}
import ads.web.mvc.{Text, Json, BaseHandler}
import ads.common.services.{ApproveItem, IUserService, IZoneToBannerService, IWebsiteService}
import scala.Some
import java.security.Policy.Parameters
import java.util
import com.google.gson.internal.LinkedTreeMap
import javax.servlet.http.HttpServletRequest
import ads.website.{PermissionUtils, Environment}
import scala.collection.mutable.ArrayBuffer
import ads.common.Syntaxs.{succeed, fail, Try}

object WebsiteKind{
    val AD = "ad"
    val PR = "pr"
}

class WebsiteModel(var id: Int,
                   var ownerId: Int,
                   var name: String,
                   var description: String,
                   var reviewType: String,
                   var zoneGroupCount: Int,
                   var zoneCount: Int,
                   var zoneModels:List[ZoneModel],
                   var kind: String = WebsiteKind.AD,
                   var frequencyCapping: Int = 0,
                   var frequencyCappingTime: Int = 0,
                   var disable: Boolean = false
) extends IItem

object defaultWebsiteModel{
    def apply() = new WebsiteModel(0,0,"",null,null,0,0,null)
}


trait IWebsiteModelService extends IDataService[WebsiteModel] {
    def listByType(from: Int, count: Int, reviewType: String): PagingResult[Website]
    def listAds(status : Int, from :Int, count : Int ) : PagingResult[ApproveAdsModel]
    def listAds(status : Int, websiteId : Int, from : Int, count : Int) : PagingResult[ApproveAdsModel]
    def listAllContainAds(status : Int) : Array[WebsiteModel]
    def listAllApproveAds(status : Int) : Array[ApproveAdsModel]
    def listByFilter(from: Int, count: Int, sortBy: String, direction: String, filter: String) : PagingResult[WebsiteModel]
    def loadZonesByGroupWebsite(kindZone:String):List[WebsiteModel]
    def listByTypeMinimize(from: Int, count: Int, reviewType: String, sortBy: String, direction: String): PagingResult[Website]
}

class WebsiteModelService (env:{
    val websiteService: IWebsiteService
    val zoneService: IDataService[Zone]
    val zoneModelService: IZoneModelService
    val zoneGroupService: IDataService[ZoneGroup]
//    val zoneToBannerService: IZoneToBannerService
//    val newZoneToBannerService: INewZoneToBannerService
//    val newBookingService: INewBookingService
    val bannerModelService : IDataService[BannerModel]
    val userService : IUserService
    val zoneToBannerService: IZoneToBannerService
})
    extends AbstractDelegateDataService[Website,WebsiteModel](env.websiteService)
    with IWebsiteModelService{
    def toModel(item: Website): WebsiteModel = {
        val zoneCount = env.zoneService.countByRef(item.id)
        val groupCount = env.zoneGroupService.countByRef(item.id)
        val zoneModels = env.zoneService.list(z=>item.id == z.siteId).map(item=>env.zoneModelService.zoneToModel(item))
        val websiteKind = {
            if(zoneModels.exists(z=>z.kind == ZoneKind.PRNetwork)) WebsiteKind.PR
            else WebsiteKind.AD
        } 
        new WebsiteModel(item.id, item.ownerId, item.name, item.description, item.reviewType, groupCount, zoneCount,zoneModels,websiteKind,item.frequencyCapping,item.frequencyCappingTime)
    }
    def fromModel(model: WebsiteModel): Website = new Website(model.id, model.ownerId, model.name, model.description, model.reviewType, model.frequencyCapping, model.frequencyCappingTime, model.disable)

    override def disable(id: Int): Unit = {
        super.disable(id)
        env.zoneService.listByReferenceId(id = id).data.map(z=>env.zoneService.disable(z.id))
        env.zoneGroupService.listByReferenceId(id = id).data.map(zg=>env.zoneGroupService.disable(zg.id))
    }
    
    override def enable(id: Int): Unit = {
        super.enable(id)
        env.zoneService.listDisable(0, Int.MaxValue).data.toList.filter(z=>z.siteId == id).map(z=>env.zoneService.enable(z.id))
        env.zoneGroupService.listDisable(0, Int.MaxValue).data.toList.filter(zg=>zg.siteId == id).map(zg=>env.zoneGroupService.enable(zg.id))
    }

    def listByType(from: Int, count: Int, siteType: String): PagingResult[Website] = {
        val rs = new ArrayBuffer[Website]
        val types = new ArrayBuffer[String]
        siteType.toLowerCase.split(",").map(t => {
            if(!types.contains(t)) {
                types += t
            }
        })

        if(types.length < 0) return new PagingResult(new Array[Website](0), 0)

        val sites = env.websiteService.list(0, Int.MaxValue)
        for(site <- sites.data) {

            val zones = env.zoneModelService.listByReferenceId(id = site.id).data
            val temp = new ArrayBuffer[String]
            temp ++= types.clone()
            for(zone <- zones) {
                for(t <- getTypeOfZone(zone)) {
                    if(types.contains(t)) temp -= t
                }
            }
            if(temp.length == 0) rs += site
        }

        if(rs == null || sites.data.length <= 0) return new PagingResult(new Array[Website](0), 0)

        val size = Math.min(count, rs.size - from)
        val arr = rs.slice(from, from + size).toArray

        new PagingResult(arr, rs.size)
    }

    def getTypeOfZone(item: ZoneModel): Array[String] = {
        val rs = new ArrayBuffer[String]
        item.kind match {
            case ZoneKind.Video => {
                item.asInstanceOf[VideoZoneModel].allowedType.map(a => {
                    if(a.equals("tvc")) rs += "tvc"
                    else if(a.equals("banner")) rs += "network"
                })
            }
            case ZoneKind.Banner => {
                item.asInstanceOf[BannerZoneModel].runningMode.map(r => {
                    if(r.equals("network")) rs += "network"
                    else if(r.equals("booking")) rs += "booking"
                })
            }
            case _ => {}
        }
        rs.toArray.distinct
    }

    def listAllApproveAds(status : Int) : Array[ApproveAdsModel] = {
        val pg = env.zoneToBannerService.listByStatus(status, 0, Int.MaxValue)
//        val pg = env.newBookingService.listByStatus(status,0,Int.MaxValue)
        val array = pg.data
        val arrZids = array.map(ztb=>ztb.zoneId).distinct
        val arrZones = env.zoneService.listByIds(arrZids.toList)

        //group zone by site id
        val webToZoneMap = arrZones.groupBy(z => z.siteId)
        val itemToZoneMap = array.groupBy(ztb=> ztb.itemId).toMap;

        var ret = Array[ApproveAdsModel]()
        var total: Int = 0
        for(siteId <- webToZoneMap.keySet){
            webToZoneMap.get(siteId) match {
                case Some(zones) => {
                    val itemIds = array.filter(ztb => zones.map(z=>z.id).contains(ztb.zoneId)).map(ztb => ztb.itemId).distinct
                    itemIds.foreach(itemId => {
                        var linkedZones : Array[Zone] = Array()
                        itemToZoneMap.get(itemId) match {
                            case Some(ztbs)=> {
                                val zids = ztbs.map(ztb=>ztb.zoneId)
                                linkedZones = zones.filter(z=> zids.contains(z.id))
                            }
                            case None=>{}
                        }
                        val banner = env.bannerModelService.load(itemId)
                        val site = env.websiteService.load(siteId)
                        if(site!=null && banner!=null){
                            val approveAd = new ApproveAdsModel(site.id, site.name, env.userService.load(site.ownerId).name, linkedZones.map(b=>b.id) ,linkedZones.map(b=>b.name), banner)
                            ret = ret:+(approveAd)
                            total += 1
                        }
                    })
                }
                case None => {}
            }
        }
        ret.filter(p => p.item != null).sortWith((a,b)=> a.item.id > b.item.id)
    }
    def listAds(status : Int, from :Int, count : Int ) : PagingResult[ApproveAdsModel] = {
        val ret = listAllApproveAds(status)
        new PagingResult[ApproveAdsModel](ret.slice(from,from+count),ret.size)
    }
    def listAds(status : Int, websiteId : Int, from : Int, count : Int) : PagingResult[ApproveAdsModel] = {
        val ret = listAllApproveAds(status).filter(ap=>ap.websiteId == websiteId)
        new PagingResult[ApproveAdsModel](ret.slice(from,from+count),ret.size)
    }
    def listAllContainAds(status : Int) : Array[WebsiteModel] = {
        val pg = env.zoneToBannerService.listByStatus(status,0,Int.MaxValue)
//        val pg = env.newBookingService.listByStatus(status,0,Int.MaxValue)
        val array = pg.data
        val arrZids = array.map(ztb=>ztb.zoneId).distinct
        val arrZones = env.zoneService.listByIds(arrZids.toList)
        //group zone by site id
        val siteIds = arrZones.map(z => z.siteId).distinct
        this.listByIds(siteIds.toList)
    }

    def listByFilter(from: Int, count: Int, sortBy: String, direction: String, filter: String) : PagingResult[WebsiteModel] = {
        if(filter != "uncompleted" && filter != "completed" && filter != "disable") list(from,count,sortBy,direction)
        else {
            val keySet = env.zoneService.list(from = 0, count = Int.MaxValue).data.map(z=>z.siteId).toList.distinct
            var rs = null.asInstanceOf[List[WebsiteModel]]
            if(filter == "uncompleted") {
                rs = list(from = 0, count = Int.MaxValue,sortBy,direction).data.filter(w=>(!keySet.contains(w.id))).toList
            } else if(filter == "completed"){
                rs = list(from = 0, count = Int.MaxValue,sortBy,direction).data.filter(w=>keySet.contains(w.id)).toList
            } else {
                return listDisable(from,count,sortBy,direction)
            }
            val size = Math.min(count, rs.size - from)
            val arr = rs.slice(from, from + size).toArray

            new PagingResult(arr, rs.size)
        }
    }
    //load list publisher for zone PR zone
    def loadZonesByGroupWebsite(kindZone:String):List[WebsiteModel] = {
        val rs = this.list(w=>{
            w.zoneModels.exists(z=>z.kind == kindZone)
        })
        rs.foreach(z=>z.zoneModels = z.zoneModels.filter(p=>p.kind == kindZone))
        rs
    }

    def listByTypeMinimize(from: Int, count: Int, reviewType: String, sortBy: String, direction: String): PagingResult[Website] = {
        val rs = new ArrayBuffer[Website]
        val types = new ArrayBuffer[String]
        reviewType.toLowerCase.split(",").map(t => {
            if(!types.contains(t)) {
                types += t
            }
        })

        if(types.length < 0) return new PagingResult(new Array[Website](0), 0)

        val sites = env.websiteService.list(0, Int.MaxValue, sortBy, direction)
        for(site <- sites.data) {

            val zones = env.zoneModelService.listByReferenceId(id = site.id).data
            val temp = new ArrayBuffer[String]
            temp ++= types.clone()
            for(zone <- zones; if temp.length > 0) {
                for(t <- getTypeOfZone(zone)) {
                    if(types.contains(t)) temp -= t
                }
            }
            if(temp.length == 0) rs += new Website(site.id, 0, site.name, null, null)
        }

        if(rs == null || sites.data.length <= 0) return new PagingResult(new Array[Website](0), 0)

        val size = Math.min(count, rs.size - from)
        val arr = rs.slice(from, from + size).toArray

        new PagingResult(arr, rs.size)
    }

}

class WebsiteRestHandler (env:{
    val websiteService: IDataService[Website]
    val websiteModelService: IWebsiteModelService
    val zoneToBannerService : IZoneToBannerService
    val zoneService: IDataService[Zone]
}) extends RestHandler[WebsiteModel,Website](()=>defaultWebsiteModel(), env.websiteModelService, env.websiteService){

    override def getItemFromModel(item: WebsiteModel) : Any = env.websiteService.load(item.id)

    override def beforeSave(request: HttpServletRequest, instance: WebsiteModel): Try[Unit] = {
        val user = PermissionUtils.getUser(request)
        if(user == null) return fail("Who are you ???")

        if (instance.name == null || instance.name.equals(""))
            return fail("Invalid name")

        if (instance.ownerId == 0)
            return fail("Invalid owner")
        instance.ownerId = user.id
        super.beforeSave(request, instance)
    }

    override def beforeUpdate(request: HttpServletRequest, instance: WebsiteModel): Try[Unit] = {
        val old = env.websiteService.load(instance.id)
        if(old == null) return fail("Item is not exist")
        if (instance.name == null || instance.name.equals(""))
            return fail("Invalid name")
        if (instance.ownerId == 0)
            return fail("Invalid owner")
        instance.ownerId = old.ownerId
        super.beforeUpdate(request, instance)
    }

    @Invoke(Parameters = "request,from,count,types")
    def listByType(request:HttpServletRequest, from: Int, count: Int, types: String) = {
        SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request,item, Permission.WEBSITE_VIEW_INFO))
        Json(env.websiteModelService.listByType(from, count, types))
    }

    @Invoke(Parameters = "request,status,site,from,count")
    def listAdsByStatus(request: HttpServletRequest, status: Int,site :Int, from: Int, count: Int) = {
        if(status == ZoneToBannerStatus.PENDING) SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request,item, Permission.WEBSITE_APPROVE))
        else SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request,item, Permission.WEBSITE_VIEW_INFO))

        if(site == 0){//list all
            val ret = env.websiteModelService.listAds(status,from,count)
            Json(ret)
        }else{
            val ret = env.websiteModelService.listAds(status,site,from,count)
            Json(ret)
        }
    }

    @Invoke(Parameters = "request")
    def getAdsSize(request:HttpServletRequest) = {
        SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request,item, Permission.WEBSITE_APPROVE))
        val size = env.websiteModelService.listAds(ZoneToBannerStatus.PENDING, 0, Int.MaxValue).data.length
        Text("{\"result\":" + size + "}")
    }

    def beforeApprove(request:HttpServletRequest, data: Array[ApproveItem]) : Try[Unit] = {
        for(i <- 0 until data.size){
            val obj  = data(i)
            val zoneId = obj.zoneId
            val zone = env.zoneService.load(zoneId)
            if(zone == null) return fail("Zone not found")
            if(!PermissionUtils.checkPermission(request, zone, Permission.WEBSITE_APPROVE)) return fail("You don't have permission")
        }
        succeed()
    }

    @Invoke(Parameters = "request,action,data")
    def approve(request:HttpServletRequest, action: Int, data : Array[ApproveItem]): Any = {
        if(data != null){
            if(!beforeApprove(request, data).isSuccess) return fail("You don't have permission")
            data.foreach(item => {
                action match {
                    case ZoneToBannerStatus.APPROVED => env.zoneToBannerService.approve(item.itemId, item.zoneId)
                    case ZoneToBannerStatus.REJECTED => env.zoneToBannerService.reject(item.itemId, item.zoneId)
                    case _ => {}
                }
            })
            Json("ok")
        }else {
            Json("failed")
        }
    }

    @Invoke(Parameters = "request,status")
    def listAdsWebsite(request:HttpServletRequest, status: Int) = {
        SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request,item, Permission.WEBSITE_VIEW_INFO))
        Json(env.websiteModelService.listAllContainAds(status))
    }

    @Invoke(Parameters = "request,from,count,filterBy")
    def listByFilter(request: HttpServletRequest,from: Int, count: Int, filterBy: String) = {
        SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request,item, Permission.WEBSITE_VIEW_INFO))
        var sortBy = request.getParameter("sortBy")
        var direction = request.getParameter("dir")

        if(sortBy != null && sortBy.length > 0){
            if(direction != "asc") direction = "desc"
        }
        else if (direction == "asc") sortBy = "id"
        else {
            sortBy = "id"
            direction = "desc"
        }

        Json(env.websiteModelService.listByFilter(from, count, sortBy, direction, filterBy))
    }

    @Invoke(Parameters = "request,from,count,types")
    def listByTypeMinimize(request: HttpServletRequest, from: Int, count: Int, types: String) = {
        SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request,item, Permission.WEBSITE_VIEW_INFO))
        var sortBy = request.getParameter("sortBy")
        var direction = request.getParameter("dir")

        if(sortBy != null && sortBy.length > 0){
            if(direction != "asc") direction = "desc"
        }
        else if (direction == "asc") sortBy = "id"
        else {
            sortBy = "id"
            direction = "desc"
        }
        Json(env.websiteModelService.listByTypeMinimize(from, count, types, sortBy, direction))
    }
}

class WebsiteServlet extends HandlerContainerServlet{
    def factory(): BaseHandler = new WebsiteRestHandler(Environment)
}
