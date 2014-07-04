package ads.website.modules

import ads.common.{SecurityContext, IItem}
import ads.common.database._
import ads.common.model._
import java.util.Date
import scala.collection.mutable.ArrayBuffer
import ads.common.Syntaxs._
import ads.web.{WebUtils, Invoke, HandlerContainerServlet}
import ads.web.mvc.{Text, Json, SmartDispatcherHandler, BaseHandler}
import javax.servlet.http.HttpServletRequest
import ads.website.{PermissionUtils, Environment}
import com.google.gson.reflect.TypeToken
import scala.concurrent.Future
import ads.common.services.{IZoneToBannerService, IActionLogService}
import java.util.concurrent.ConcurrentHashMap
import ads.common.Syntaxs.Failure
import ads.common.Syntaxs.Success

trait INewBookService extends IDataService[NewBookRecord]{

}

class NewBookService(env :{
    val newBookDataDriver: IDataDriver[NewBookRecord]
    val zoneToBannerService: IZoneToBannerService
    val actionLogServiceRef : Future[IActionLogService]
})
    extends InMemoryDataService[NewBookRecord](b=>b.zoneId, env.newBookDataDriver, env.actionLogServiceRef) with INewBookService {
    override def enable(id: Int) {
    }

    override def afterSave(item: NewBookRecord) = {
        env.zoneToBannerService.save(new ZoneToBanner(0, item.itemId, item.zoneId, ZoneToBannerStatus.PENDING))
    }

    override def remove(id : Int) = {
        val link = load(id)
        env.zoneToBannerService.disableLink(link.itemId, link.zoneId)
        super.remove(id)
    }
}

trait INewBookingService extends IDataService[NewBookRecordModel]{
    def getAvailableBooks(siteId: Int, from: Long, to: Long): List[BookAvailable]

    def getBookedByType(id: Int, kind: String, from: Long, to: Long): List[NewBookRecordModel]

    def book(data: List[NewBookRecord]) : Array[Int]

    def book(data: NewBookRecord) : Try[Any]

    def removeBook(data: List[Int])

    def updateBook(item: NewBookRecord) : Try[Unit]

    def getUsageByZone(zoneId: Int, from: Long, to: Long) : Float

    //from zone to banner
    def getLinkedByItem(itemId: Int): Array[Int]

    def listByStatus(status : Int, from: Int, count : Int) : PagingResult[NewBookRecord]

    def getItemsByZoneId(zoneId: Int) : PagingResult[NewBookRecord]
    def getItemsByItemId(itemId : Int) : PagingResult[NewBookRecord]

    def findBy(zoneId : Int, itemId : Int) : Array[NewBookRecord]
    def approve(action : Int, id : Int) : Int

    def getUnlinkedItem(campId: Int): ArrayBuffer[Int]
    def getUnlinkedZone(campId: Int): ArrayBuffer[Int]
    def getItemLinkedZones(itemId: Int): ArrayBuffer[Int]
}

class NewBookingService(env: {
    val newBookService: IDataService[NewBookRecord]
    val zoneService: IDataService[Zone]
    val websiteService: IDataService[Website]
    val bannerService: IDataService[Banner]

//    val bookService: IDataService[BookRecord]
    val campaignService: IDataService[Campaign]
//    val actionLogServiceRef : Future[IActionLogService]

//})extends InMemoryDataService[NewBookRecord](b=>b.zoneId, env.newBookService,env.actionLogServiceRef) with INewBookingService {
})    extends AbstractDelegateDataService[NewBookRecord,NewBookRecordModel](env.newBookService) with INewBookingService {

    val timeStep = 24L * 3600 * 1000

    def roundTime(f: Long): Long = {
        val d = new Date(f)
        return new Date(d.getYear, d.getMonth, d.getDate).getTime
    }

    def fromModel(model: NewBookRecordModel): NewBookRecord = new NewBookRecord(model.id, model.zoneId,model.itemId,model.from,model.to,model.share,model.status, model.disable)

    def toModel(item: NewBookRecord): NewBookRecordModel = {
        val zone = env.zoneService.load(item.zoneId)
        if (zone == null) return null.asInstanceOf[NewBookRecordModel]
        val site = env.websiteService.load(zone.siteId)
        if (site == null) return null.asInstanceOf[NewBookRecordModel]
        val banner = env.bannerService.load(item.itemId)
        if (banner == null) return null.asInstanceOf[NewBookRecordModel]
        new NewBookRecordModel(item.id, item.zoneId, zone.name, item.itemId, banner.name, site.id, site.name, item.from, item.to, item.share, ZoneToBannerStatus.PENDING, banner.width, banner.height)
    }

    def getAvailableBooks(siteId: Int, xFrom: Long, xTo: Long): List[BookAvailable] = {
        // correct from & to
        val from = roundTime(xFrom)
        val to = roundTime(xTo)

        //val zones = env.zoneService.list(0, Int.MaxValue).data
        val zones = env.zoneService.listByReferenceId(0, Int.MaxValue, siteId).data.filter(z => z.runningMode.contains("booking"))

        zones.map(zone => {
            val availables = calcAvailables(zone, from, to)
            new BookAvailable(zone.id, zone.name, env.websiteService.load(siteId).name, availables.toList, zone.disable)
        }).toList
    }

    def calcAvailables(zone: Zone, from: Long, to: Long): ArrayBuffer[AvailableRecord] = {
        val zoneId = zone.id
        var time = from
        var span = 0
        var currentShare = 101.0f
        val list = new ArrayBuffer[AvailableRecord]

        while (time < to) {
            var share : Float = 0
            val books = env.newBookService.listByReferenceId(id = zoneId).data
            share = books.filter(b => b.from <= time && b.to > time).map(b => b.share).sum

            if (share != currentShare) {
                if (span > 0 && currentShare > 0) {
                    // save current span
                    list += (new AvailableRecord(zoneId, time - span * timeStep, time, currentShare))
                }

                // new span
                span = 0
                currentShare = share
            }

            // next day
            time += timeStep
            span += 1
        }

        if (span > 0 && currentShare > 0) {
            list += (new AvailableRecord(zoneId, time - span * timeStep, time, currentShare))
        }

        list
    }

    def getBookedByType(id: Int, kind: String, from: Long, to: Long): List[NewBookRecordModel] = {
        kind.toLowerCase() match {
            case "item" =>
                getBookedByItems(List(id), from, to)
            case "camp" => {
                val ids = env.bannerService.loadAll().filter(p => p.campaignId == id).map(p => p.id).toList
                getBookedByItems(ids, from, to)
            }
            case "zone" => getBookedByZones(List(id), from, to)
            case "website" => {
                val ids = env.zoneService.loadAll().filter(p => p.siteId == id).map(p => p.id).toList
                getBookedByZones(ids, from, to)
            }
            case _ => null.asInstanceOf[List[NewBookRecordModel]]
        }
    }

    def getBookedByItems(ids: List[Int], from: Long, to: Long): List[NewBookRecordModel] = {
        if(from > 0 && to > 0)
            env.newBookService.loadAll().filter(p => ids.contains(p.itemId) && !(p.to < roundTime(from) ||  p.from > roundTime(to))).map(p => toModel(p)).filter(p => p != null).toList
        else
            env.newBookService.loadAll().filter(p => ids.contains(p.itemId)).map(p => toModel(p)).filter(p => p != null).toList
    }

    def getBookedByZones(ids: List[Int], from: Long, to: Long): List[NewBookRecordModel] = {
        if(from > 0 && to > 0)
            env.newBookService.loadAll().filter(p => ids.contains(p.zoneId) && !(p.to < roundTime(from) ||  p.from > roundTime(to))).map(p => toModel(p)).filter(p => p != null).toList
        else
            env.newBookService.loadAll().filter(p => ids.contains(p.zoneId)).map(p => toModel(p)).filter(p => p != null).toList
    }

    def getZoneStatus(zoneId: Int): Boolean = {
        val zoneItem = env.zoneService.load(zoneId)
        if (zoneItem == null) return true
        return zoneItem.disable
    }

    def validateBook(itemId: Int, zoneId: Int, f: Long, t: Long, share: Float, oldShare: Float, kind: String): Int = {
        val currentTime = roundTime(System.currentTimeMillis())
        if (getZoneStatus(zoneId)) return -1
        if (share <= 0 || share > 100) {
            return -2
        } else {
            val from = roundTime(f)
            val to = roundTime(t)
            val item = env.bannerService.load(itemId)
            if(item == null) return -3
            val camp = env.campaignService.load(item.campaignId)
            if(camp == null) return -4
            if(camp.status == CampaignStatus.TERMINATED) return -5
            
            if(from > to || (kind == "book" && currentTime > from && (camp.startDate!=0 && from < roundTime(camp.startDate))) || (camp.endDate!=0 && to > roundTime(camp.endDate))) return -6

            var time = from
            val totalShare = 100
            val books = env.newBookService.listByReferenceId(id = zoneId).data

            while (time < to) {
                val shareExist = books.filter(b => b.from <= time && b.to > time).map(b => b.share).sum
                if (share + shareExist - oldShare > totalShare) return -7
                // next day
                time += timeStep
            }
            0
        }
    }

    def book(book: NewBookRecord): Try[Any] = {
        book.from = roundTime(book.from)
        book.to = roundTime(book.to)
        validateBook(book.itemId,book.zoneId, book.from, book.to, book.share, 0, "book") match {
            case 0 => {
                val id = env.newBookService.save(book)
                succeed(id)
            }
            case -1 => {
                fail("Zone not found")
            }
            case -2 => {
                fail("Invalid share")
            }
            case -3 => fail("Item not found")
            case -4 => fail("Campaign not found")
            case -5 => fail("Campaign is terminating")
            case -6 => fail("Invalid time settings")
            case -7 => fail("Your share is too high")
        }

    }

    def book(data: List[NewBookRecord]) : Array[Int] = {
        val rs = new ArrayBuffer[Int]
        data.foreach(book => {
            book.from = roundTime(book.from)
            book.to = roundTime(book.to)
            if (validateBook(book.itemId,book.zoneId, book.from, book.to, book.share, 0, "book") == 0) rs += env.newBookService.save(book)
        })
        return rs.toArray
    }

    def removeBook(data: List[Int]): Unit = data.foreach(id =>env.newBookService.remove(id))

    def updateBook(item: NewBookRecord): Try[Unit] = {
        val currentTime = System.currentTimeMillis()
        val old = env.newBookService.load(item.id)
        if (item == null) return fail("Item is not found")
        item.from = roundTime(item.from)
        item.to = roundTime(item.to)
        if((old.from < currentTime && old.from != item.from) && (old.to < currentTime && old.to != item.to)) return fail("Can't change the past")
        if(item.from < old.from || item.to > old.to) return fail("Can't extend book")
        if (validateBook(item.itemId, item.zoneId, item.from, item.to, item.share, old.share,"updateBook") == 0) env.newBookService.update(item)
        succeed(true)
    }

    def getUsageByZone(zoneId: Int, f: Long, t: Long): Float = {
        var from = roundTime(f)
        val to = roundTime(t)
        val books = env.newBookService.listByReferenceId(id = zoneId).data
        if(books.length == 0) return 0

        var rs = 0.0f
        do{
            val tempTo = from + timeStep
            val total = books.filter(book=>((from >= book.from && from < book.to) || (tempTo <= book.to && tempTo > book.from))).map(b=>b.share).sum
            rs = Math.max(rs, total)
            from += timeStep
        } while(from < to)

        rs
    }

    //from zone to banner
    def getLinkedByItem(itemId: Int): Array[Int] = {
        env.newBookService.loadAll().filter(p=>p.itemId == itemId).map(p=>p.zoneId).toList.distinct.toArray
    }

    def listByStatus(status: Int, from: Int, count: Int): PagingResult[NewBookRecord] = {
        val list = env.newBookService.loadAll().filter(p => p.status == status).toArray
        new PagingResult[NewBookRecord](list.slice(from,from+count),list.size)
    }

    def getItemsByZoneId(zoneId: Int): PagingResult[NewBookRecord] = env.newBookService.listByReferenceId(id = zoneId)

    def getItemsByItemId(itemId: Int): PagingResult[NewBookRecord] = {
        val data = env.newBookService.loadAll().filter(p=>p.itemId == itemId).toArray
        new PagingResult[NewBookRecord](data,data.size)
    }

    def findBy(zoneId: Int, itemId: Int): Array[NewBookRecord] = {
        env.newBookService.listByReferenceId(id = zoneId).data.filter(p=>p.itemId == itemId).toArray
    }

    def approve(action: Int, id: Int): Int = {
        val item = env.newBookService.load(id)
        if(item == null) return 0
        var status = ZoneToBannerStatus.PENDING
        if(action == 0){
            status = ZoneToBannerStatus.REJECTED
        }else if(action == 1){
            status = ZoneToBannerStatus.APPROVED
        }
        item.status = status
        if(env.newBookService.update(item)) 1 else 0
    }

    def getUnlinkedItem(campId: Int): ArrayBuffer[Int] = {
        val itemSet = env.bannerService.listByReferenceId(id = campId).data.map(i=>i.id).toList
        val result = ArrayBuffer[Int]()
        val itemLinks = env.newBookService.loadAll().map(p=>p.itemId).toList
        for (itemId <- itemSet){
            if(!itemLinks.contains(itemId)) result += itemId
        }
        result
    }

    def getUnlinkedZone(campId: Int): ArrayBuffer[Int] = {
        val zoneSet = env.zoneService.loadAll().map(i=>i.id).toSet
        val result = ArrayBuffer[Int]()
        val zoneLinks = env.newBookService.loadAll().map(p=>p.zoneId)
        for(zId <- zoneSet) {
            if(!zoneLinks.contains(zId)) result+= zId
        }
        new ArrayBuffer[Int]()
    }

    def getItemLinkedZones(itemId: Int): ArrayBuffer[Int] =
    {
        new ArrayBuffer[Int]()
    }
}

class NewBookingHandler(env:{
    val newBookingService: INewBookingService
    val zoneService: IDataService[Zone]
    val newBookService: IDataService[NewBookRecord]
    val bannerService: IDataService[Banner]
    val campaignService: IDataService[Campaign]
}) extends SmartDispatcherHandler{

    def beforeBookAction(request:HttpServletRequest, zones: Array[Int], items :Array[Int]) : Try[Unit] = {
        zones.foreach(zid=>{
            val zone = env.zoneService.load(zid)
            if(zone == null) return fail("Zone not found")
            if(!PermissionUtils.checkPermission(request, zone, Permission.WEBSITE_BOOKING)) return fail("You don't have permission")
        })
        items.foreach(id=>{
            val item = env.bannerService.load(id)
            if(item == null) return fail("Item not found")
            val campaign = env.campaignService.load(item.campaignId)
            if(campaign == null || campaign.status == CampaignStatus.TERMINATED) return fail("Can't book on this campaign")
            if(!PermissionUtils.checkPermission(request, item, Permission.ORDER_EDIT)) return fail("You don't have permission")
        })
        succeed()
    }

    @Invoke(Parameters = "siteId,from,to")
    def getAvailableBooks(siteId: Int, from: Long, to: Long) = env.newBookingService.getAvailableBooks(siteId, from, to) |> Json.apply

    @Invoke(Parameters = "request")
    def book1(request: HttpServletRequest): Any = {
        val instance = readFromRequest[NewBookRecord](request, new NewBookRecord(0,0,0,0,0,0,ZoneToBannerStatus.PENDING))
        val s = beforeBookAction(request, Array(instance.zoneId), Array(instance.itemId))
        if (!s.isSuccess) return s
        Json(env.newBookingService.book(instance))
    }

    @Invoke(Parameters = "request,data")
    def book(request: HttpServletRequest, data: String): Any = {
        val list = WebUtils.fromJson[List[NewBookRecord]](new TypeToken[List[NewBookRecord]](){}.getType, data)
        val s = beforeBookAction(request, list.map(b=>b.zoneId).toArray, list.map(b=>b.itemId).toArray)
        if (!s.isSuccess) return s
        Json(env.newBookingService.book(list))
    }

    @Invoke(Parameters = "request,ids")
    def removeBook(request: HttpServletRequest, ids : Array[Int]): Any = {
        val books = env.newBookService.listByIds(ids.toList)
        if(books == null || books.length == 0) return fail("Items not found")
        val s = beforeBookAction(request, books.map(b=>b.zoneId) , books.map(b=>b.itemId))
        if (!s.isSuccess) return s
        env.newBookingService.removeBook(ids.toList)
        success()
    }

    @Invoke(Parameters = "request,id,kind,from,to")
    def getBookedByKind(request: HttpServletRequest, id: Int, kind: String, from: Long, to: Long) = {
        SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request,item,Permission.WEBSITE_BOOKING))
        Json(env.newBookingService.getBookedByType(id, kind, from, to))
    }

    @Invoke(Parameters = "zoneId,from,to")
    def getUsageByZone(zoneId: Int, from: Long, to: Long)  = {
        Json(new UsageResult(env.newBookingService.getUsageByZone(zoneId,from,to)))
    }

    @Invoke(Parameters = "request")
    def update(request: HttpServletRequest): Any = {
        val instance = readFromRequest[NewBookRecord](request, new NewBookRecord(0,0,0,0,0,0,ZoneToBannerStatus.PENDING))
        val s = beforeBookAction(request, Array(instance.zoneId), Array(instance.itemId))
        if (!s.isSuccess) return s
        env.newBookingService.updateBook(instance) match {
            case Success(_) => success()
            case Failure(cause) => fail(cause.toString)
        }
    }
}

class NewBookingServlet extends HandlerContainerServlet{
    def factory(): BaseHandler = new NewBookingHandler(Environment)
}
