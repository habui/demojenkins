package ads.common.services.report

import java.util
import scala.collection.mutable

class Range(var from: Long, var to: Long)
class ReportFilter(var key: String, var value: String)

class ReportRequest{
    var range: Range = null
    var compareRange: Range = null
    var offset: Int = 0
    var count: Int = 10
    var filters: util.ArrayList[ReportFilter] = null
    var graphData: String = "impression"
    def mapFilter(): Map[String, String] = {
        var map: Map[String, String] = Map()
        for(i <- 0 until filters.size){
            map += (filters.get(i).key -> filters.get(i).value)
        }
        map
    }
}

class CRMReportRequest(
    var from : Long,
    var to : Long,
    var items : Array[Int],
    var zone: Int,
    var filter: String
)

class CRMItemResponse (
    var clicks : Double,
    var impressions : Double
)
class SimpleCRMItemResponse (
    var c: Int = 0,
    var i: Int = 0,
    var cp: Int = 0
)
class CRMReportResponse(
    var data  : util.HashMap[String, util.HashMap[String, CRMItemResponse]]
)

class GraphPoint (var value: Double, var date: Long)

class FastGraphPoint (var impression: Double, var click: Double, var ctr: Double, var conversion: Double, var date: Long)

class ReportGraph (var points: util.ArrayList[GraphPoint], var comparePoints: util.ArrayList[GraphPoint])

class FastReportGraph(var points: util.ArrayList[FastGraphPoint], var comparePoints: util.ArrayList[FastGraphPoint])

class ConversionRecord(var date: Long, var item: util.ArrayList[ConversionDetail])

class ConversionDetail(var label: String, var conversion: Long, var value: Double)

class ReportItem(
    var name: String = "report_item_name",
    var id: Int,
    var extraField: util.ArrayList[ExtraField],
    var properties: util.ArrayList[ReportItemProperty])


class Summary(var properties: util.ArrayList[ReportItemProperty])

class ReportItemProperty(var label: String, var value: Double, var compareValue: Double, var change: Double)

class ReportResponse(
    var range: Range,
    var compareRange: Range,
    var total: Long,
    var items: util.ArrayList[ReportItem],
    var graph: ReportGraph,
    var summary: Summary
)


class FastReportItemProperty {
    var value: Double = 0
    var compareValue: Double = 0
    var change: Double = -100

    def convertToReportItemProperty(label: String): ReportItemProperty = {
        new ReportItemProperty(label, value, compareValue, change)
    }
}

class ExtraField(var label: String, var value: String)

class FastReportItem(
    var name: String = "report_item_name",
    var id: Int,
    var extraField: util.ArrayList[ExtraField],
    var properties: Map[String, FastReportItemProperty] =
        Map(
            "impression" -> new FastReportItemProperty,
            "click" -> new FastReportItemProperty,
            "click.spam" -> new FastReportItemProperty,
            "validclick" -> new FastReportItemProperty,
            "ctr" -> new FastReportItemProperty,
            "collapse" -> new FastReportItemProperty,
            "expand" -> new FastReportItemProperty,
            "spent" -> new FastReportItemProperty,
            "creative" -> new FastReportItemProperty,
            "firstQuartile" -> new FastReportItemProperty,
            "midPoint" -> new FastReportItemProperty,
            "thirdQuartile" -> new FastReportItemProperty,
            "complete" -> new FastReportItemProperty,
            "close" -> new FastReportItemProperty,
            "pause" -> new FastReportItemProperty,
            "resume" -> new FastReportItemProperty,
            "fullscreen" -> new FastReportItemProperty,
            "mute" -> new FastReportItemProperty,
            "unmute" -> new FastReportItemProperty,
            "skip" -> new FastReportItemProperty,
            "revenueconversion" -> new FastReportItemProperty,
            "conversion" -> new FastReportItemProperty,
            "costconversion" -> new FastReportItemProperty,
            "conversionrate" -> new FastReportItemProperty,
            "roi" -> new FastReportItemProperty,
            "requestimpression" -> new FastReportItemProperty,
            "pageview" -> new FastReportItemProperty
        )
    )

class ConversionItem(var name: String, var conversion: Long, var value: Double)

class FastReportResponse{
    var range: Range = new Range(0, 0)
    var compareRange: Range = null
    var total: Long = 0
    var items: Map[Int, FastReportItem] = Map()
    var graph: FastReportGraph = new FastReportGraph(new util.ArrayList[FastGraphPoint], new util.ArrayList[FastGraphPoint])
    var conversion: mutable.HashMap[Long, mutable.HashMap[Int, ConversionItem]] = new mutable.HashMap[Long, mutable.HashMap[Int, ConversionItem]]
    var summary: Map[String, FastReportItemProperty] =
        Map(
            "impression" -> new FastReportItemProperty,
            "click" -> new FastReportItemProperty,
            "click.spam" -> new FastReportItemProperty,
            "validclick" -> new FastReportItemProperty,
            "ctr" -> new FastReportItemProperty,
            "collapse" -> new FastReportItemProperty,
            "expand" -> new FastReportItemProperty,
            "spent" -> new FastReportItemProperty,
            "creative" -> new FastReportItemProperty,
            "firstQuartile" -> new FastReportItemProperty,
            "midPoint" -> new FastReportItemProperty,
            "thirdQuartile" -> new FastReportItemProperty,
            "complete" -> new FastReportItemProperty,
            "close" -> new FastReportItemProperty,
            "pause" -> new FastReportItemProperty,
            "resume" -> new FastReportItemProperty,
            "fullscreen" -> new FastReportItemProperty,
            "mute" -> new FastReportItemProperty,
            "unmute" -> new FastReportItemProperty,
            "skip" -> new FastReportItemProperty,
            "conversion" -> new FastReportItemProperty,
            "revenueconversion" -> new FastReportItemProperty,
            "conversionrate" -> new FastReportItemProperty,
            "requestimpression" -> new FastReportItemProperty,
            "pageview" -> new FastReportItemProperty
        )

    def convertToReportResponse(): NewReportResponse = {
        val listReportItem = new util.ArrayList[ReportItem]()
        for ((k,v) <- items) {
            val item = new ReportItem(v.name, v.id, v.extraField, new util.ArrayList[ReportItemProperty]())
            item.properties.add(v.properties("impression").convertToReportItemProperty("impression"))
            v.properties -= ("impression")
            item.properties.add(v.properties("click").convertToReportItemProperty("click"))
            v.properties -= ("click")
            item.properties.add(v.properties("click.spam").convertToReportItemProperty("click.spam"))
            v.properties -= ("click.spam")
            item.properties.add(v.properties("ctr").convertToReportItemProperty("ctr"))
            v.properties -= ("ctr")
            item.properties.add(v.properties("spent").convertToReportItemProperty("spent"))
            v.properties -= ("spent")
            for ((k1,v1) <- v.properties){
                item.properties.add(v1.convertToReportItemProperty(k1))
            }
            listReportItem.add(item)
        }
        val sum = new Summary(new util.ArrayList[ReportItemProperty]())
        for ((k, v) <- summary) {
            sum.properties.add(new ReportItemProperty(k, v.value, v.compareValue, v.change))
        }
        val conv = new util.ArrayList[ConversionRecord]()
        for ((k,v) <- conversion) {
            val record = new util.ArrayList[ConversionDetail]()
            for ((k1,v1) <- v) {
                record.add(new ConversionDetail(v1.name, v1.conversion, v1.value))
            }
            conv.add(new ConversionRecord(k, record))
        }
        new NewReportResponse(range, compareRange, 0, listReportItem, graph, sum, conv)
    }
}

class NewReportResponse(
                       var range: Range,
                       var compareRange: Range,
                       var total: Long,
                       var items: util.ArrayList[ReportItem],
                       var graph: FastReportGraph,
                       var summary: Summary,
                       var conversion: util.ArrayList[ConversionRecord]
                       )

object AdType {
    val Overlay = 1
    val Pause = 2
    val Preroll = 3
    val Midroll = 4
    val Postroll = 5
    val Trueview = 6
    val BranderSkin = 7
}

object PayType {
    val CPC = 1
    val CPM = 2
    val Completed = 3
}

class ReportZingTVItemInfo(var i: Long = 0, var c: Long = 0, var cp: Long = 0, var r: Double = 0)

class ReportZingTVItemProperty(var p: util.HashMap[Int , ReportZingTVItemInfo])

class ReportZingTVItem(var t: String = "", var pn: String = "", var pi: Int = 0, var a: util.HashMap[Int, util.HashMap[Int , ReportZingTVItemInfo]] = new util.HashMap[Int, util.HashMap[Int , ReportZingTVItemInfo]])

class ZingTVReportResponse(var range: Range = new Range(0, 0),
                           var videos: Map[Int, ReportZingTVItem] = Map(),
                           graph: FastReportGraph = new FastReportGraph(new util.ArrayList[FastGraphPoint], new util.ArrayList[FastGraphPoint]),
                           var summary: Map[String, ReportZingTVItemProperty] = Map())

class ZingTVReportCRM(var videos: Map[Int, util.HashMap[Int, SimpleCRMItemResponse]] = Map())

class FullZingTVReportCRM(var banners: Map[Int, ZingTVReportCRM] = Map())
