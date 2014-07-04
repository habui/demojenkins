package ads.website.modules.serving

import ads.common.services.report._
import java.util.concurrent.ConcurrentHashMap
import java.util
import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer
//import TimeSeries._
import ads.common.Syntaxs._
import ads.common.database.IDataService
import ads.common.services.{IZoneToBannerService, CachedLog}
import scala.collection.JavaConversions._
import scala._
import scala.concurrent.ops._
import java.util.{TimeZone, GregorianCalendar, Calendar, Date}
import ads.common.model._
import ads.website.Environment
import scala.util.control.Breaks._
import java.security.Timestamp
import com.netflix.config.DynamicPropertyFactory
import com.google.common.collect.Lists
import ads.common.services.report.Range
import scala.Some
import java.io.{PrintWriter, BufferedWriter, FileWriter}
import scala.io.Source
import ads.common.services.serving.CachedLogService
import ads.website.modules.INewBookingService
import ads.web.WebUtils

object Resolution {
    val Hour = 1
    val Day = 2
    val Week = 3
    val Month = 4
}
/*
object TimeSeries {
    def mapToMin(x: Long) = (x / 1000 / 60).toInt

    def mapToHour(x: Long) = (x / 1000 / 60 / 60).toInt

    def mapToDay(x: Long) = (x / 1000 / 3600 / 24).toInt

    def mapTo(x: Long, res: Int) = res match {
        case Resolution.Hour => (x / 1000 / 3600).toInt
        case Resolution.Day => ((x+25200000) /1000 / 3600 / 24 ).toInt
        case Resolution.Week => {
            val c = Calendar.getInstance()
            c.setTimeInMillis(x)
            c.get(Calendar.WEEK_OF_YEAR) + c.get(Calendar.YEAR) * 100
        }
        case Resolution.Month => {
            val c = Calendar.getInstance()
            c.setTimeInMillis(x)
            c.get(Calendar.MONTH) + c.get(Calendar.YEAR) * 100
        }
    }

    def merge(sum: (Double, Double) => Double)(x: util.HashMap[String, Double], y: util.HashMap[String, Double]): util.HashMap[String, Double] = {
        val r = new util.HashMap[String, Double]()
        for (xi <- x.entrySet()) {
            val vX = xi.getValue

            if (y.containsKey(xi.getKey)) {
                r.put(xi.getKey, sum(vX, y.get(xi.getKey)))
            }
            else {
                r.put(xi.getKey, vX)
            }
        }
        r
    }

    def mergeSum = merge((x, y) => x + y) _
}

trait ITimeSeriesProcessor[T] {
    def process(source: Iterator[T]): util.HashMap[Int, util.HashMap[String, Double]]
}

trait LogProcessor extends ITimeSeriesProcessor[LogRecord]

class SumProcessor(keyExtractor: LogRecord => String, valueExtractor: LogRecord => Double) extends LogProcessor {

    def process(source: Iterator[LogRecord]): util.HashMap[Int, util.HashMap[String, Double]] = {
        var timeIndex = 0
        val result = new util.HashMap[Int, util.HashMap[String, Double]]()
        var map = new util.HashMap[String, Double]()

        for (r <- source) {
            val time = mapToMin(r.time)

            if (time != timeIndex) {
                if (timeIndex != 0) {
                    result.put(timeIndex, map)
                    map = new util.HashMap[String, Double]()
                    timeIndex = time
                }
            }

            val key = keyExtractor(r)
            val value = valueExtractor(r)

            if (!map.containsKey(key)) {
                map.put(key, value)
            }
            else {
                map.put(key, map.get(key) + value)
            }
        }
        result
    }
}

class CountProcessor(extractor: LogRecord => String) extends LogProcessor {

    def process(source: Iterator[LogRecord]): util.HashMap[Int, util.HashMap[String, Double]] = {
        var timeIndex = 0
        val result = new util.HashMap[Int, util.HashMap[String, Double]]()
        var map = new util.HashMap[String, Double]()

        for (r <- source) {
            val time = mapToMin(r.time)

            if (time != timeIndex) {
                if (timeIndex != 0) {
                    result.put(timeIndex, map)
                    map = new util.HashMap[String, Double]()
                    timeIndex = time
                }
            }

            val key = extractor(r)
            if (!map.containsKey(key)) {
                map.put(key, 1)
            }
            else {
                map.put(key, map.get(key) + 1)
            }
        }
        result
    }
}

class ReportEngine {
    val _map = new ConcurrentHashMap[String, ConcurrentHashMap[Int, util.HashMap[String, Double]]]()

    def count(from: Long, to: Long, key: String, value: String) = {
        val f = mapToHour(from)
        val t = mapToHour(to)

        val map = _map.get("count")
        val dataKey = key + "@" + value
        var result = 0L

        for (i <- f to t) {
            val data = map.get(i)
            val value = data.containsKey(dataKey) ? data.get(dataKey) | 0
            result += value.toLong
        }

        result
    }
}

class ReportService(env: {
    val log: ILog
    val cachedLogService: CachedLogService
    val campaignService: IDataService[Campaign]
    val orderService: IDataService[Order]
    val websiteService: IDataService[Website]
    val zoneService: IDataService[Zone]
    val newBookingService: INewBookingService
    val zoneToZoneGroupService: IDataService[ZoneToZoneGroup]
    val zoneGroupService: IDataService[ZoneGroup]
    val bannerService: IDataService[Banner]}) extends IReportService {
    lazy val cached = new ConcurrentHashMap[Int, util.HashMap[String, Double]]()
    var current = new util.HashMap[String, Double]
    var updated = 0L
    var currentTime = 0L
    val factory = DynamicPropertyFactory.getInstance()
    val reportFieldConversion = factory.getStringProperty("report.field_conversion", "impression,click,ctr,spent,revenueconversion,conversion,costconversion,conversionrate,roi").getValue
    val reportFieldOrder = factory.getStringProperty("report.field_order", "impression,click,click.spam,ctr,spent,creative,firstQuartile,midPoint,thirdQuartile,complete,close,pause,resume,fullscreen,mute,unmute").getValue
    val reportFieldWebsite = factory.getStringProperty("report.field_website", "impression,click,click.spam,ctr,revenue,creative,firstQuartile,midPoint,thirdQuartile,complete,close,pause,resume,fullscreen,mute,unmute").getValue
    def count(from: Long, to: Long, filter: LogRecord => Boolean): Long = {
        val f = mapToMin(from)
        val t = mapToMin(to)

        for (time <- f to t) {
        }

        0
    }

    def count(p: Int, filter: LogRecord => Boolean): Long = 0

    def buildSummary(map: util.HashMap[String, Double], compareMap: util.HashMap[String, Double]): Summary = {
        val properties = new util.ArrayList[ReportItemProperty]()
        for (entry <- map.entrySet()) {
            val value = entry.getValue
            val compareValue = compareMap(entry.getKey)
            val property = new ReportItemProperty(entry.getKey, value, compareValue, getPercent(value, compareValue) - 100)
            properties.add(property)
        }
        new Summary(properties)
    }
    def getReport(request: ReportRequest): ReportResponse = {
        val report = request.filters.find(f => f.key.equalsIgnoreCase("report")).get
        if(report == null) getReportOrderCampaign(request)
        return report.value match {
            case "orders" => getReportOrders(request)
            case "websites" => getReportWebsites(request) // list all website
            case "website_by_item" => getReportWebsite(request) // list website by item id
            case "zonegroup_by_site" => getReportWebsiteZoneGroup(request)
            case "zone_by_site" => getReportWebsiteZone(request)
            case "zone_of_site_by_campaign" => getReportWebsiteZonebyCampaign(request)
            case "zone_of_site_by_item" => getReportWebsiteZoneByItem(request)
            case "zone_of_site_by_order" => getReportWebsiteZoneByOrder(request)
            case "item_by_site" => getReportWebsiteItem(request)
            case "item_by_campaign" => getReportItem(request)
            case "item_of_campaign_by_site" => getReportItemCampaignBySite(request)
            case "item_of_campaign_by_zonegroup" => getReportCampaignItemByZoneGroup(request)
            case "item_of_campaign_by_zone" => getReportCampaignItemByZone(request)
            case "zone_by_campaign" => getReportCampaignZone(request)
            case "zone_by_zonegroup" => getReportZoneGroupZone(request)
            case "item_by_zonegroup" => getReportZoneGroupItem(request)
            case "website_by_campaign" => getReportCampaignWebsite(request)
            case "campaign_by_order" => getReportOrderCampaign(request)
            case "campaign_by_site" => getReportWebsiteCampaign(request)
            case "campaign_by_zone" => getReportZoneCampaign(request)
            case "campaign_by_zonegroup" => getReportZoneGroupCampaign(request)
            case "item_by_zone" => getReportZoneItem(request)
            case "item_by_order" => getReportOrderItem(request)
            case "zone_by_order" => getReportOrderZone(request)
            case "zone_by_item" => getReportItemZone(request)
            case "website_by_order" => getReportOrderWebsite(request)
            case "orders_conversion" => getReportConversionOrders(request)
            case "campaigns_conversion" => getReportConversionOrderCampaign(request)
            case "items_conversion" => getReportConversionItemByCampaing(request)
            case _=> getReportOrderCampaign(request)
        }
    }

    def getCRMReport(request : CRMReportRequest) : CRMReportResponse = {
        val result : util.HashMap[String, util.HashMap[String, CRMItemResponse]] =
            new util.HashMap[String, util.HashMap[String, CRMItemResponse]]()

        for( itemId <- request.items){
            val clickFilter = "click.item." + itemId
            val impFilter = "impression.item." + itemId
            val range = new Range(request.from, request.to)
            val clickReport : (Long, util.ArrayList[GraphPoint]) = getReport(range,clickFilter,Resolution.Day)
            val impReport : (Long, util.ArrayList[GraphPoint]) = getReport(range, impFilter, Resolution.Day)
            val dates :  util.HashMap[String,CRMItemResponse] = new util.HashMap[String, CRMItemResponse]()
            for(i <- 0 until clickReport._2.size()) {
                val click = clickReport._2.get(i)
                val imp = impReport._2.get(i)
                dates.put(click.date.toString, new CRMItemResponse(click.value, imp.value))
            }
                result.put(itemId.toString,dates)
        }
        new CRMReportResponse(result)
    }

    def getCampaingFillterType(filter:String):String = {
        val campaignType = filter match {
            case "booking" => "booking"
            case "networkads" => "network"
            case "networktvc" => "networkTVC"
        }
        campaignType
    }

    def getReportConversionOrderCampaign(request: ReportRequest): ReportResponse = {
        val orderId = request.filters.find(f => f.key.equalsIgnoreCase("orderId")).get.value.toInt
        val filter = request.filters.find(f => f.key.equalsIgnoreCase("filter")).get.value
        var camps = env.campaignService.listByReferenceId(id = orderId).data
        if(filter != "all" && filter != null) {
            val campaignType = getCampaingFillterType(filter)
            camps = camps.filter(x=>x.campaignType == campaignType);
        }
        val kind = getKind(request)
        var res = getResolution(request)
        //val items = camps.flatMap(c => env.bannerDataService.listByReferenceId(id = c.id).data).toList

        val reportProperties = WebUtils.parseStringToArray(reportFieldConversion) //Array("impression", "click", "ctr", "spent")

        val list = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        val compareList = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        var reportItems = new util.ArrayList[ReportItem]()
        val summaryBag = new util.HashMap[String, Double]()
        val compareSummaryBag = new util.HashMap[String, Double]()
        for (p <- reportProperties) {
            summaryBag(p) = 0
            compareSummaryBag(p) = 0
        }
        // items list

        if(request.range.from ==0 && request.range.to == 0) {
            val ret = getAllTimeReport(camps,request.range,res)
            request.range.from = ret._2
            request.range.to = ret._3
            res = ret._1
        }
        for (campaign <- camps) {
            val ret = getReportProperties(reportProperties,request,res,campaign,0,0,null,getValueZoneItem)
            for (p <- reportProperties) {
                summaryBag(p) = summaryBag(p) + ret._2(p)
                compareSummaryBag(p) = ret._3(p)

            }
            new ReportItem(name = campaign.name,id = campaign.id, ret._1) |> reportItems.add
        }
        getConversionReport(reportItems)
        summaryBag("ctr") = getPercent(summaryBag("click"), summaryBag("impression"))
        var count = 0L;
        var graph = new util.ArrayList[GraphPoint]()
        var compareGraph = new util.ArrayList[GraphPoint]()
        // graph
        if(kind != "ctr") {
            for (campaign <- camps) {
                val items = env.bannerService.listByReferenceId(id=campaign.id).data
                for (item <- items) {
                    val filter = kind + ".item." + item.id
                    list += getReport(request.range, filter, res)
                    if (request.compareRange != null) compareList += getReport(request.compareRange, filter, res)
                }
            }
            count = list.map(c => c._1).sum

            // combined graph
            graph = combineGraph(list.map(i => i._2))
            compareGraph = combineGraph(compareList.map(i => i._2))
        } else {
            val listClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val listImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            count = listClick.map(c => c._1).sum
            for (campaign <- camps) {
                val items = env.bannerService.listByReferenceId(id=campaign.id).data
                for (item <- items) {
                    val filterClick = "click.item." + item.id
                    val filterImpression = "impression.item." + item.id
                    listClick += getReport(request.range, filterClick, res)
                    listImpression += getReport(request.range, filterImpression, res)
                    if (request.compareRange != null) {
                        compareListClick += getReport(request.compareRange, filterClick, res)
                        compareListImpression += getReport(request.compareRange, filterImpression, res)
                    }
                }
            }
            graph = combineGraphCTR(listClick.map(i=>i._2),listImpression.map(j=>j._2))
            compareGraph = combineGraphCTR(compareListClick.map(i => i._2),compareListImpression.map(j => j._2))
        }
        new ReportResponse(
            request.range,
            request.compareRange,
            count,
            reportItems,
            new ReportGraph(graph, compareGraph),
            buildSummary(summaryBag, compareSummaryBag))
    }
    def getReportOrderCampaign(request: ReportRequest): ReportResponse = {
        val orderId = request.filters.find(f => f.key.equalsIgnoreCase("orderId")).get.value.toInt
        val filter = request.filters.find(f => f.key.equalsIgnoreCase("filter")).get.value
        var camps = env.campaignService.listByReferenceId(id = orderId).data
        if(filter != "all" && filter != null) {
            val campaignType = getCampaingFillterType(filter)
            camps = camps.filter(x=>x.campaignType == campaignType);
        }
        val kind = getKind(request)
        var res = getResolution(request)
        //val items = camps.flatMap(c => env.bannerDataService.listByReferenceId(id = c.id).data).toList

        val reportProperties = WebUtils.parseStringToArray(reportFieldOrder) //Array("impression", "click", "ctr", "spent")

        val list = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        val compareList = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        var reportItems = new util.ArrayList[ReportItem]()
        val summaryBag = new util.HashMap[String, Double]()
        val compareSummaryBag = new util.HashMap[String, Double]()
        for (p <- reportProperties) {
            summaryBag(p) = 0
            compareSummaryBag(p) = 0
        }
        // items list

        if(request.range.from ==0 && request.range.to == 0) {
            val ret = getAllTimeReport(camps,request.range,res)
            request.range.from = ret._2
            request.range.to = ret._3
            res = ret._1
        }
        for (campaign <- camps) {
            val ret = getReportProperties(reportProperties,request,res,campaign,0,0,null,getValueZoneItem)
            for (p <- reportProperties) {
                summaryBag(p) = summaryBag(p) + ret._2(p)
                compareSummaryBag(p) = ret._3(p)

            }
            new ReportItem(name = campaign.name,id = campaign.id, ret._1) |> reportItems.add
        }
        summaryBag("ctr") = getPercent(summaryBag("click"), summaryBag("impression"))
        var count = 0L;
        var graph = new util.ArrayList[GraphPoint]()
        var compareGraph = new util.ArrayList[GraphPoint]()
        // graph
        if(kind != "ctr") {
            for (campaign <- camps) {
                val items = env.bannerService.listByReferenceId(id=campaign.id).data
                for (item <- items) {
                    val filter = kind + ".item." + item.id
                    list += getReport(request.range, filter, res)
                    if (request.compareRange != null) compareList += getReport(request.compareRange, filter, res)
                }
            }
            count = list.map(c => c._1).sum

            // combined graph
            graph = combineGraph(list.map(i => i._2))
            compareGraph = combineGraph(compareList.map(i => i._2))
        } else {
            val listClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val listImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            count = listClick.map(c => c._1).sum
            for (campaign <- camps) {
                val items = env.bannerService.listByReferenceId(id=campaign.id).data
                for (item <- items) {
                    val filterClick = "click.item." + item.id
                    val filterImpression = "impression.item." + item.id
                    listClick += getReport(request.range, filterClick, res)
                    listImpression += getReport(request.range, filterImpression, res)
                    if (request.compareRange != null) {
                        compareListClick += getReport(request.compareRange, filterClick, res)
                        compareListImpression += getReport(request.compareRange, filterImpression, res)
                    }
                }
            }
            graph = combineGraphCTR(listClick.map(i=>i._2),listImpression.map(j=>j._2))
            compareGraph = combineGraphCTR(compareListClick.map(i => i._2),compareListImpression.map(j => j._2))
        }
        new ReportResponse(
            request.range,
            request.compareRange,
            count,
            reportItems,
            new ReportGraph(graph, compareGraph),
            buildSummary(summaryBag, compareSummaryBag))
    }
    def getReportOrderItem(request: ReportRequest): ReportResponse = {
        val orderId = request.filters.find(f => f.key.equalsIgnoreCase("orderId")).get.value.toInt
        val camps = env.campaignService.listByReferenceId(id = orderId).data
        val kind = getKind(request)
        var res = getResolution(request)
        //val items = camps.flatMap(c => env.bannerDataService.listByReferenceId(id = c.id).data).toList
        val items = new ArrayBuffer[Banner]()
        val reportProperties = WebUtils.parseStringToArray(reportFieldOrder) //Array("impression", "click", "ctr", "spent")
        for (camp <- camps) {
            val list = env.bannerService.listByReferenceId(id = camp.id).data
            if (list != null) {
                items ++= list
            }
        }
        val list = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        val compareList = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        var reportItems = new util.ArrayList[ReportItem]()
        val summaryBag = new util.HashMap[String, Double]()
        val compareSummaryBag = new util.HashMap[String, Double]()

        for (p <- reportProperties) {
            summaryBag(p) = 0
            compareSummaryBag(p) = 0
        }
        if(request.range.from ==0 && request.range.to == 0) {
            val ret = getAllTimeReport(items,request.range,res)
            request.range.from = ret._2
            request.range.to = ret._3
            res = ret._1
        }
        // items list
        for (item <- items) {
            val ret = getReportProperties(reportProperties,request,res,null,0,item.id,null,getValueZoneItem)
            for (p <- reportProperties) {
                summaryBag(p) = summaryBag(p) + ret._2(p)
                compareSummaryBag(p) = ret._3(p)

            }
            new ReportItem(name = item.name,id = item.id, ret._1) |> reportItems.add
        }

        summaryBag("ctr") = getPercent(summaryBag("click"), summaryBag("impression"))
        var count = 0L;
        var graph = new util.ArrayList[GraphPoint]()
        var compareGraph = new util.ArrayList[GraphPoint]()
        // graph
        if(kind != "ctr") {
            for (item <- items) {
                val filter = kind + ".item." + item.id
                list += getReport(request.range, filter, res)
                if (request.compareRange != null) compareList += getReport(request.compareRange, filter, res)
            }
            count = list.map(c => c._1).sum

            // combined graph
            graph = combineGraph(list.map(i => i._2))
            compareGraph = combineGraph(compareList.map(i => i._2))
        } else {
            val listClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val listImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            count = listClick.map(c => c._1).sum
            for (item <- items) {
                val filterClick = "click.item." + item.id
                val filterImpression = "impression.item." + item.id
                listClick += getReport(request.range, filterClick, res)
                listImpression += getReport(request.range, filterImpression, res)
                if (request.compareRange != null) {
                    compareListClick += getReport(request.compareRange, filterClick, res)
                    compareListImpression += getReport(request.compareRange, filterImpression, res)
                }
            }
            graph = combineGraphCTR(listClick.map(i=>i._2),listImpression.map(j=>j._2))
            compareGraph = combineGraphCTR(compareListClick.map(i => i._2),compareListImpression.map(j => j._2))
        }
        new ReportResponse(
            request.range,
            request.compareRange,
            count,
            reportItems,

            new ReportGraph(graph, compareGraph),
            buildSummary(summaryBag, compareSummaryBag))
    }
    def getReportOrderZone(request: ReportRequest): ReportResponse = {


        val orderId = request.filters.find(f => f.key.equalsIgnoreCase("orderId")).get.value.toInt
        val camps = env.campaignService.listByReferenceId(id = orderId).data
        val kind = getKind(request)
        var res = getResolution(request)
        //val items = camps.flatMap(c => env.bannerDataService.listByReferenceId(id = c.id).data).toList
        var items = Array[Banner]()
        val reportProperties = WebUtils.parseStringToArray(reportFieldOrder) //Array("impression", "click", "ctr", "spent")

        for (camp <- camps) {
            val list = env.bannerService.listByReferenceId(id = camp.id).data
            if (list != null) {
                items ++= list

            }
        }

        var zoneids = new ArrayBuffer[Int]
        for (item <- items) {
            val list = getZoneByItem(request.range.from,request.range.to-request.range.from, item.id)
            zoneids ++= list;
        }
        zoneids = zoneids.distinct
        val list = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        val compareList = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]


        val reportItems = new util.ArrayList[ReportItem]()
        val summaryBag = new util.HashMap[String, Double]()
        val compareSummaryBag = new util.HashMap[String, Double]()

        for (p <- reportProperties) {
            summaryBag(p) = 0
            compareSummaryBag(p) = 0
        }
        val zones = env.zoneService.listByIds(zoneids.toList)
        if(request.range.from ==0 && request.range.to == 0) {
            val ret = getAllTimeReport(zones,request.range,res)
            request.range.from = ret._2
            request.range.to = ret._3
            res = ret._1
        }
        // items list
        for(zone<-zones) {

            val ret = getReportProperties(reportProperties,request,res,zone,0,0,items,getValueZoneItem)
            for (p <- reportProperties) {
                summaryBag(p) = summaryBag(p) + ret._2(p)
                compareSummaryBag(p) = ret._3(p)

            }
            new ReportItem(name = zone.name,id = zone.id, ret._1) |> reportItems.add
        }
        summaryBag("ctr") = getPercent(summaryBag("click"), summaryBag("impression"))
        var count = 0L;
        var graph = new util.ArrayList[GraphPoint]()
        var compareGraph = new util.ArrayList[GraphPoint]()
        // graph
        if(kind != "ctr") {
            for(zone<-zones) {
                for (item <- items) {
                    val filter = "zone."+ zone.id +"."+kind
                    list += getReportByZone(request.range, filter, res,item.id)
                    if (request.compareRange != null) compareList += getReportByZone(request.compareRange, filter, res,item.id)
                }
            }
            count = list.map(c => c._1).sum

            // combined graph
            graph = combineGraph(list.map(i => i._2))
            compareGraph = combineGraph(compareList.map(i => i._2))
        } else {
            val listClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val listImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            count = listClick.map(c => c._1).sum
            for(zone<-zones) {
                for (item <- items) {
                    val filterClick = "zone."+ zone.id +".click"
                    val filterImpression = "zone."+ zone.id +".impression"
                    listClick += getReportByZone(request.range, filterClick, res,item.id)
                    listImpression += getReportByZone(request.range, filterImpression, res,item.id)
                    if (request.compareRange != null) {
                        compareListClick += getReportByZone(request.compareRange, filterClick, res,item.id)
                        compareListImpression += getReportByZone(request.compareRange, filterImpression, res,item.id)
                    }
                }
            }
            graph = combineGraphCTR(listClick.map(i=>i._2),listImpression.map(j=>j._2))
            compareGraph = combineGraphCTR(compareListClick.map(i => i._2),compareListImpression.map(j => j._2))
        }
        new ReportResponse(
            request.range,
            request.compareRange,
            count,
            reportItems,

            new ReportGraph(graph, compareGraph),
            buildSummary(summaryBag, compareSummaryBag))
    }
    def getReportItemZone(request: ReportRequest): ReportResponse = {
        val itemId = request.filters.find(f => f.key.equalsIgnoreCase("itemId")).get.value.toInt
        val kind = getKind(request)
        var res = getResolution(request)
        val reportProperties = WebUtils.parseStringToArray(reportFieldOrder) //Array("impression", "click", "ctr", "spent")

        val list = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        val compareList = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        val reportItems = new util.ArrayList[ReportItem]()
        val summaryBag = new util.HashMap[String, Double]()
        val compareSummaryBag = new util.HashMap[String, Double]()

        for (p <- reportProperties) {
            summaryBag(p) = 0
            compareSummaryBag(p) = 0
        }
        val zoneids = getZoneByItem(request.range.from,request.range.to-request.range.from,itemId)
        val zones = env.zoneService.listByIds(zoneids.toList)
        // items list
        for(zone<-zones) {

            val ret = getReportProperties(reportProperties,request,res,zone,0,itemId,null,getValueZoneItem)
            for (p <- reportProperties) {
                summaryBag(p) = summaryBag(p) + ret._2(p)
                compareSummaryBag(p) = ret._3(p)

            }
            new ReportItem(name = zone.name,id = zone.id, ret._1) |> reportItems.add
        }
        summaryBag("ctr") = getPercent(summaryBag("click"), summaryBag("impression"))
        var count = 0L;
        var graph = new util.ArrayList[GraphPoint]()
        var compareGraph = new util.ArrayList[GraphPoint]()
        // graph
        if(kind != "ctr") {
            for(zone<-zones) {
                    val filter ="zone."+ zone.id +"." +  kind
                    list += getReportByZone(request.range, filter, res,itemId)
                    if (request.compareRange != null) compareList += getReportByZone(request.compareRange, filter, res,itemId)
            }
            count = list.map(c => c._1).sum

            // combined graph
            graph = combineGraph(list.map(i => i._2))
            compareGraph = combineGraph(compareList.map(i => i._2))
        } else {
            val listClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val listImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            count = listClick.map(c => c._1).sum
            for(zone<-zones) {

                    val filterClick = "zone."+ zone.id +"." + "click"
                    val filterImpression = "zone."+ zone.id +"." + "impression"
                    listClick += getReportByZone(request.range, filterClick, res,itemId)
                    listImpression += getReportByZone(request.range, filterImpression, res,itemId)
                    if (request.compareRange != null) {
                        compareListClick += getReportByZone(request.compareRange, filterClick, res,itemId)
                        compareListImpression += getReportByZone(request.compareRange, filterImpression, res,itemId)
                    }

            }
            graph = combineGraphCTR(listClick.map(i=>i._2),listImpression.map(j=>j._2))
            compareGraph = combineGraphCTR(compareListClick.map(i => i._2),compareListImpression.map(j => j._2))
        }
        new ReportResponse(
            request.range,
            request.compareRange,
            count,
            reportItems,

            new ReportGraph(graph, compareGraph),
            buildSummary(summaryBag, compareSummaryBag))
    }
    def getReportOrderWebsite(request: ReportRequest): ReportResponse = {
        var fromTime = 0l
        var toTime = 0l

        val orderId = request.filters.find(f => f.key.equalsIgnoreCase("orderId")).get.value.toInt
        val camps = env.campaignService.listByReferenceId(id = orderId).data
        val kind = getKind(request)
        var res = getResolution(request)
        //val items = camps.flatMap(c => env.bannerDataService.listByReferenceId(id = c.id).data).toList
        var items = Array[Banner]()
        val reportProperties = WebUtils.parseStringToArray(reportFieldOrder) //Array("impression", "click", "ctr", "spent")
        fromTime = System.currentTimeMillis()
        for (camp <- camps) {
            val list = env.bannerService.listByReferenceId(id = camp.id).data
            if (list != null) {
                items ++= list

            }
        }
        toTime = System.currentTimeMillis()
        System.out.println("Website by order - get items of order:" + (toTime - fromTime))
        var websiteids = Array[Int]()
        fromTime = System.currentTimeMillis()
        for (item <- items) {
            val list = getWebsiteByItem(request.range.from,request.range.to-request.range.from,item.id)
            websiteids ++= list;
        }
        toTime = System.currentTimeMillis()
        websiteids = websiteids.distinct
        System.out.println("Website by order - get websites by items:" + (toTime - fromTime))

        val websites = env.websiteService.listByIds(websiteids.toList)
        val list = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        val compareList = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        val reportItems = new util.ArrayList[ReportItem]()
        val summaryBag = new util.HashMap[String, Double]()
        val compareSummaryBag = new util.HashMap[String, Double]()

        for (p <- reportProperties) {
            summaryBag(p) = 0
            compareSummaryBag(p) = 0
        }
        if(request.range.from ==0 && request.range.to == 0) {
            val ret = getAllTimeReport(websites,request.range,res)
            request.range.from = ret._2
            request.range.to = ret._3
            res = ret._1
        }
        fromTime = System.currentTimeMillis()
        for(website <- websites) {
            val ret = getReportProperties(reportProperties,request,res,website,0,0,items,getValueZoneItem)
            for (p <- reportProperties) {
                summaryBag(p) = summaryBag(p) + ret._2(p)
                compareSummaryBag(p) = ret._3(p)
            }
            new ReportItem(name = website.name,id = website.id, ret._1) |> reportItems.add
        }
        toTime = System.currentTimeMillis()
        System.out.println("Website by order - get reportItems:" + (toTime - fromTime))

        summaryBag("ctr") = getPercent(summaryBag("click"), summaryBag("impression"))
        var count = 0L;
        var graph = new util.ArrayList[GraphPoint]()
        var compareGraph = new util.ArrayList[GraphPoint]()
        // graph
        if(kind != "ctr") {
            for(website <- websites) {
                for (item <- items) {
                    val filter = "site."+ website.id +"." + kind
                    list += getReportBySite(request.range, filter, res,item.id)
                    if (request.compareRange != null) compareList += getReportBySite(request.compareRange, filter, res,item.id)
                }
            }
            count = list.map(c => c._1).sum

            // combined graph
            graph = combineGraph(list.map(i => i._2))
            compareGraph = combineGraph(compareList.map(i => i._2))
        } else {
            val listClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val listImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            count = listClick.map(c => c._1).sum
            for(website <- websites) {
                for (item <- items) {
                    val filterClick = "site."+ website.id +".click"
                    val filterImpression = "site."+ website.id +".impression"
                    listClick += getReportBySite(request.range, filterClick, res,item.id)
                    listImpression += getReportBySite(request.range, filterImpression, res,item.id)
                    if (request.compareRange != null) {
                        compareListClick += getReportBySite(request.compareRange, filterClick, res,item.id)
                        compareListImpression += getReportBySite(request.compareRange, filterImpression, res,item.id)
                    }
                }
            }
            graph = combineGraphCTR(listClick.map(i=>i._2),listImpression.map(j=>j._2))
            compareGraph = combineGraphCTR(compareListClick.map(i => i._2),compareListImpression.map(j => j._2))
        }
        new ReportResponse(
            request.range,
            request.compareRange,
            count,
            reportItems,

            new ReportGraph(graph, compareGraph),
            buildSummary(summaryBag, compareSummaryBag))
    }

    def getReportConversionOrders(request: ReportRequest): ReportResponse = {

        val orders = env.orderService.loadAll().toArray[Order]
        val kind = getKind(request)
        var res = getResolution(request)
        val reportOrders = new util.ArrayList[ReportItem]()
        val reportProperties = WebUtils.parseStringToArray(reportFieldConversion)
        val list = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        val compareList = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        val summaryBag = new util.HashMap[String, Double]()
        val compareSummaryBag = new util.HashMap[String, Double]()
        for (p <- reportProperties) {
            summaryBag(p) = 0
            compareSummaryBag(p) = 0
        }
        if(request.range.from ==0 && request.range.to == 0) {
            val ret = getAllTimeReport(orders,request.range,res)
            request.range.from = ret._2
            request.range.to = ret._3
            res = ret._1
        }
        // items list
        for(order <- orders) {
            val campaigns = env.campaignService.listByReferenceId(id=order.id).data
            val ret = getReportProperties(reportProperties,request,res,campaigns,0,0,null,getValueZoneItem)
            for (p <- reportProperties) {
                summaryBag(p) = summaryBag(p) + ret._2(p)
                compareSummaryBag(p) = ret._3(p)

            }
            new ReportItem(name = order.name,id = order.id, ret._1) |> reportOrders.add
        }
        getConversionReport(reportOrders)
        summaryBag("ctr") = getPercent(summaryBag("click"), summaryBag("impression"))
        var count = 0L;
        var graph = new util.ArrayList[GraphPoint]()
        var compareGraph = new util.ArrayList[GraphPoint]()
        // graph
        if(kind != "ctr") {
            for(order <- orders) {
                val campaigns = env.campaignService.listByReferenceId(id=order.id).data
                for(campaign <- campaigns) {
                    val items = env.bannerService.listByReferenceId(id=campaign.id).data
                    for (item <- items) {
                        val filter = kind + ".item." + item.id
                        list += getReport(request.range, filter, res)
                        if (request.compareRange != null) compareList += getReport(request.compareRange, filter, res)
                    }
                }
            }
            count = list.map(c => c._1).sum

            // combined graph
            graph = combineGraph(list.map(i => i._2))
            compareGraph = combineGraph(compareList.map(i => i._2))
        } else {
            val listClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val listImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            count = listClick.map(c => c._1).sum
            for(order <- orders) {
                val campaigns = env.campaignService.listByReferenceId(id=order.id).data
                for(campaign <- campaigns) {
                    val items = env.bannerService.listByReferenceId(id=campaign.id).data
                    for (item <- items) {
                        val filterClick = "click.item." + item.id
                        val filterImpression = "impression.item." + item.id
                        listClick += getReport(request.range, filterClick, res)
                        listImpression += getReport(request.range, filterImpression, res)
                        if (request.compareRange != null) {
                            compareListClick += getReport(request.compareRange, filterClick, res)
                            compareListImpression += getReport(request.compareRange, filterImpression, res)
                        }
                    }
                }
            }
            graph = combineGraphCTR(listClick.map(i=>i._2),listImpression.map(j=>j._2))
            compareGraph = combineGraphCTR(compareListClick.map(i => i._2),compareListImpression.map(j => j._2))
        }


        new ReportResponse(
            request.range,
            request.compareRange,
            count,
            reportOrders,
            new ReportGraph(graph, compareGraph),
            buildSummary(summaryBag, compareSummaryBag))
    }
    def getConversionReport(reportOrders:util.ArrayList[ReportItem]) = {
        for(report <-reportOrders) {
            for(p <- report.properties) {
                if(p.label == "costconversion") {
                    p.value = getCostConversion(report.properties.filter(x=>x.label=="spent").toList.get(0).value,report.properties.filter(x=>x.label=="conversion").toList.get(0).value)
                    p.compareValue = getCostConversion(report.properties.filter(x=>x.label=="spent").toList.get(0).compareValue,report.properties.filter(x=>x.label=="conversion").toList.get(0).compareValue)
                } else if(p.label == "conversionrate") {
                    p.value = getConversionRate(report.properties.filter(x=>x.label=="conversion").toList.get(0).value,report.properties.filter(x=>x.label=="click").toList.get(0).value)
                    p.compareValue = getConversionRate(report.properties.filter(x=>x.label=="conversion").toList.get(0).compareValue,report.properties.filter(x=>x.label=="click").toList.get(0).compareValue)
                } else if(p.label == "roi") {
                    p.value = getConversionRoi(report.properties.filter(x=>x.label=="revenueconversion").toList.get(0).value,report.properties.filter(x=>x.label=="spent").toList.get(0).value)
                    p.compareValue = getConversionRoi(report.properties.filter(x=>x.label=="revenueconversion").toList.get(0).compareValue,report.properties.filter(x=>x.label=="spent").toList.get(0).compareValue)
                }

            }
        }
    }
    def getCostConversion(spent:Double,conversion:Double):Double = {
        if(conversion == 0) return 0
        return spent/conversion
    }
    def getConversionRate(conversion:Double,click:Double):Double = {
        if(click == 0) return 0
        return conversion/click
    }
    def getConversionRoi(revenueconversion:Double,spent:Double):Double = {
        if(spent == 0) return 0
        return (revenueconversion-spent)/spent
    }
    def getReportOrders(request: ReportRequest): ReportResponse = {

        val orders = env.orderService.loadAll().toArray[Order]
        val kind = getKind(request)
        var res = getResolution(request)
        val reportOrders = new util.ArrayList[ReportItem]()
        val reportProperties = WebUtils.parseStringToArray(reportFieldOrder) //Array("impression", "click", "ctr", "spent")
        val list = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        val compareList = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        val summaryBag = new util.HashMap[String, Double]()
        val compareSummaryBag = new util.HashMap[String, Double]()
        for (p <- reportProperties) {
            summaryBag(p) = 0
            compareSummaryBag(p) = 0
        }
        if(request.range.from ==0 && request.range.to == 0) {
            val ret = getAllTimeReport(orders,request.range,res)
            request.range.from = ret._2
            request.range.to = ret._3
            res = ret._1
        }
        // items list
        for(order <- orders) {
            val campaigns = env.campaignService.listByReferenceId(id=order.id).data
            val ret = getReportProperties(reportProperties,request,res,campaigns,0,0,null,getValueZoneItem)
            for (p <- reportProperties) {
                summaryBag(p) = summaryBag(p) + ret._2(p)
                compareSummaryBag(p) = ret._3(p)

            }
            new ReportItem(name = order.name,id = order.id, ret._1) |> reportOrders.add
        }
        summaryBag("ctr") = getPercent(summaryBag("click"), summaryBag("impression"))
        var count = 0L;
        var graph = new util.ArrayList[GraphPoint]()
        var compareGraph = new util.ArrayList[GraphPoint]()
        // graph
        if(kind != "ctr") {
            for(order <- orders) {
                val campaigns = env.campaignService.listByReferenceId(id=order.id).data
                for(campaign <- campaigns) {
                    val items = env.bannerService.listByReferenceId(id=campaign.id).data
                    for (item <- items) {
                        val filter = kind + ".item." + item.id
                        list += getReport(request.range, filter, res)
                        if (request.compareRange != null) compareList += getReport(request.compareRange, filter, res)
                    }
                }
            }
            count = list.map(c => c._1).sum

            // combined graph
            graph = combineGraph(list.map(i => i._2))
            compareGraph = combineGraph(compareList.map(i => i._2))
        } else {
            val listClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val listImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            count = listClick.map(c => c._1).sum
            for(order <- orders) {
                val campaigns = env.campaignService.listByReferenceId(id=order.id).data
                for(campaign <- campaigns) {
                    val items = env.bannerService.listByReferenceId(id=campaign.id).data
                    for (item <- items) {
                        val filterClick = "click.item." + item.id
                        val filterImpression = "impression.item." + item.id
                        listClick += getReport(request.range, filterClick, res)
                        listImpression += getReport(request.range, filterImpression, res)
                        if (request.compareRange != null) {
                            compareListClick += getReport(request.compareRange, filterClick, res)
                            compareListImpression += getReport(request.compareRange, filterImpression, res)
                        }
                    }
                }
            }
            graph = combineGraphCTR(listClick.map(i=>i._2),listImpression.map(j=>j._2))
            compareGraph = combineGraphCTR(compareListClick.map(i => i._2),compareListImpression.map(j => j._2))
        }


        new ReportResponse(
            request.range,
            request.compareRange,
            count,
            reportOrders,
            new ReportGraph(graph, compareGraph),
            buildSummary(summaryBag, compareSummaryBag))
    }

    def getReportConversionItemByCampaing(request: ReportRequest): ReportResponse = {

        val campId = request.filters.find(f => f.key.equalsIgnoreCase("campId")).get.value.toInt
        val camp = env.campaignService.load(id = campId)
        val kind = getKind(request)
        var res = getResolution(request)
        //val items = camps.flatMap(c => env.bannerDataService.listByReferenceId(id = c.id).data).toList
        val items = new ArrayBuffer[Banner]()
        val reportProperties = WebUtils.parseStringToArray(reportFieldConversion) //Array("impression", "click", "ctr", "spent")
        val listItem = env.bannerService.listByReferenceId(id = camp.id).data
        if (listItem != null) {
            items ++= listItem
        }
        val list = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        val compareList = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        var reportItems = new util.ArrayList[ReportItem]()
        val summaryBag = new util.HashMap[String, Double]()
        val compareSummaryBag = new util.HashMap[String, Double]()
        for (p <- reportProperties) {
            summaryBag(p) = 0
            compareSummaryBag(p) = 0
        }
        if(request.range.from ==0 && request.range.to == 0) {
            val ret = getAllTimeReport(items,request.range,res)
            request.range.from = ret._2
            request.range.to = ret._3
            res = ret._1
        }
        // items list
        for (item <- items) {
            val ret = getReportProperties(reportProperties,request,res,null,0,item.id,null,getValueZoneItem)
            for (p <- reportProperties) {
                summaryBag(p) = summaryBag(p) + ret._2(p)
                compareSummaryBag(p) = ret._3(p)

            }
            new ReportItem(name = item.name,id = item.id, ret._1) |> reportItems.add
        }
        getConversionReport(reportItems)
        summaryBag("ctr") = getPercent(summaryBag("click"), summaryBag("impression"))
        var count = 0L;
        var graph = new util.ArrayList[GraphPoint]()
        var compareGraph = new util.ArrayList[GraphPoint]()
        // graph
        if(kind != "ctr") {
            for (item <- items) {
                val filter = kind + ".item." + item.id
                list += getReport(request.range, filter, res)
                if (request.compareRange != null) compareList += getReport(request.compareRange, filter, res)
            }
            count = list.map(c => c._1).sum

            // combined graph
            graph = combineGraph(list.map(i => i._2))
            compareGraph = combineGraph(compareList.map(i => i._2))
        } else {
            val listClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val listImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            count = listClick.map(c => c._1).sum
            for (item <- items) {
                val filterClick = "click.item." + item.id
                val filterImpression = "impression.item." + item.id
                listClick += getReport(request.range, filterClick, res)
                listImpression += getReport(request.range, filterImpression, res)
                if (request.compareRange != null) {
                    compareListClick += getReport(request.compareRange, filterClick, res)
                    compareListImpression += getReport(request.compareRange, filterImpression, res)
                }
            }
            graph = combineGraphCTR(listClick.map(i=>i._2),listImpression.map(j=>j._2))
            compareGraph = combineGraphCTR(compareListClick.map(i => i._2),compareListImpression.map(j => j._2))
        }
        new ReportResponse(
            request.range,
            request.compareRange,
            count,
            reportItems,

            new ReportGraph(graph, compareGraph),
            buildSummary(summaryBag, compareSummaryBag))
    }
    /** *
      * get report by campaign id
      * @param request
      * @return
      */
    def getReportItem(request: ReportRequest): ReportResponse = {

        val campId = request.filters.find(f => f.key.equalsIgnoreCase("campId")).get.value.toInt
        val camp = env.campaignService.load(id = campId)
        val kind = getKind(request)
        var res = getResolution(request)
        //val items = camps.flatMap(c => env.bannerDataService.listByReferenceId(id = c.id).data).toList
        val items = new ArrayBuffer[Banner]()
        val reportProperties = WebUtils.parseStringToArray(reportFieldOrder) //Array("impression", "click", "ctr", "spent")
        val listItem = env.bannerService.listByReferenceId(id = camp.id).data
        if (listItem != null) {
            items ++= listItem
        }
        val list = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        val compareList = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        var reportItems = new util.ArrayList[ReportItem]()
        val summaryBag = new util.HashMap[String, Double]()
        val compareSummaryBag = new util.HashMap[String, Double]()
        for (p <- reportProperties) {
            summaryBag(p) = 0
            compareSummaryBag(p) = 0
        }
        if(request.range.from ==0 && request.range.to == 0) {
            val ret = getAllTimeReport(items,request.range,res)
            request.range.from = ret._2
            request.range.to = ret._3
            res = ret._1
        }
        // items list
        for (item <- items) {
            val ret = getReportProperties(reportProperties,request,res,null,0,item.id,null,getValueZoneItem)
            for (p <- reportProperties) {
                summaryBag(p) = summaryBag(p) + ret._2(p)
                compareSummaryBag(p) = ret._3(p)

            }
            new ReportItem(name = item.name,id = item.id, ret._1) |> reportItems.add
        }

        summaryBag("ctr") = getPercent(summaryBag("click"), summaryBag("impression"))
        var count = 0L;
        var graph = new util.ArrayList[GraphPoint]()
        var compareGraph = new util.ArrayList[GraphPoint]()
        // graph
        if(kind != "ctr") {
            for (item <- items) {
                val filter = kind + ".item." + item.id
                list += getReport(request.range, filter, res)
                if (request.compareRange != null) compareList += getReport(request.compareRange, filter, res)
            }
            count = list.map(c => c._1).sum

            // combined graph
            graph = combineGraph(list.map(i => i._2))
            compareGraph = combineGraph(compareList.map(i => i._2))
        } else {
            val listClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val listImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            count = listClick.map(c => c._1).sum
            for (item <- items) {
                val filterClick = "click.item." + item.id
                val filterImpression = "impression.item." + item.id
                listClick += getReport(request.range, filterClick, res)
                listImpression += getReport(request.range, filterImpression, res)
                if (request.compareRange != null) {
                    compareListClick += getReport(request.compareRange, filterClick, res)
                    compareListImpression += getReport(request.compareRange, filterImpression, res)
                }
            }
            graph = combineGraphCTR(listClick.map(i=>i._2),listImpression.map(j=>j._2))
            compareGraph = combineGraphCTR(compareListClick.map(i => i._2),compareListImpression.map(j => j._2))
        }
        new ReportResponse(
            request.range,
            request.compareRange,
            count,
            reportItems,

            new ReportGraph(graph, compareGraph),
            buildSummary(summaryBag, compareSummaryBag))
    }
    def getReportItemCampaignBySite(request: ReportRequest): ReportResponse = {

        val websiteId = request.filters.find(f => f.key.equalsIgnoreCase("websiteId")).get.value.toInt
        val campId = request.filters.find(f => f.key.equalsIgnoreCase("campId")).get.value.toInt
        val camp = env.campaignService.load(id = campId)

        val zones = env.zoneService.listByReferenceId(id=websiteId).data


        val kind = getKind(request)
        var res = getResolution(request)
        //val items = camps.flatMap(c => env.bannerDataService.listByReferenceId(id = c.id).data).toList
        var items = Array[Banner]()
        val reportProperties = WebUtils.parseStringToArray(reportFieldWebsite) //Array("impression", "click", "ctr", "revenue")

        val listItem = env.bannerService.listByReferenceId(id = camp.id).data
        if (listItem != null) {
            items ++= listItem
        }
        val list = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        val compareList = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]


        var reportItems = new util.ArrayList[ReportItem]()
        val summaryBag = new util.HashMap[String, Double]()
        val compareSummaryBag = new util.HashMap[String, Double]()

        for (p <- reportProperties) {
            summaryBag(p) = 0
            compareSummaryBag(p) = 0
        }
        if(request.range.from ==0 && request.range.to == 0) {
            val ret = getAllTimeReport(items,request.range,res)
            request.range.from = ret._2
            request.range.to = ret._3
            res = ret._1
        }
        // items list
        for (item <- items) {
            val zoneids = getZoneByItem(request.range.from,request.range.to-request.range.from,item.id)
            val zones = env.zoneService.listByIds(zoneids.toList)
            val ret = getReportProperties(reportProperties,request,res,zones,0,item.id,null,getValueZoneItem)
            for (p <- reportProperties) {
                summaryBag(p) = summaryBag(p) + ret._2(p)
                compareSummaryBag(p) = ret._3(p)

            }
            new ReportItem(name = item.name,id = item.id, ret._1) |> reportItems.add
        }

        summaryBag("ctr") = getPercent(summaryBag("click"), summaryBag("impression"))
        var count = 0L;
        var graph = new util.ArrayList[GraphPoint]()
        var compareGraph = new util.ArrayList[GraphPoint]()
        // graph
        if(kind != "ctr") {
            for (item <- items) {
                for(zone <- zones)  {
                    val filter = "zone."+ zone.id +"."+  kind + ".item." + item.id
                    list += getReport(request.range, filter, res)
                    if (request.compareRange != null) compareList += getReport(request.compareRange, filter, res)
                }
            }
            count = list.map(c => c._1).sum

            // combined graph
            graph = combineGraph(list.map(i => i._2))
            compareGraph = combineGraph(compareList.map(i => i._2))
        } else {
            val listClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val listImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            count = listClick.map(c => c._1).sum
            for (item <- items) {
                for(zone <- zones)  {
                    val filterClick = "zone."+ zone.id +"."+  "click.item." + item.id
                    val filterImpression = "zone."+ zone.id +"."+  "impression.item." + item.id
                    listClick += getReport(request.range, filterClick, res)
                    listImpression += getReport(request.range, filterImpression, res)
                    if (request.compareRange != null) {
                        compareListClick += getReport(request.compareRange, filterClick, res)
                        compareListImpression += getReport(request.compareRange, filterImpression, res)
                    }
                }
            }
            graph = combineGraphCTR(listClick.map(i=>i._2),listImpression.map(j=>j._2))
            compareGraph = combineGraphCTR(compareListClick.map(i => i._2),compareListImpression.map(j => j._2))
        }
        new ReportResponse(
            request.range,
            request.compareRange,
            count,
            reportItems,

            new ReportGraph(graph, compareGraph),
            buildSummary(summaryBag, compareSummaryBag))
    }

    def getReportCampaignItemByZoneGroup(request: ReportRequest): ReportResponse = {
        val zonegroupId = request.filters.find(f => f.key.equalsIgnoreCase("zoneGroupId")).get.value.toInt
        val campId = request.filters.find(f => f.key.equalsIgnoreCase("campId")).get.value.toInt
        val camp = env.campaignService.load(id = campId)
        val kind = getKind(request)
        val zones = env.zoneToZoneGroupService.listByReferenceId(id=zonegroupId).data
        var res = getResolution(request)
        //val items = camps.flatMap(c => env.bannerDataService.listByReferenceId(id = c.id).data).toList
        val items = new ArrayBuffer[Banner]()
        val reportProperties = WebUtils.parseStringToArray(reportFieldWebsite) //Array("impression", "click", "ctr", "revenue")
        val listItem = env.bannerService.listByReferenceId(id = camp.id).data
        if (listItem != null) {
            items ++= listItem
        }
        val list = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        val compareList = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        var reportItems = new util.ArrayList[ReportItem]()
        val summaryBag = new util.HashMap[String, Double]()
        val compareSummaryBag = new util.HashMap[String, Double]()
        for (p <- reportProperties) {
            summaryBag(p) = 0
            compareSummaryBag(p) = 0
        }
        if(request.range.from ==0 && request.range.to == 0) {
            val ret = getAllTimeReport(items,request.range,res)
            request.range.from = ret._2
            request.range.to = ret._3
            res = ret._1
        }
        for (item <- items) {
            val zoneids = getZoneByItem(request.range.from,request.range.to-request.range.from,item.id)
            val zones = env.zoneService.listByIds(zoneids.toList)
            val ret = getReportProperties(reportProperties,request,res,zones,0,item.id,null,getValueZoneItem)
            for (p <- reportProperties) {
                summaryBag(p) = summaryBag(p) + ret._2(p)
                compareSummaryBag(p) = ret._3(p)

            }
            new ReportItem(name = item.name,id = item.id, ret._1) |> reportItems.add
        }
        summaryBag("ctr") = getPercent(summaryBag("click"), summaryBag("impression"))
        var count = 0L;
        var graph = new util.ArrayList[GraphPoint]()
        var compareGraph = new util.ArrayList[GraphPoint]()
        // graph
        if(kind != "ctr") {
            for (item <- items) {
                for(zone <- zones) {
                    val filter = "zone."+ zone.zoneId +"."+ kind + ".item." + item.id
                    list += getReport(request.range, filter, res)
                    if (request.compareRange != null) compareList += getReport(request.compareRange, filter, res)
                }
            }
            count = list.map(c => c._1).sum

            // combined graph
            graph = combineGraph(list.map(i => i._2))
            compareGraph = combineGraph(compareList.map(i => i._2))
        } else {
            val listClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val listImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            count = listClick.map(c => c._1).sum
            for (item <- items) {
                for(zone <- zones) {
                    val filterClick = "zone."+ zone.zoneId +"."+"click.item." + item.id
                    val filterImpression = "zone."+ zone.zoneId +"."+"impression.item." + item.id
                    listClick += getReport(request.range, filterClick, res)
                    listImpression += getReport(request.range, filterImpression, res)
                    if (request.compareRange != null) {
                        compareListClick += getReport(request.compareRange, filterClick, res)
                        compareListImpression += getReport(request.compareRange, filterImpression, res)
                    }
                }
            }
            graph = combineGraphCTR(listClick.map(i=>i._2),listImpression.map(j=>j._2))
            compareGraph = combineGraphCTR(compareListClick.map(i => i._2),compareListImpression.map(j => j._2))
        }
        new ReportResponse(
            request.range,
            request.compareRange,
            count,
            reportItems,

            new ReportGraph(graph, compareGraph),
            buildSummary(summaryBag, compareSummaryBag))
    }
    def getReportCampaignItemByZone(request: ReportRequest): ReportResponse = {
        val zoneId = request.filters.find(f => f.key.equalsIgnoreCase("zoneId")).get.value.toInt
        val campId = request.filters.find(f => f.key.equalsIgnoreCase("campId")).get.value.toInt
        val camp = env.campaignService.load(id = campId)
        val kind = getKind(request)
        var res = getResolution(request)
        //val items = camps.flatMap(c => env.bannerDataService.listByReferenceId(id = c.id).data).toList
        val items = new ArrayBuffer[Banner]()
        val reportProperties = WebUtils.parseStringToArray(reportFieldWebsite) //Array("impression", "click", "ctr", "revenue")

        val listItem = env.bannerService.listByReferenceId(id = camp.id).data
        if (listItem != null) {
            items ++= listItem
        }
        val list = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        val compareList = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        var reportItems = new util.ArrayList[ReportItem]()
        val summaryBag = new util.HashMap[String, Double]()
        val compareSummaryBag = new util.HashMap[String, Double]()
        for (p <- reportProperties) {
            summaryBag(p) = 0
            compareSummaryBag(p) = 0
        }
        val zone = env.zoneService.load(zoneId)
        // items list
        val itemsOfZone = getItemByZone(request.range.from,request.range.to-request.range.from,zone.id)
        if(request.range.from ==0 && request.range.to == 0) {
            val ret = getAllTimeReport(items,request.range,res)
            request.range.from = ret._2
            request.range.to = ret._3
            res = ret._1
        }
        for (item <- items) {


            if(itemsOfZone.filter(x => x == item.id).length > 0) {
                val ret = getReportProperties(reportProperties,request,res,null,zone.id,item.id,null,getValueZoneItem)
                for (p <- reportProperties) {
                    summaryBag(p) = summaryBag(p) + ret._2(p)
                    compareSummaryBag(p) = ret._3(p)

                }
                new ReportItem(name = item.name,id = item.id, ret._1) |> reportItems.add
            }

        }

        summaryBag("ctr") = getPercent(summaryBag("click"), summaryBag("impression"))
        var count = 0L;
        var graph = new util.ArrayList[GraphPoint]()
        var compareGraph = new util.ArrayList[GraphPoint]()
        // graph
        if(kind != "ctr") {
            for (item <- items) {
                val filter = "zone."+ zoneId +"."+kind + ".item." + item.id
                list += getReport(request.range, filter, res)
                if (request.compareRange != null) compareList += getReport(request.compareRange, filter, res)
            }
            count = list.map(c => c._1).sum

            // combined graph
            graph = combineGraph(list.map(i => i._2))
            compareGraph = combineGraph(compareList.map(i => i._2))
        } else {
            val listClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val listImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            count = listClick.map(c => c._1).sum
            for (item <- items) {
                val filterClick = "zone."+ zoneId +"."+"click.item." + item.id
                val filterImpression = "zone."+ zoneId +"."+"impression.item." + item.id
                listClick += getReport(request.range, filterClick, res)
                listImpression += getReport(request.range, filterImpression, res)
                if (request.compareRange != null) {
                    compareListClick += getReport(request.compareRange, filterClick, res)
                    compareListImpression += getReport(request.compareRange, filterImpression, res)
                }
            }
            graph = combineGraphCTR(listClick.map(i=>i._2),listImpression.map(j=>j._2))
            compareGraph = combineGraphCTR(compareListClick.map(i => i._2),compareListImpression.map(j => j._2))
        }
        new ReportResponse(
            request.range,
            request.compareRange,
            count,
            reportItems,

            new ReportGraph(graph, compareGraph),
            buildSummary(summaryBag, compareSummaryBag))
    }

    def getReportCampaignZone(request: ReportRequest): ReportResponse = {
        val campId = request.filters.find(f => f.key.equalsIgnoreCase("campId")).get.value.toInt
        val camp = env.campaignService.load(id = campId)
        val kind = getKind(request)
        var res = getResolution(request)
        //val items = camps.flatMap(c => env.bannerDataService.listByReferenceId(id = c.id).data).toList
        var items = Array[Banner]()
        val reportProperties = WebUtils.parseStringToArray(reportFieldOrder) //Array("impression", "click", "ctr", "spent")
        var zoneids = new ArrayBuffer[Int]
        val listItem = env.bannerService.listByReferenceId(id = camp.id).data
        if (listItem != null) {
            items ++= listItem
        }
        for (item <- items) {
            val list = getZoneByItem(request.range.from,request.range.to-request.range.from,item.id)
            zoneids ++= list;
        }
        zoneids = zoneids.distinct
        val zones = env.zoneService.listByIds(zoneids.toList)
        val list = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        val compareList = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        var reportItems = new util.ArrayList[ReportItem]()
        val summaryBag = new util.HashMap[String, Double]()
        val compareSummaryBag = new util.HashMap[String, Double]()
        for (p <- reportProperties) {
            summaryBag(p) = 0
            compareSummaryBag(p) = 0
        }
        if(request.range.from ==0 && request.range.to == 0) {
            val ret = getAllTimeReport(zones,request.range,res)
            request.range.from = ret._2
            request.range.to = ret._3
            res = ret._1
        }
        // items list
        for(zone <- zones) {
            val ret = getReportProperties(reportProperties,request,res,zone,0,0,items,getValueZoneItem)
            for (p <- reportProperties) {
                summaryBag(p) = summaryBag(p) + ret._2(p)
                compareSummaryBag(p) = ret._3(p)

            }

            new ReportItem(name = zone.name,id = zone.id, ret._1) |> reportItems.add
        }

        summaryBag("ctr") = getPercent(summaryBag("click"), summaryBag("impression"))
        var count = 0L;
        var graph = new util.ArrayList[GraphPoint]()
        var compareGraph = new util.ArrayList[GraphPoint]()
        // graph
        if(kind != "ctr") {
            for(zone <- zones) {
                for (item <- items) {
                    val filter = "zone." + zone.id + "." + kind + ".item." + item.id
                    list += getReport(request.range, filter, res)
                    if (request.compareRange != null) compareList += getReport(request.compareRange, filter, res)
                }
            }
            count = list.map(c => c._1).sum

            // combined graph
            graph = combineGraph(list.map(i => i._2))
            compareGraph = combineGraph(compareList.map(i => i._2))
        } else {
            val listClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val listImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            count = listClick.map(c => c._1).sum
            for(zone <- zones) {
                for (item <- items) {
                    val filterClick = "zone."+ zone.id +".click.item." + item.id
                    val filterImpression = "zone."+ zone.id +".impression.item." + item.id
                    listClick += getReport(request.range, filterClick, res)
                    listImpression += getReport(request.range, filterImpression, res)
                    if (request.compareRange != null) {
                        compareListClick += getReport(request.compareRange, filterClick, res)
                        compareListImpression += getReport(request.compareRange, filterImpression, res)
                    }
                }
            }
            graph = combineGraphCTR(listClick.map(i=>i._2),listImpression.map(j=>j._2))
            compareGraph = combineGraphCTR(compareListClick.map(i => i._2),compareListImpression.map(j => j._2))
        }
        new ReportResponse(
            request.range,
            request.compareRange,
            count,
            reportItems,

            new ReportGraph(graph, compareGraph),
            buildSummary(summaryBag, compareSummaryBag))
    }
    def getReportZoneGroupZone(request: ReportRequest): ReportResponse = {

        val zonegroupId = request.filters.find(f => f.key.equalsIgnoreCase("zoneGroupId")).get.value.toInt
        val zones = env.zoneToZoneGroupService.listByReferenceId(id=zonegroupId).data
        val kind = getKind(request)
        var res = getResolution(request)
        //val items = camps.flatMap(c => env.bannerDataService.listByReferenceId(id = c.id).data).toList
        val reportProperties = WebUtils.parseStringToArray(reportFieldWebsite) //Array("impression", "click", "ctr", "revenue")

        val list = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        val compareList = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]


        var reportItems = new util.ArrayList[ReportItem]()
        val summaryBag = new util.HashMap[String, Double]()
        val compareSummaryBag = new util.HashMap[String, Double]()

        for (p <- reportProperties) {
            summaryBag(p) = 0
            compareSummaryBag(p) = 0
        }
        if(request.range.from ==0 && request.range.to == 0) {
            val ret = getAllTimeReport(zones,request.range,res)
            request.range.from = ret._2
            request.range.to = ret._3
            res = ret._1
        }
        // items list
        for(zgroup <- zones) {
            val zone = env.zoneService.load(zgroup.zoneId)
            val ret = getReportProperties(reportProperties,request,res,zgroup,0,0,null,getValueZoneItem)
            for (p <- reportProperties) {
                summaryBag(p) = summaryBag(p) + ret._2(p)
                compareSummaryBag(p) = ret._3(p)

            }
            new ReportItem(name = zone.name,id = zone.id, ret._1) |> reportItems.add
        }

        summaryBag("ctr") = getPercent(summaryBag("click"), summaryBag("impression"))
        var count = 0L;
        var graph = new util.ArrayList[GraphPoint]()
        var compareGraph = new util.ArrayList[GraphPoint]()
        // graph
        if(kind != "ctr") {
            for(zgroup <- zones) {
                    val filter = "zone."+ zgroup.zoneId +"."+ kind
                    list += getReportByZone(request.range, filter, res)
                    if (request.compareRange != null) compareList += getReportByZone(request.compareRange, filter, res)
            }
            count = list.map(c => c._1).sum

            // combined graph
            graph = combineGraph(list.map(i => i._2))
            compareGraph = combineGraph(compareList.map(i => i._2))
        } else {
            val listClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val listImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            count = listClick.map(c => c._1).sum
            for(zgroup <- zones) {
                    val filterClick = "zone."+ zgroup.zoneId +"."+"click"
                    val filterImpression = "zone."+ zgroup.zoneId +"."+ "impression"
                    listClick += getReportByZone(request.range, filterClick, res)
                    listImpression += getReportByZone(request.range, filterImpression, res)
                    if (request.compareRange != null) {
                        compareListClick += getReportByZone(request.compareRange, filterClick, res)
                        compareListImpression += getReportByZone(request.compareRange, filterImpression, res)
                    }
            }
            graph = combineGraphCTR(listClick.map(i=>i._2),listImpression.map(j=>j._2))
            compareGraph = combineGraphCTR(compareListClick.map(i => i._2),compareListImpression.map(j => j._2))
        }
        new ReportResponse(
            request.range,
            request.compareRange,
            count,
            reportItems,

            new ReportGraph(graph, compareGraph),
            buildSummary(summaryBag, compareSummaryBag))
    }
    def getReportZoneGroupItem(request: ReportRequest): ReportResponse = {
        val zonegroupId = request.filters.find(f => f.key.equalsIgnoreCase("zoneGroupId")).get.value.toInt
        val zones = env.zoneToZoneGroupService.listByReferenceId(id=zonegroupId).data
        val kind = getKind(request)
        var res = getResolution(request)
        val reportProperties = WebUtils.parseStringToArray(reportFieldWebsite) //Array("impression", "click", "ctr", "revenue")
        val list = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        val compareList = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        var reportItems = new util.ArrayList[ReportItem]()
        val summaryBag = new util.HashMap[String, Double]()
        val compareSummaryBag = new util.HashMap[String, Double]()
        for (p <- reportProperties) {
            summaryBag(p) = 0
            compareSummaryBag(p) = 0
        }
        var itemids = new ArrayBuffer[Int]()
        for(zgroup <- zones) {
            val list = getItemByZone(request.range.from,request.range.to-request.range.from,zgroup.zoneId)
            for(item <- list) {
                itemids += item
            }
        }
        itemids = itemids.distinct
        val items = env.bannerService.listByIds(itemids.toList)
        if(request.range.from ==0 && request.range.to == 0) {
            val ret = getAllTimeReport(items,request.range,res)
            request.range.from = ret._2
            request.range.to = ret._3
            res = ret._1
        }
        // items list
        for(item <- items) {
            val ret = getReportProperties(reportProperties,request,res,item,0,0,zones,getValueZoneItem)
            for (p <- reportProperties) {
                summaryBag(p) = summaryBag(p) + ret._2(p)
                compareSummaryBag(p) = ret._3(p)

            }

            new ReportItem(name = item.name,id=item.id, ret._1) |> reportItems.add
        }

        summaryBag("ctr") = getPercent(summaryBag("click"), summaryBag("impression"))
        var count = 0L;
        var graph = new util.ArrayList[GraphPoint]()
        var compareGraph = new util.ArrayList[GraphPoint]()
        // graph
        if(kind != "ctr") {
            for(itemid <- itemids) {
                for (zoneTGroup <- zones) {
                    val filter = "zone."+ zoneTGroup.zoneId +"."+ kind + ".item." + itemid
                    list += getReport(request.range, filter, res)
                    if (request.compareRange != null) compareList += getReport(request.compareRange, filter, res)
                }
            }
            count = list.map(c => c._1).sum

            // combined graph
            graph = combineGraph(list.map(i => i._2))
            compareGraph = combineGraph(compareList.map(i => i._2))
        } else {
            val listClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val listImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            count = listClick.map(c => c._1).sum
            for(itemid <- itemids) {

                for (zoneTGroup <- zones) {
                    val filterClick = "zone."+ zoneTGroup.zoneId +"."+"click.item." + itemid
                    val filterImpression = "zone."+ zoneTGroup.zoneId +"."+ "impression.item." + itemid
                    listClick += getReport(request.range, filterClick, res)
                    listImpression += getReport(request.range, filterImpression, res)
                    if (request.compareRange != null) {
                        compareListClick += getReport(request.compareRange, filterClick, res)
                        compareListImpression += getReport(request.compareRange, filterImpression, res)
                    }
                }
            }
            graph = combineGraphCTR(listClick.map(i=>i._2),listImpression.map(j=>j._2))
            compareGraph = combineGraphCTR(compareListClick.map(i => i._2),compareListImpression.map(j => j._2))
        }
        new ReportResponse(
            request.range,
            request.compareRange,
            count,
            reportItems,

            new ReportGraph(graph, compareGraph),
            buildSummary(summaryBag, compareSummaryBag))
    }
    def getReportCampaignWebsite(request: ReportRequest): ReportResponse = {
        val campId = request.filters.find(f => f.key.equalsIgnoreCase("campId")).get.value.toInt
        val camp = env.campaignService.load(id = campId)
        val kind = getKind(request)
        var res = getResolution(request)
        val reportProperties = WebUtils.parseStringToArray(reportFieldOrder) //Array("impression", "click", "ctr", "spent")
        var websiteids = Array[Int]()
        val items = env.bannerService.listByReferenceId(id = camp.id).data

        for (item <- items) {
            val list = getWebsiteByItem(request.range.from,request.range.to-request.range.from,item.id)
            websiteids ++= list;
        }
        websiteids = websiteids.distinct
        val websites = env.websiteService.listByIds(websiteids.toList)
        val list = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        val compareList = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        var reportItems = new util.ArrayList[ReportItem]()
        val summaryBag = new util.HashMap[String, Double]()
        val compareSummaryBag = new util.HashMap[String, Double]()
        for (p <- reportProperties) {
            summaryBag(p) = 0
            compareSummaryBag(p) = 0
        }

        websiteids = websiteids.distinct
        if(request.range.from ==0 && request.range.to == 0) {
            val ret = getAllTimeReport(websites,request.range,res)
            request.range.from = ret._2
            request.range.to = ret._3
            res = ret._1
        }
        for(website <- websites) {
            val ret = getReportProperties(reportProperties,request,res,website,0,0,items,getValueZoneItem)
            for (p <- reportProperties) {
                summaryBag(p) = summaryBag(p) + ret._2(p)
                compareSummaryBag(p) = ret._3(p)

            }
            new ReportItem(name = website.name,id = website.id, ret._1) |> reportItems.add
        }
        summaryBag("ctr") = getPercent(summaryBag("click"), summaryBag("impression"))
        var count = 0L;
        var graph = new util.ArrayList[GraphPoint]()
        var compareGraph = new util.ArrayList[GraphPoint]()
        // graph
        if(kind != "ctr") {
            for(website <- websites) {
                for (item <- items) {
                    val filter = "site."+ website.id +"."+ kind
                    list += getReportBySite(request.range, filter, res,item.id)
                    if (request.compareRange != null) compareList += getReportBySite(request.compareRange, filter, res,item.id)
                }
            }
            count = list.map(c => c._1).sum

            // combined graph
            graph = combineGraph(list.map(i => i._2))
            compareGraph = combineGraph(compareList.map(i => i._2))
        } else {
            val listClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val listImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            count = listClick.map(c => c._1).sum
            for(website <- websites) {
                for (item <- items) {
                    val filterClick = "site."+ website.id +".click"
                    val filterImpression = "site."+ website.id +".impression"
                    listClick += getReportBySite(request.range, filterClick, res,item.id)
                    listImpression += getReportBySite(request.range, filterImpression, res,item.id)
                    if (request.compareRange != null) {
                        compareListClick += getReportBySite(request.compareRange, filterClick, res,item.id)
                        compareListImpression += getReportBySite(request.compareRange, filterImpression, res,item.id)
                    }
                }
            }
            graph = combineGraphCTR(listClick.map(i=>i._2),listImpression.map(j=>j._2))
            compareGraph = combineGraphCTR(compareListClick.map(i => i._2),compareListImpression.map(j => j._2))
        }
        new ReportResponse(
            request.range,
            request.compareRange,
            count,
            reportItems,

            new ReportGraph(graph, compareGraph),
            buildSummary(summaryBag, compareSummaryBag))
    }
    /** *
      * Report website by item id
      * @param request
      * @return
      */
    def getReportWebsite(request: ReportRequest): ReportResponse = {
        val itemId = request.filters.find(f => f.key.equalsIgnoreCase("itemId")).get.value.toInt

        val kind = getKind(request)
        var res = getResolution(request)
        val reportProperties = WebUtils.parseStringToArray(reportFieldOrder) //Array("impression", "click", "ctr", "spent")
        val list = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        val compareList = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        var reportItems = new util.ArrayList[ReportItem]()
        val summaryBag = new util.HashMap[String, Double]()
        val compareSummaryBag = new util.HashMap[String, Double]()
        var websites = Array[Int]()
        for (p <- reportProperties) {
            summaryBag(p) = 0
            compareSummaryBag(p) = 0
        }
        websites = getWebsiteByItem(request.range.from, request.range.to - request.range.from,itemId)
        val webs = env.websiteService.listByIds(websites.toList)
        // items list
        if(request.range.from ==0 && request.range.to == 0) {
            val ret = getAllTimeReport(webs,request.range,res)
            request.range.from = ret._2
            request.range.to = ret._3
            res = ret._1
        }
        for(web <-webs) {
              //val zones = env.zoneService.listByReferenceId(id=website.id).data
            val ret = getReportProperties(reportProperties,request,res,web,0,itemId,null,getValueZoneItem)
            for (p <- reportProperties) {
                summaryBag(p) = summaryBag(p) + ret._2(p)
                compareSummaryBag(p) = ret._3(p)

            }
            new ReportItem(name = web.name,id=web.id, ret._1) |> reportItems.add
        }

        summaryBag("ctr") = getPercent(summaryBag("click"), summaryBag("impression"))
        var count = 0L;
        var graph = new util.ArrayList[GraphPoint]()
        var compareGraph = new util.ArrayList[GraphPoint]()
        // graph
        if(kind != "ctr") {
            for(web <-webs) {
                    val filter = "site."+ web.id + "." + kind
                    list += getReportBySite(request.range, filter, res,itemId)
                    if (request.compareRange != null) compareList += getReportBySite(request.compareRange, filter, res,itemId)

            }
            count = list.map(c => c._1).sum

            // combined graph
            graph = combineGraph(list.map(i => i._2))
            compareGraph = combineGraph(compareList.map(i => i._2))
        } else {
            val listClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val listImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            count = listClick.map(c => c._1).sum
            for(web <-webs) {

                    val filterClick = "site."+ web.id +".click"
                    val filterImpression = "site."+ web.id +".impression"
                    listClick += getReportBySite(request.range, filterClick, res,itemId)
                    listImpression += getReportBySite(request.range, filterImpression, res,itemId)
                    if (request.compareRange != null) {
                        compareListClick += getReportBySite(request.compareRange, filterClick, res,itemId)
                        compareListImpression += getReportBySite(request.compareRange, filterImpression, res,itemId)
                    }

            }
            graph = combineGraphCTR(listClick.map(i=>i._2),listImpression.map(j=>j._2))
            compareGraph = combineGraphCTR(compareListClick.map(i => i._2),compareListImpression.map(j => j._2))
        }
        new ReportResponse(
            request.range,
            request.compareRange,
            count,
            reportItems,
            new ReportGraph(graph, compareGraph),
            buildSummary(summaryBag, compareSummaryBag))
    }
    def getReportWebsiteZoneGroup(request: ReportRequest): ReportResponse = {

        val websiteId = request.filters.find(f => f.key.equalsIgnoreCase("websiteId")).get.value.toInt
        val zoneGroups = env.zoneGroupService.listByReferenceId(id=websiteId).data
        val kind = getKind(request)
        var res = getResolution(request)
        val reportProperties = WebUtils.parseStringToArray(reportFieldWebsite) //Array("impression", "click", "ctr", "revenue")
        val list = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        val compareList = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        var reportItems = new util.ArrayList[ReportItem]()
        val summaryBag = new util.HashMap[String, Double]()
        val compareSummaryBag = new util.HashMap[String, Double]()

        for (p <- reportProperties) {
            summaryBag(p) = 0
            compareSummaryBag(p) = 0
        }
        if(request.range.from ==0 && request.range.to == 0) {
            val ret = getAllTimeReport(zoneGroups,request.range,res)
            request.range.from = ret._2
            request.range.to = ret._3
            res = ret._1
        }
        for(zoneGroup <- zoneGroups) {
            val list = env.zoneToZoneGroupService.listByReferenceId(id=zoneGroup.id).data
            val ret = getReportProperties(reportProperties,request,res,list,0,0,null,getValueZoneItem)
            for (p <- reportProperties) {
                summaryBag(p) = summaryBag(p) + ret._2(p)
                compareSummaryBag(p) = ret._3(p)

            }
            new ReportItem(name = zoneGroup.name,id=zoneGroup.id, ret._1) |> reportItems.add
        }
        summaryBag("ctr") = getPercent(summaryBag("click"), summaryBag("impression"))
        var count = 0L;
        var graph = new util.ArrayList[GraphPoint]()
        var compareGraph = new util.ArrayList[GraphPoint]()
        // graph
        if(kind != "ctr") {
            for(zoneGroup <- zoneGroups) {
                val listZone = env.zoneToZoneGroupService.listByReferenceId(id=zoneGroup.id).data
                for (zone <- listZone) {

                        val filter = "zone."+ zone.zoneId + "." + kind
                        list += getReportByZone(request.range, filter, res)
                        if (request.compareRange != null) compareList += getReportByZone(request.compareRange, filter, res)


                }
            }
            count = list.map(c => c._1).sum

            // combined graph
            graph = combineGraph(list.map(i => i._2))
            compareGraph = combineGraph(compareList.map(i => i._2))
        } else {
            val listClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val listImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            count = listClick.map(c => c._1).sum
            for(zoneGroup <- zoneGroups) {
                val listZone = env.zoneToZoneGroupService.listByReferenceId(id=zoneGroup.id).data
                for (zone <- listZone) {
                    val filterClick = "zone."+ zone.zoneId +".click"
                    val filterImpression = "zone."+ zone.zoneId +".impression"
                    listClick += getReportByZone(request.range, filterClick, res)
                    listImpression += getReportByZone(request.range, filterImpression, res)
                    if (request.compareRange != null) {
                        compareListClick += getReportByZone(request.compareRange, filterClick, res)
                        compareListImpression += getReportByZone(request.compareRange, filterImpression, res)
                    }
                }
            }
            graph = combineGraphCTR(listClick.map(i=>i._2),listImpression.map(j=>j._2))
            compareGraph = combineGraphCTR(compareListClick.map(i => i._2),compareListImpression.map(j => j._2))
        }
        new ReportResponse(
            request.range,
            request.compareRange,
            count,
            reportItems,
            new ReportGraph(graph, compareGraph),
            buildSummary(summaryBag, compareSummaryBag))
    }
    def getReportWebsiteZone(request: ReportRequest): ReportResponse = {
        val websiteId = request.filters.find(f => f.key.equalsIgnoreCase("websiteId")).get.value.toInt
        val zones = env.zoneService.listByReferenceId(id=websiteId).data
        //val zones = env.zoneService.listByReferenceId()
        val kind = getKind(request)
        var res = getResolution(request)
        //val items = camps.flatMap(c => env.bannerDataService.listByReferenceId(id = c.id).data).toList
        val reportProperties = WebUtils.parseStringToArray(reportFieldWebsite) //Array("impression", "click", "ctr", "revenue")
        val list = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        val compareList = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        var reportItems = new util.ArrayList[ReportItem]()
        val summaryBag = new util.HashMap[String, Double]()
        val compareSummaryBag = new util.HashMap[String, Double]()
        for (p <- reportProperties) {
            summaryBag(p) = 0
            compareSummaryBag(p) = 0
        }
        if(request.range.from ==0 && request.range.to == 0) {
            val ret = getAllTimeReport(zones,request.range,res)
            request.range.from = ret._2
            request.range.to = ret._3
            res = ret._1
        }
        // items list
        var fromTime = System.currentTimeMillis()
        for (zone <- zones) {
            //val items = env.zoneToBannerService.getItemsByZoneId(zone.id).data
            val ret = getReportProperties(reportProperties,request,res,zone,0,0,null,getValueZoneItem)
            for (p <- reportProperties) {
                summaryBag(p) = summaryBag(p) + ret._2(p)
                compareSummaryBag(p) = ret._3(p)
            }
            new ReportItem(name = zone.name,id=zone.id, ret._1) |> reportItems.add
        }
        var toTime = System.currentTimeMillis()
        System.out.print("Zone by site - report report item by "+ res +": " + (toTime-fromTime))

        summaryBag("ctr") = getPercent(summaryBag("click"), summaryBag("impression"))
        var count = 0L;
        var graph = new util.ArrayList[GraphPoint]()
        var compareGraph = new util.ArrayList[GraphPoint]()
        // graph
        if(kind != "ctr") {
            fromTime = System.currentTimeMillis()
            for (zone <- zones) {
                    val filter = "zone."+ zone.id + "." + kind
                    list += getReportByZone(request.range, filter, res)
                    if (request.compareRange != null) compareList += getReportByZone(request.compareRange, filter, res)
            }
            toTime = System.currentTimeMillis()
            System.out.print("Zone by site - get data buid graph "+ res +": " + (toTime-fromTime))
            count = list.map(c => c._1).sum
            // combined graph
            fromTime = System.currentTimeMillis()
            graph = combineGraph(list.map(i => i._2))
            compareGraph = combineGraph(compareList.map(i => i._2))
            toTime = System.currentTimeMillis()
            System.out.print("Zone by site - build graph "+ res +": " + (toTime-fromTime))
        } else {
            val listClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val listImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            count = listClick.map(c => c._1).sum
            for (zone <- zones) {

                    val filterClick = "zone."+ zone.id +".click"
                    val filterImpression = "zone."+ zone.id +".impression"
                    listClick += getReportByZone(request.range, filterClick, res)
                    listImpression += getReportByZone(request.range, filterImpression, res)
                    if (request.compareRange != null) {
                        compareListClick += getReportByZone(request.compareRange, filterClick, res)
                        compareListImpression += getReportByZone(request.compareRange, filterImpression, res)
                    }


            }
            graph = combineGraphCTR(listClick.map(i=>i._2),listImpression.map(j=>j._2))
            compareGraph = combineGraphCTR(compareListClick.map(i => i._2),compareListImpression.map(j => j._2))
        }
        new ReportResponse(
            request.range,
            request.compareRange,
            count,
            reportItems,
            new ReportGraph(graph, compareGraph),
            buildSummary(summaryBag, compareSummaryBag))
    }
    def getReportWebsiteZonebyCampaign(request: ReportRequest): ReportResponse = {
        val campaignId = request.filters.find(f => f.key.equalsIgnoreCase("campId")).get.value.toInt
        val websiteId = request.filters.find(f => f.key.equalsIgnoreCase("websiteId")).get.value.toInt
        val zones = env.zoneService.listByReferenceId(id=websiteId).data
        //val zones = env.zoneService.listByReferenceId()
        val kind = getKind(request)
        var res = getResolution(request)
        //val items = camps.flatMap(c => env.bannerDataService.listByReferenceId(id = c.id).data).toList

        val reportProperties = WebUtils.parseStringToArray(reportFieldOrder) //Array("impression", "click", "ctr", "spent")

        val list = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        val compareList = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]


        var reportItems = new util.ArrayList[ReportItem]()
        val summaryBag = new util.HashMap[String, Double]()
        val compareSummaryBag = new util.HashMap[String, Double]()

        for (p <- reportProperties) {
            summaryBag(p) = 0
            compareSummaryBag(p) = 0
        }
        val itemsCampaign = env.bannerService.listByReferenceId(id=campaignId).data
        // items list
        if(request.range.from ==0 && request.range.to == 0) {
            val ret = getAllTimeReport(zones,request.range,res,filter=itemsCampaign)
            request.range.from = ret._2
            request.range.to = ret._3
            res = ret._1
        }
        for (zone <- zones) {
            val itemids = getItemByZone(request.range.from,request.range.to-request.range.from,zone.id)
            val items = env.bannerService.listByIds(itemids.toList)
            if(items.exists(a => itemsCampaign.exists( b => a.id == b.id))) {

                val ret = getReportProperties(reportProperties,request,res,zone,0,0,items,getValueZoneItem)
                for (p <- reportProperties) {
                    summaryBag(p) = summaryBag(p) + ret._2(p)
                    compareSummaryBag(p) = ret._3(p)

                }
                new ReportItem(name = zone.name,id=zone.id, ret._1) |> reportItems.add
            }
        }


        summaryBag("ctr") = getPercent(summaryBag("click"), summaryBag("impression"))
        var count = 0L;
        var graph = new util.ArrayList[GraphPoint]()
        var compareGraph = new util.ArrayList[GraphPoint]()
        // graph
        if(kind != "ctr") {
            for (zone <- zones) {
                val itemids = getItemByZone(request.range.from,request.range.to-request.range.from,zone.id)
                val items = env.bannerService.listByIds(itemids.toList)
                for(item <- items) {
                    if(item != null) {
                        if(item.campaignId == campaignId) {
                            val filter = "zone."+ zone.id + "." + kind + ".item." + item.id
                            list += getReport(request.range, filter, res)
                            if (request.compareRange != null) compareList += getReport(request.compareRange, filter, res)
                        }
                    }
                }

            }
            count = list.map(c => c._1).sum

            // combined graph
            graph = combineGraph(list.map(i => i._2))
            compareGraph = combineGraph(compareList.map(i => i._2))
        } else {
            val listClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val listImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            count = listClick.map(c => c._1).sum
            for (zone <- zones) {
                val itemids = getItemByZone(request.range.from,request.range.to-request.range.from,zone.id)
                val items = env.bannerService.listByIds(itemids.toList)
                for(item <- items) {
                    if(item!=null) {
                        if(item.campaignId == campaignId) {
                            val filterClick = "zone."+ zone.id +".click.item." + item.id
                            val filterImpression = "zone."+ zone.id +".impression.item." + item.id
                            listClick += getReport(request.range, filterClick, res)
                            listImpression += getReport(request.range, filterImpression, res)
                            if (request.compareRange != null) {
                                compareListClick += getReport(request.compareRange, filterClick, res)
                                compareListImpression += getReport(request.compareRange, filterImpression, res)
                            }
                        }
                    }
                }

            }
            graph = combineGraphCTR(listClick.map(i=>i._2),listImpression.map(j=>j._2))
            compareGraph = combineGraphCTR(compareListClick.map(i => i._2),compareListImpression.map(j => j._2))
        }
        new ReportResponse(
            request.range,
            request.compareRange,
            count,
            reportItems,
            new ReportGraph(graph, compareGraph),
            buildSummary(summaryBag, compareSummaryBag))
    }

    def getReportWebsiteZoneByItem(request: ReportRequest): ReportResponse = {
        val websiteId = request.filters.find(f => f.key.equalsIgnoreCase("websiteId")).get.value.toInt
        val itemId = request.filters.find(f => f.key.equalsIgnoreCase("itemId")).get.value.toInt
        val zones = env.zoneService.listByReferenceId(id=websiteId).data
        val kind = getKind(request)
        var res = getResolution(request)
        val reportProperties = WebUtils.parseStringToArray(reportFieldOrder) //Array("impression", "click", "ctr", "spent")
        val list = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        val compareList = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        var reportItems = new util.ArrayList[ReportItem]()
        val summaryBag = new util.HashMap[String, Double]()
        val compareSummaryBag = new util.HashMap[String, Double]()
        for (p <- reportProperties) {
            summaryBag(p) = 0
            compareSummaryBag(p) = 0
        }
        val zoneids = getZoneByItem(request.range.from,request.range.to-request.range.from,itemId)
        val zoneOfItem = env.zoneService.listByIds(zoneids.toList)
        if(request.range.from ==0 && request.range.to == 0) {
            val ret = getAllTimeReport(zones,request.range,res,filter=zoneOfItem)
            request.range.from = ret._2
            request.range.to = ret._3
            res = ret._1
        }
        // items list
        for (zone <- zones) {
            if(zoneOfItem.filter(x => x.id == zone.id).length > 0) {
                val ret = getReportProperties(reportProperties,request,res,null,zone.id,itemId,null,getValueZoneItem)
                for (p <- reportProperties) {
                    summaryBag(p) = summaryBag(p) + ret._2(p)
                    compareSummaryBag(p) = ret._3(p)

                }
                new ReportItem(name = zone.name,id=zone.id, ret._1) |> reportItems.add
            }
        }


        summaryBag("ctr") = getPercent(summaryBag("click"), summaryBag("impression"))
        var count = 0L;
        var graph = new util.ArrayList[GraphPoint]()
        var compareGraph = new util.ArrayList[GraphPoint]()
        // graph
        if(kind != "ctr") {
            for (zone <- zones) {
                if(zoneOfItem.filter(x => x.id == zone.id).length > 0) {
                    val filter = "zone."+ zone.id + "." + kind + ".item." + itemId
                    list += getReport(request.range, filter, res)
                    if (request.compareRange != null) compareList += getReport(request.compareRange, filter, res)

                }
            }
            count = list.map(c => c._1).sum

            // combined graph
            graph = combineGraph(list.map(i => i._2))
            compareGraph = combineGraph(compareList.map(i => i._2))
        } else {
            val listClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val listImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            count = listClick.map(c => c._1).sum
            for (zone <- zones) {
                if(zoneOfItem.filter(x => x.id == zone.id).length > 0) {
                    val filterClick = "zone."+ zone.id +".click.item." + itemId
                    val filterImpression = "zone."+ zone.id +".impression.item." + itemId
                    listClick += getReport(request.range, filterClick, res)
                    listImpression += getReport(request.range, filterImpression, res)
                    if (request.compareRange != null) {
                        compareListClick += getReport(request.compareRange, filterClick, res)
                        compareListImpression += getReport(request.compareRange, filterImpression, res)
                    }
                }

            }
            graph = combineGraphCTR(listClick.map(i=>i._2),listImpression.map(j=>j._2))
            compareGraph = combineGraphCTR(compareListClick.map(i => i._2),compareListImpression.map(j => j._2))
        }
        new ReportResponse(
            request.range,
            request.compareRange,
            count,
            reportItems,
            new ReportGraph(graph, compareGraph),
            buildSummary(summaryBag, compareSummaryBag))
    }
    def getReportWebsiteZoneByOrder(request: ReportRequest): ReportResponse = {
        val websiteId = request.filters.find(f => f.key.equalsIgnoreCase("websiteId")).get.value.toInt
        val orderId = request.filters.find(f => f.key.equalsIgnoreCase("orderId")).get.value.toInt
        val camps = env.campaignService.listByReferenceId(id=orderId).data
        val zones = env.zoneService.listByReferenceId(id=websiteId).data
        val kind = getKind(request)
        var res = getResolution(request)
        val reportProperties = WebUtils.parseStringToArray(reportFieldOrder) //Array("impression", "click", "ctr", "spent")
        val list = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        val compareList = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        var reportItems = new util.ArrayList[ReportItem]()
        val summaryBag = new util.HashMap[String, Double]()
        val compareSummaryBag = new util.HashMap[String, Double]()
        for (p <- reportProperties) {
            summaryBag(p) = 0
            compareSummaryBag(p) = 0
        }

        if(request.range.from ==0 && request.range.to == 0) {
            val ret = getAllTimeReport(zones,request.range,res,filter=camps)
            request.range.from = ret._2
            request.range.to = ret._3
            res = ret._1
        }

        // items list
        for (zone <- zones) {
            val itemids = getItemByZone(request.range.from,request.range.to-request.range.from,zone.id)
            val items = env.bannerService.listByIds(itemids.toList)
                val ret = getReportProperties(reportProperties,request,res,items,zone.id,0,camps,getValueZoneItem)
                for (p <- reportProperties) {
                    summaryBag(p) = summaryBag(p) + ret._2(p)
                    compareSummaryBag(p) = ret._3(p)

                }
                new ReportItem(name = zone.name,id=zone.id, ret._1) |> reportItems.add

        }
        summaryBag("ctr") = getPercent(summaryBag("click"), summaryBag("impression"))
        var count = 0L;
        var graph = new util.ArrayList[GraphPoint]()
        var compareGraph = new util.ArrayList[GraphPoint]()
        // graph
        if(kind != "ctr") {
            for (zone <- zones) {
                val itemids = getItemByZone(request.range.from,request.range.to-request.range.from,zone.id)
                val items = env.bannerService.listByIds(itemids.toList)
                for(item <- items) {
                        if(camps.filter(x => x.id == item.campaignId).length > 0) {
                            val filter = "zone."+ zone.id + "." + kind + ".item." + item.id
                            list += getReport(request.range, filter, res)
                            if (request.compareRange != null) compareList += getReport(request.compareRange, filter, res)
                        }
                }
            }
            count = list.map(c => c._1).sum

            // combined graph
            graph = combineGraph(list.map(i => i._2))
            compareGraph = combineGraph(compareList.map(i => i._2))
        } else {
            val listClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val listImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            count = listClick.map(c => c._1).sum
            for (zone <- zones) {
                val itemids = getItemByZone(request.range.from,request.range.to-request.range.from,zone.id)
                val items = env.bannerService.listByIds(itemids.toList)
                for(item <- items) {
                        if(camps.filter(x => x.id == item.campaignId).length > 0) {
                            val filterClick = "zone."+ zone.id +".click.item." + item.id
                            val filterImpression = "zone."+ zone.id +".impression.item." + item.id
                            listClick += getReport(request.range, filterClick, res)
                            listImpression += getReport(request.range, filterImpression, res)
                            if (request.compareRange != null) {
                                compareListClick += getReport(request.compareRange, filterClick, res)
                                compareListImpression += getReport(request.compareRange, filterImpression, res)
                            }
                        }
                }
            }
            graph = combineGraphCTR(listClick.map(i=>i._2),listImpression.map(j=>j._2))
            compareGraph = combineGraphCTR(compareListClick.map(i => i._2),compareListImpression.map(j => j._2))
        }
        new ReportResponse(
            request.range,
            request.compareRange,
            count,
            reportItems,
            new ReportGraph(graph, compareGraph),
            buildSummary(summaryBag, compareSummaryBag))
    }

    def getReportWebsiteItem(request: ReportRequest): ReportResponse = {

        val websiteId = request.filters.find(f => f.key.equalsIgnoreCase("websiteId")).get.value.toInt
        val website = env.websiteService.load(websiteId)
        val zones = env.zoneService.listByReferenceId(id=websiteId).data
        val kind = getKind(request)
        var res = getResolution(request)
        val reportProperties = WebUtils.parseStringToArray(reportFieldWebsite) //Array("impression", "click", "ctr", "revenue")
        val list = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        val compareList = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        var reportItems = new util.ArrayList[ReportItem]()
        val summaryBag = new util.HashMap[String, Double]()
        val compareSummaryBag = new util.HashMap[String, Double]()
        for (p <- reportProperties) {
            summaryBag(p) = 0
            compareSummaryBag(p) = 0
        }
        var itemids = getItemBySite(request.range.from,request.range.to-request.range.from,websiteId)
        val items = env.bannerService.listByIds(itemids.toList)
        if(request.range.from ==0 && request.range.to == 0) {
            val ret = getAllTimeReport(items,request.range,res)
            request.range.from = ret._2
            request.range.to = ret._3
            res = ret._1
        }
        // items list
        for(item <- items) {

                val ret = getReportProperties(reportProperties,request,res,website,0,item.id,null,getValueZoneItem)
                for (p <- reportProperties) {
                    summaryBag(p) = summaryBag(p) + ret._2(p)
                    compareSummaryBag(p) = ret._3(p)

                }
                new ReportItem(name = item.name,id=item.id, ret._1) |> reportItems.add


        }


        summaryBag("ctr") = getPercent(summaryBag("click"), summaryBag("impression"))
        var count = 0L;
        var graph = new util.ArrayList[GraphPoint]()
        var compareGraph = new util.ArrayList[GraphPoint]()
        // graph
        if(kind != "ctr") {
            for(itemid <- itemids) {
                val filter = "site."+ websiteId + "." + kind
                list += getReportBySite(request.range, filter, res,itemid)
                if (request.compareRange != null) compareList += getReportBySite(request.compareRange, filter, res,itemid)
            }
            count = list.map(c => c._1).sum

            // combined graph
            graph = combineGraph(list.map(i => i._2))
            compareGraph = combineGraph(compareList.map(i => i._2))
        } else {
            val listClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val listImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            count = listClick.map(c => c._1).sum
            for(itemid <- itemids) {


                    val filterClick = "site."+ websiteId +".click"
                    val filterImpression = "site."+ websiteId +".impression"
                    listClick += getReportBySite(request.range, filterClick, res,itemid)
                    listImpression += getReportBySite(request.range, filterImpression, res,itemid)
                    if (request.compareRange != null) {
                        compareListClick += getReportBySite(request.compareRange, filterClick, res,itemid)
                        compareListImpression += getReportBySite(request.compareRange, filterImpression, res,itemid)
                    }


            }
            graph = combineGraphCTR(listClick.map(i=>i._2),listImpression.map(j=>j._2))
            compareGraph = combineGraphCTR(compareListClick.map(i => i._2),compareListImpression.map(j => j._2))
        }
        new ReportResponse(
            request.range,
            request.compareRange,
            count,
            reportItems,
            new ReportGraph(graph, compareGraph),
            buildSummary(summaryBag, compareSummaryBag))
    }
    def getReportWebsiteCampaign(request: ReportRequest): ReportResponse = {

        val websiteId = request.filters.find(f => f.key.equalsIgnoreCase("websiteId")).get.value.toInt
        val website = env.websiteService.load(websiteId)
        val zones = env.zoneService.listByReferenceId(id=websiteId).data
        var campaignids = Array[Int]()
        val kind = getKind(request)
        var res = getResolution(request)

        val reportProperties = WebUtils.parseStringToArray(reportFieldWebsite) //Array("impression", "click", "ctr", "revenue")

        val list = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        val compareList = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]

        val items = new ArrayBuffer[Banner]()
        var reportItems = new util.ArrayList[ReportItem]()
        val summaryBag = new util.HashMap[String, Double]()
        val compareSummaryBag = new util.HashMap[String, Double]()

        for (p <- reportProperties) {
            summaryBag(p) = 0
            compareSummaryBag(p) = 0
        }

        val itemids = getItemBySite(request.range.from,request.range.to-request.range.from,websiteId)
        for(item <- itemids) {
             val banner = env.bannerService.load(item)
             if(banner != null){
                  campaignids ++= Array(banner.campaignId)
             }
        }

        campaignids = campaignids.distinct
        var camps = env.campaignService.listByIds(campaignids.toList)
        val filter = request.filters.find(f => f.key.equalsIgnoreCase("filter")).get.value
        if(filter != "all" && filter != null) {
            val campaignType = getCampaingFillterType(filter)
            camps = camps.filter(x=>x.campaignType == campaignType);
        }
        // items list
        if(request.range.from ==0 && request.range.to == 0) {
            val ret = getAllTimeReport(camps,request.range,res)
            request.range.from = ret._2
            request.range.to = ret._3
            res = ret._1
        }
        for(camp <- camps) {
            val listItem = env.bannerService.listByReferenceId(id = camp.id).data
            //val items = listItem.filter(x=>itemids.exists(y=>y==x.id))
            val ret = getReportProperties(reportProperties,request,res,website,0,0,listItem,getValueZoneItem)
            for (p <- reportProperties) {
                summaryBag(p) = summaryBag(p) + ret._2(p)
                compareSummaryBag(p) = ret._3(p)

            }
            new ReportItem(name = camp.name,id=camp.id, ret._1) |> reportItems.add
        }
        summaryBag("ctr") = getPercent(summaryBag("click"), summaryBag("impression"))
        var count = 0L;
        var graph = new util.ArrayList[GraphPoint]()
        var compareGraph = new util.ArrayList[GraphPoint]()
        // graph
        if(kind != "ctr") {
            for(camp <- camps) {
                val listItem = env.bannerService.listByReferenceId(id = camp.id).data
                val items = listItem.filter(x=>itemids.exists(y=>y==x.id))
                for(item <- items) {
                    val filter = "site."+ websiteId + "." + kind
                    list += getReportBySite(request.range, filter, res,item.id)
                    if (request.compareRange != null) compareList += getReport(request.compareRange, filter, res)
                }

            }
            count = list.map(c => c._1).sum

            // combined graph
            graph = combineGraph(list.map(i => i._2))
            compareGraph = combineGraph(compareList.map(i => i._2))
        } else {
            val listClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val listImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            count = listClick.map(c => c._1).sum
            for(camp <- camps) {
                val listItem = env.bannerService.listByReferenceId(id = camp.id).data
                val items = listItem.filter(x=>itemids.exists(y=>y==x.id))
                for(item <- items) {
                    val filterClick = "site."+ websiteId +".click"
                    val filterImpression = "site."+ websiteId +".impression"
                    listClick += getReportBySite(request.range, filterClick, res,item.id)
                    listImpression += getReportBySite(request.range, filterImpression, res,item.id)
                    if (request.compareRange != null) {
                        compareListClick += getReportBySite(request.compareRange, filterClick, res,item.id)
                        compareListImpression += getReportBySite(request.compareRange, filterImpression, res,item.id)
                    }
                }

            }
            graph = combineGraphCTR(listClick.map(i=>i._2),listImpression.map(j=>j._2))
            compareGraph = combineGraphCTR(compareListClick.map(i => i._2),compareListImpression.map(j => j._2))
        }
        new ReportResponse(
            request.range,
            request.compareRange,
            count,
            reportItems,
            new ReportGraph(graph, compareGraph),
            buildSummary(summaryBag, compareSummaryBag))
    }
    def getReportZoneCampaign(request: ReportRequest): ReportResponse = {

        val zoneId = request.filters.find(f => f.key.equalsIgnoreCase("zoneId")).get.value.toInt
        val zone = env.zoneService.load(zoneId)
        var campaignids = new ArrayBuffer[Int]
        val kind = getKind(request)
        var res = getResolution(request)
        val reportProperties = WebUtils.parseStringToArray(reportFieldWebsite) //Array("impression", "click", "ctr", "revenue")

        val list = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        val compareList = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]

        val items = new ArrayBuffer[Banner]()
        var reportItems = new util.ArrayList[ReportItem]()
        val summaryBag = new util.HashMap[String, Double]()
        val compareSummaryBag = new util.HashMap[String, Double]()

        for (p <- reportProperties) {
            summaryBag(p) = 0
            compareSummaryBag(p) = 0
        }

                val listids = getItemByZone(request.range.from,request.range.to-request.range.from,zoneId)
                for(item <- listids) {
                    val banner = env.bannerService.load(item)
                    if(banner != null)
                        campaignids ++= Array(banner.campaignId)
                }


        campaignids = campaignids.distinct
        var camps = env.campaignService.listByIds(campaignids.toList)
        val filter = request.filters.find(f => f.key.equalsIgnoreCase("filter")).get.value
        if(filter != "all" && filter != null) {
            val campaignType = getCampaingFillterType(filter)
            camps = camps.filter(x=>x.campaignType == campaignType);
        }
        // items list
        if(request.range.from ==0 && request.range.to == 0) {
            val ret = getAllTimeReport(camps,request.range,res)
            request.range.from = ret._2
            request.range.to = ret._3
            res = ret._1
        }
        for(camp <- camps) {
            val listItem = env.bannerService.listByReferenceId(id = camp.id).data
            if(listItem != null) {
                val ret = getReportProperties(reportProperties,request,res,listItem,zoneId,0,null,getValueZoneItem)
                for (p <- reportProperties) {
                    summaryBag(p) = summaryBag(p) + ret._2(p)
                    compareSummaryBag(p) = ret._3(p)

                }
                new ReportItem(name = camp.name,id=camp.id, ret._1) |> reportItems.add
            }
        }



        summaryBag("ctr") = getPercent(summaryBag("click"), summaryBag("impression"))
        var count = 0L;
        var graph = new util.ArrayList[GraphPoint]()
        var compareGraph = new util.ArrayList[GraphPoint]()
        // graph
        if(kind != "ctr") {


                for(item <- listids) {
                    val filter = "zone."+ zone.id + "." + kind + ".item." + item
                    list += getReport(request.range, filter, res)
                    if (request.compareRange != null) compareList += getReport(request.compareRange, filter, res)
                }


            count = list.map(c => c._1).sum

            // combined graph
            graph = combineGraph(list.map(i => i._2))
            compareGraph = combineGraph(compareList.map(i => i._2))
        } else {
            val listClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val listImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            count = listClick.map(c => c._1).sum


                for(item <- listids) {
                    val filterClick = "zone."+ zone.id +".click.item." + item
                    val filterImpression = "zone."+ zone.id +".impression.item." + item
                    listClick += getReport(request.range, filterClick, res)
                    listImpression += getReport(request.range, filterImpression, res)
                    if (request.compareRange != null) {
                        compareListClick += getReport(request.compareRange, filterClick, res)
                        compareListImpression += getReport(request.compareRange, filterImpression, res)
                    }
                }


            graph = combineGraphCTR(listClick.map(i=>i._2),listImpression.map(j=>j._2))
            compareGraph = combineGraphCTR(compareListClick.map(i => i._2),compareListImpression.map(j => j._2))
        }
        new ReportResponse(
            request.range,
            request.compareRange,
            count,
            reportItems,
            new ReportGraph(graph, compareGraph),
            buildSummary(summaryBag, compareSummaryBag))
    }
    def getReportZoneGroupCampaign(request: ReportRequest): ReportResponse = {

        val zonegroupId = request.filters.find(f => f.key.equalsIgnoreCase("zoneGroupId")).get.value.toInt
        val zones = env.zoneToZoneGroupService.listByReferenceId(id=zonegroupId).data
        var campaignids = new ArrayBuffer[Int]
        val kind = getKind(request)
        var res = getResolution(request)

        val reportProperties = WebUtils.parseStringToArray(reportFieldWebsite) //Array("impression", "click", "ctr", "revenue")

        val list = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        val compareList = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]

        val items = new ArrayBuffer[Banner]()
        var reportItems = new util.ArrayList[ReportItem]()
        val summaryBag = new util.HashMap[String, Double]()
        val compareSummaryBag = new util.HashMap[String, Double]()

        for (p <- reportProperties) {
            summaryBag(p) = 0
            compareSummaryBag(p) = 0
        }
        for(zgroup<-zones) {
            val zid = zgroup.zoneId
                val list = getItemByZone(request.range.from,request.range.to-request.range.from,zgroup.zoneId)
                for(item <- list) {
                    val banner = env.bannerService.load(item)
                    if(banner != null) {
                        campaignids ++= ArrayBuffer(banner.campaignId)
                    }
                }
        }
        campaignids = campaignids.distinct
        var camps = env.campaignService.listByIds(campaignids.toList)
        val filter = request.filters.find(f => f.key.equalsIgnoreCase("filter")).get.value
        if(filter != "all" && filter != null) {
            val campaignType = getCampaingFillterType(filter)
            camps = camps.filter(x=>x.campaignType == campaignType);
        }
        if(request.range.from ==0 && request.range.to == 0) {
            val ret = getAllTimeReport(camps,request.range,res)
            request.range.from = ret._2
            request.range.to = ret._3
            res = ret._1
        }
        // items list
        for(camp <- camps) {
            val listItem = env.bannerService.listByReferenceId(id = camp.id).data
            if(listItem != null) {
                val ret = getReportProperties(reportProperties,request,res,listItem,0,0,zones,getValueZoneItem)
                for (p <- reportProperties) {
                    summaryBag(p) = summaryBag(p) + ret._2(p)
                    compareSummaryBag(p) = ret._3(p)

                }
                new ReportItem(name = camp.name,id=camp.id, ret._1) |> reportItems.add
            }
        }
        summaryBag("ctr") = getPercent(summaryBag("click"), summaryBag("impression"))
        var count = 0L;
        var graph = new util.ArrayList[GraphPoint]()
        var compareGraph = new util.ArrayList[GraphPoint]()
        // graph
        if(kind != "ctr") {
            for(zgroup<-zones) {
                    val filter = "zone."+ zgroup.zoneId + "." + kind
                    list += getReportByZone(request.range, filter, res)
                    if (request.compareRange != null) compareList += getReportByZone(request.compareRange, filter, res)
            }

            count = list.map(c => c._1).sum

            // combined graph
            graph = combineGraph(list.map(i => i._2))
            compareGraph = combineGraph(compareList.map(i => i._2))
        } else {
            val listClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val listImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            count = listClick.map(c => c._1).sum
            for(zgroup<-zones) {
                    val filterClick = "zone."+ zgroup.zoneId +".click"
                    val filterImpression = "zone."+ zgroup.zoneId +".impression"
                    listClick += getReportByZone(request.range, filterClick, res)
                    listImpression += getReportByZone(request.range, filterImpression, res)
                    if (request.compareRange != null) {
                        compareListClick += getReportByZone(request.compareRange, filterClick, res)
                        compareListImpression += getReportByZone(request.compareRange, filterImpression, res)
                    }
            }

            graph = combineGraphCTR(listClick.map(i=>i._2),listImpression.map(j=>j._2))
            compareGraph = combineGraphCTR(compareListClick.map(i => i._2),compareListImpression.map(j => j._2))
        }
        new ReportResponse(
            request.range,
            request.compareRange,
            count,
            reportItems,
            new ReportGraph(graph, compareGraph),
            buildSummary(summaryBag, compareSummaryBag))
    }

    def getReportZoneItem(request: ReportRequest): ReportResponse = {

        val zoneId = request.filters.find(f => f.key.equalsIgnoreCase("zoneId")).get.value.toInt
        val zone = env.zoneService.load(zoneId)
        val kind = getKind(request)
        var res = getResolution(request)
        //val items = camps.flatMap(c => env.bannerDataService.listByReferenceId(id = c.id).data).toList

        val reportProperties : Array[String] = WebUtils.parseStringToArray(reportFieldWebsite) //Array("impression", "click", "ctr", "revenue")

        val list = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        val compareList = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        var reportItems = new util.ArrayList[ReportItem]()
        val summaryBag = new util.HashMap[String, Double]()
        val compareSummaryBag = new util.HashMap[String, Double]()


        for (p <- reportProperties) {
            summaryBag(p) = 0
            compareSummaryBag(p) = 0
        }
            val listids = getItemByZone(request.range.from,request.range.to-request.range.from,zone.id)
            val listItem = env.bannerService.listByIds(listids.toList)
            if(listItem != null) {
                if(request.range.from ==0 && request.range.to == 0) {
                    val ret = getAllTimeReport(listItem,request.range,res)
                    request.range.from = ret._2
                    request.range.to = ret._3
                    res = ret._1
                }
                    for(item <- listItem) {
                        val ret = getReportProperties(reportProperties,request,res,null,zone.id,item.id,null,getValueZoneItem)
                        for (p <- reportProperties) {
                            summaryBag(p) = summaryBag(p) + ret._2(p)
                            compareSummaryBag(p) = ret._3(p)

                        }
                        new ReportItem(name = item.name,id=item.id, ret._1) |> reportItems.add
                    }


            }
        summaryBag("ctr") = getPercent(summaryBag("click"), summaryBag("impression"))
        var count = 0L;
        var graph = new util.ArrayList[GraphPoint]()
        var compareGraph = new util.ArrayList[GraphPoint]()
        // graph
        if(kind != "ctr") {


            for(item <- listItem) {
                val filter = "zone."+ zone.id + "." + kind + ".item." + item.id
                list += getReport(request.range, filter, res)
                if (request.compareRange != null) compareList += getReport(request.compareRange, filter, res)
            }


            count = list.map(c => c._1).sum

            // combined graph
            graph = combineGraph(list.map(i => i._2))
            compareGraph = combineGraph(compareList.map(i => i._2))
        } else {
            val listClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val listImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            count = listClick.map(c => c._1).sum

            for(item <- listItem) {
                val filterClick = "zone."+ zone.id +".click.item." + item.id
                val filterImpression = "zone."+ zone.id +".impression.item." + item.id
                listClick += getReport(request.range, filterClick, res)
                listImpression += getReport(request.range, filterImpression, res)
                if (request.compareRange != null) {
                    compareListClick += getReport(request.compareRange, filterClick, res)
                    compareListImpression += getReport(request.compareRange, filterImpression, res)
                }
            }


            graph = combineGraphCTR(listClick.map(i=>i._2),listImpression.map(j=>j._2))
            compareGraph = combineGraphCTR(compareListClick.map(i => i._2),compareListImpression.map(j => j._2))
        }
        new ReportResponse(
            request.range,
            request.compareRange,
            count,
            reportItems,
            new ReportGraph(graph, compareGraph),
            buildSummary(summaryBag, compareSummaryBag))
    }
    def getReportWebsites(request: ReportRequest): ReportResponse = {

        val websites = env.websiteService.loadAll().toArray
        val reportProperties = WebUtils.parseStringToArray(reportFieldWebsite) //Array("impression", "click", "ctr", "revenue")
        val kind = getKind(request)
        var res = getResolution(request)
        val list = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
        val compareList = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]

        var reportItems = new util.ArrayList[ReportItem]()
        val summaryBag = new util.HashMap[String, Double]()
        val compareSummaryBag = new util.HashMap[String, Double]()


        for (p <- reportProperties) {
            summaryBag(p) = 0
            compareSummaryBag(p) = 0
        }
        // items list
        if(request.range.from ==0 && request.range.to == 0) {
            val ret = getAllTimeReport(websites,request.range,res)
            request.range.from = ret._2
            request.range.to = ret._3
            res = ret._1
        }

        for(website <- websites) {
            //val zones = env.zoneService.listByReferenceId(id=website.id).data
            val ret = getReportProperties(reportProperties,request,res,website,0,0,null,getValueZoneItem)
            for (p <- reportProperties) {
                summaryBag(p) = summaryBag(p) + ret._2(p)
                compareSummaryBag(p) = ret._3(p)

            }
            new ReportItem(name = website.name,id=website.id, ret._1) |> reportItems.add
        }
        summaryBag("ctr") = getPercent(summaryBag("click"), summaryBag("impression"))
        var count = 0L;
        var graph = new util.ArrayList[GraphPoint]()
        var compareGraph = new util.ArrayList[GraphPoint]()
        // graph
        if(kind != "ctr") {
            for(website <- websites) {
                val filter = "site."+ website.id + "." + kind
                list += getReportBySite(request.range, filter, res)
                if (request.compareRange != null) compareList += getReportBySite(request.compareRange, filter, res)
            }
            count = list.map(c => c._1).sum
            // combined graph
            graph = combineGraph(list.map(i => i._2))
            compareGraph = combineGraph(compareList.map(i => i._2))
        } else {
            val listClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val listImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListClick = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            val compareListImpression = new ArrayBuffer[(Long, util.ArrayList[GraphPoint])]
            count = listClick.map(c => c._1).sum
            for(website <- websites) {
                        val filterClick = "site."+ website.id +".click"
                        val filterImpression = "site."+ website.id +".impression"
                        listClick += getReport(request.range, filterClick, res)
                        listImpression += getReport(request.range, filterImpression, res)
                        if (request.compareRange != null) {
                            compareListClick += getReport(request.compareRange, filterClick, res)
                            compareListImpression += getReport(request.compareRange, filterImpression, res)
                        }
            }
            graph = combineGraphCTR(listClick.map(i=>i._2),listImpression.map(j=>j._2))
            compareGraph = combineGraphCTR(compareListClick.map(i => i._2),compareListImpression.map(j => j._2))
        }
        new ReportResponse(
            request.range,
            request.compareRange,
            count,
            reportItems,
            new ReportGraph(graph, compareGraph),
            buildSummary(summaryBag, compareSummaryBag))
    }
    def getReportProperties(reportProperties:Array[String],request:ReportRequest,res:Int,lists:Any,zoneId:Int,itemId:Int,filter:Any,getValue:(ReportRequest,Int,String,Int,Int) => (Double,Double,Double,Double,Double,Double)) : (util.ArrayList[ReportItemProperty],util.HashMap[String, Double],util.HashMap[String, Double]) = {
        val report = request.filters.find(f => f.key.equalsIgnoreCase("report")).get
        val properties = new util.ArrayList[ReportItemProperty]()
        val summaryBag = new util.HashMap[String, Double]()
        val compareSummaryBag = new util.HashMap[String, Double]()
        for (p <- reportProperties) {
            summaryBag(p) = 0
            compareSummaryBag(p) = 0
        }
        for (property <- reportProperties) {
            var value = 0d
            var compareValue = 0d
            var valueClick = 0d
            var compareValueClick = 0d
            var valueImpression = 0d
            var compareValueImpression = 0d
            if(lists == null) {
                if(zoneId > 0) {
                    if(property == "ctr") {
                        val ret = getValueZoneItem(request,res,property,zoneId,itemId)
                        valueClick += ret._3
                        valueImpression += ret._4
                        compareValueClick += ret._5
                        compareValueImpression += ret._6
                    } else {
                        val ret = getValueZoneItem(request,res,property,zoneId,itemId)
                        value += ret._1
                        compareValue += ret._2
                    }
                } else {
                    if(property == "ctr") {
                        val ret = getValueItem(request,res,property,itemId)
                        valueClick += ret._3
                        valueImpression += ret._4
                        compareValueClick += ret._5
                        compareValueImpression += ret._6
                    } else {
                        val ret = getValueItem(request,res,property,itemId)
                        value += ret._1
                        compareValue += ret._2
                    }
                }
            }
            else if(lists.isInstanceOf[Array[Zone]]) {
                val zones = lists.asInstanceOf[Array[Zone]]
                for (zone <- zones) {
                    if(itemId == 0) {

                            if(property == "ctr") {
                                val ret = getValueByZone(request,res,property,zone.id)
                                valueClick += ret._3
                                valueImpression += ret._4
                                compareValueClick += ret._5
                                compareValueImpression += ret._6
                            } else {
                                val ret = getValueByZone(request,res,property,zone.id)
                                value += ret._1
                                compareValue += ret._2
                            }

                    } else {
                        if(property == "ctr") {
                            val ret = getValueZoneItem(request,res,property,zone.id,itemId)
                            valueClick += ret._3
                            valueImpression += ret._4
                            compareValueClick += ret._5
                            compareValueImpression += ret._6
                        } else {
                            val ret = getValueZoneItem(request,res,property,zone.id,itemId)
                            value += ret._1
                            compareValue += ret._2
                        }
                    }

                }
            } else if(lists.isInstanceOf[Array[Banner]]) {
                val listItem = lists.asInstanceOf[Array[Banner]]
                if(zoneId > 0) {
                    for(item <- listItem) {
                        if(property == "ctr") {
                            val ret = getValueZoneItem(request,res,property,zoneId,item.id)
                            valueClick += ret._3
                            valueImpression += ret._4
                            compareValueClick += ret._5
                            compareValueImpression += ret._6
                        } else {
                            val ret = getValueZoneItem(request,res,property,zoneId,item.id)
                            value += ret._1
                            compareValue += ret._2
                        }
                    }
                } else {
                    if(filter.isInstanceOf[Int]) {
                        val websiteId = filter.asInstanceOf[Int]
                        for(item <- listItem) {
                            if(property == "ctr") {
                                val ret = getValueItemBySite(request,res,property,item.id,websiteId)
                                valueClick += ret._3
                                valueImpression += ret._4
                                compareValueClick += ret._5
                                compareValueImpression += ret._6
                            } else {
                                val ret = getValueItemBySite(request,res,property,item.id,websiteId)
                                value += ret._1
                                compareValue += ret._2
                            }
                        }
                    } else if(filter.isInstanceOf[Array[ZoneToZoneGroup]]) {
                        val zones = filter.asInstanceOf[Array[ZoneToZoneGroup]]
                        for(zone <- zones) {
                            for(item <- listItem) {
                                if(property == "ctr") {
                                    val ret = getValueZoneItem(request,res,property,zone.zoneId,item.id)
                                    valueClick += ret._3
                                    valueImpression += ret._4
                                    compareValueClick += ret._5
                                    compareValueImpression += ret._6
                                } else {
                                    val ret = getValueZoneItem(request,res,property,zone.zoneId,item.id)
                                    value += ret._1
                                    compareValue += ret._2
                                }
                            }
                        }
                    }
                }

            }
            else if(lists.isInstanceOf[Array[ZoneToBanner]]) {
                val items = lists.asInstanceOf[Array[ZoneToBanner]]
                if(zoneId > 0) {
                    for (item <- items) {
                        if(property == "ctr") {
                            val ret = getValueZoneItem(request,res,property,zoneId,item.itemId)
                            valueClick += ret._3
                            valueImpression += ret._4
                            compareValueClick += ret._5
                            compareValueImpression += ret._6
                        } else {
                            val ret = getValueZoneItem(request,res,property,zoneId,item.itemId)
                            value += ret._1
                            compareValue += ret._2
                        }
                    }
                } else {
                    if(filter != null) {
                        val camps = filter.asInstanceOf[Array[Campaign]]
                        for(zoneToBanner <- items) {
                            val item = env.bannerService.load(zoneToBanner.itemId)
                            if(item != null) {
                                if(camps.filter(x => x.id == item.campaignId).length > 0) {
                                    if(property == "ctr") {
                                        val ret = getValueZoneItem(request,res,property,zoneId,item.id)
                                        valueClick += ret._3
                                        valueImpression += ret._4
                                        compareValueClick += ret._5
                                        compareValueImpression += ret._6
                                    } else {
                                        val ret = getValueZoneItem(request,res,property,zoneId,item.id)
                                        value += ret._1
                                        compareValue += ret._2
                                    }
                                }
                            }
                        }
                    } else {
                        for(zoneToBanner <- items) {
                            val item = env.bannerService.load(zoneToBanner.itemId)
                            if(item != null) {
                                    if(property == "ctr") {
                                        val ret = getValueZoneItem(request,res,property,zoneToBanner.zoneId,item.id)
                                        valueClick += ret._3
                                        valueImpression += ret._4
                                        compareValueClick += ret._5
                                        compareValueImpression += ret._6
                                    } else {
                                        val ret = getValueZoneItem(request,res,property,zoneToBanner.zoneId,item.id)
                                        value += ret._1
                                        compareValue += ret._2
                                    }
                            }
                        }
                    }
                }
            } else if(lists.isInstanceOf[Array[ZoneToZoneGroup]]) {
                val list = lists.asInstanceOf[Array[ZoneToZoneGroup]]
                for (zoneToGroup <- list) {
                    val zone = env.zoneService.load(zoneToGroup.zoneId)
                        if(property == "ctr") {
                            val ret = getValueByZone(request,res,property,zone.id)
                            valueClick += ret._3
                            valueImpression += ret._4
                            compareValueClick += ret._5
                            compareValueImpression += ret._6
                        } else {
                            val ret = getValueByZone(request,res,property,zone.id)
                            value += ret._1
                            compareValue += ret._2
                        }

                }
            } else if(lists.isInstanceOf[Array[Campaign]]) {
                val campaigns = lists.asInstanceOf[Array[Campaign]]
                for(campaign <- campaigns) {
                    val items = env.bannerService.listByReferenceId(id=campaign.id).data
                    for (item <- items) {
                        if(property == "ctr") {
                            val ret = getValueItem(request,res,property,item.id)
                            valueClick += ret._3
                            valueImpression += ret._4
                            compareValueClick += ret._5
                            compareValueImpression += ret._6
                        } else {
                            val ret = getValueItem(request,res,property,item.id)
                            value += ret._1
                            compareValue += ret._2
                        }
                    }
                }
            } else if(lists.isInstanceOf[Campaign]) {
                val campaign = lists.asInstanceOf[Campaign]

                    val items = env.bannerService.listByReferenceId(id=campaign.id).data
                    for (item <- items) {
                        if(property == "ctr") {
                            val ret = getValueItem(request,res,property,item.id)
                            valueClick += ret._3
                            valueImpression += ret._4
                            compareValueClick += ret._5
                            compareValueImpression += ret._6
                        }  else {
                            val ret = getValueItem(request,res,property,item.id)
                            value += ret._1
                            compareValue += ret._2
                        }
                    }

            } else if(lists.isInstanceOf[Website]) {
                val website = lists.asInstanceOf[Website]
                if(itemId > 0) {
                    if(property == "ctr") {
                        val ret = getValueBySite(request,res,property,website.id,itemId)
                        valueClick += ret._3
                        valueImpression += ret._4
                        compareValueClick += ret._5
                        compareValueImpression += ret._6
                    } else {
                        val ret = getValueBySite(request,res,property,website.id,itemId)
                        value += ret._1
                        compareValue += ret._2
                    }
                } else {

                    if(filter.isInstanceOf[Array[Banner]]) {
                        val items = filter.asInstanceOf[Array[Banner]]
                        for(item <- items) {
                            if(property == "ctr") {
                                val ret = getValueBySite(request,res,property,website.id,item.id)
                                valueClick += ret._3
                                valueImpression += ret._4
                                compareValueClick += ret._5
                                compareValueImpression += ret._6
                            } else {
                                val ret = getValueBySite(request,res,property,website.id,item.id)
                                value += ret._1
                                compareValue += ret._2
                            }
                        }
                    } else {
                        if(property == "ctr") {
                            val ret = getValueBySite(request,res,property,website.id)
                            valueClick += ret._3
                            valueImpression += ret._4
                            compareValueClick += ret._5
                            compareValueImpression += ret._6
                        } else {
                            val ret = getValueBySite(request,res,property,website.id)
                            value += ret._1
                            compareValue += ret._2
                        }
                    }
                }
            }else if(lists.isInstanceOf[Zone]) {
                val zone = lists.asInstanceOf[Zone]

                    if(itemId == 0) {
                        if(filter.isInstanceOf[Array[Banner]]) {
                            val banners = filter.asInstanceOf[Array[Banner]]
                            for(banner <- banners) {
                                if(property == "ctr") {
                                    val ret = getValueZoneItem(request,res,property,zone.id,banner.id)
                                    valueClick += ret._3
                                    valueImpression += ret._4
                                    compareValueClick += ret._5
                                    compareValueImpression += ret._6
                                } else {
                                    val ret = getValueZoneItem(request,res,property,zone.id,banner.id)
                                    value += ret._1
                                    compareValue += ret._2
                                }
                            }
                        } else {
                            if(property == "ctr") {
                                val ret = getValueByZone(request,res,property,zone.id)
                                valueClick += ret._3
                                valueImpression += ret._4
                                compareValueClick += ret._5
                                compareValueImpression += ret._6
                            } else {
                                val ret = getValueByZone(request,res,property,zone.id)
                                value += ret._1
                                compareValue += ret._2
                            }
                        }
                    } else {
                        if(property == "ctr") {
                            val ret = getValueZoneItem(request,res,property,zone.id,itemId)
                            valueClick += ret._3
                            valueImpression += ret._4
                            compareValueClick += ret._5
                            compareValueImpression += ret._6
                        } else {
                            val ret = getValueZoneItem(request,res,property,zone.id,itemId)
                            value += ret._1
                            compareValue += ret._2
                        }
                    }


            } else if(lists.isInstanceOf[Banner]) {
                val banner = lists.asInstanceOf[Banner]
                if(zoneId == 0) {
                    if(filter.isInstanceOf[Int]) {
                        val websiteId =  filter.asInstanceOf[Int]
                        if(property == "ctr") {
                            val ret = getValueBySite(request,res,property,websiteId,banner.id)
                            valueClick += ret._3
                            valueImpression += ret._4
                            compareValueClick += ret._5
                            compareValueImpression += ret._6
                        } else {
                            val ret = getValueBySite(request,res,property,websiteId,banner.id)
                            value += ret._1
                            compareValue += ret._2
                        }
                    } else if(filter.isInstanceOf[Array[ZoneToZoneGroup]]) {
                        val zones =  filter.asInstanceOf[Array[ZoneToZoneGroup]]
                        for(zone <- zones) {
                            if(property == "ctr") {
                                val ret = getValueZoneItem(request,res,property,zone.zoneId,banner.id)
                                valueClick += ret._3
                                valueImpression += ret._4
                                compareValueClick += ret._5
                                compareValueImpression += ret._6
                            } else {
                                val ret = getValueZoneItem(request,res,property,zone.zoneId,banner.id)
                                value += ret._1
                                compareValue += ret._2
                            }
                        }
                    }
                } else {
                    if(property == "ctr") {
                        val ret = getValueZoneItem(request,res,property,zoneId,banner.id)
                        valueClick += ret._3
                        valueImpression += ret._4
                        compareValueClick += ret._5
                        compareValueImpression += ret._6
                    } else {
                        val ret = getValueZoneItem(request,res,property,zoneId,banner.id)
                        value += ret._1
                        compareValue += ret._2
                    }
                }
            }else if(lists.isInstanceOf[ZoneToZoneGroup]) {
                val zoneTGroup = lists.asInstanceOf[ZoneToZoneGroup]

                    val zone = env.zoneService.load(zoneTGroup.zoneId)
                    if(property == "ctr") {
                        val ret = getValueByZone(request,res,property,zone.id)
                        valueClick += ret._3
                        valueImpression += ret._4
                        compareValueClick += ret._5
                        compareValueImpression += ret._6
                    } else {
                        val ret = getValueByZone(request,res,property,zone.id)
                        value += ret._1
                        compareValue += ret._2
                    }


            }

            if(property == "ctr") {
                value = getPercent(valueClick,valueImpression)
                compareValue = getPercent(compareValueClick,compareValueImpression)
            }
            val p = new ReportItemProperty(property, value, compareValue, getPercent(value, compareValue) - 100)
            properties.add(p)
            summaryBag(property) = summaryBag(property) + value
            compareSummaryBag(property) = compareSummaryBag(property) + compareValue
        }
        (properties,summaryBag,compareSummaryBag)
    }
    def addCtr(properties: util.ArrayList[ReportItemProperty], item: Banner, reportItems: util.ArrayList[ReportItem]): Boolean = {
        val click = properties.find(p => p.label == "click").get
        val impression = properties.find(p => p.label == "impression").get

        val ctrValue = getPercent(click.value, impression.value)
        val ctrCompareValue = getPercent(click.compareValue, impression.compareValue)

        new ReportItemProperty("ctr", ctrValue, ctrCompareValue, getPercent(ctrValue, ctrCompareValue) - 100) |> properties.add

        new ReportItem(name = item.name,id=item.id, properties) |> reportItems.add
    }
    def addZoneCtr(properties: util.ArrayList[ReportItemProperty], item: Zone, reportItems: util.ArrayList[ReportItem]): Boolean = {
        val click = properties.find(p => p.label == "click").get
        val impression = properties.find(p => p.label == "impression").get

        val ctrValue = getPercent(click.value, impression.value)
        val ctrCompareValue = getPercent(click.compareValue, impression.compareValue)

        new ReportItemProperty("ctr", ctrValue, ctrCompareValue, getPercent(ctrValue, ctrCompareValue) - 100) |> properties.add

        new ReportItem(name = item.name,id=item.id, properties) |> reportItems.add
    }
    def getResolution(request:ReportRequest):Int = {
        val res = request.filters.find(f => f.key.equalsIgnoreCase("resolution")) match {
            case Some(f) => f.value match {
                case "day" => Resolution.Day
                case "week" => Resolution.Week
                case "month" => Resolution.Month
                case _ => Resolution.Hour
            }
            case None => Resolution.Hour
        }
        res
    }
    def getKind(request:ReportRequest):String = {
        val kind = request.filters.find(f => f.key.equalsIgnoreCase("kind")) match {
            case Some(f) => f.value
            case None => "impression"
        }
        kind
    }
    def getValueItem(request:ReportRequest,res:Int,property:String,itemid:Int): (Double,Double,Double,Double,Double,Double) = {
        var value = 0d
        var compareValue = 0d
        var valueClick = 0d
        var valueImpression = 0d
        var compareValueClick = 0d
        var compareValueImpression = 0d
        if(property == "ctr") {
            val filterClick = "click.item." + itemid
            val filterImpression = "impression.item." + itemid
            valueClick = getReport(request.range, filterClick, res)._1
            valueImpression = getReport(request.range, filterImpression, res)._1
            compareValueClick = request.compareRange match {
                case null => 0
                case _ => getReport(request.compareRange, filterClick, res)._1
            }
            compareValueImpression = request.compareRange match {
                case null => 0
                case _ => getReport(request.compareRange, filterImpression, res)._1
            }
        } else {
            val filter = property + ".item." + itemid
            value = getReport(request.range, filter, res)._1
            compareValue = request.compareRange match {
                case null => 0
                case _ => getReport(request.compareRange, filter, res)._1
            }
        }

        (value,compareValue,valueClick,valueImpression,compareValueClick,compareValueImpression)
    }
    def getValueItemBySite(request:ReportRequest,res:Int,property:String,itemId:Int,websiteid:Int): (Double,Double,Double,Double,Double,Double) = {

        var value = 0d
        var compareValue = 0d
        var valueClick = 0d
        var valueImpression = 0d
        var compareValueClick = 0d
        var compareValueImpression = 0d
        if(property == "ctr") {
            val filterClick = "site."+ websiteid + ".click"
            val filterImpression = "site."+ websiteid + ".impression"
            valueClick = getReportBySite(request.range, filterClick, res,itemId)._1
            valueImpression = getReportBySite(request.range, filterImpression, res,itemId)._1

            val cValueClick = request.compareRange match {
                case null => 0
                case _ => getReportBySite(request.compareRange, filterClick, res,itemId)._1
            }
            val cValueImpression = request.compareRange match {
                case null => 0
                case _ => getReportBySite(request.compareRange, filterImpression, res,itemId)._1
            }
            compareValueClick = cValueClick
            compareValueImpression = cValueImpression
        } else {
            val filter = "site."+ websiteid + "." + property
            value = getReportBySite(request.range, filter, res,itemId)._1

            compareValue = request.compareRange match {
                case null => 0
                case _ => getReportBySite(request.compareRange, filter, res,itemId)._1
            }
        }
        (value,compareValue,valueClick,valueImpression,compareValueClick,compareValueImpression)
    }
    def getValueItemByZone(request:ReportRequest,res:Int,property:String,itemId:Int,zoneId:Int): (Double,Double,Double,Double,Double,Double) = {

        var value = 0d
        var compareValue = 0d
        var valueClick = 0d
        var valueImpression = 0d
        var compareValueClick = 0d
        var compareValueImpression = 0d
        if(property == "ctr") {
            val filterClick = "zone."+ zoneId + ".click"
            val filterImpression = "zone."+ zoneId + ".impression"
            valueClick = getReportByZone(request.range, filterClick, res,itemId)._1
            valueImpression = getReportByZone(request.range, filterImpression, res,itemId)._1

            val cValueClick = request.compareRange match {
                case null => 0
                case _ => getReportByZone(request.compareRange, filterClick, res,itemId)._1
            }
            val cValueImpression = request.compareRange match {
                case null => 0
                case _ => getReportByZone(request.compareRange, filterImpression, res,itemId)._1
            }
            compareValueClick = cValueClick
            compareValueImpression = cValueImpression
        } else {
            val filter = "zone."+ zoneId + "." + property
            value = getReportByZone(request.range, filter, res,itemId)._1

            compareValue = request.compareRange match {
                case null => 0
                case _ => getReportByZone(request.compareRange, filter, res,itemId)._1
            }
        }
        (value,compareValue,valueClick,valueImpression,compareValueClick,compareValueImpression)
    }
    def getValueZoneItem(request:ReportRequest,res:Int,property:String,zoneid:Int,itemid:Int): (Double,Double,Double,Double,Double,Double) = {
        //val zone = env.zoneService.load(zoneid)
        var value = 0d
        var compareValue = 0d
        var valueClick = 0d
        var valueImpression = 0d
        var compareValueClick = 0d
        var compareValueImpression = 0d
        if(property == "ctr") {
            val filterClick = "zone."+ zoneid + ".click.item." + itemid
            val filterImpression = "zone."+ zoneid + ".impression.item." + itemid
            valueClick = getReport(request.range, filterClick, res)._1
            valueImpression = getReport(request.range, filterImpression, res)._1

            val cValueClick = request.compareRange match {
                case null => 0
                case _ => getReport(request.compareRange, filterClick, res)._1
            }
            val cValueImpression = request.compareRange match {
                case null => 0
                case _ => getReport(request.compareRange, filterImpression, res)._1
            }
            compareValueClick = cValueClick
            compareValueImpression = cValueImpression
        } /*else if(property == "revenue") {
            val filter = "zone."+ zone.id + ".impression.item." + itemid
            value = (getReport(request.range, filter, res)._1/1000)*zone.minCPM
            val cValue = request.compareRange match {
                case null => 0
                case _ => (getReport(request.compareRange, filter, res)._1/1000)*zone.minCPM
            }
            compareValue = cValue

        } */else {
            val filter = "zone."+ zoneid + "." + property + ".item." + itemid
            value = getReport(request.range, filter, res)._1

            compareValue = request.compareRange match {
                case null => 0
                case _ => getReport(request.compareRange, filter, res)._1
            }
        }
        (value,compareValue,valueClick,valueImpression,compareValueClick,compareValueImpression)
    }
    def getValueByZone(request:ReportRequest,res:Int,property:String,zoneid:Int): (Double,Double,Double,Double,Double,Double) = {
        //val zone = env.zoneService.load(zoneid)
        var value = 0d
        var compareValue = 0d
        var valueClick = 0d
        var valueImpression = 0d
        var compareValueClick = 0d
        var compareValueImpression = 0d
        if(property == "ctr") {
            val filterClick = "zone."+ zoneid + ".click"
            val filterImpression = "zone."+ zoneid + ".impression"
            valueClick = getReport(request.range, filterClick, res)._1
            valueImpression = getReport(request.range, filterImpression, res)._1

            val cValueClick = request.compareRange match {
                case null => 0
                case _ => getReport(request.compareRange, filterClick, res)._1
            }
            val cValueImpression = request.compareRange match {
                case null => 0
                case _ => getReport(request.compareRange, filterImpression, res)._1
            }
            compareValueClick = cValueClick
            compareValueImpression = cValueImpression
        }/* else if(property == "revenue") {
            val filter = "zone."+ zone.id + ".impression"
            value = (getReportByZone(request.range, filter, res)._1/1000)*zone.minCPM
            val cValue = request.compareRange match {
                case null => 0
                case _ => (getReportByZone(request.compareRange, filter, res)._1/1000)*zone.minCPM
            }
            compareValue = cValue

        } */else {
            val filter = "zone."+ zoneid + "." + property
            value = getReportByZone(request.range, filter, res)._1

            compareValue = request.compareRange match {
                case null => 0
                case _ => getReportByZone(request.compareRange, filter, res)._1
            }
        }
        (value,compareValue,valueClick,valueImpression,compareValueClick,compareValueImpression)
    }
    def getValueBySite(request:ReportRequest,res:Int,property:String,siteid:Int,itemId:Int=0): (Double,Double,Double,Double,Double,Double) = {

        var value = 0d
        var compareValue = 0d
        var valueClick = 0d
        var valueImpression = 0d
        var compareValueClick = 0d
        var compareValueImpression = 0d
        if(property == "ctr") {
            val filterClick = "site."+ siteid + ".click"
            val filterImpression = "site."+ siteid + ".impression"
            valueClick = getReportBySite(request.range, filterClick, res,itemId)._1
            valueImpression = getReportBySite(request.range, filterImpression, res,itemId)._1

            val cValueClick = request.compareRange match {
                case null => 0
                case _ => getReportBySite(request.compareRange, filterClick, res,itemId)._1
            }
            val cValueImpression = request.compareRange match {
                case null => 0
                case _ => getReportBySite(request.compareRange, filterImpression, res,itemId)._1
            }
            compareValueClick = cValueClick
            compareValueImpression = cValueImpression
        } else {
            val filter = "site."+ siteid + "." + property
            value = getReportBySite(request.range, filter, res,itemId)._1

            compareValue = request.compareRange match {
                case null => 0
                case _ => getReportBySite(request.compareRange, filter, res,itemId)._1
            }
        }
        (value,compareValue,valueClick,valueImpression,compareValueClick,compareValueImpression)
    }
    def addWebsiteCtr(properties: util.ArrayList[ReportItemProperty], item: Website, reportItems: util.ArrayList[ReportItem]): Boolean = {
        val click = properties.find(p => p.label == "click").get
        val impression = properties.find(p => p.label == "impression").get

        val ctrValue = getPercent(click.value, impression.value)
        val ctrCompareValue = getPercent(click.compareValue, impression.compareValue)

        new ReportItemProperty("ctr", ctrValue, ctrCompareValue, getPercent(ctrValue, ctrCompareValue) - 100) |> properties.add

        new ReportItem(name = item.name,id=item.id, properties) |> reportItems.add
    }
    def addCampaignCtr(request: ReportRequest,res:Int,reportProperties:Array[String],items: Array[Banner],campaign:Campaign, reportItems: util.ArrayList[ReportItem]): Boolean = {
        val summaryBag = new util.HashMap[String, Double]()
        val compareSummaryBag = new util.HashMap[String, Double]()
        for (p <- reportProperties) {
            summaryBag(p) = 0
            compareSummaryBag(p) = 0
        }
        val properties = new util.ArrayList[ReportItemProperty]()
        for (item <- items) {
            for (property <- reportProperties) {
                val filter = property + ".item." + item.id
                val value = getReport(request.range, filter, res)._1
                val compareValue = request.compareRange match {
                    case null => 0
                    case _ => getReport(request.compareRange, filter, res)._1
                }
                summaryBag(property) = summaryBag(property) + value
                compareSummaryBag(property) = compareSummaryBag(property) + compareValue
            }
        }
        for (property <- reportProperties) {
            if(property == "ctr") {
                summaryBag("ctr") = getPercent(summaryBag("click"),summaryBag("impression"))
            }
            properties.add(new ReportItemProperty(property,summaryBag(property),compareSummaryBag(property),getChangeValue(summaryBag(property),compareSummaryBag(property))))
        }
        new ReportItem(name = campaign.name,id=campaign.id, properties) |> reportItems.add
    }
    def addOrderCtr(request: ReportRequest,res:Int,reportProperties:Array[String],items: ArrayBuffer[Banner],order:Order, reportItems: util.ArrayList[ReportItem]): Boolean = {
        val summaryBag = new util.HashMap[String, Double]()
        val compareSummaryBag = new util.HashMap[String, Double]()
        for (p <- reportProperties) {
            summaryBag(p) = 0
            compareSummaryBag(p) = 0
        }
        val properties = new util.ArrayList[ReportItemProperty]()
        for (item <- items) {
            for (property <- reportProperties) {
                val filter = property + ".item." + item.id
                val value = getReport(request.range, filter, res)._1
                val compareValue = request.compareRange match {
                    case null => 0
                    case _ => getReport(request.compareRange, filter, res)._1
                }
                summaryBag(property) = summaryBag(property) + value
                compareSummaryBag(property) = compareSummaryBag(property) + compareValue
            }
        }
        for (property <- reportProperties) {
            if(property == "ctr") {
                summaryBag("ctr") = getPercent(summaryBag("click"),summaryBag("impression"))
            }
            properties.add(new ReportItemProperty(property,summaryBag(property),compareSummaryBag(property),getChangeValue(summaryBag(property),compareSummaryBag(property))))
        }
        new ReportItem(name = order.name,id=order.id, properties) |> reportItems.add
    }

    def combineGraph(list: ArrayBuffer[util.ArrayList[GraphPoint]], avg: Boolean = false): util.ArrayList[GraphPoint] = {
        if (list.size == 0) return null

        val count = list(0).size()
        val r = new util.ArrayList[GraphPoint]()
        for (i <- 0 until count) {
            var value = 0d
            val date = list(0)(i).date
            for (subList <- list) {
                if(subList.length > i) {
                    value += subList(i).value
                }
            }
            r.add(new GraphPoint(value, date))
        }
        r
    }
    def combineGraphCTR(listClick: ArrayBuffer[util.ArrayList[GraphPoint]],listImpression: ArrayBuffer[util.ArrayList[GraphPoint]], avg: Boolean = false): util.ArrayList[GraphPoint] = {
        if (listClick.size == 0) return null

        val count = listClick(0).size()
        val r = new util.ArrayList[GraphPoint]()
        for (i <- 0 until count) {
            var value = 0d
            var valueClick = 0d
            var valueImpression = 0d
            val date = listClick(0)(i).date
            for (subList <- listClick) {
                if(subList.length > i) {
                    valueClick += subList(i).value
                }
            }
            for (subList <- listImpression) {
                if(subList.length > i) {
                    if(subList.length > i) {
                        valueImpression += subList(i).value
                    }
                }
            }
            value = getPercent(valueClick,valueImpression);
            r.add(new GraphPoint(value, date))
        }
        r
    }

    def getPercent(x: Double, y: Double): Double = {
        if (y == 0) return 0
        return x * 100d / y
    }
    def getChangeValue(x:Double,y:Double): Double = {
        return (x-y)/y*100
    }
    def getReport(range: Range, filter: String, res: Int = Resolution.Hour): (Long, util.ArrayList[GraphPoint]) = {

        val points = new util.ArrayList[GraphPoint]()

        res match {
            case Resolution.Hour => {
                val part = report(range.from, range.to - range.from , filter)
                //System.out.println("size:"+part._2.size)
                val length = part._2.size
                for (item <- 0 until length) {

                    points.add(new GraphPoint(part._2(item), item.toLong * 3600L * 1000L + range.from))
                    
                    //System.out.println("Time:"+item * 3600 * 1000)
                }
                (part._1, points)
            }
            case _ => {
                val from = range.from
                val part = report(from, range.to - range.from , filter)
                var value = 0d

                var time = from
                var current = 0L
                var currentTime = 0L
                var span = 0
                var lastTime = 0L
                for (i <- 0 until part._2.size) {
                    val d = mapTo(time, res)
                    if (d != current) {
                        if (span > 0) {
                            points.add(new GraphPoint(value, time - (24*3600000)))
                            currentTime = time
                            lastTime = time
                        }

                        current = d
                        span = 0
                        value = 0
                    }

                    span += 1
                    value += part._2(i)
                    time += (3600L * 1000L)


                }

                if (span > 0) {
                    //if(lastTime != currentTime) {
                    val sub = (span*3600000)

                    points.add(new GraphPoint(value, time-sub))
                    /*
                    }
                    else {
                        if(points.size() > 0) {
                            points.get(points.size()-1).value += value
                        } else {
                            points.add(new GraphPoint(value, time))
                        }
                    }*/
                }
                (part._1, points)
            }
        }


    }
    def getReportByZone(range: Range, filter: String, res: Int = Resolution.Hour,itemId:Int=0): (Long, util.ArrayList[GraphPoint]) = {
        val points = new util.ArrayList[GraphPoint]()
        res match {
            case Resolution.Hour => {
                val part = reportByZone(range.from, range.to - range.from , filter,itemId)
                //System.out.println("size:"+part._2.size)
                val length = part._2.size
                for (item <- 0 until length) {

                    points.add(new GraphPoint(part._2(item), item.toLong * 3600L * 1000L + range.from))

                    //System.out.println("Time:"+item * 3600 * 1000)
                }
                (part._1, points)
            }
            case _ => {
                val from = range.from
                val part = reportByZone(from, range.to - range.from , filter,itemId)
                var value = 0d

                var time = from
                var current = 0L
                var currentTime = 0L
                var span = 0
                var lastTime = 0L
                for (i <- 0 until part._2.size) {
                    val d = mapTo(time, res)
                    if (d != current) {
                        if (span > 0) {
                            points.add(new GraphPoint(value, time - (24*3600000)))
                            currentTime = time
                            lastTime = time
                        }

                        current = d
                        span = 0
                        value = 0
                    }

                    span += 1
                    value += part._2(i)
                    time += (3600L * 1000L)


                }

                if (span > 0) {
                    //if(lastTime != currentTime) {
                    val sub = (span*3600000)

                    points.add(new GraphPoint(value, time-sub))
                    /*
                    }
                    else {
                        if(points.size() > 0) {
                            points.get(points.size()-1).value += value
                        } else {
                            points.add(new GraphPoint(value, time))
                        }
                    }*/
                }
                (part._1, points)
            }
        }


    }
    def getReportBySite(range: Range, filter: String, res: Int = Resolution.Hour,itemId:Int=0): (Long, util.ArrayList[GraphPoint]) = {

        val points = new util.ArrayList[GraphPoint]()

        res match {
            case Resolution.Hour => {
                val part = reportBySite(range.from, range.to - range.from , filter,itemId)
                //System.out.println("size:"+part._2.size)
                val length = part._2.size
                for (item <- 0 until length) {

                    points.add(new GraphPoint(part._2(item), item.toLong * 3600L * 1000L + range.from))

                    //System.out.println("Time:"+item * 3600 * 1000)
                }
                (part._1, points)
            }
            case _ => {
                val from = range.from
                val part = reportBySite(from, range.to - range.from , filter,itemId)
                var value = 0d

                var time = from
                var current = 0L
                var currentTime = 0L
                var span = 0
                var lastTime = 0L
                for (i <- 0 until part._2.size) {
                    val d = mapTo(time, res)
                    if (d != current) {
                        if (span > 0) {
                            points.add(new GraphPoint(value, time - (24*3600000)))
                            currentTime = time
                            lastTime = time
                        }

                        current = d
                        span = 0
                        value = 0
                    }

                    span += 1
                    value += part._2(i)
                    time += (3600L * 1000L)


                }

                if (span > 0) {
                    //if(lastTime != currentTime) {
                    val sub = (span*3600000)

                    points.add(new GraphPoint(value, time-sub))
                    /*
                    }
                    else {
                        if(points.size() > 0) {
                            points.get(points.size()-1).value += value
                        } else {
                            points.add(new GraphPoint(value, time))
                        }
                    }*/
                }
                (part._1, points)
            }
        }


    }

    def getAllTimeReport(lists:Any,range: Range, res: Int = Resolution.Hour,itemId:Int = 0,zoneId:Int=0,filter:Any = null):(Int,Long,Long) = {
        var min = (0,0L,0L)
        if(lists.isInstanceOf[Array[Website]]) {
            val list = lists.asInstanceOf[Array[Website]]
            for(website <- list) {
                val zones = env.zoneService.listByReferenceId(id=website.id).data
                for(zone <- zones) {
                    val itemids = env.newBookingService.getItemsByZoneId(zone.id).data
                    for(zonebanner <- itemids) {
                        min = getMinReport(zone.id,zonebanner.itemId,min,range,res)
                    }
                }
            }
        }else if(lists.isInstanceOf[Array[Campaign]]) {
            val list = lists.asInstanceOf[Array[Campaign]]
            for(camp <- list) {
                val items = env.bannerService.listByReferenceId(id=camp.id).data
                for(item <- items) {
                    val zoneids = env.newBookingService.getLinkedByItem(item.id)
                    for(zid  <- zoneids) {
                        min = getMinReport(zid,item.id,min,range,res)
                    }
                }
            }
        } else if(lists.isInstanceOf[Array[Zone]]) {
            val list = lists.asInstanceOf[Array[Zone]]
            var itemsCampaign = Array[Banner]()
            var zoneFillter = Array[Zone]()
            var campaignFillter = Array[Campaign]()
            var filterType = "items"
            if(filter !=null) {
                if(filter.isInstanceOf[Array[Banner]]) {
                    itemsCampaign = filter.asInstanceOf[Array[Banner]]
                    filterType = "items"
                } else if (filter.isInstanceOf[Array[Zone]]) {
                    zoneFillter = filter.asInstanceOf[Array[Zone]]
                    filterType = "zones"
                } else if (filter.isInstanceOf[Array[Campaign]]) {
                    campaignFillter = filter.asInstanceOf[Array[Campaign]]
                    filterType = "campaigns"
                }
            }

            for(zone <- list) {
                    val itemids = env.newBookingService.getItemsByZoneId(zone.id).data
                    for(zoneBanner <- itemids) {
                        if(filterType == "items") {
                            if(itemsCampaign.exists(a => zoneBanner.itemId == a.id) || itemsCampaign.size == 0) {
                                min = getMinReport(zone.id,zoneBanner.itemId,min,range,res)
                            }
                        } else if(filterType == "zones") {
                            if(zoneFillter.exists(x => zone.id == x.id)) {
                                min = getMinReport(zone.id,zoneBanner.itemId,min,range,res)
                            }
                        } else if(filterType == "campaigns") {
                            val itemFillter = env.bannerService.load(zoneBanner.itemId)
                            if(itemFillter != null) {
                                if(campaignFillter.exists(a=>itemFillter.campaignId == a.id )) {
                                    min = getMinReport(zone.id,zoneBanner.itemId,min,range,res)
                                }
                            }
                        }
                    }

            }
        } else if (lists.isInstanceOf[Array[ZoneToZoneGroup]]) {
            val list = lists.asInstanceOf[Array[ZoneToZoneGroup]]
                for(zoneToZoneGroup <- list) {
                    val itemids = env.newBookingService.getItemsByZoneId(zoneToZoneGroup.zoneId).data
                    for(zoneBanner <- itemids) {
                        min = getMinReport(zoneToZoneGroup.zoneId,zoneBanner.itemId,min,range,res)
                    }
                }
        }else if(lists.isInstanceOf[Array[ZoneGroup]]) {
            val list = lists.asInstanceOf[Array[ZoneGroup]]
            for(group <- list) {
                val zones = env.zoneToZoneGroupService.listByReferenceId(id=group.id).data
                for(zone <- zones) {
                    val itemids = env.newBookingService.getItemsByZoneId(zone.zoneId).data
                    for(zoneBanner <- itemids) {
                        min = getMinReport(zone.zoneId,zoneBanner.itemId,min,range,res)
                    }
                }
            }
        }else if(lists.isInstanceOf[Array[Order]]) {
            val list = lists.asInstanceOf[Array[Order]]
            for(order <- list) {
                val campaigns = env.campaignService.listByReferenceId(id=order.id).data
                for(camp <- campaigns) {
                    val items = env.bannerService.listByReferenceId(id=camp.id).data
                    for(item <- items) {
                        min = getMinReport(0,item.id,min,range,res)
                    }
                }
            }
        }else if(lists.isInstanceOf[Array[Banner]]) {
            val list = lists.asInstanceOf[Array[Banner]]
            var zoneFillter = Array[ZoneToBanner]()
            if(filter.isInstanceOf[Array[ZoneToBanner]]) {
                zoneFillter = filter.asInstanceOf[Array[ZoneToBanner]]
            }
            for(item <- list) {
                val zoneids = env.newBookingService.getLinkedByItem(item.id)
                if(zoneFillter.size > 0) {
                        if(zoneFillter.filter(x => x.itemId == item.id).length > 0) {
                            for(zid <- zoneids) {
                                min = getMinReport(zid,item.id,min,range,res)
                            }
                        }
                } else {
                    for(zid <- zoneids) {
                        min = getMinReport(zid,item.id,min,range,res)
                    }
                }

            }
        }else if(lists.isInstanceOf[ArrayBuffer[Banner]]) {
            val list = lists.asInstanceOf[ArrayBuffer[Banner]]
            var zoneFillter = Array[ZoneToBanner]()
            if(filter.isInstanceOf[Array[ZoneToBanner]]) {
                zoneFillter = filter.asInstanceOf[Array[ZoneToBanner]]
            }
            for(item <- list) {
                val zoneids = env.newBookingService.getLinkedByItem(item.id)
                if(zoneFillter.size > 0) {
                    if(zoneFillter.filter(x => x.itemId == item.id).length > 0) {
                        for(zid <- zoneids) {
                            min = getMinReport(zid,item.id,min,range,res)
                        }
                    }
                } else {
                    for(zid <- zoneids) {
                        min = getMinReport(zid,item.id,min,range,res)
                    }
                }

            }
        }

        min
    }
    def getMinReport(zoneid:Int,itemid:Int,min:(Int,Long,Long),range: Range,res:Int): (Int,Long,Long) = {
        var filter = ""
        var minC:(Int,Long,Long) = min
        if(zoneid > 0) {
            filter = "zone."+ zoneid +".impression.item." + itemid
        } else {
            filter = "impression.item." + itemid
        }
        val ret = detachAllTimeReport(range,filter,res)
        if(min._2 > 0 && ret._2 > 0) {
            if(ret._2 < min._2) {
                minC = ret
            }
        } else {
            if(min._2 == 0)  minC = ret
        }
        (minC)
    }
    def detachAllTimeReport(range: Range, filter: String, res: Int = Resolution.Hour): (Int,Long,Long) = {
        var to = System.currentTimeMillis()
        var resFix = res
        var fromFix = range.from
        var from = 0L

        if(res == Resolution.Hour) {
            val from = (to - (509*3600*1000));
            fromFix = from
            val part = report(from,(to - from),filter)
            val length = part._2.length
            var chk = false
            for (i <- 0 until 10) {
                if(part._2(i) != 0) {
                    chk = true
                }
            }
            if(chk == true) {
                val cal = Calendar.getInstance();
                cal.setTimeInMillis(to);
                cal.add(Calendar.DATE, -510);
                val from = cal.getTimeInMillis()
                fromFix = from
                val part = report(from,(to - from),filter)
                val length = part._2.length
                var chk = false
                for (i <- 0 until 10) {
                    if(part._2(i) != 0) {
                        chk = true
                    }
                }
                if(chk == true) {
                    val resRet = Resolution.Week
                } else {
                    resFix = Resolution.Day
                    breakable {
                        for (i <- 0 until length) {
                            if(part._2(i) != 0) {
                                val cal = Calendar.getInstance();
                                cal.setTimeInMillis(from);
                                cal.add(Calendar.DATE,(i/24));
                                fromFix =cal.getTimeInMillis
                                break
                            }
                        }
                        fromFix = 0
                    }
                }
            } else {
                val length = part._2.length
                breakable {
                    for (i <- 0 until length) {
                        if(part._2(i) != 0) {
                            fromFix = from + (i * 1000 * 60 * 60)
                            break
                        }
                    }
                    fromFix = 0
                }
            }
        } else if(res == Resolution.Day) {
            val cal = Calendar.getInstance();
            cal.setTimeInMillis(to);
            cal.add(Calendar.DATE, -510);
            val from = cal.getTimeInMillis()
            fromFix = from
            val part = report(from,(to - from),filter)
            val length = part._2.length
            var chk = false
            for (i <- 0 until 10) {
                if(part._2(i) != 0) {
                    chk = true
                }
            }
            if(chk == true) {
                val resRet = Resolution.Week
            } else {
                val length = part._2.length
                breakable {
                    for (i <- 0 until length) {
                        if(part._2(i) != 0) {
                            val cal = Calendar.getInstance();
                            cal.setTimeInMillis(to);
                            cal.add(Calendar.DATE,(-1)*(510-((i/24))));
                            fromFix =cal.getTimeInMillis
                            break
                        }
                    }
                    fromFix = 0
                }

            }
        } else if (res == Resolution.Week) {
            val cal = Calendar.getInstance();
            cal.setTimeInMillis(to);
            cal.add(Calendar.DATE, -(510*7));
            val from = cal.getTimeInMillis()
            fromFix = from
            val part = report(from,(to - from),filter)
            val length = part._2.length
            var chk = false
            for (i <- 0 until 10) {
                if(part._2(i) != 0) {
                    chk = true
                }
            }
            if(chk == true) {
                val resRet = Resolution.Month
            } else {
                val length = part._2.length
                breakable {
                    for (i <- 0 until length) {
                        if(part._2(i) != 0) {
                            val cal = Calendar.getInstance();
                            cal.setTimeInMillis(to);
                            cal.add(Calendar.DATE,(-1)*(7*510-((i/24))));
                            fromFix =cal.getTimeInMillis
                            break
                        }
                    }
                    fromFix = 0
                }
            }
        }else if (res == Resolution.Month) {
            val cal = Calendar.getInstance();
            cal.setTimeInMillis(to);
            cal.add(Calendar.DATE, -(510*30));
            val from = cal.getTimeInMillis()
            fromFix = from
            val part = report(from,(to - from),filter)
            val length = part._2.length
                breakable {
                    for (i <- 0 until length) {
                        if(part._2(i) != 0) {
                            val cal = Calendar.getInstance();
                            cal.setTimeInMillis(to);
                            cal.add(Calendar.DATE,(-1)*(30*510-((i/24))));
                            fromFix =cal.getTimeInMillis
                            break
                        }
                    }
                    fromFix = 0
                }

        }
        if(fromFix > 0) {
            if(resFix != Resolution.Hour) {
                val d = new Date(fromFix)
                d.setHours(0)
                d.setMinutes(0)
                d.setSeconds(0)
                fromFix = d.getTime()
            }
        } else {
            to = 0
        }
        (resFix,fromFix,to)
    }
    def report(from: Long, duration: Long, kind: String): (Long, ArrayBuffer[Double]) = {
        val f = Math.max(mapToHour(from), mapToHour(new Date(113, 8, 20, 9, 0).getTime))
        val t = Math.min (mapToHour(from + duration), mapToHour(System.currentTimeMillis()))

        var total = 0d
        var buf = new ArrayBuffer[Double]()

        for (i <- f to t) {
            val map = cached.get(i)
            //val c = (map != null) ? map.get(kind) | 0d
            val c = map match {
                case null => 0d
                case _ => map.get(kind)
            }

            total += c
            buf += c
        }
        (total.toLong, buf)
    }
    def getWebsiteByItem(from: Long, duration: Long, itemId:Int): Array[Int] = {
        val f = Math.max(mapToHour(from), mapToHour(new Date(113, 8, 20, 9, 0).getTime))
        val t = Math.min (mapToHour(from + duration), mapToHour(System.currentTimeMillis()))
        var total = 0d
        var buf = new ArrayBuffer[Double]()
        val filter = s"site."
        var ret = Array[Int]()
        for (i <- f to t) {
            val map = cached.get(i)
            if(map != null) {
                map.foreach { case (key, value) => {
                        if(key.startsWith(filter) && key.endsWith(".item."+itemId)) {
                            val arr = key.split("\\.")
                            if(arr.length > 2) {
                                ret ++= Array(Integer.parseInt(arr(1)))
                            }
                        }
                    }
                }

            }
        }
        ret.distinct
    }
    def getZoneByItem(from: Long, duration: Long, itemId:Int): Array[Int] = {
        val f = Math.max(mapToHour(from), mapToHour(new Date(113, 8, 20, 9, 0).getTime))
        val t = Math.min (mapToHour(from + duration), mapToHour(System.currentTimeMillis()))
        var total = 0d
        var buf = new ArrayBuffer[Double]()
        val filter = s"zone."
        var ret = Array[Int]()
        for (i <- f to t) {
            val map = cached.get(i)
            if(map != null) {
                map.foreach { case (key, value) => {
                    if(key.startsWith(filter) && key.endsWith(".item."+itemId)) {
                        val arr = key.split("\\.")
                        if(arr.length > 2) {
                            ret ++= Array(Integer.parseInt(arr(1)))
                        }
                    }
                }
                }

            }
        }
        ret.distinct
    }
    def getItemByZone(from: Long, duration: Long, zoneId:Int): Array[Int] = {
        val f = Math.max(mapToHour(from), mapToHour(new Date(113, 8, 20, 9, 0).getTime))
        val t = Math.min (mapToHour(from + duration), mapToHour(System.currentTimeMillis()))
        var total = 0d
        var buf = new ArrayBuffer[Double]()
        val filter = s"zone.$zoneId.impression.item."
        var ret = Array[Int]()
        for (i <- f to t) {
            val map = cached.get(i)
            if(map != null) {
                map.foreach { case (key, value) => {
                    if(key.startsWith(filter)) {
                        val arr = key.split("\\.")
                        if(arr.length == 5) {
                            ret ++= Array(Integer.parseInt(arr(4)))
                        }
                    }
                }
                }

            }
        }
        ret.distinct
    }
    def getItemBySite(from: Long, duration: Long, siteId:Int): Array[Int] = {
        val f = Math.max(mapToHour(from), mapToHour(new Date(113, 8, 20, 9, 0).getTime))
        val t = Math.min (mapToHour(from + duration), mapToHour(System.currentTimeMillis()))
        var total = 0d
        var buf = new ArrayBuffer[Double]()
        val filter = s"site.$siteId.impression.zone."
        var ret = Array[Int]()
        for (i <- f to t) {
            val map = cached.get(i)
            if(map != null) {
                map.foreach { case (key, value) => {
                    if(key.startsWith(filter)) {
                        val arr = key.split("\\.")
                        if(arr.length == 7) {
                            ret ++= Array(Integer.parseInt(arr(6)))
                        }
                    }
                }
                }

            }
        }
        ret.distinct
    }
    def reportByZone(from: Long, duration: Long, kind:String,itemId:Int=0): (Long, ArrayBuffer[Double]) = {
        val f = Math.max(mapToHour(from), mapToHour(new Date(113, 8, 20, 9, 0).getTime))
        val t = Math.min (mapToHour(from + duration), mapToHour(System.currentTimeMillis()))

        var total = 0d
        var buf = new ArrayBuffer[Double]()
        var filter = ""
        if(itemId > 0) {
            filter = kind + ".item." + itemId
        } else {
            filter = kind + ".item."
        }
        for (i <- f to t) {
            val map = cached.get(i)
            if(map != null) {
                var ichk = false
                map.foreach { case (key, value) => {
                        if(key.startsWith(filter)) {
                            val c = map match {
                                case null => 0d
                                case _ => value
                            }
                            total += c
                            buf += c
                            ichk = true
                        }
                    }
                }
                if(!ichk) {
                    total += 0d
                    buf += 0d
                }
            }  else {
                total += 0d
                buf += 0d
            }
        }
        (total.toLong, buf)
    }
    def reportBySite(from: Long, duration: Long, kind:String,itemId:Int = 0): (Long, ArrayBuffer[Double]) = {
        val f = Math.max(mapToHour(from), mapToHour(new Date(113, 8, 20, 9, 0).getTime))
        val t = Math.min (mapToHour(from + duration), mapToHour(System.currentTimeMillis()))

        var total = 0d
        var buf = new ArrayBuffer[Double]()
        val filter = kind + ".zone."
        for (i <- f to t) {
            val map = cached.get(i)
            if(map != null) {
                var ichk = false
                map.foreach { case (key, value) => {
                        if(key.startsWith(filter)) {
                            if(itemId > 0) {
                                if(key.endsWith(".item."+itemId)) {
                                    val c = map match {
                                        case null => 0d
                                        case _ => value
                                    }
                                    total += c
                                    buf += c
                                    ichk = true
                                }
                            } else {
                                val c = map match {
                                    case null => 0d
                                    case _ => value
                                }
                                total += c
                                buf += c
                                ichk = true
                            }

                        }
                    }
                }
                if(!ichk) {
                    total += 0d
                    buf += 0d
                }
            } else {
                total += 0d
                buf += 0d
            }
        }
        (total.toLong, buf)
    }

    def run(): Unit = {
        spawn {
            val date = (new Date(updated) < new Date(113, 11, 20, 9, 0)) ? new Date(113, 11, 20, 9, 0) | new Date(updated)
            updated = date.getTime

            while (true) {

                val time = System.currentTimeMillis()
                val iterator = env.log.readAll(new Date(updated), 1.hour)

                val map = new util.HashMap[String, Double]()
                var min = 0L
                var totalRecords = 0


                try {

                    val start = System.currentTimeMillis()
                    val itemSet = new util.HashSet[Int]()
                    val zoneSet = new util.HashSet[Int]()
                    using(iterator) {
                        for (item <- iterator) {

                            if (item != null) {

                                if (min == 0) {
                                    min = item.time
                                }

                                //log zone
                                map.putOrUpdate(s"zone.${item.zoneId}.${item.kind}.item.${item.itemId}", 1, v => v + 1)
                                map.putOrUpdate(s"zone.${item.zoneId}.spent.item.${item.itemId}", 1, v => v + item.value)
                                val zone = env.zoneService.load(item.zoneId)
                                if(zone !=null) {
                                    val siteid = zone.siteId
                                    map.putOrUpdate(s"site.${siteid}.${item.kind}.zone.${item.zoneId}.item.${item.itemId}", 1, v => v + 1)
                                    map.putOrUpdate(s"site.${siteid}.spent.zone.${item.zoneId}.item.${item.itemId}", 1, v => v + item.value)
                                }
                                // count
                                map.putOrUpdate(s"${item.kind}.item.${item.itemId}", 1, v => v + 1)
                                // spent
                                map.putOrUpdate(s"spent.item.${item.itemId}", 1, v => v + item.value)
                                // revenue conversion
                                if(item.kind == "conversion") {
                                    map.putOrUpdate(s"revenueconversion.item.${item.itemId}", 1, v => v + item.value)
                                }
                                itemSet.add(item.itemId)
                                zoneSet.add(item.zoneId)
                                totalRecords += 1
                            }
                        }
                    }

                    // ctr
                    for (item <- itemSet) {
                        var click = map.get(s"click.item.$item")
                        var impression = map.get(s"impression.item.$item")

                        if (!map.containsKey(s"impression.item.$item")) impression = 1
                        if (!map.containsKey(s"click.item.$item")) click = 0

                        map(s"ctr.item.$item") = click * 100d / impression

                        for(zid <- zoneSet) {
                            var zClick = map.get(s"zone.$zid.click.item.$item")
                            var zImpression = map.get(s"zone.$zid.impression.item.$item")
                            if (!map.containsKey(s"zone.$zid.impression.item.$item")) zImpression = 1
                            if (!map.containsKey(s"zone.$zid.click.item.$item")) zClick = 0
                            map(s"zone.$zid.ctr.item.$item") = zClick * 100d / zImpression
                            val zone = env.zoneService.load(zid)
                            if(zone !=null) {
                                map(s"zone.$zid.revenue.item.$item") = zImpression * 1000d / zone.minCPM
                            }
                        }
                    }

                    updated = (Math.max(updated, min) / 3600 / 1000) * 3600 * 1000

                    val behind = (time - updated) / 3600 / 1000

                    //println(s"behind: $behind hours")

                    val index = (updated / 3600 / 1000).toInt
                    if (map.size > 0) cached.put(index, map)

                    if (time >= updated + 3610 * 1000) {


                        if (map.size() > 0) {
                            for (p <- map.entrySet()) {
                                //env.cachedLogService.save(new CachedLog(0, index, "count", p.getKey, p.getValue))
                                env.cachedLogService.save(index,p.getKey,p.getValue)
                            }
                            env.cachedLogService.flush()
                        }

                        updated = (index + 1L) * 3600 * 1000
                    }

                    var updateTime = System.currentTimeMillis() - start
                    if (updateTime == 0) updateTime = 1
                    val speed = totalRecords / updateTime

                    println(s"report updated: $totalRecords records, take ${updateTime} ms, speed =$speed K/sec at ${new Date(updated)}")
                    //for (record <- map.entrySet()) {
                    //    println(s"    ${record.getKey} = ${record.getValue}")
                    //}

                    current = map
                    currentTime = min

                    if (behind <= 2) Thread.sleep(30 * 1000)
                }
                catch {
                    case e: Throwable => {
                        printException(e, System.out)
                        Thread.sleep(10 * 1000)
                    }
                }
            }
        }

        env.cachedLogService.readAll(
            (time, key, value)=>{
                val map = cached.getOrAdd(time, (i) => new util.HashMap[String, Double]())
                map.synchronized {
                    map.put(key, value)
                }

                val v = time * 1000L * 3600
                if (v > updated) updated = v
            }
        )
    }

    def getImpression(bannerId: Int, from: Long, duration: Long): Long = {
//        val p = getReport(new Range(from, from + to), "click.item." + bannerId)
//        p._1
        report(from, duration, "impression.item." + bannerId)._1
    }

    def getInfo(bannerId: Int, from: Long, duration: Long): ads.common.services.report.ReportRecord = {
        val click = getReport(new Range(from,from+duration),"click.item." + bannerId)
        val impression = getReport(new Range(from,from+duration),"impression.item." + bannerId)
        val ctr = getPercent(click._1, impression._1)
        return new ads.common.services.report.ReportRecord(bannerId, click._1, impression._1, ctr)
    }
}
*/