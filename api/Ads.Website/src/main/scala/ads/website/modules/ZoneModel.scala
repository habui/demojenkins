package ads.website.modules

import ads.common._
import ads.common.database.{InMemoryDataService, PagingResult, IDataService, AbstractDelegateDataService}
import ads.common.model._
import ads.website.handler.RestHandler
import ads.web.mvc._
import ads.web.{WebUtils, Invoke, HandlerContainerServlet}
import scala.collection.JavaConversions._
import javax.servlet.http.HttpServletRequest
import ads.website.{PermissionUtils, Environment}
import ads.common.services._
import ads.common.Syntaxs.{fail, succeed, Try}
import scala.collection.mutable.ArrayBuffer
import org.omg.Dynamic.Parameter
import com.google.gson.Gson
import scala.collection.parallel.mutable


object ZoneRender{
    def getHtmlCode(zone: Zone): String = {
        ""
    }
}

trait IZoneModelService extends  IDataService[ZoneModel] {
    def listByRefId(refId : Int, kind : String, from : Int, count : Int) : PagingResult[ZoneModel]
    def listByRefIdAndRunningMode(refId : Int, runningMode: String, from : Int, count : Int) : PagingResult[ZoneModel]
    def listByFilter(pId: Int, from: Int, count: Int, sortBy: String, direction: String, filter: String, pType: String) : PagingResult[ZoneModel]
    def getLinkedByItem(itemId : Int, from : Int, count : Int) : PagingResult[ZoneModel]
    def getTargetedByItem(itemId : Int, from : Int, count : Int) : PagingResult[ZoneModel]
    def getReferencedByItem(itemId : Int,from : Int, count : Int) : PagingResult[ZoneModel]
    def zoneToModel(item: Zone): ZoneModel
}

class ZoneModelService(env: {
    val bannerService: IBannerService
    val zoneService: IZoneService
    val bannerModelService : IBannerModelService
//    val zoneToZoneGroupService: IZoneToZoneGroupService
    val zoneToZoneGroupService: IDataService[ZoneToZoneGroup]
//    val newZoneToBannerService: INewZoneToBannerService
    val newBookingService : INewBookingService
    val newBookService: IDataService[NewBookRecord]
    })
    extends AbstractDelegateDataService[Zone, ZoneModel](env.zoneService) with IZoneModelService {

    val groupLink = env.zoneToZoneGroupService

    def toModel(zone: Zone): ZoneModel = {
        zone.kind match {
            case ZoneKind.Banner => {
                val currentTime = System.currentTimeMillis()
                var books = env.newBookService.listByReferenceId(id = zone.id).data.filter(b=>b != null && b.from <= currentTime && b.to >= currentTime)
                if(books != null && books.length > 0) {
                    val bannerIds = env.bannerService.listByIds(books.map(b=>b.itemId).toList.distinct).map(b=>b.id)
                    books = books.filter(b=>bannerIds.contains(b.itemId))
                }
                val links = books.size
                val usage = books.map(l => l.share).sum
//                val links = 0
//                val usage = 0

                //val groupList = groupLink.list(link => link.zoneId == zone.id, link => link.zoneGroup)
                //val groups = new Array[Int](groupList.length)
                //for (i <- 0 until groupList.length) groups(i) = groupList.get(i)
                val groups: Array[Int] = Array.empty
                if(zone.extra == null){
                    val m =  new BannerZoneModel(zone.id, zone.siteId, zone.name, zone.runningMode, zone.categories, zone.bookingPrice, zone.minCPC, zone.minCPM, links, usage,false , zone.width, zone.height, groups)
                    val json = WebUtils.toRawJson(m)
                    zone.extra = json
                    env.zoneService.update(zone)
                    m.id = zone.id
                    m
                }else{
                    val m = WebUtils.fromJson(classOf[BannerZoneModel],zone.extra)
                    if(m!=null){
                        m.id = zone.id
                        m.links = links
                        m.usage = usage
                        m.groups = groups
                    }
                    m
                }

            }
            case ZoneKind.Video => {
                val instance = WebUtils.fromJson(classOf[VideoZoneModel],zone.extra)
                if(instance != null){
                    instance.id = zone.id
                }
                instance
            }
            case ZoneKind.PRNetwork => {
                val instance = WebUtils.fromJson(classOf[PRZoneModel],zone.extra)
                if(instance != null){
                    instance.id = zone.id
                }
                instance
            }
        }
    }
    def fromModel(model: ZoneModel): Zone = {
        model.kind match {

            case ZoneKind.Video => {
                val m = model.asInstanceOf[VideoZoneModel]
                val json = WebUtils.toRawJson(m)
                new Zone( m.id, m.siteId, m.name, m.runningMode.toList, m.categories, 0, m.minCPC, m.minCPM, m.width, m.height, m.kind, m.renderKind,json, m.frequencyCapping, m.frequencyCappingTime, model.disable)
            }
            case ZoneKind.PRNetwork => {
                val m = model.asInstanceOf[PRZoneModel]
                val json = WebUtils.toRawJson(m)
                new Zone( m.id, m.siteId, m.name,null,null, 0,0,0,0, 0, m.kind, m.renderKind,json, m.frequencyCapping, m.frequencyCappingTime, model.disable)
            }
            case _ => {
                val m = model.asInstanceOf[BannerZoneModel]
                val json = WebUtils.toRawJson(m)
                new Zone(m.id, m.siteId, m.name, m.runningMode, m.categories, m.bookingPrice, m.minCPC, m.minCPM, m.width, m.height,ZoneKind.Banner, m.renderKind,json, m.frequencyCapping, m.frequencyCappingTime, model.disable)
            }
        }
    }

    override def afterSave(model: ZoneModel){
        //@TODO: missing banner
        val records = groupLink.list(link => link.zoneId == model.id).listIterator().toArray
        val ids = records.map(g => g.zoneGroup).toList

        if (model.groups != null){
            for (group <- model.groups) {
                if (!ids.contains(group)) {
                    groupLink.save(new ZoneToZoneGroup(0, model.id, group))
                }
            }
        }
        for (record <- records) {
            if (!model.groups.contains(record.zoneGroup)) groupLink.remove(record.id)
        }
    }

    override def remove(id: Int): Unit = {
        val old = load(id)
        if(old == null) return
        val zoneGroupIds = old.groups
        for(zId <- zoneGroupIds) groupLink.listByReferenceId(id = zId).data.filter(z=>z.zoneId == id).map(z=>groupLink.remove(z.id))
        //remove zone2Banner Item have zoneId = id
//        env.zoneToBannerService.listByReferenceId(id = id).data.map(z=>env.zoneToBannerService.remove(z.id))
        //remove bookRecord have zoneId = id
//        env.bookService.listByReferenceId(id = id).data.map(z=>env.bookService.remove(z.id))
        super.remove(id)
    }

    override def search(query: String, pId: Int, isDisable: Boolean, take: Int = 10) = {
        if(pId > 0) env.zoneService.loadAll().filter(p => p.siteId == pId && p.disable == isDisable && p.name.toLowerCase.contains(query.toLowerCase)).take(take).map(toModel).toList
        else env.zoneService.loadAll().filter(p => p.disable == isDisable && p.name.toLowerCase.contains(query.toLowerCase)).take(take).map(toModel).toList
    }
    def listByRefId(refId : Int, kind : String, from : Int, count : Int) : PagingResult[ZoneModel] = {
        convertRecord(env.zoneService.listByRefId(refId,kind,from,count))
    }
    def listByRefIdAndRunningMode(refId : Int, runningMode: String, from : Int, count : Int) : PagingResult[ZoneModel] = {

        convertRecord(env.zoneService.listByRefIdAndRunningMode(refId,runningMode,from,count))
    }

    def listByFilter(pId: Int, from: Int, count: Int, sortBy: String, direction: String, filter: String, pType: String) : PagingResult[ZoneModel] = {
        var zones = null.asInstanceOf[Array[Zone]]

        pType match {
            case "website" => {
                filter match {
                    case "disable" => zones = env.zoneService.listDisable(0, Int.MaxValue, sortBy, direction).data
                    case "video" => zones = env.zoneService.list(0, Int.MaxValue, sortBy, direction).data.filter(z=>z.kind.equals("video"))
                    case "banner" => zones = env.zoneService.list(0, Int.MaxValue, sortBy, direction).data.filter(z=>z.kind.equals("banner"))
                    case _ => zones = env.zoneService.list(0, Int.MaxValue, sortBy, direction).data
                }

                zones = zones.filter(z=>z.siteId == pId)
            }
            case _ => {
                val zids = env.zoneToZoneGroupService.loadAll().toList.filter(p=>p.zoneGroup == pId).map(p=>p.zoneId).toList
                val disabledZids = env.zoneToZoneGroupService.listDisable(0,Int.MaxValue).data.toList.filter(p=>p.zoneGroup == pId).map(p=>p.zoneId).toList

                filter match {
                    case "disable" => zones = env.zoneService.listDisable(0, Int.MaxValue, sortBy, direction).data.filter(z=>disabledZids.contains(z.id))
                    case "video" => zones = env.zoneService.list(0, Int.MaxValue, sortBy, direction).data.filter(z=>z.kind.equals("video") && zids.contains(z.id))
                    case "banner" => zones = env.zoneService.list(0, Int.MaxValue, sortBy, direction).data.filter(z=>z.kind.equals("banner")  && zids.contains(z.id))
                    case _ => zones = env.zoneService.list(0, Int.MaxValue, sortBy, direction).data.filter(z=> zids.contains(z.id))
                }

            }
        }
        val size = Math.min(count, zones.size - from)
        val arr = zones.slice(from, from + size).toArray

        new PagingResult(arr.map(z=>toModel(z)), zones.size)
    }
    def getLinkedByItem(itemId : Int, from : Int, count : Int) : PagingResult[ZoneModel] = {
        val zones = listByIds(env.newBookingService.getItemsByItemId(itemId).data.map(ztb => ztb.zoneId).distinct.toList)

        if(zones != null){
            new PagingResult[ZoneModel](zones.slice(from, from + count),zones.size)
        }else {
            new PagingResult[ZoneModel](Array(),0)
        }
    }
    def getTargetedByItem(itemId : Int, from : Int, count : Int) : PagingResult[ZoneModel] = {
        val item = env.bannerModelService.load(itemId)
        if(item != null){
             val zids = item match {
                 case network: INetwork => network.targetZones
                 case _ => Array()
             }
            val zones : Array[ZoneModel] = if(zids != null) listByIds(zids.toList) else Array()
            new PagingResult[ZoneModel](zones.slice(from,from + count),zones.size)
        }else {
            new PagingResult[ZoneModel](Array(),0)
        }
    }
    def getReferencedByItem(itemId : Int,from : Int, count : Int) : PagingResult[ZoneModel] = {
        val linked = getLinkedByItem(itemId,0,Int.MaxValue)
        val targeted = getTargetedByItem(itemId,0, Int.MaxValue)
        val all = linked.data.++:(targeted.data)
        new PagingResult[ZoneModel](all.slice(from,from + count),all.size)
    }

    def zoneToModel(item: Zone): ZoneModel = internalToModel(item)
}

object defaultZoneModel {
    def apply() = new BannerZoneModel(0, 0, "", null, null, 0, 0, 0, 0,0, false, 0, 0, null)
}

class ZoneRestHandler(env: {
    val zoneModelService: IZoneModelService
    val zoneService: IDataService[Zone]
    val websiteService: IDataService[Website]
    val bannerModelService: IBannerModelService
})
    extends RestHandler[ZoneModel,Zone](() => defaultZoneModel(), env.zoneModelService, env.zoneService) {

    override def getItemFromModel(item: ZoneModel) : Any = env.zoneService.load(item.id)

    override def beforeSave(request: HttpServletRequest, instance: ZoneModel): Try[Unit] = {
     if(instance.kind == ZoneKind.PRNetwork) {
         if (instance.name == null || instance.name.equals("")) return fail("Invalid name")
         if (instance.siteId == 0) return fail("Invalid siteId")
         if(PermissionUtils.checkPermission(request, env.websiteService.load(instance.siteId), Permission.WEBSITE_EDIT)) succeed()
         else fail("Don't have permmission")
     } else {
         if(instance.name == null || instance.name.equals("")) return fail("Invalid name")
         if(instance.siteId == 0) return fail("Invalid site")
         if(instance.categories == null || instance.categories.length <= 0 || instance.categories(0) == 0) return fail("Invalid categories")
         if(((instance.height == 0 || instance.width == 0) && instance.kind == ZoneKind.Banner)) return fail("Invalid size")
         if (PermissionUtils.checkPermission(request, env.websiteService.load(instance.siteId), Permission.WEBSITE_EDIT)) succeed()
         else fail("Don't have permmission")
        }
    }

    override def beforeUpdate(request: HttpServletRequest, instance: ZoneModel): Try[Unit] = {
        if(instance.kind == ZoneKind.PRNetwork) {
            if(instance.name == null || instance.name.equals("")) return fail("Invalid name")
            if (instance.siteId == 0) return fail("Invalid siteId")
            return super.beforeUpdate(request,instance)
        }
        if(instance.name == null || instance.name.equals("")) return fail("Invalid name")
        if(instance.siteId == 0) return fail("Invalid siteId")
        if(((instance.height == 0 || instance.width == 0) && instance.kind == ZoneKind.Banner)) return fail("Invalid size")
        if (instance.categories == null || instance.categories.length <= 0 || instance.categories(0) == 0)
            return fail("Invalid categories")
        super.beforeUpdate(request, instance)
    }

    @Invoke(Parameters = "request")
    override def save(request: HttpServletRequest): Any = {
        val kind = request.getParameter("kind")
        val instance = kind match{
            case ZoneKind.Video => {
                val ins : VideoZoneModel = readFromRequest[VideoZoneModel](request,
                new VideoZoneModel(0,0,"",Array(0),0,0,Array(0),Array(""),0,Array(),Array(""),Array(""),
                0,0,0,0,Array(""),0,0,0,0,0,0,0,0,0,0,0,0,0,TimeUnit.MINUTE,0,0,0,0))
                val timeSegments = request.getParameter("timeSegments")
                val json = if(timeSegments != null) "[" + timeSegments + "]" else null
                val timeSgs : Array[VideoZoneTimeSegment] = if(json != null) WebUtils.fromJson(classOf[Array[VideoZoneTimeSegment]],json) else null
                ins.timeSegments = timeSgs
                ins
            }
            case ZoneKind.PRNetwork => {
                val ins : PRZoneModel = readFromRequest[PRZoneModel](request,
                    new PRZoneModel(0,0,"",0,0,0))
                ins.kind = ZoneKind.PRNetwork
                ins
            }
            case _ => {
                val is = readFromRequest[BannerZoneModel](request, new BannerZoneModel(0,0,"",List(""),Array(0),0,0,0,0,0,false,0,0,Array(0)))
                is.kind = ZoneKind.Banner
                is
            }
        }
        val s = beforeSave(request, instance)
        if (!s.isSuccess) return s
        instance.id = modelService.save(instance)
        afterSave(instance)
        Json(instance)
    }


    @Invoke(Parameters = "request,id")
    override def update(request: HttpServletRequest, id : Int): Any = {
//        SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request,item, Permission.WEBSITE_VIEW_INFO))
        val old = env.zoneModelService.load(id)
        if(old == null) return Text("{\"result\":\"error\", \"message\": \"not found!\"}")

        val kind = request.getParameter("kind")
        val instance = kind match{
            case ZoneKind.Video => {
                val ins : VideoZoneModel = readFromRequest[VideoZoneModel](request,
                    new VideoZoneModel(0,0,"",Array(0),0,0,Array(0),Array(""),0,Array(),Array(""),Array(""),
                        0,0,0,0,Array(""),0,0,0,0,0,0,0,0,0,0,0,0,0,TimeUnit.MINUTE,0,0,0,0))
                ins
            }
            case ZoneKind.PRNetwork => {
                val ins : PRZoneModel = readFromRequest[PRZoneModel](request,
                    new PRZoneModel(0,0,"",0,0,0))
                ins.kind = ZoneKind.PRNetwork
                ins
            }

            case _ => {
                val is = readFromRequest[BannerZoneModel](request, new BannerZoneModel(0,0,"",List(""),Array(0),0,0,0,0,0,false,0,0,Array(0)))
                is.kind = ZoneKind.Banner
                is
            }

        }
        val s = beforeUpdate(request, instance)
        if (!s.isSuccess) return s
        val ret = modelService.update(instance)
        afterUpdate(old,instance)
        Text("{\"result\": \"success\"}")
    }
    @Invoke(Parameters = "zoneId", bypassFilter = true)
    def getHtmlCode(zoneId: Int) = Text(Render.renderZone(env.zoneService.load(zoneId)))

    @Invoke(Parameters = "zoneId", bypassFilter = true)
    def getLinkHtml(zoneId: Int) = Text(Config.fullHostDomain + "/rdext/html?zoneId=" + SecurityUtils.encode(zoneId))

    @Invoke(Parameters = "zoneId", bypassFilter = true)
    def getLinkVast(zoneId: Int) = {
        val zone = modelService.load(zoneId)
        if(zone != null && zone.kind == ZoneKind.Video) Text(Config.fullHostDomain + "/zad/videoad?zid=" + SecurityUtils.encode(zoneId))
        else Text("")
    }

    @Invoke(Parameters = "zoneId", bypassFilter = true)
    def getLinkJson(zoneId: Int) = Text(Config.fullHostDomain + "/rdext/json?zoneId=" + SecurityUtils.encode(zoneId) + "&count=100")

    @Invoke(Parameters = "request,refId,kind,from,count")
    def listByRefIdAndKind(request: HttpServletRequest,refId : Int, kind :String, from :Int, count : Int) = {
        SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request,item, Permission.WEBSITE_VIEW_INFO))
        Json(env.zoneModelService.listByRefId(refId,kind,from,count))
    }

    @Invoke(Parameters = "request,id,runningMode,from,count", bypassFilter = true)
    def listByRefIdAndRunningMode(request: HttpServletRequest,refId : Int, runningMode : String, from : Int, count : Int)  = {
        SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request, item, Permission.WEBSITE_VIEW_INFO))
        if(runningMode.toLowerCase.equals("tvc")){
            val ret = env.zoneModelService.listByRefIdAndRunningMode(refId,runningMode,0,Int.MaxValue)
            val data = ret.data.filter(z => {
                val zm = z.asInstanceOf[VideoZoneModel]
                zm.allowedType.contains("tvc")
            })
            Json(new PagingResult[ZoneModel](data.slice(from,from+count),data.size))
        }else if(runningMode.toLowerCase.equals("networkbanner")){
            val tvc = env.zoneModelService.listByRefIdAndRunningMode(refId,"tvc",0,Int.MaxValue)
            val tvcBanners = tvc.data.filter(p => {
                val zm = p.asInstanceOf[VideoZoneModel]
                zm.allowedType.contains("banner")
            })
            val network = env.zoneModelService.listByRefIdAndRunningMode(refId,"network",0,Int.MaxValue).data
            val networkBanners = network.filter(z => {
                if(z.isInstanceOf[VideoZoneModel]){
                    val m = z.asInstanceOf[VideoZoneModel]
                    if(m.allowedType != null && m.allowedType.contains("banner")) true else false
                }else {
                    true
                }
            })
            val ret =tvcBanners.++:(networkBanners)
            Json(new PagingResult[ZoneModel](ret.slice(from,from + count),ret.size))
        }else{
            val pret =  env.zoneModelService.listByRefIdAndRunningMode(refId,runningMode,from,count)
            Json(pret)
        }
    }

    @Invoke(Parameters = "request,pId,pType,from,count,filterBy,status")
    def countPrByStatus(request: HttpServletRequest, pId: Int, pType:String, from: Int, count: Int, filterBy: String, status: Int) = {
        SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request,item, Permission.WEBSITE_VIEW_INFO))
        val sortBy = "id"
        val direction = "desc"

        val newCount = Int.MaxValue
        val rawJson = JsonExt.gson.toJson(env.zoneModelService.listByFilter(pId, from, newCount, sortBy, direction, filterBy, pType))
        val len = rawJson.length()
        val sb = new StringBuilder()
        var i = rawJson.indexOf("\"id\"",0)
        var result : PagingResult[ArticleModel] = null
        var c : Int = 0
        var finalResult : Map[Int, Int] = Map()
        var tt = new java.util.HashMap[Int, Int]()

        while( i >=0 ) {
            for (j <- i until len ; if c < 3) {
                if( rawJson.charAt(j) == '"' ) c += 1

                if(c>=2 && c<3 && rawJson.charAt(j) >= '0' && rawJson.charAt(j) <= '9' ) sb.append(rawJson.charAt(j))
            }

            val zoneIdF = sb.toInt
            result = env.bannerModelService.listByFilterInZone(zoneIdF,0,0,10,"name","","")
            val finalRawJson = JsonExt.gson.toJson(result)
            val lenF = finalRawJson.length()
            var ii = finalRawJson.indexOf("status",0)
            //finalResult.put(zoneIdF, 0)
            finalResult += ((zoneIdF, 0))
            sb.clear()

            while( ii >= 0) {
                c = 0
                for( jj <- ii until lenF ; if c < 3) {
                    if( finalRawJson.charAt(jj) == '"' ) c += 1

                    if(c>=1 && c<2 && finalRawJson.charAt(jj) >= '0' && finalRawJson.charAt(jj) <= '9' ) sb.append(finalRawJson.charAt(jj))
                }

                if( sb.toInt == status) finalResult = finalResult.updated(zoneIdF, finalResult(zoneIdF) + 1)
                ii = finalRawJson.indexOf("status",ii+1)
                sb.clear()
            }

            i = rawJson.indexOf("\"id\"",i+1)
            c = 0
        }

        sb.append("{\"data\":[")
        var flag : Int = 0
        for( (k,v) <- finalResult ) {
            if(flag == 1)
                sb.append(",")
            sb.append(s"""{"zoneID":$k,"count":$v}""")
            flag = 1
        }
        sb.append("]}")
        Text(sb)
    }

    @Invoke(Parameters = "request,pId,pType,from,count,filterBy")
    def listByFilter(request: HttpServletRequest, pId: Int, pType:String, from: Int, count: Int, filterBy: String) = {
        SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request,item, Permission.WEBSITE_VIEW_INFO))
        var sortBy = request.getParameter("sorlistByFiltertBy")
        var direction = request.getParameter("dir")

        if(sortBy != null && sortBy.length > 0){
            if(direction != "asc") direction = "desc"
        }
        else if (direction == "asc") sortBy = "id"
        else {
            sortBy = "id"
            direction = "desc"
        }
        Json(env.zoneModelService.listByFilter(pId, from, count, sortBy, direction, filterBy, pType))
    }
}

class ZoneServlet extends HandlerContainerServlet {
    def factory(): BaseHandler = new ZoneRestHandler(Environment)
}

trait IZone2ModelService extends IDataService[ZoneModel] {

}

class Zone2ModelService(env: {
    val zone2Service: IZone2Service
    val zoneToZoneGroupService: IDataService[ZoneToZoneGroup]
    val newBookingService : INewBookingService
})
    extends AbstractDelegateDataService[Zone, ZoneModel](env.zone2Service) with IZone2ModelService {

    val groupLink = env.zoneToZoneGroupService

    def toModel(item: Zone): ZoneModel = {

        item.kind match {
            case ZoneKind.Banner => {
                val currentTime = System.currentTimeMillis()
                val books = env.newBookingService.listByReferenceId(id = item.id).data.filter(b=>b!=null && b.from <= currentTime && b.to >= currentTime)
                val links = books.size
                val usage = books.map(l => l.share).sum

                val groupList = groupLink.list(link => link.zoneId == item.id, link => link.zoneGroup)
                val groups = new Array[Int](groupList.length)
                for (i <- 0 until groupList.length) groups(i) = groupList.get(i)
                if(item.extra == null){
                    val m =  new BannerZoneModel(item.id, item.siteId, item.name, item.runningMode, item.categories, item.bookingPrice, item.minCPC, item.minCPM, links, usage,false , item.width, item.height, groups)
                    val json = WebUtils.toRawJson(m)
                    item.extra = json
                    env.zone2Service.update(item)
                    m.id = item.id
                    m
                }else{
                    val m = WebUtils.fromJson(classOf[BannerZoneModel],item.extra)
                    if(m!=null){
                        m.id = item.id
                        m.links = links
                        m.usage = usage
                        m.groups = groups
                    }
                    m
                }

            }
            case ZoneKind.Video => {
                val instance = WebUtils.fromJson(classOf[VideoZoneModel],item.extra)
                if(instance != null){
                    instance.id = item.id
                }
                instance
            }
            case ZoneKind.PRNetwork => {
                val instance = WebUtils.fromJson(classOf[PRZoneModel],item.extra)
                if(instance != null){
                    instance.id = item.id
                }
                instance
            }
        }
    }
    def fromModel(model: ZoneModel): Zone = {
        model.kind match {

            case ZoneKind.Video => {
                val m = model.asInstanceOf[VideoZoneModel]
                val json = WebUtils.toRawJson(m)
                new Zone( m.id, m.siteId, m.name, m.runningMode.toList, m.categories, 0, m.minCPC, m.minCPM, m.width, m.height, m.kind, m.renderKind,json)
            }
            case ZoneKind.PRNetwork => {
                val m = model.asInstanceOf[PRZoneModel]
                val json = WebUtils.toRawJson(m)
                new Zone( m.id, m.siteId, m.name,null,null, 0,0,0,0, 0, m.kind, m.renderKind,json)
            }
            case _ => {
                val m = model.asInstanceOf[BannerZoneModel]
                val json = WebUtils.toRawJson(m)
                new Zone(m.id, m.siteId, m.name, m.runningMode, m.categories, m.bookingPrice, m.minCPC, m.minCPM, m.width, m.height,ZoneKind.Banner, m.renderKind,json)
            }
        }
    }
}