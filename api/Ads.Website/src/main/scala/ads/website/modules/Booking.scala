package ads.website.modules

import ads.common.model._
import ads.common.database._
import scala.collection.JavaConversions._
import ads.web.mvc.{Text, BaseHandler, Json, SmartDispatcherHandler}
import ads.web.{WebUtils, HandlerContainerServlet, Invoke}
import ads.common.Syntaxs._
import com.google.gson.reflect.TypeToken
import ads.common.services.{ActionLogService, ZoneToBannerService, IZoneToBannerService}
import scala.collection.mutable.{ArrayBuffer}
import scala.Some
import java.util.Date
import javax.servlet.http.HttpServletRequest
import ads.website.{PermissionUtils, Environment}

class AvailableRecord(var zoneId: Int, var from: Long, var to: Long, var share: Float)

class BookAvailable(var zoneId: Int, var zoneName: String, var site: String, var availables: List[AvailableRecord], var disable: Boolean)

class QuoteRecord(var zoneId: Int, var zoneName: String, var site: String, var cost: Long, var availables: List[AvailableRecord])

class QuoteResponse(var total: Long, var items: List[QuoteRecord])

class BookedZone(var zoneId: Int, var name: String, var siteName: String, var width: Int, var height: Int, var share: Int, var items: Int, var disable: Boolean)

class BookedPlan(var id: Int, var zoneId: Int, var zone: String, var links: Int, var site: String, var share: Int, var from: Long, var to: Long, var disable: Boolean)

class UsageResult(var total: Float)


trait IBookingService {
//    def book(campaignId: Int, zoneId: Int, from: Long, to: Long, share: Int) : Try[Boolean]
//
//    def book(campaignId: Int, list: List[BookRecord])
//
//    def getBooked(website: Int, from: Long, to: Long): List[BookAvailable]
//
//    def getQuote(records: List[AvailableRecord]): QuoteResponse
//
//    def getBookedZones(campId: Int): List[BookedZone]
//
//    def getUnlinkedItem(campId: Int): ArrayBuffer[Int]
//    def getUnlinkedZone(campId: Int): ArrayBuffer[Int]
//    def getItemLinkedZones(itemId: Int): ArrayBuffer[Int]
//
//    def updateZoneLinks(campId: Int, zoneId: Int, banners: Array[Int])
//
//    def getBookedByCamp(campId: Int, from: Int, count: Int, sortBy: String, direction: String) : PagingResult[BookedPlan]
//
//    def getUsageByZone(zoneId: Int, from: Long, to: Long) : Int
//
//    def updateShare(id : Int, usage: Int) : Try[Boolean]
//
//    def removeBook(id : Int) : Unit
//
//    def updateDateBook(id: Int, f: Long, t: Long) : Unit
}

class TestBookingService{

    object MockEnv{
        val bookService: IDataService[BookRecord] = new InMemoryDataService[BookRecord](b=>b.zoneId, new SqlDataService[BookRecord](()=>new BookRecord(0, 0,0,0,0,0)), Environment.actionLogServiceRef)
        val zoneService: IDataService[Zone] = new InMemoryDataService[Zone](z=>z.siteId,new SqlDataService[Zone](()=>new Zone(0,0,null,null,null,0,0,0,0,0,ZoneKind.Banner,null,null)), Environment.actionLogServiceRef)
        val websiteService: IDataService[Website] = new InMemoryDataService[Website](s=>s.id, new SqlDataService[Website](()=>new Website(0,0,"","","")),null)
        val bannerService: IDataService[Banner] = new InMemoryDataService[Banner](b=>b.campaignId, new SqlDataService[Banner](()=>new Banner(0,0,null,null,null,0,0,null,null,0,0)),Environment.actionLogServiceRef)
        val zoneToBannerService: IZoneToBannerService = new ZoneToBannerService(Environment)
    }

    def getService() = new BookingService(getMockEnv())

    def getMockEnv() = MockEnv

    def testBookDuplicate(){
        val service = getService()
    }

    def testBooking() {
        
    }
}

class BookingService(env: {
    val bookService: IDataService[BookRecord]
    val zoneService: IDataService[Zone]
    val websiteService: IDataService[Website]
    val bannerService: IDataService[Banner]
    val zoneToBannerService: IZoneToBannerService}) extends IBookingService {
    val timeStep = 24L * 3600 * 1000

//    def roundTime(f: Long): Long = {
//        val d = new Date(f)
//        return new Date(d.getYear, d.getMonth, d.getDate).getTime
//    }
//
//    def getZoneStatus(zoneId: Int) : Boolean = {
//        val zoneItem = env.zoneService.load(zoneId)
//        if(zoneItem == null) return true
//        return zoneItem.disable
//    }
//
//    def validateBook(zoneId: Int, f: Long, t: Long, share: Int, oldShare: Int) : Int = {
//        if(getZoneStatus(zoneId)) return -3
//        if (share <= 0) {
//            -1
//        } else {
//            val from = roundTime(f)
//            val to = roundTime(t)
//            var time = from
//            var flag = true
//            val totalShare = 100
//            val books = env.bookService.listByReferenceId(id = zoneId).data
//
//            while(flag && time < to) {
//                val shareExist = books.filter(b => b.from <= time && b.to > time).map(b => b.share).sum
//                if(share + shareExist - oldShare > totalShare) flag = false
//                // next day
//                time += timeStep
//            }
//            if(flag) 0
//            else -2
//        }
//    }
//
//    def book(campaignId: Int, zoneId: Int, f: Long, t: Long, share: Int) : Try[Boolean] = {
//        validateBook(zoneId, f, t, share, 0) match {
//            case -3 => fail("Invalid Zone")
//            case -2 => fail("Total share is greater than 0")
//            case -1 => fail("Share is less than 0")
//            case 0 => {
//                var from = roundTime(f)
//                var to = roundTime(t)
//                val refs = env.bookService.listByReferenceId(id = zoneId).data.filter(b=>(b.campaignId == campaignId && b.share == share))
//                val beforeBook = refs.find(b=>b.to == from) match {
//                    case Some(b) => b
//                    case None => null
//                }
//                val afterBook = refs.find(b=>b.from == to) match {
//                    case Some(b) => b
//                    case None => null
//                }
//
//                if(beforeBook != null && afterBook != null) {
//                    env.bookService.remove(beforeBook.id)
//                    afterBook.from = beforeBook.from
//                    env.bookService.update(afterBook)
//                    from = afterBook.from
//                    to = afterBook.to
//                } else if(beforeBook != null && afterBook == null) {
//                    beforeBook.to = to
//                    env.bookService.update(beforeBook)
//                    from = beforeBook.from
//                    to = beforeBook.to
//                } else if(beforeBook == null && afterBook != null) {
//                    afterBook.from = from
//                    env.bookService.update(afterBook)
//                    from = afterBook.from
//                    to = afterBook.to
//                } else {
//                    env.bookService.save(new BookRecord(0, campaignId, zoneId, share, roundTime(f), roundTime(t)))
//                }
//
//                val rel = env.bookService.listByReferenceId(id = zoneId).data.filter(b=>(b.campaignId == campaignId && (!(b.to < from || b.from > to))))
//                if (rel.length < 2) return succeed(true)
//                var relTime = new ArrayBuffer[Long]
//                rel.map(record=>{
//                    relTime += record.from
//                    relTime += record.to
//                })
//
//                relTime = relTime.distinct.sortWith((a, b) => (a < b))
//                for(i <- 0 to (relTime.length - 2)) {
//                    val from = relTime(i)
//                    val to = relTime(i + 1)
//
////                    val data = rel.filter(r => (from >= r.from && to <= r.to))
//
//                    var totalShare = 0
//                    var count = 0
//                    val listTimeEqualBook = new ArrayBuffer[BookRecord]
//                    for(record <- rel.filter(r => (from >= r.from && to <= r.to))) {
//                        if(record.to > to) {
//                            env.bookService.update(new BookRecord(record.id, record.campaignId , record.zoneId, record.share, to, record.to))
//                            totalShare += record.share
//                            count +=1
//                        } else if (record.to == to) {
//                            listTimeEqualBook += record
//                            totalShare += record.share
//                        }
//                    }
//                    if(listTimeEqualBook.length > 1) {
//                        listTimeEqualBook.slice(1,listTimeEqualBook.length - 1).map(a => env.bookService.remove(a.id))
//                        env.bookService.save(new BookRecord(0, campaignId, zoneId, Math.min(100, totalShare) , from, to)) // truong hop total share > 0 chua check
//                        val record = listTimeEqualBook(0)
//                        env.bookService.update(new BookRecord(record.id, record.campaignId, record.zoneId, Math.min(100, totalShare) , from, to))
//                    } else if(listTimeEqualBook.length == 1 && count > 0) {
//                        val record = listTimeEqualBook(0)
//                        env.bookService.update(new BookRecord(record.id, record.campaignId, record.zoneId, Math.min(100, totalShare) , from, to))
//                    } else if(count > 0) {
//                        env.bookService.save(new BookRecord(0, campaignId, zoneId, Math.min(100, totalShare), from, to))
//                    }
//                }
//                succeed(true)
//            }
//        }
//    }
//
//    def book(campaignId: Int, list: List[BookRecord]) {
//        for (record <- list.sortBy(b=>b.from)) {
//            book(campaignId, record.zoneId, record.from, record.to, record.share)
//        }
//    }
//
//
//    def getBooked(website: Int, xFrom: Long, xTo: Long): List[BookAvailable] = {
//        // correct from & to
//        val from = roundTime(xFrom)
//        val to = roundTime(xTo)
//
//        //val zones = env.zoneService.list(0, Int.MaxValue).data
//        val zones = env.zoneService.listByReferenceId(0, Int.MaxValue, website).data.filter(z=>z.runningMode.contains("booking"))
//
//        zones.map(zone=>{
//            val availables = calcAvailables(zone, from, to)
//            new BookAvailable(zone.id, zone.name, env.websiteService.load(website).name, availables.toList, zone.disable)
//        }).toList
//    }
//
//
//    def calcAvailables(zone: Zone, from: Long, to: Long): ArrayBuffer[AvailableRecord] = {
//        val zoneId = zone.id
//        var time = from
//        var span = 0
//        var currentShare = 101
//        val list = new ArrayBuffer[AvailableRecord]()
//
//        while (time < to) {
//            var share = 0
//            val books = env.bookService.listByReferenceId(id = zoneId).data
//
//            share = books.filter(b => b.from <= time && b.to > time).map(b => b.share).sum
//
//            if (share != currentShare) {
//                if (span > 0 && currentShare > 0) {
//                    // save current span
//                    list.add(new AvailableRecord(zoneId, time - span * timeStep, time, currentShare))
//                }
//
//                // new span
//                span = 0
//                currentShare = share
//            }
//
//            // next day
//            time += timeStep
//            span += 1
//        }
//
//        if (span > 0 && currentShare > 0) {
//            list.add(new AvailableRecord(zoneId, time - span * timeStep, time, currentShare))
//        }
//
//        list
//    }
//
//    def getQuote(records: List[AvailableRecord]): QuoteResponse = {
//        val map = records.groupBy(r => r.zoneId)
//        val zones = env.zoneService.listByIds(map.keySet.toList).filter(t => t != null).map(t => (t.id, t)).toMap
//
//        var total = 0L
//        val quotes = new ArrayBuffer[QuoteRecord]()
//
//        for (zoneId <- map.keySet) {
//            zones.get(zoneId) match {
//                case Some(zone) => {
//                    val list = map.get(zoneId).get
//                    val cost = list.map(r => (r.to - r.from + 1) / timeStep).sum * zone.bookingPrice
//
//                    val availables = list.map(r => new AvailableRecord(zoneId, r.from, r.to, r.share)).toList
//
//                    quotes.add(new QuoteRecord(zoneId, zone.name, env.websiteService.load(zone.siteId).name, cost, availables))
//
//                    total += cost
//                }
//                case None => {}
//            }
//        }
//
//        return new QuoteResponse(total, quotes.toList)
//    }
//
////    def getUnlinkedItem(campId: Int): ArrayBuffer[Int] = {
////        val itemSet = env.bannerService.listByReferenceId(id = campId).data.map(i=>i.id).toSet
////        val result = ArrayBuffer[Int]()
////        for (itemId <- itemSet){
////            val hasLink = env.zoneToBannerService.getLinkedByItem(itemId).size > 0
////            if (!hasLink) result += itemId
////        }
////        result
////    }
//
//    def getUnlinkedZone(campId: Int): ArrayBuffer[Int] = {
//        val zoneSet = env.bookService.loadAll().filter(b=>b.campaignId == campId).toList.groupBy(b=>b.zoneId).map(b=>b._1)
//        val itemSet = env.bannerService.listByReferenceId(id = campId).data.map(i=>i.id).toSet
//        val result = ArrayBuffer[Int]()
//        for (zoneId <- zoneSet){
//            val hasLink = env.zoneToBannerService.listByReferenceId(id = zoneId).data.exists(z=>itemSet.contains(z.itemId))
//            if (!hasLink) result += zoneId
//        }
//        result
//    }
//
////    def getItemLinkedZones(itemId: Int): ArrayBuffer[Int] = env.zoneToBannerService.getLinkedByItem(itemId)
//
//
//    def updateZoneLinks(campId: Int, zoneId: Int, banners: Array[Int]) {
//        val itemSet = env.bannerService.listByReferenceId(id = campId).data.map(i=>i.id).toSet
//        val linked = env.zoneToBannerService.listByReferenceId(id = zoneId).data.filter(b=>itemSet.contains(b.itemId)).toList
//        val bannerSet = banners.toSet
//        val linkedSet = linked.map(l=>l.itemId).toSet
//
//        val adds = banners.filter(id => !linkedSet.contains(id))
//        val removes = linked.filter(l=> !bannerSet.contains(l.itemId)).toList
//
//        for (add <- adds) env.zoneToBannerService.save(new ZoneToBanner(0, zoneId, add, 0, 0))
//        for (remove <- removes) env.zoneToBannerService.remove(remove.id)
//    }
//
//    def getBookedZones(campId: Int): List[BookedZone] = {
//        val zoneRecords = env.bookService.loadAll().filter(b=>b.campaignId == campId).toList.groupBy(b=>b.zoneId)
//        val itemSet = env.bannerService.listByReferenceId(id = campId).data.map(i=>i.id).toSet
//
//        val rs = new ArrayBuffer[BookedZone]
//
//        zoneRecords.map(zoneRecord=>{
//            val zoneId = zoneRecord._1
//            val zone = env.zoneService.load(zoneId)
//            if(zone != null) {
//                val website = env.websiteService.load(zone.siteId)
//                if(website != null) {
//                    val currentTime = System.currentTimeMillis()
//                    val share = zoneRecord._2.filter(p=>p.from <= currentTime && p.to >= currentTime).map(b=>b.share).sum
//                    val items = env.zoneToBannerService.listByReferenceId(id = zoneId).data.count(z=>itemSet.contains(z.itemId))
//
//                    rs += new BookedZone(zoneId, zone.name, website.name, zone.width, zone.height, share, items, zone.disable)
//                }
//            }
//
//        })
//        rs.toList
//    }
//
//    def getBookedByCamp(campId: Int, from: Int, count: Int, sortBy: String, direction: String) = {
//        val bookeds = env.bookService.list(0, Int.MaxValue, sortBy, direction).data.filter(b=>b.campaignId == campId).toList
//        val size = Math.min(count, bookeds.size - from)
//
//        var rs = new ArrayBuffer[BookedPlan]
//
//        for(booked <- bookeds.slice(from, from + size)) {
//            val zone = env.zoneService.load(booked.zoneId)
//            if(zone != null) {
//                val site = env.websiteService.load(zone.siteId)
//                if(site != null) {
//                    var count = 0
//                    val zoneToBanner = env.zoneToBannerService.listByReferenceId(id = zone.id).data
//                    if(zoneToBanner != null && zoneToBanner.size > 0) {
//                        val banners = env.bannerService.listByIds(zoneToBanner.map(b=>b.itemId).toList.distinct)
//                        if(banners != null && banners.size < 9) count = banners.filter(b=>b.campaignId == campId).length
//                    }
//
//                    rs = rs :+ (new BookedPlan(booked.id, booked.zoneId, zone.name, count, site.name, booked.share, booked.from, booked.to, zone.disable))
//                }
//            }
//        }
//        new PagingResult[BookedPlan](rs.toArray, bookeds.size)
//    }
//
//    def getUsageByZone(zoneId: Int, f: Long, t: Long): Int = {
//        var from = roundTime(f)
//        val to = roundTime(t)
//        val books = env.bookService.listByReferenceId(id = zoneId).data.filter(book=>((book.from >= from && book.from < to) || (book.to <= to && book.to > from)))
//        if(books.length == 0) return 0
//
//        var rs = 101
//        do{
//            val tempTo = from + timeStep
//            val total = books.filter(book=>((from >= book.from && from < book.to) || (tempTo <= book.to && tempTo > book.from))).map(b=>b.share).sum
//            rs = Math.min(rs, total)
//            from += timeStep
//        } while(from < to)
//
//        rs
//    }
//
//    def updateShare(id: Int, usage: Int) = {
//        val old = env.bookService.load(id)
//        if(old == null) fail("Book record is not exist")
//
//        validateBook(old.zoneId, old.from, old.to, usage, old.share) match {
//            case -2 => fail("Total share is greater than 0")
//            case -1 => fail("Share is less than 0")
//            case 0 => {
//
//                val refs = env.bookService.listByReferenceId(id = old.zoneId).data.filter(b=>(b.campaignId == old.campaignId && b.share == usage))
//                val beforeBooks = refs.filter(b=>(b.to >= old.from && b.to < old.to && b.from < old.from))
//                val afterBooks = refs.filter(b=>(b.from <= old.to && b.from > old.from && b.to > old.to))
//
//                if(beforeBooks.length > 0 && afterBooks.length > 0) {
//                    val maxToBook = afterBooks.maxBy(b => b.to)
//                    val minFromBook = beforeBooks.minBy(b => b.from)
//                    env.bookService.remove(minFromBook.id)
//                    env.bookService.remove(old.id)
//                    maxToBook.from = minFromBook.from
//                    env.bookService.update(maxToBook)
//                } else if(beforeBooks.length > 0 && afterBooks.length == 0) {
//                    val minFromBook = beforeBooks.minBy(b => b.from)
//                    minFromBook.to = old.to
//                    env.bookService.remove(old.id)
//                    env.bookService.update(minFromBook)
//                } else if(beforeBooks.length == 0 && afterBooks.length > 0) {
//                    val maxToBook = afterBooks.maxBy(b=>b.to)
//                    maxToBook.from = old.from
//                    env.bookService.remove(old.id)
//                    env.bookService.update(maxToBook)
//                } else {
//                    old.share = usage
//                    env.bookService.update(old)
//                }
//                succeed(true)
//            }
//        }
//    }
//
//    def removeBook(id: Int) = {
//        val book = env.bookService.load(id)
//        val zoneToBannerItems = env.zoneToBannerService.listByReferenceId(id = book.zoneId)
//
//        if(zoneToBannerItems.data != null && zoneToBannerItems.data.length > 0) {
//            val bannerIds = env.bannerService.listByReferenceId(id = book.campaignId).data.map(b=>b.id).toList
//            val temp = zoneToBannerItems.data.filter(i=>bannerIds.contains(i.itemId)).toList
//            temp.foreach(i=>env.zoneToBannerService.remove(i.id))
//        }
//
//        env.bookService.remove(id)
//    }
//
//    def updateDateBook(id: Int, f: Long, t: Long): Unit = {
//        val from = roundTime(f)
//        val to = roundTime(t)
//        if(from >= to) {
//            return
//        }
//        val book = env.bookService.load(id)
//        if(book != null) {
//            if(!(from < book.from || to > book.to)) {
//                book.from = from
//                book.to = to
//                env.bookService.update(book)
//            }
//        }
//    }
}

class BookingHandler(env:{
    val bookingService: IBookingService
    val zoneService: IDataService[Zone]
    val bookService: IDataService[BookRecord]
    val campaignService: IDataService[Campaign]
}) extends SmartDispatcherHandler{

//    def beforeBookAction(request:HttpServletRequest, zones: Array[Int], campId :Int) : Try[Unit] = {
//        zones.foreach(zid=>{
//            val zone = env.zoneService.load(zid)
//            if(zone == null) return fail("Zone not found")
//            if(!PermissionUtils.checkPermission(request, zone, Permission.WEBSITE_BOOKING)) return fail("You don't have permission")
//        })
//        val campaign = env.campaignService.load(campId)
//        if(campaign == null) return fail("Campaign not found")
//        if(!PermissionUtils.checkPermission(request, campaign, Permission.ORDER_EDIT)) return fail("You don't have permission")
//        succeed()
//    }
//
//    @Invoke(Parameters = "siteId,from,to")
//    def getBooked(siteId: Int, from: Long, to: Long) = env.bookingService.getBooked(siteId, from, to) |> Json.apply
//
//    @Invoke(Parameters = "data")
//    def getQuote(data: String) = {
//        val list = WebUtils.fromJson[List[AvailableRecord]](new TypeToken[List[AvailableRecord]](){}.getType, data)
//        Json(env.bookingService.getQuote(list))
//    }
//
//    @Invoke(Parameters = "request,campaignId,data")
//    def book(request: HttpServletRequest, campaignId: Int, data: String): Any = {
//        val list = WebUtils.fromJson[List[BookRecord]](new TypeToken[List[BookRecord]](){}.getType, data)
//        val s = beforeBookAction(request, list.map(b=>b.zoneId).toArray, campaignId)
//        if (!s.isSuccess) return s
//        env.bookingService.book(campaignId, list)
//        Text("{\"result\": \"success\"}")
//    }
//
//    @Invoke(Parameters = "request,campId,from,count")
//    def getBookedByCamp(request: HttpServletRequest, campId : Int, from: Int, count: Int) = {
//        var sortBy = request.getParameter("sortBy")
//        var direction = request.getParameter("dir")
//        if(sortBy != null && sortBy.length > 0){
//            if(direction != "asc") direction = "desc"
//        }
//        else if (direction == "asc") sortBy = "id"
//        else {
//            sortBy = "id"
//            direction = "desc"
//        }
//        Json(env.bookingService.getBookedByCamp(campId, from, count, sortBy, direction))
//    }
//
//    @Invoke(Parameters = "zoneId,from,to")
//    def getUsageByZone(zoneId: Int, from: Long, to: Long)  = {
//        Json(new UsageResult(env.bookingService.getUsageByZone(zoneId,from,to)))
//    }
//
//    @Invoke(Parameters = "request,id,usage")
//    def updateShare(request:HttpServletRequest, id: Int, usage: Int): Any = {
//        val book = env.bookService.load(id)
//        if(book == null) return fail("Item not found")
//        val s = beforeBookAction(request, Array(book.zoneId), book.campaignId)
//        if (!s.isSuccess) return s
//        env.bookingService.updateShare(id, usage) match {
//            case Success(_) => success()
//            case Failure(cause) => fail(cause.toString)
//        }
//    }
//
//    @Invoke(Parameters = "request,id")
//    def removeBook(request: HttpServletRequest, id : Int): Any = {
//        val book = env.bookService.load(id)
//        if(book == null) return fail("Item not found")
//        val s = beforeBookAction(request, Array(book.zoneId), book.campaignId)
//        if (!s.isSuccess) return s
//        env.bookingService.removeBook(id)
//        success()
//    }
//
//    @Invoke(Parameters = "request,id,from,to")
//    def updateDateBook(request:HttpServletRequest, id: Int, from: Long, to: Long): Any = {
//        val book = env.bookService.load(id)
//        if(book == null) return fail("Item not found")
//        val s = beforeBookAction(request, Array(book.zoneId), book.campaignId)
//        if (!s.isSuccess) return s
//        env.bookingService.updateDateBook(id, from, to)
//        success()
//    }
}

class BookingServlet extends HandlerContainerServlet{
    def factory(): BaseHandler = new BookingHandler(Environment)
}