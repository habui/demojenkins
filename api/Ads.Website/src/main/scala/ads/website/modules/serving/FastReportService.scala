package ads.website.modules.serving

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ops._
import java.util.{Timer, TimerTask, Calendar, Date}
import ads.common.collections.Cube
import ads.common.Syntaxs._
import ads.common.services.report._
import com.netflix.config.DynamicPropertyFactory
import scala.io.Source
import java.io._
import ads.website.modules._
import ads.common.database.IDataService
import scala.Some
import ads.common.model._
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import ads.website.{ModelCache, PermissionUtils}
import ads.common.services.{IBannerService, IFastReportService, IConversionTypeService, ZoneToBannerService}
import java.util
import scala.Some
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.methods.HttpPost
import ads.web.WebUtils
import com.google.gson.internal.LinkedTreeMap
import scala.Some
import scala.Some
import java.util.zip.{GZIPOutputStream, ZipEntry, ZipOutputStream}
import ads.common.storage.Compress


class ReportRecord (var order: Int, var camp: Int, var item: Int, var website: Int, var zoneGroup: Array[Int], var zone: Int,
                    var impression: Int, var click: Int, var spam: Int, var conversion: Int, var conversionValue: Int, var conversionType: Int,
                    var render: Int, var pageView: Int, var collapse: Int, var expand: Int, var creative: Int, var firstQuartile: Int,
                    var midPoint: Int, var thirdQuartile: Int, var complete: Int, var close: Int, var pause: Int, var resume: Int, var fullScreen: Int,
                    var mute: Int, var unMute: Int, var skip: Int) {

    override def hashCode(): Int = {
        var x = (order * 13) ^ (camp * 23) ^ (item * 37) ^ (website * 101) ^ (zone + 13) ^ (conversionType + 23)
        for (i <- zoneGroup) x ^= i * 13
        x
    }

    override def equals(obj: scala.Any): Boolean =
        if (super.equals(obj)) true
        else
        if (!obj.isInstanceOf[ReportRecord]) false
        else {
            val y = obj.asInstanceOf[ReportRecord]
            return order == y.order && camp == y.camp && item == y.item && website == y.website && zoneGroup.filter(x=>x != 0).deep == y.zoneGroup.filter(x=>x != 0).deep && zone == y.zone && conversionType == y.conversionType
        }
}

object TrackKind {
    val Impression = "impression"
    val Click = "click"
    val Spam = "click.spam"
    val Render = "render"
    val PageView = "pageview"
    val Collapse = "trackCollapse"
    val Expand = "trackExpand"
    val SubClick = "trackSubClick"
    val Creative = "creative"
    val FirstQuartile = "firstQuartile"
    val MidPoint = "midPoint"
    val ThirdQuartile = "thirdQuartile"
    val Complete = "complete"
    val Close = "close"
    val Pause = "pause"
    val Resume = "resume"
    val Fullscreen = "fullscreen"
    val Mute = "mute"
    val Unmute = "unmute"
    val Preroll = "preroll"
    val Midroll = "midroll"
    val Postroll = "postroll"
    val Skip = "skip"
}

object InfoKind {
    val Order = "o"
    val Campaign = "c"
    val Item = "i"
    val Website = "w"
    val ZoneGroup = "zg"
    val Zone = "z"
    val Video = "v"
    val AdType = "ad"
    val Impression = "imp"
    val Click = "click"
    val Spam = "spam"
    val Conversion = "conv"
    val ConversionValue = "convv"
    val ConversionType = "convt"
    val Render = "render"
    val PageView = "pview"
    val Collapse= "collapse"
    val Expand = "expand"
    val Creative = "creative"
    val FirstQuartile = "first"
    val MidPoint = "mid"
    val ThirdQuartile = "third"
    val Complete = "complete"
    val Close = "close"
    val Pause = "pause"
    val Resume = "resume"
    val Fullscreen = "full"
    val Mute = "mute"
    val Unmute = "unmute"
    val Skip = "skip"
}

trait IFastCachedLogService {
    def readAll(f: (Int, ReportRecord)=>Unit)
    def save(index: Int, r: ReportRecord)
    def flush()
}

class FastCachedLogService (path: String) extends IFastCachedLogService  {
    val buf = new ArrayBuffer[(Int,ReportRecord)]()

    def readAll(f: (Int, ReportRecord) => Unit): Unit = {
        var map: Map[String, String] = Map()
        for (line <- Source.fromFile(path).getLines()){
            val seqs = line.split(',')
            val time = seqs(0).toInt
            for (i <- 1 to seqs.length / 2)
                map += (seqs(i*2-1) -> seqs(i*2))

            val order = map.getOrElse(InfoKind.Order, "0").toInt
            val camp = map.getOrElse(InfoKind.Campaign, "0").toInt
            val item = map.getOrElse(InfoKind.Item, "0").toInt
            val website = map.getOrElse(InfoKind.Website, "0").toInt
            val zoneGroup = map.getOrElse(InfoKind.ZoneGroup, "").split("\\|").filter(x=>x.equals("") == false).foldLeft[ArrayBuffer[Int]](new ArrayBuffer[Int]())(_ += _.toInt).toArray
            val zone = map.getOrElse(InfoKind.Zone, "0").toInt
            val impression = map.getOrElse(InfoKind.Impression, "0").toInt
            val click = map.getOrElse(InfoKind.Click, "0").toInt
            val spam = map.getOrElse(InfoKind.Spam, "0").toInt
            val conversion = map.getOrElse(InfoKind.Conversion, "0").toInt
            val conversionValue = map.getOrElse(InfoKind.ConversionValue, "0").toInt
            val conversionType = map.getOrElse(InfoKind.ConversionType, "0").toInt
            val render = map.getOrElse(InfoKind.Render, "0").toInt
            val pageView = map.getOrElse(InfoKind.PageView, "0").toInt
            val collapse = map.getOrElse(InfoKind.Collapse, "0").toInt
            val expand = map.getOrElse(InfoKind.Expand, "0").toInt
            val creative = map.getOrElse(InfoKind.Creative, "0").toInt
            val firstQuartile = map.getOrElse(InfoKind.FirstQuartile, "0").toInt
            val midPoint = map.getOrElse(InfoKind.MidPoint, "0").toInt
            val thirdQuartile = map.getOrElse(InfoKind.ThirdQuartile, "0").toInt
            val complete = map.getOrElse(InfoKind.Complete, "0").toInt
            val close = map.getOrElse(InfoKind.Close, "0").toInt
            val pause = map.getOrElse(InfoKind.Pause, "0").toInt
            val resume = map.getOrElse(InfoKind.Resume, "0").toInt
            val fullScreen = map.getOrElse(InfoKind.Fullscreen, "0").toInt
            val mute = map.getOrElse(InfoKind.Mute, "0").toInt
            val unMute = map.getOrElse(InfoKind.Unmute, "0").toInt
            val skip = map.getOrElse(InfoKind.Skip, "0").toInt
            val record: ReportRecord = new ReportRecord(order, camp, item, website, zoneGroup, zone, impression, click, spam, conversion, conversionValue, conversionType, render,
                                                        pageView, collapse, expand, creative, firstQuartile, midPoint, thirdQuartile, complete, close, pause, resume, fullScreen, mute, unMute, skip)
            f(time, record)
        }
    }

    def save(index: Int, r: ReportRecord): Unit = {
        buf += ((index, r))
    }

    def flush(): Unit = {
        val s = new PrintWriter(new BufferedWriter(new FileWriter(path, true)))
        for ((time,record) <- buf){
            s.println(s"${time}," +
                s"${InfoKind.Order},${record.order}," +
                s"${InfoKind.Campaign},${record.camp}," +
                s"${InfoKind.Item},${record.item}," +
                s"${InfoKind.Website},${record.website}," +
                s"${InfoKind.ZoneGroup},${record.zoneGroup.foldLeft[String]("")(_ + _ + "|")}," +
                s"${InfoKind.Zone},${record.zone}," +
                s"${InfoKind.Impression},${record.impression}," +
                s"${InfoKind.Click},${record.click}," +
                s"${InfoKind.Spam},${record.spam}," +
                s"${InfoKind.Conversion},${record.conversion}," +
                s"${InfoKind.ConversionValue},${record.conversionValue}," +
                s"${InfoKind.ConversionType},${record.conversionType}," +
                s"${InfoKind.Render},${record.render}," +
                s"${InfoKind.PageView},${record.pageView}," +
                s"${InfoKind.Collapse},${record.collapse}," +
                s"${InfoKind.Expand},${record.expand}," +
                s"${InfoKind.Creative},${record.creative}," +
                s"${InfoKind.FirstQuartile},${record.firstQuartile}," +
                s"${InfoKind.MidPoint},${record.midPoint}," +
                s"${InfoKind.ThirdQuartile},${record.thirdQuartile}," +
                s"${InfoKind.Complete},${record.complete}," +
                s"${InfoKind.Close},${record.close}," +
                s"${InfoKind.Pause},${record.pause}," +
                s"${InfoKind.Resume},${record.resume}," +
                s"${InfoKind.Fullscreen},${record.fullScreen}," +
                s"${InfoKind.Mute},${record.mute}," +
                s"${InfoKind.Unmute},${record.unMute}," +
                s"${InfoKind.Skip},${record.skip}"
            )
        }
        s.close()
        buf.clear()
    }
}

class FastReportService
    (env: {
            val log: ILog
            val fastCachedLogService: IFastCachedLogService
            val orderModelService: IOrderModelService
            val campaignModelService: IDataService[CampaignModel]
            val websiteModelService: IWebsiteModelService
            val zoneGroupModelService: IZoneGroupModelService
            val zoneModelService: IZoneModelService
            val bannerModelService: IBannerModelService
            val bannerCache: ModelCache[BannerModel]
            val conversionTypeService: IConversionTypeService
            val compressService: ICompressService
    }) extends IFastReportService {
    class ReportItemInfo (val id: Int, val time: Long, val itemKind: String, var impression: Long, var click: Long, var spam: Long, var validClick: Long, var conversion: Long, var conversionValue: Long, var requestImpression: Long, var pageView: Long, var collapse: Long, var expand: Long,
                          var creative: Long, var firstQuartile: Long, var midPoint: Long, var thirdQuartile: Long, var complete: Long, var close: Long, var pause: Long, var resume: Long, var fullScreen: Long,
                          var mute: Long, var unMute: Long, var skip: Long)

    val factory = DynamicPropertyFactory.getInstance()
    var updated = 0L
    var currentChar: Long = 0
    var startDate: Date = null
    val maps = new mutable.HashMap[Int,Cube[ReportRecord]]()
    var currentCompressTime = 0L

    def processLog(map: mutable.HashMap[ReportRecord,ReportRecord], iterator: Iterator[LogRecord]) = {
        using (iterator){
            for (item <- iterator){
                if (item != null){
                    currentChar += item.length
                    if (item.time > 0) {
                        val conversionValue = if (item.kind.startsWith("conversion")) item.value else 0
                        var (impression, click, spam, render, pageView, collapse, expand, creative, firstQuartile, midPoint, thirdQuartile, complete, close, pause, resume, fullScreen, mute, unMute, skip) =
                            (0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0)
                        item.kind match {
                            case TrackKind.Impression => impression = 1
                            case TrackKind.Click => click = 1
                            case TrackKind.Spam => spam = 1
                            case TrackKind.Render => render = 1
                            case TrackKind.PageView => pageView = 1
                            case TrackKind.Collapse => collapse = 1
                            case TrackKind.Expand => expand = 1
                            case TrackKind.SubClick =>
                            case TrackKind.Creative => creative = 1
                            case TrackKind.FirstQuartile => firstQuartile = 1
                            case TrackKind.MidPoint => midPoint = 1
                            case TrackKind.ThirdQuartile => thirdQuartile = 1
                            case TrackKind.Complete => complete = 1
                            case TrackKind.Close => close = 1
                            case TrackKind.Pause => pause = 1
                            case TrackKind.Resume => resume = 1
                            case TrackKind.Fullscreen => fullScreen = 1
                            case TrackKind.Mute => mute = 1
                            case TrackKind.Unmute => unMute = 1
                            case TrackKind.Skip => skip = 1
                            case _ =>
                        }
                        val getConversionType = item.kind.split("_@#")
                        var conversion = 0
                        val conversionTypeId =
                            if (item.kind.startsWith("conversion")){
                                conversion = 1
                                if (getConversionType.length > 1){
                                    val conversionType = getConversionType(1)
                                    val objConvType = env.conversionTypeService.searchByName(conversionType)
                                    if (objConvType == null)
                                        env.conversionTypeService.save(new ConversionType(0, conversionType))
                                    else objConvType.id
                                } else 0
                            } else 0
                        val record = new ReportRecord(item.orderId, item.campId, item.itemId, item.siteId, item.zoneGroupIds, item.zoneId, impression, click, spam, conversion, conversionValue, conversionTypeId, render, pageView, collapse, expand, creative, firstQuartile, midPoint, thirdQuartile, complete, close, pause, resume, fullScreen, mute, unMute, skip)
                        map.get(record) match{
                            case Some(x) => {
                                x.impression += record.impression
                                x.click += record.click
                                x.spam += record.spam
                                x.conversion += record.conversion
                                x.conversionValue += record.conversionValue
                                x.render += record.render
                                x.pageView += record.pageView
                                x.collapse += record.collapse
                                x.expand += record.expand
                                x.creative += record.creative
                                x.firstQuartile += record.firstQuartile
                                x.midPoint += record.midPoint
                                x.thirdQuartile += record.thirdQuartile
                                x.complete += record.complete
                                x.close += record.close
                                x.pause += record.pause
                                x.resume += record.resume
                                x.fullScreen += record.fullScreen
                                x.mute += record.mute
                                x.unMute += record.unMute
                                x.skip += record.skip
                            }
                            case None => map.put(record, record)
                        }
                    }
                }
            }
        }
        map
    }

    def run(){
        startDate = new Date(2014 - 1900, 0, 9)  //9/1/2014
        updated = startDate.getTime
        // read all `stable cube` from cached-log
        env.fastCachedLogService.flush()


        env.fastCachedLogService.readAll(
            (index,record)=>{
                if (!maps.contains(index)) maps.put(index, new Cube[ReportRecord]())
                val cube = maps(index)
                addToCube(cube, record)

                val v = (index + 1) * 1000L * 3600
                if (v > updated) updated = v
            })
        currentCompressTime = env.compressService.getCurrentCompressTime(startDate.getTime)
        spawn { loop()}
        spawn { cronJobReportZingTV() }


        /*
        val xxx = System.currentTimeMillis()
        //renew fast cache log
        val cache = new mutable.HashMap[Int,mutable.HashMap[ReportRecord, ReportRecord]]()
        env.fastCachedLogService.readAll(
            (index,record)=>{
                if (!cache.contains(index)) cache.put(index, new mutable.HashMap[ReportRecord, ReportRecord]())
                val cube = cache(index)
                if (!cube.contains(record)) cube.put(record, record)
                else {
                    val r = cube(record)
                    r.impression += record.impression
                    r.click += record.click
                    r.spam += record.spam
                    r.conversion += record.conversion
                    r.conversionValue += record.conversionValue
                    r.conversionType += record.conversionType
                    r.render += record.render
                    r.pageView += record.pageView
                    r.collapse += record.collapse
                    r.expand += record.expand
                    r.creative += record.creative
                    r.firstQuartile += record.firstQuartile
                    r. midPoint += record. midPoint
                    r.thirdQuartile += record.thirdQuartile
                    r.complete += record.complete
                    r.close += record.close
                    r.pause += record.pause
                    r.resume += record.resume
                    r.fullScreen += record.fullScreen
                    r.mute += record.mute
                    r.unMute += record.unMute
                }
            })

        val path = Config.fastCachedLog.getValue + "_new.txt"
        val s = new PrintWriter(new BufferedWriter(new FileWriter(path, true)))
        val max = cache.keySet.max
        val min = cache.keySet.min
        for (time <- min to max; if cache.contains(time)){
            val cube = cache(time)
            for ((r,record) <- cube) {
                s.print(s"${time}," +
                s"${InfoKind.Order},${record.order}," +
                s"${InfoKind.Campaign},${record.camp}," +
                s"${InfoKind.Item},${record.item}," +
                s"${InfoKind.Website},${record.website}," +
                s"${InfoKind.ZoneGroup},${record.zoneGroup.foldLeft[String]("")(_ + _ + "|")}," +
                s"${InfoKind.Zone},${record.zone}," +
                s"${InfoKind.Impression},${record.impression}," +
                s"${InfoKind.Click},${record.click}," +
                s"${InfoKind.Spam},${record.spam}," +
                s"${InfoKind.Conversion},${record.conversion}," +
                s"${InfoKind.ConversionValue},${record.conversionValue}," +
                s"${InfoKind.ConversionType},${record.conversionType}," +
                s"${InfoKind.Render},${record.render}," +
                s"${InfoKind.PageView},${record.pageView}," +
                s"${InfoKind.Collapse},${record.collapse}," +
                s"${InfoKind.Expand},${record.expand}," +
                s"${InfoKind.Creative},${record.creative}," +
                s"${InfoKind.FirstQuartile},${record.firstQuartile}," +
                s"${InfoKind.MidPoint},${record.midPoint}," +
                s"${InfoKind.ThirdQuartile},${record.thirdQuartile}," +
                s"${InfoKind.Complete},${record.complete}," +
                s"${InfoKind.Close},${record.close}," +
                s"${InfoKind.Pause},${record.pause}," +
                s"${InfoKind.Resume},${record.resume}," +
                s"${InfoKind.Fullscreen},${record.fullScreen}," +
                s"${InfoKind.Mute},${record.mute}," +
                s"${InfoKind.Unmute},${record.unMute}\n"
                )
            }
        }
        s.close()

        println(s"Time renew fastcachelog: ${System.currentTimeMillis() - xxx}ms")
        */
    }

    def addToCube(cube: Cube[ReportRecord], record: ReportRecord){
        cube.add(record,
            Array(("order", record.order),
                ("camp", record.camp),
                ("item", record.item),
                ("website", record.website),
                ("zone", record.zone),
                ("convType", record.conversionType)),
            Array(("zoneGroup", record.zoneGroup)))
    }

    def loop(){
        var map = new mutable.HashMap[ReportRecord,ReportRecord]()
        while (true){
            val d = new Date(updated)
            val iterator = env.log.readAll(d, 3600*1000, currentChar)
            try{
                if ((System.currentTimeMillis() - updated)/3600/1000 == 0) {
                    //synchronize log file while read and write
                    env.log.lockFile({
                        map ++= processLog(map, iterator)
                    })
                } else map ++= processLog(map, iterator)

                val cube = new Cube[ReportRecord]()

                for ((record, _) <- map){
                    addToCube(cube, record)
                }

                val index = (updated/3600/1000).toInt
                maps.put(index, cube)

                val now = System.currentTimeMillis()
                if (now >= updated + 3610*1000){

                    // save current `cube` to disk
                    if (map.size > 0) {
                        for ((record,_) <- map)
                            env.fastCachedLogService.save(index, record)
                        env.fastCachedLogService.flush()
                        map.clear()
                    }
                    //delete log
                    spawn{
                        env.compressService.deleteLog(updated - 3600L*1000*25, updated - 3600L*1000*24)
                    }

                    updated += 3600*1000
                    currentChar = 0
                }

                if (now - updated < 3600*1000 && !env.compressService.isCurrentCompress && now >= currentCompressTime + 4800*1000) {
                    //compress log
                    spawn {
                        currentCompressTime = env.compressService.compress(currentCompressTime, now - 3600*1000)
                    }
                }

                val behind = (now - updated)/3600/1000
                if (behind == 0) Thread.sleep(30*1000)
            }
            catch {
                case e: Throwable => {
                    printException(e, System.out)
                    Thread.sleep(10 * 1000)
                }
            }
        }
    }

    def mapToMin(x: Long) = (x / 1000 / 60).toInt

    def mapToHour(x: Long) = (x / 1000 / 60 / 60).toInt

    def mapToDay(x: Long) = (mapTo(x, Resolution.Day) / 1000 / 3600 / 24).toInt + 1

    def getPercent(x: Double, y: Double): Double = {
        if (y == 0) return 0
        return x * 100d / y
    }

    def getChangeValue(x:Double,y:Double): Double = {
        if (y == 0) return 0
        return (x-y)/y*100
    }


    def mapTo(x: Long, res: Int): Long = res match {
        case Resolution.Hour => (x / 1000 / 3600 * 1000 * 3600)
        case Resolution.Day => {
            val cal = Calendar.getInstance()
            cal.setTimeInMillis(x)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.getTimeInMillis
        }
        case Resolution.Week => {
            val c = Calendar.getInstance
            c.setTimeInMillis(mapTo(x, Resolution.Day))
            c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            c.getTimeInMillis
        }
        case Resolution.Month => {
            val cal = Calendar.getInstance()
            cal.setTimeInMillis(mapTo(x, Resolution.Day))
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.getTimeInMillis
        }
    }

    def getFilterReport(filters: Map[String, String]): (String, ArrayBuffer[(String, Int)]) = {
        val arrayFilter = new ArrayBuffer[(String, Int)]()
        val itemKind = filters.get("report") match {
            case Some(x) =>
                if (x.startsWith("order")) "order"
                else if (x.startsWith("campaign")) "camp"
                else if (x.startsWith("item")) "item"
                else if (x.startsWith("website")) "website"
                else if (x.startsWith("zonegroup")) "zoneGroup"
                else if (x.startsWith("zone")) "zone"
                else if (x.startsWith("convt")) "convType"
                else if (x.startsWith("video")) "video"
                else ""
            case None => ""
        }

        filters.get("report") match {
            case Some(x) =>
                x match {
                    case "website_by_item" | "zone_by_item" | "convt_of_item_detail_conversion" | "website_of_item_delivery_conversion" | "zone_item_delivery_conversion" =>
                        arrayFilter += (("item", filters("itemId").toInt))
                    case "zonegroup_by_site" | "zone_by_site" | "campaign_by_site" | "item_by_site" | "subzone_by_site"  =>
                        arrayFilter += (("website", filters("websiteId").toInt))
                    case "item_by_campaign" | "zone_by_campaign" | "website_by_campaign" | "item_conversion" | "convt_of_campaign_detail_conversion" | "website_of_campaign_delivery_conversion" | "zone_of_campaign_delivery_conversion" =>
                        arrayFilter += (("camp", filters("campId").toInt))
                    case "zone_by_zonegroup" | "item_by_zonegroup" | "campaign_by_zonegroup" =>
                        arrayFilter += (("zoneGroup", filters("zoneGroupId").toInt))
                    case "campaign_by_order" | "item_by_order" | "zone_by_order" |  "website_by_order" | "campaign_conversion" | "convt_of_order_detail_conversion" | "website_of_order_delivery_conversion" | "zone_of_order_delivery_conversion" =>
                        arrayFilter += (("order", filters("orderId").toInt))
                    case "campaign_by_zone" | "item_by_zone" =>
                        arrayFilter += (("zone", filters("zoneId").toInt))
                    case "zone_of_site_by_campaign" => {
                        arrayFilter += (("website", filters("websiteId").toInt))
                        arrayFilter += (("camp", filters("campId").toInt))
                    }
                    case "zone_of_site_by_item" => {
                        arrayFilter += (("website", filters("websiteId").toInt))
                        arrayFilter += (("item", filters("itemId").toInt))
                    }
                    case "zone_of_site_by_order" => {
                        arrayFilter += (("website", filters("websiteId").toInt))
                        arrayFilter += (("order", filters("orderId").toInt))
                    }
                    case "item_of_campaign_by_site" => {
                        arrayFilter += (("website", filters("websiteId").toInt))
                        arrayFilter += (("camp", filters("campId").toInt))
                    }
                    case "item_of_campaign_by_zonegroup" => {
                        arrayFilter += (("zoneGroup", filters("zoneGroupId").toInt))
                        arrayFilter += (("camp", filters("campId").toInt))
                    }
                    case "item_of_campaign_by_zone" => {
                        arrayFilter += (("zone", filters("zoneId").toInt))
                        arrayFilter += (("camp", filters("campId").toInt))
                    }
                    case _=>
                }
            case None =>
        }
        (itemKind, arrayFilter)
    }

    def getName(kind: Option[String], id: Int): String = {
        var name = "unknown"
        kind match {
            case Some(x) =>
                if (x.startsWith("order")) {
                    val item = env.orderModelService.load(id)
                    if (item != null) name = item.name
                }
                else if (x.startsWith("campaign")) {
                    val item = env.campaignModelService.load(id)
                    if (item != null) name = item.name
                }
                else if (x.startsWith("item")) {
                    val item = env.bannerModelService.load(id)
                    if (item != null) name = item.name
                }
                else if (x.startsWith("website")) {
                    val item = env.websiteModelService.load(id)
                    if (item != null) name = item.name
                }
                else if (x.startsWith("zonegroup")) {
                    val item = env.zoneGroupModelService.load(id)
                    if (item != null) name = item.name
                }
                else if (x.startsWith("zone")) {
                    val item = env.zoneModelService.load(id)
                    if (item != null) name = item.name
                }
                else if (x.startsWith("convt")) {
                    val item = env.conversionTypeService.load(id)
                    if (item != null) name = item.name
                }
                else if (x.startsWith("subzone")) {
//                    val item = env.videoService.load(id)
//                    if (item != null) name = item.name
                }
            case None =>
        }
        name
    }

    def filterItemByPermission(items: mutable.HashMap[Int, Int], kind: Option[String], userName: String): ArrayBuffer[Int] = {
        val result = new ArrayBuffer[Int]
        if(Config.rootUserName.get.split(",").contains(userName)){
            for ((k,v) <- items) result += k
            return result
        }
        for ((k,v) <- items) {
            var isValid = true
            kind match {
                case Some(x) =>
                    if (x.startsWith("order") && env.orderModelService.load(k) == null) isValid = false
                    else if (x.startsWith("website") && env.websiteModelService.load(k) == null) isValid = false
                    else if (x.startsWith("zonegroup") && env.zoneGroupModelService.load(k) == null) isValid = false
                    else if (x.startsWith("zone") && env.zoneModelService.load(k) == null) isValid = false
                    else if (x.startsWith("campaign") && env.campaignModelService.load(k) == null) isValid = false
                    else if (x.startsWith("item") && env.bannerModelService.load(k) == null) isValid = false
                case None => return new ArrayBuffer[Int]
            }
            if (isValid) result += k
        }
        result
    }


    def getListConversionItem(items: ArrayBuffer[Int]) = {
        var result: mutable.HashMap[Int, ConversionItem] = new mutable.HashMap[Int, ConversionItem]()
        for (item <- items)
            if (item != 0)
                result += ((item, new ConversionItem(getName(Option("convt"), item), 0, 0)))
        result
    }

    def updateReportItem(reportItem: FastReportItem, item: ReportItemInfo, compareItem: ReportItemInfo): FastReportItem = {
        reportItem.properties("impression").value += item.impression
        reportItem.properties("impression").compareValue += compareItem.impression
        reportItem.properties("click").value += item.click
        reportItem.properties("click").compareValue += compareItem.click
        reportItem.properties("click.spam").value += item.spam
        reportItem.properties("click.spam").compareValue += compareItem.spam
        reportItem.properties("validclick").value += item.validClick
        reportItem.properties("validclick").compareValue += compareItem.validClick
        reportItem.properties("requestimpression").value += item.requestImpression
        reportItem.properties("requestimpression").compareValue += compareItem.requestImpression
        reportItem.properties("pageview").value += item.pageView
        reportItem.properties("pageview").compareValue += compareItem.pageView
        reportItem.properties("collapse").value += item.collapse
        reportItem.properties("collapse").compareValue += compareItem.collapse
        reportItem.properties("expand").value += item.expand
        reportItem.properties("expand").compareValue += compareItem.expand

        reportItem.properties("creative").value += item.creative
        reportItem.properties("creative").compareValue += compareItem.creative
        reportItem.properties("firstQuartile").value += item.firstQuartile
        reportItem.properties("firstQuartile").compareValue += compareItem.firstQuartile
        reportItem.properties("midPoint").value += item.midPoint
        reportItem.properties("midPoint").compareValue += compareItem.midPoint
        reportItem.properties("thirdQuartile").value += item.thirdQuartile
        reportItem.properties("thirdQuartile").compareValue += compareItem.thirdQuartile
        reportItem.properties("complete").value += item.complete
        reportItem.properties("complete").compareValue += compareItem.complete
        reportItem.properties("close").value += item.close
        reportItem.properties("close").compareValue += compareItem.close
        reportItem.properties("pause").value += item.pause
        reportItem.properties("pause").compareValue += compareItem.pause
        reportItem.properties("resume").value += item.resume
        reportItem.properties("resume").compareValue += compareItem.resume
        reportItem.properties("fullscreen").value += item.fullScreen
        reportItem.properties("fullscreen").compareValue += compareItem.fullScreen
        reportItem.properties("mute").value += item.mute
        reportItem.properties("mute").compareValue += compareItem.mute
        reportItem.properties("unmute").value += item.unMute
        reportItem.properties("unmute").compareValue += compareItem.unMute
        reportItem.properties("skip").value += item.skip
        reportItem.properties("skip").compareValue += compareItem.skip

        //conversion
        reportItem.properties("conversion").value += item.conversion
        reportItem.properties("conversion").compareValue += compareItem.conversion
        reportItem.properties("revenueconversion").value += item.conversionValue
        reportItem.properties("revenueconversion").compareValue += compareItem.conversionValue
        reportItem
    }

    def getReport(request: ReportRequest, userName: String): NewReportResponse = {
        //get kind and list filter for each report type
        val filter = getFilterReport(request.mapFilter)
        val itemKind = filter._1
        val arrayFilter = filter._2
        //calculate range and compare range
        var from = Math.max(mapToHour(request.range.from), mapToHour(startDate.getTime))
        var to = Math.min(mapToHour(request.range.to), mapToHour(System.currentTimeMillis()))
        if (to == 0) to = mapToHour(System.currentTimeMillis())
        if (request.range.from == 0 && request.range.to == 0 && request.mapFilter()("report").endsWith("_item")) {
            val b = env.bannerModelService.load(request.mapFilter()("itemId").toInt)
            if (b.isInstanceOf[INetwork]) {
                from = Math.max(mapToHour(b.asInstanceOf[INetwork].startDate), mapToHour(startDate.getTime))
                to = if (b.asInstanceOf[INetwork].endDate == 0) mapToHour(System.currentTimeMillis()) else mapToHour(b.asInstanceOf[INetwork].endDate)
            }
        }
        var compareFrom = 0
        val reportRespone: FastReportResponse = new FastReportResponse
        reportRespone.range = new Range(from*3600L*1000, to*3600L*1000)
        if (request.compareRange != null) {
            compareFrom = Math.max(mapToHour(request.compareRange.from), mapToHour(startDate.getTime))
            reportRespone.compareRange = new Range(compareFrom*3600L*1000, (compareFrom + to - from)*3600L*1000)
        }
        //get item of report
        var items = new mutable.HashMap[Int, Int]()
        if (request.mapFilter.contains("ids") && request.mapFilter.get("ids").get.length > 0) {
            val ids = request.mapFilter.get("ids").get.split(",")
            for (id <- ids) items += ((id.toInt, 0))
        } else {
            for (time <- from to to){
                val cube = if (maps.contains(time)) maps(time) else null
                if (cube != null) {
                    val itemFromCube =
                        if (arrayFilter.size > 0) cube.getValues(arrayFilter.toArray, itemKind)
                        else cube.getValues(itemKind)

                    for (i <- itemFromCube)
                        if (!items.contains(i)) items += ((i, i))
                }
            }
        }
        val validItems = filterItemByPermission(items, request.mapFilter.get("report"), userName)

        val convertDay: (Long) => Long =
            request.mapFilter()("resolution") match {
                case "hour" => (time: Long) => mapTo(time*3600L*1000, Resolution.Hour)
                case "week" => (time: Long) => mapTo(time*3600L*1000, Resolution.Week)
                case "month" => (time: Long) => mapTo(time*3600L*1000, Resolution.Month)
                case _ => (time: Long) => mapTo(time*3600L*1000, Resolution.Day)
            }

        for (time <- from to to){
            val cube = if (maps.contains(time)) maps(time) else null
            val compareTime = compareFrom + time - from
            val compareCube = if (compareFrom != 0 && maps.contains(compareTime)) maps(compareTime) else null
            if (cube != null) {
                //time of graph
                val date: Long = convertDay(time)
                if (request.mapFilter.get("report").get.endsWith("detail_conversion") && !reportRespone.conversion.contains(date))
                    reportRespone.conversion += ((date, getListConversionItem(validItems)))
                //init value of graph
                var totalImpressionValue = 0.0
                var totalClickValue = 0.0
                var totalConversionValue = 0.0
                var totalImpressionCompareValue = 0.0
                var totalClickCompareValue = 0.0
                var totalConversionCompareValue = 0.0
                for (itemId <- validItems) {
                    if (itemId != 0) {
                        val item = cube.fold[ReportItemInfo]((arrayFilter :+ ((itemKind, itemId))).toArray, new ReportItemInfo(itemId, time, itemKind, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L),
                            (s, r) => {
                                s.impression += r.impression
                                s.click += r.click + r.spam
                                s.conversion += r.conversion
                                s.conversionValue += r.conversionValue
                                s.spam += r.spam
                                s.validClick += r.click
                                s.requestImpression += r.render
                                s.pageView += r.pageView
                                s.collapse += r.collapse
                                s.expand += r.expand
                                s.creative += r.creative
                                s.firstQuartile += r.firstQuartile
                                s.midPoint += r.midPoint
                                s.thirdQuartile += r.thirdQuartile
                                s.complete += r.complete
                                s.close += r.close
                                s.pause += r.pause
                                s.resume += r.resume
                                s.fullScreen += r.fullScreen
                                s.mute += r.mute
                                s.unMute += r.unMute
                                s.skip += r.skip

                                totalImpressionValue += r.impression
                                totalClickValue += r.click + r.spam
                                totalConversionValue += r.conversion
                                s
                            })
                        val compareItem =
                            if (compareCube != null)
                                compareCube.fold[ReportItemInfo]((arrayFilter :+ ((itemKind, itemId))).toArray, new ReportItemInfo(itemId, compareTime, itemKind, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L),
                                    (s, r) => {
                                        s.impression += r.impression
                                        s.click += r.click + r.spam
                                        s.conversion += r.conversion
                                        s.conversionValue += r.conversionValue
                                        s.spam += r.spam
                                        s.validClick += r.click
                                        s.requestImpression += r.render
                                        s.pageView += r.pageView
                                        s.collapse += r.collapse
                                        s.expand += r.expand
                                        s.creative += r.creative
                                        s.firstQuartile += r.firstQuartile
                                        s.midPoint += r.midPoint
                                        s.thirdQuartile += r.thirdQuartile
                                        s.complete += r.complete
                                        s.close += r.close
                                        s.pause += r.pause
                                        s.resume += r.resume
                                        s.fullScreen += r.fullScreen
                                        s.mute += r.mute
                                        s.unMute += r.unMute
                                        s.skip += r.skip

                                        totalImpressionCompareValue += r.impression
                                        totalClickCompareValue += r.click + r.spam
                                        totalConversionCompareValue += r.conversion
                                        s
                                    })
                            else new ReportItemInfo(0, 0, "", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)

                        if (request.mapFilter.get("report").get.endsWith("detail_conversion")) {
                            reportRespone.conversion(date)(itemId).conversion += item.conversion
                            reportRespone.conversion(date)(itemId).value += item.conversionValue
                        }
                        //add or update list items of report
                        if (!reportRespone.items.contains(itemId)) {
                            val name = getName(request.mapFilter.get("report"), itemId)
                            //get order name if report for campaign
                            val extraFiled = new util.ArrayList[ExtraField]()
                            itemKind match {
                                case "camp" => {
                                    if (!request.mapFilter.get("report").getOrElse("").equals("campaign_by_order")) {
                                        val campObj = env.campaignModelService.load(itemId)
                                        if (campObj != null) extraFiled.add(new ExtraField("orderName", getName(Option("order"), campObj.orderId)))
                                    }
                                }
                                case "subzone" =>
                                case _ =>
                            }
                            reportRespone.items += (itemId -> new FastReportItem(name, itemId, extraFiled))
                        }
                        updateReportItem(reportRespone.items(itemId), item, compareItem)
                    }
                }


                //add or update graph point (total value)
                var stop = false
                for (i <- 0 until reportRespone.graph.points.size; if !stop) {
                    val point = reportRespone.graph.points.get(i)
                    if (point.date == date) { //update
                        point.impression += totalImpressionValue
                        point.click += totalClickValue
                        point.conversion += totalConversionValue

                        reportRespone.graph.comparePoints.get(i).impression += totalImpressionCompareValue
                        reportRespone.graph.comparePoints.get(i).click += totalClickCompareValue
                        reportRespone.graph.comparePoints.get(i).conversion += totalConversionCompareValue
                        stop = true
                    }
                }
                if(!stop){ //add new
                    reportRespone.graph.points.add(new FastGraphPoint(totalImpressionValue, totalClickValue, 0, totalConversionValue, date))
                    reportRespone.graph.comparePoints.add(new FastGraphPoint(totalImpressionCompareValue, totalClickCompareValue, 0, totalConversionCompareValue, date))
                }
            }
        }
        //calculate ctr point for graph
        for (i <- 0 until reportRespone.graph.points.size) {
            reportRespone.graph.points.get(i).ctr = getPercent(reportRespone.graph.points.get(i).click, reportRespone.graph.points.get(i).impression)
            reportRespone.graph.comparePoints.get(i).ctr = getPercent(reportRespone.graph.comparePoints.get(i).click, reportRespone.graph.comparePoints.get(i).impression)
        }

        for ((k,v) <- reportRespone.items) {
            //calculate value and compare value of ctr
            v.properties("ctr").value = getPercent(v.properties("click").value, v.properties("impression").value)
            v.properties("ctr").compareValue = getPercent(v.properties("click").compareValue, v.properties("impression").compareValue)
            //calculate change rate
            v.properties("impression").change = getChangeValue(v.properties("impression").value, v.properties("impression").compareValue)
            v.properties("click").change = getChangeValue(v.properties("click").value, v.properties("click").compareValue)
            v.properties("click.spam").change = getChangeValue(v.properties("click.spam").value, v.properties("click.spam").compareValue)
            v.properties("validclick").change = getChangeValue(v.properties("validclick").value, v.properties("validclick").compareValue)
            v.properties("ctr").change = getChangeValue(v.properties("ctr").value, v.properties("ctr").compareValue)
            v.properties("requestimpression").change = getChangeValue(v.properties("requestimpression").value, v.properties("requestimpression").compareValue)
            v.properties("pageview").change = getChangeValue(v.properties("pageview").value, v.properties("pageview").compareValue)
            v.properties("collapse").change = getChangeValue(v.properties("collapse").value, v.properties("collapse").compareValue)
            v.properties("expand").change = getChangeValue(v.properties("expand").value, v.properties("expand").compareValue)

            v.properties("creative").change = getChangeValue(v.properties("creative").value, v.properties("creative").compareValue)
            v.properties("firstQuartile").change = getChangeValue(v.properties("firstQuartile").value, v.properties("firstQuartile").compareValue)
            v.properties("midPoint").change = getChangeValue(v.properties("midPoint").value, v.properties("midPoint").compareValue)
            v.properties("thirdQuartile").change = getChangeValue(v.properties("thirdQuartile").value, v.properties("thirdQuartile").compareValue)
            v.properties("complete").change = getChangeValue(v.properties("complete").value, v.properties("complete").compareValue)
            v.properties("close").change = getChangeValue(v.properties("close").value, v.properties("close").compareValue)
            v.properties("pause").change = getChangeValue(v.properties("pause").value, v.properties("pause").compareValue)
            v.properties("resume").change = getChangeValue(v.properties("resume").value, v.properties("resume").compareValue)
            v.properties("fullscreen").change = getChangeValue(v.properties("fullscreen").value, v.properties("fullscreen").compareValue)
            v.properties("mute").change = getChangeValue(v.properties("mute").value, v.properties("mute").compareValue)
            v.properties("unmute").change = getChangeValue(v.properties("unmute").value, v.properties("unmute").compareValue)
            v.properties("skip").change = getChangeValue(v.properties("skip").value, v.properties("skip").compareValue)

            //conversion
            v.properties("conversionrate").value = getPercent(v.properties("conversion").value, v.properties("click").value)
            v.properties("conversionrate").compareValue = getPercent(v.properties("conversion").compareValue, v.properties("click").compareValue)
            v.properties("conversionrate").change = getChangeValue(v.properties("conversionrate").value, v.properties("conversionrate").compareValue)
            v.properties("conversion").change = getChangeValue(v.properties("conversion").value, v.properties("conversion").compareValue)
            v.properties("revenueconversion").change = getChangeValue(v.properties("revenueconversion").value, v.properties("revenueconversion").compareValue)
            //calculate value and change value for summary
            reportRespone.summary("impression").value += v.properties("impression").value
            reportRespone.summary("impression").compareValue += v.properties("impression").compareValue
            reportRespone.summary("click").value += v.properties("click").value
            reportRespone.summary("click").compareValue += v.properties("click").compareValue
            reportRespone.summary("click.spam").value += v.properties("click.spam").value
            reportRespone.summary("click.spam").compareValue += v.properties("click.spam").compareValue
            reportRespone.summary("validclick").value += v.properties("validclick").value
            reportRespone.summary("validclick").compareValue += v.properties("validclick").compareValue
            reportRespone.summary("spent").value += v.properties("spent").value
            reportRespone.summary("spent").compareValue += v.properties("spent").compareValue
            reportRespone.summary("conversion").value += v.properties("conversion").value
            reportRespone.summary("conversion").compareValue += v.properties("conversion").compareValue
            reportRespone.summary("revenueconversion").value += v.properties("revenueconversion").value
            reportRespone.summary("revenueconversion").compareValue += v.properties("revenueconversion").compareValue
            reportRespone.summary("requestimpression").value += v.properties("requestimpression").value
            reportRespone.summary("requestimpression").compareValue += v.properties("requestimpression").compareValue
            reportRespone.summary("pageview").value += v.properties("pageview").value
            reportRespone.summary("pageview").compareValue += v.properties("pageview").compareValue
            reportRespone.summary("collapse").value += v.properties("collapse").value
            reportRespone.summary("collapse").compareValue += v.properties("collapse").compareValue
            reportRespone.summary("expand").value += v.properties("expand").value
            reportRespone.summary("expand").compareValue += v.properties("expand").compareValue

            reportRespone.summary("creative").value += v.properties("creative").value
            reportRespone.summary("creative").compareValue += v.properties("creative").compareValue
            reportRespone.summary("firstQuartile").value += v.properties("firstQuartile").value
            reportRespone.summary("firstQuartile").compareValue += v.properties("firstQuartile").compareValue
            reportRespone.summary("midPoint").value += v.properties("midPoint").value
            reportRespone.summary("midPoint").compareValue += v.properties("midPoint").compareValue
            reportRespone.summary("thirdQuartile").value += v.properties("thirdQuartile").value
            reportRespone.summary("thirdQuartile").compareValue += v.properties("thirdQuartile").compareValue
            reportRespone.summary("complete").value += v.properties("complete").value
            reportRespone.summary("complete").compareValue += v.properties("complete").compareValue
            reportRespone.summary("close").value += v.properties("close").value
            reportRespone.summary("close").compareValue += v.properties("close").compareValue
            reportRespone.summary("pause").value += v.properties("pause").value
            reportRespone.summary("pause").compareValue += v.properties("pause").compareValue
            reportRespone.summary("resume").value += v.properties("resume").value
            reportRespone.summary("resume").compareValue += v.properties("resume").compareValue
            reportRespone.summary("fullscreen").value += v.properties("fullscreen").value
            reportRespone.summary("fullscreen").compareValue += v.properties("fullscreen").compareValue
            reportRespone.summary("mute").value += v.properties("mute").value
            reportRespone.summary("mute").compareValue += v.properties("mute").compareValue
            reportRespone.summary("unmute").value += v.properties("unmute").value
            reportRespone.summary("unmute").compareValue += v.properties("unmute").compareValue
            reportRespone.summary("skip").value += v.properties("skip").value
            reportRespone.summary("skip").compareValue += v.properties("skip").compareValue

        }
        //calculate change rate for summary
        reportRespone.summary("impression").change = getChangeValue(reportRespone.summary("impression").value, reportRespone.summary("impression").compareValue)
        reportRespone.summary("click").change = getChangeValue(reportRespone.summary("click").value, reportRespone.summary("click").compareValue)
        reportRespone.summary("click.spam").change = getChangeValue(reportRespone.summary("click.spam").value, reportRespone.summary("click.spam").compareValue)
        reportRespone.summary("validclick").change = getChangeValue(reportRespone.summary("validclick").value, reportRespone.summary("validclick").compareValue)
        reportRespone.summary("spent").change = getChangeValue(reportRespone.summary("spent").value, reportRespone.summary("spent").compareValue)
        reportRespone.summary("conversion").change = getChangeValue(reportRespone.summary("conversion").value, reportRespone.summary("conversion").compareValue)
        reportRespone.summary("pageview").change = getChangeValue(reportRespone.summary("pageview").value, reportRespone.summary("pageview").compareValue)
        reportRespone.summary("revenueconversion").change = getChangeValue(reportRespone.summary("revenueconversion").value, reportRespone.summary("revenueconversion").compareValue)
        reportRespone.summary("requestimpression").change = getChangeValue(reportRespone.summary("requestimpression").value, reportRespone.summary("requestimpression").compareValue)
        reportRespone.summary("collapse").change = getChangeValue(reportRespone.summary("collapse").value, reportRespone.summary("collapse").compareValue)
        reportRespone.summary("expand").change = getChangeValue(reportRespone.summary("expand").value, reportRespone.summary("expand").compareValue)

        reportRespone.summary("creative").change = getChangeValue(reportRespone.summary("creative").value, reportRespone.summary("creative").compareValue)
        reportRespone.summary("firstQuartile").change = getChangeValue(reportRespone.summary("firstQuartile").value, reportRespone.summary("firstQuartile").compareValue)
        reportRespone.summary("midPoint").change = getChangeValue(reportRespone.summary("midPoint").value, reportRespone.summary("midPoint").compareValue)
        reportRespone.summary("thirdQuartile").change = getChangeValue(reportRespone.summary("thirdQuartile").value, reportRespone.summary("thirdQuartile").compareValue)
        reportRespone.summary("complete").change = getChangeValue(reportRespone.summary("complete").value, reportRespone.summary("complete").compareValue)
        reportRespone.summary("close").change = getChangeValue(reportRespone.summary("close").value, reportRespone.summary("close").compareValue)
        reportRespone.summary("pause").change = getChangeValue(reportRespone.summary("pause").value, reportRespone.summary("pause").compareValue)
        reportRespone.summary("resume").change = getChangeValue(reportRespone.summary("resume").value, reportRespone.summary("resume").compareValue)
        reportRespone.summary("fullscreen").change = getChangeValue(reportRespone.summary("fullscreen").value, reportRespone.summary("fullscreen").compareValue)
        reportRespone.summary("mute").change = getChangeValue(reportRespone.summary("mute").value, reportRespone.summary("mute").compareValue)
        reportRespone.summary("unmute").change = getChangeValue(reportRespone.summary("unmute").value, reportRespone.summary("unmute").compareValue)
        reportRespone.summary("skip").change = getChangeValue(reportRespone.summary("skip").value, reportRespone.summary("skip").compareValue)

        reportRespone.summary("ctr").value = getPercent(reportRespone.summary("click").value, reportRespone.summary("impression").value)
        reportRespone.summary("ctr").compareValue = getPercent(reportRespone.summary("click").compareValue, reportRespone.summary("impression").compareValue)
        reportRespone.summary("ctr").change = getChangeValue(reportRespone.summary("ctr").value, reportRespone.summary("ctr").compareValue)
        reportRespone.summary("conversionrate").value = getPercent(reportRespone.summary("conversion").value, reportRespone.summary("click").value)
        reportRespone.summary("conversionrate").compareValue = getPercent(reportRespone.summary("conversion").compareValue, reportRespone.summary("click").compareValue)
        reportRespone.summary("conversionrate").change = getChangeValue(reportRespone.summary("conversionrate").value, reportRespone.summary("conversionrate").compareValue)
        reportRespone.convertToReportResponse
    }

    def getCRMReport(request : CRMReportRequest): Object = {
        val from = Math.max(mapToHour(request.from), mapToHour(startDate.getTime))
        var to = Math.min(mapToHour(request.to), mapToHour(System.currentTimeMillis()))
        if (to == 0) to = mapToHour(System.currentTimeMillis())

        var itemKind = ""
        var filters: ArrayBuffer[(String, Int)] = new ArrayBuffer[(String, Int)]

        request.filter match {
            case "itemzone" => {
                itemKind = "item"
                filters += (("zone", request.zone))
            }
            case "videobanner" => {
                itemKind = "item"
                filters += (("video", request.zone))
                var listItem = Set[Int]()
                for (time <- from to to) {
                    val cube = if (maps.contains(time)) maps(time) else null
                    if (cube != null) {
                        val itemFromCube =
                            cube.getValues(filters.toArray, itemKind)
                        for (i <- itemFromCube)
                            if (!listItem.contains(i)) listItem += i
                        request.items = listItem.toArray
                    }
                }
            }
            case "videobybanner" => {
                itemKind = "video"
                filters += (("item", request.zone))
                var listItem = Set[Int]()
                for (time <- from to to) {
                    val cube = if (maps.contains(time)) maps(time) else null
                    if (cube != null) {
                        val itemFromCube =
                            cube.getValues(filters.toArray, itemKind)
                        for (i <- itemFromCube)
                            if (!listItem.contains(i)) listItem += i
                        request.items = listItem.toArray
                    }
                }
            }
            case _ => itemKind = request.filter
        }
        val result : util.HashMap[Int, util.HashMap[Long, CRMItemResponse]] =
            new util.HashMap[Int, util.HashMap[Long, CRMItemResponse]]()
        if (request.filter == "videobybanner") {
            val videos = new ZingTVReportCRM()
            val listAdType = Array(AdType.Preroll, AdType.Midroll, AdType.Postroll)
            for (time <- from to to){
                val cube = if (maps.contains(time)) maps(time) else null
                if (cube != null) {
                    for(itemId <- request.items) {
                        if (!videos.videos.contains(itemId)) {
                            val listAd = new util.HashMap[Int, SimpleCRMItemResponse]
                            listAd.put(AdType.Preroll, new SimpleCRMItemResponse(0, 0))
                            listAd.put(AdType.Midroll, new SimpleCRMItemResponse(0, 0))
                            listAd.put(AdType.Postroll, new SimpleCRMItemResponse(0, 0))
                            videos.videos += ((itemId, listAd))
                        }
                        val itemReport = videos.videos(itemId)
                        for (ad <- listAdType) {
                            val item = cube.fold[SimpleCRMItemResponse]((filters :+ ("adType", ad) :+ ("video", itemId)).toArray, new SimpleCRMItemResponse(),
                            (s, r) => {
                                s.i += r.impression
                                s.c += r.click + r.spam
                                s
                            })
                            itemReport.get(ad).i += item.i
                            itemReport.get(ad).c += item.c
                        }
                        videos.videos += ((itemId, itemReport))
                    }
                }
            }
            return videos
        } else {
            for (time <- from to to){
                val date: Long = mapTo(time*3600L*1000, Resolution.Day)
                val cube = if (maps.contains(time)) maps(time) else null
                if (cube != null) {
                    for(itemId <- request.items) {
                        val item = cube.fold[CRMItemResponse]((filters :+ ((itemKind, itemId))).toArray, new CRMItemResponse(0, 0),
                            (s, r) => {
                                s.impressions += r.impression
                                s.clicks += r.click + r.spam
                                s
                            })
                        //add or update list items of report
                        if (!result.containsKey(itemId)) {
                            result.put(itemId, new util.HashMap[Long, CRMItemResponse]())
                        }
                        val dates = result.get(itemId)
                        var stop = false
                        for (i <- dates.keySet.toArray) {
                            if (date == i.asInstanceOf[Long]) {
                                dates.get(i).impressions += item.impressions
                                dates.get(i).clicks += item.clicks
                                stop = true
                            }
                        }
                        //add new
                        if(!stop) dates.put(date, new CRMItemResponse(item.clicks, item.impressions))
                    }
                }
            }
        }
        return result
    }

    //return (Impression, Click)
    def getImpressionClick(id: Int, from: Long, to: Long, kind: String): (Long, Long) = {
        val f: Int = Math.max(mapToHour(from), mapToHour(startDate.getTime))
        var t: Int = Math.min(mapToHour(to), mapToHour(System.currentTimeMillis()))
        if (t == 0) t = mapToHour(System.currentTimeMillis())
        var impression = 0L
        var click = 0L
        for (time <- f to t) {
            val cube = if (maps.contains(time)) maps(time) else null
            if (cube != null) {
                cube.fold[Long](Array((kind, id)), 0L,
                    (s, r) => {
                        impression += r.impression
                        click += r.click + r.spam
                        0
                    })
            }
        }
        (impression, click)
    }
    
    class InfoVideo(val media_id: Int, val media_name: String, val program_id: Int, val program_name: String)
    class InfoVideoResponse(val response: Map[String, InfoVideo])

    val zingtvApiKey = "5d58caaea032799b14dbaeffd042ce8f"

    def getVideoInfo(videoIds: Array[Int]): InfoVideoResponse = {
        try {
            val client = new DefaultHttpClient
            val post = new HttpPost(s"http://api.tv.zing.vn/2.0/share/adtima/media/infos?api_key=$zingtvApiKey&media_id_arr=${videoIds.mkString(",")}")
            val response = client.execute(post)
            val source = scala.io.Source.fromInputStream(response.getEntity().getContent()).getLines().mkString
            val videos = WebUtils.fromJson(classOf[InfoVideoResponse], source)
            if (videos != null && videos.response != null) {
                return videos
            }
        } catch {
            case ex:Throwable => printException(ex, System.out)
        }
        return null
    }

    class TimerTaskReportZingTV extends TimerTask {
        def run {
            val now = System.currentTimeMillis()
            if (new Date().getHours < 12) //before 12h run all report of previous day
                cacheReportZingTV(mapToDay(now) - 1, 24)
            else //after 12h run from 0h to 12h of today
                cacheReportZingTV(mapToDay(now), 12)
        }
    }

    def cronJobReportZingTV() {
        val startDateReportZingTV = new Date(2014 - 1900, 4, 7)  //8/5/2014
        val current =  mapToDay(startDateReportZingTV.getTime)
        val now = mapToDay(System.currentTimeMillis())
        var startTime = startDateReportZingTV
        for (i <- current + 1 until now) {
            val f = new File(getPathCacheReportZingTVFile(new Date(mapTo(i*3600L*1000*24, Resolution.Day))))
            if (!f.exists()) cacheReportZingTV(i, 24)
        }
        startTime = new Date(mapTo(System.currentTimeMillis(), Resolution.Day) + 25*3600L*1000) //run at 1h tomorow
        val timerTask: TimerTask = new TimerTaskReportZingTV
        val timer: Timer = new Timer(true)
        timer.scheduleAtFixedRate(timerTask, startTime, 12*3600L*1000)
        println(s"CronJob report zingtv will star at: $startTime")
    }

    def getCacheReportZingTVFile(time: Date): String = f"CacheReportZingTV_${time.getYear + 1900}_${time.getMonth + 1}%02d_${time.getDate}%02d.txt"
    def getCacheCRMReportZingTVFile(time: Date): String = f"CacheCRMReportZingTV_${time.getYear + 1900}_${time.getMonth + 1}%02d_${time.getDate}%02d.txt"
    def getPathCacheReportZingTVFile(time: Date): String = Config.zingTVReportLog.getValue + "/" + getCacheReportZingTVFile(time) + ".zip"
    def getPathCacheCRMReportZingTVFile(time: Date): String = Config.zingTVCRMReportLog.getValue + "/" + getCacheCRMReportZingTVFile(time) + ".zip"

    def cacheReportZingTV(date: Date) = {
        cacheReportZingTV(mapToDay(date.getTime), 24)
    }

    def getZingTVReport(response: HttpServletResponse, range: Range) {
        val path = getPathCacheReportZingTVFile(new Date(range.from))
        WebUtils.downloadFile(response, path)
    }

    def getZingTVCRMReport(response: HttpServletResponse, date: Long) {
        val path = getPathCacheCRMReportZingTVFile(new Date(date))
        WebUtils.downloadFile(response, path)
    }

    def cacheReportZingTV(time: Int, duration: Int) {
        val startTime = System.currentTimeMillis()
        val cacheDate = new Date(mapTo(time*3600L*1000*24, Resolution.Day))
        try {
            println(s"Start cache report zingTV from ${cacheDate}, duration: $duration")
            val from = Math.max(mapToHour(mapTo(time*3600L*1000*24, Resolution.Day)), mapToHour(startDate.getTime))
            val to = from + duration - 1
            val reportZingTV: ZingTVReportResponse = new ZingTVReportResponse(new Range(from*3600L*1000, to*3600*1000L))
            val reportCRMZingTV = new FullZingTVReportCRM()
            for (t <- 0 until duration) {
                val iterator = env.log.readAll(cacheDate + t*3600L*1000, duration.hour)
                using(iterator) {
                    for (record <- iterator; if record != null; if record.videoId > 0; if (record.kind == TrackKind.Impression || record.kind == TrackKind.Click || record.kind == TrackKind.Spam || record.kind == TrackKind.Complete)) {
                        val banner = env.bannerCache.get(record.itemId)
                        if (banner != null && banner.isInstanceOf[INetwork]) {
                            if (!reportZingTV.videos.contains(record.videoId)) reportZingTV.videos += ((record.videoId, new ReportZingTVItem))
                            val ad = reportZingTV.videos(record.videoId).a
                            val bannerNetWork = banner.asInstanceOf[INetwork]
                            val adTypeId = banner.kind match {
                                case BannerKind.NetworkOverlayBanner => AdType.Overlay
                                case BannerKind.NetworkPauseAd => AdType.Pause
                                case BannerKind.NetworkMedia => AdType.BranderSkin
                                case BannerKind.NetworkTVC | BannerKind.NetworkTVCBanner => record.adType
                                case _ => 0
                            }
                            if (!ad.containsKey(adTypeId)) ad.put(adTypeId, new util.HashMap[Int, ReportZingTVItemInfo]())
                            val payType = ad.get(adTypeId)
                            val payTypeId = bannerNetWork.rateUnit match {
                                case RateUnit.CPC => PayType.CPC
                                case RateUnit.CPM => PayType.CPM
                                case RateUnit.FULL_VIDEO => PayType.Completed
                                case _ => 0
                            }
                            if (!payType.containsKey(payTypeId)) payType.put(payTypeId, new ReportZingTVItemInfo())
                            record.kind match {
                                case TrackKind.Impression => {
                                    payType.get(payTypeId).i += 1
                                    if (bannerNetWork.rateUnit == RateUnit.CPM) payType.get(payTypeId).r += bannerNetWork.rate / 1000.0
                                }
                                case TrackKind.Click | TrackKind.Spam => {
                                    payType.get(payTypeId).c += 1
                                    if (bannerNetWork.rateUnit == RateUnit.CPC) payType.get(payTypeId).r += bannerNetWork.rate
                                }
                                case TrackKind.Complete => {
                                    payType.get(payTypeId).cp += 1
                                    if (bannerNetWork.rateUnit == RateUnit.FULL_VIDEO) payType.get(payTypeId).r += bannerNetWork.rate
                                }
                                case _ =>
                            }
                            //CRM
                            if (!reportCRMZingTV.banners.contains(record.itemId)) reportCRMZingTV.banners += ((record.itemId, new ZingTVReportCRM()))
                            val reportBanner = reportCRMZingTV.banners(record.itemId)
                            if (!reportBanner.videos.contains(record.videoId)) reportBanner.videos += ((record.videoId, new util.HashMap[Int, SimpleCRMItemResponse]))
                            val reportVideo = reportBanner.videos(record.videoId)
                            if (!reportVideo.containsKey(adTypeId)) reportVideo.put(adTypeId, new SimpleCRMItemResponse())
                            record.kind match {
                                case TrackKind.Impression => reportVideo.get(adTypeId).i += 1
                                case TrackKind.Click | TrackKind.Spam => reportVideo.get(adTypeId).c += 1
                                case TrackKind.Complete => reportVideo.get(adTypeId).cp += 1
                                case _ =>
                            }
                        }
                    }
                }
            }
            val take = 200
            val keys = reportZingTV.videos.keys
            for (i <- 0 to keys.size / 200) {
                val list = keys.drop(i*take).take(take)
                val videoInfo = getVideoInfo(list.toArray)
                if (videoInfo != null) {
                    for (vid <- list; if (videoInfo.response.contains(vid.toString))) {
                        val reportVideo = reportZingTV.videos(vid)
                        val video = videoInfo.response(vid.toString)
                        reportVideo.t =  video.media_name
                        reportVideo.pi = video.program_id
                        reportVideo.pn = video.program_name
                    }
                }
            }
            new File(Config.zingTVReportLog.getValue).mkdirs
            new File(Config.zingTVCRMReportLog.getValue).mkdirs
            //compress
            Compress.compressStringToFile(WebUtils.toJson(reportZingTV), Config.zingTVReportLog.getValue, getCacheReportZingTVFile(cacheDate))
            Compress.compressStringToFile(WebUtils.toJson(reportCRMZingTV), Config.zingTVCRMReportLog.getValue, getCacheCRMReportZingTVFile(cacheDate))
        }catch {
            case ex: Throwable => {
                println(s"Error while cache report zingTV from ${new Date(mapTo(time*3600L*1000*24, Resolution.Day))}, duration: $duration")
                printException(ex, System.out)
            }
        }
        println(s"Finish cache report ZingTV from ${cacheDate}, duration: $duration, time: ${System.currentTimeMillis() - startTime}ms")
    }
}


