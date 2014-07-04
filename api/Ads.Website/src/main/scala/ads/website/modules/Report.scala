package ads.website.modules

import ads.web.mvc.{Action, BaseHandler, Json, SmartDispatcherHandler, Text}
import ads.common.services.report._
import ads.web.{WebUtils, Invoke, HandlerContainerServlet, BaseServlet}
import ads.website.{PermissionUtils, Environment}
import ads.common.{SecurityContext}
import ads.common.model.Permission
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import ads.common.services.IFastReportService
import java.util.Date
import java.io._

//import org.jfree.chart.{ChartFactory, JFreeChart}
//import com.itextpdf.text.pdf._
//import com.itextpdf.text._
//import java.awt.{Color, Graphics2D}
//import com.itextpdf.awt.DefaultFontMapper
//import java.awt.geom.Rectangle2D
//import org.jfree.chart.plot.XYPlot
//import org.jfree.ui.RectangleInsets
//import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
//import scala.collection.mutable.ArrayBuffer
//import org.jfree.chart.axis.DateAxis
//import org.jfree.data.time._
//import java.text.{DecimalFormat, SimpleDateFormat}
import ads.website.modules.serving.{FastReportService}
/*
class ReportHandler (env: { val reportService: IReportService}) extends SmartDispatcherHandler{

    @Invoke(Parameters = "request,data")
    def getReport(request:HttpServletRequest, data: String):Action={
        SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request, item, Permission.WEBSITE_REPORT))
        val reportRequest:ReportRequest = WebUtils.fromJson(classOf[ReportRequest], data)
         return Json(env.reportService.getReport(reportRequest))
    }

    @Invoke(Parameters = "request,data")
    def getCRMReport(request: HttpServletRequest, data : String) = {
        SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request, item, Permission.WEBSITE_REPORT))
        val reportRequest : CRMReportRequest = WebUtils.fromJson(classOf[CRMReportRequest],data)
        val ret = env.reportService.getCRMReport(reportRequest)
        Json(ret.data)
    }
}
*/

class FastReportHandler (env: {
    var fastReportService: IFastReportService
    }) extends SmartDispatcherHandler{

    @Invoke(Parameters = "request,data")
    def getReport(request:HttpServletRequest, data: String):Action={
        SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request, item, Permission.WEBSITE_REPORT))
        val reportRequest:ReportRequest = WebUtils.fromJson(classOf[ReportRequest], data)
        Json(env.fastReportService.getReport(reportRequest, PermissionUtils.getUser(request).name))
    }

    @Invoke(Parameters = "request,data")
    def getCRMReport(request: HttpServletRequest, data : String) = {
        SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request, item, Permission.WEBSITE_REPORT))
        val reportRequest : CRMReportRequest = WebUtils.fromJson(classOf[CRMReportRequest],data)
        val ret = env.fastReportService.getCRMReport(reportRequest)
        Json(ret)
    }

    @Invoke(Parameters = "request,response,range")
    def getZingTVReport(request: HttpServletRequest, response: HttpServletResponse, range : String) {
        SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request, item, Permission.ROOT_PERMISSION))
        val data: Range = WebUtils.fromJson(classOf[Range], range)
        env.fastReportService.getZingTVReport(response, data)
    }

    @Invoke(Parameters = "request,response,date")
    def getZingTVCRMReport(request: HttpServletRequest, response: HttpServletResponse, date : Long) {
        SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request, item, Permission.ROOT_PERMISSION))
        env.fastReportService.getZingTVCRMReport(response, date)
    }

    @Invoke(Parameters = "request")
    def runZingTVReport(request: HttpServletRequest) = {
        SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request, item, Permission.ROOT_PERMISSION))
        val from = request.getParameter("from").split("-").map(_.toInt)
        val startDate = new Date(from(2) - 1900, from(1) - 1, from(0))
        env.fastReportService.cacheReportZingTV(startDate)
    }
}

/*
class ExportReportHandler (env: { var fastReportService: IFastReportService}) extends BaseHandler{

    def process(request: HttpServletRequest, response: HttpServletResponse) {
        val data: String = request.getParameter("data")
        val reportRequest:ReportRequest = WebUtils.fromJson(classOf[ReportRequest], data)
        var format: String = ""
        for (i <- 0 until reportRequest.filters.size if format.equals("")){
            if (reportRequest.filters.get(i).key.equals("resolution")) {
                format = reportRequest.filters.get(i).value
            }
        }
        writeToClient(response, reportRequest, request)
    }

    def writeToClient(response: HttpServletResponse, reportRequest:ReportRequest, request: HttpServletRequest) {
        var format: String = ""
        for (i <- 0 until reportRequest.filters.size if format.equals("")){
            if (reportRequest.filters.get(i).key.equals("resolution")) {
                format = reportRequest.filters.get(i).value
            }
        }
        var data: NewReportResponse = null
        response.setContentType("application/pdf")
        val document: Document = new Document
        try {
            val writer = PdfWriter.getInstance(document, response.getOutputStream)
            document.open
            //add graph
            data = env.fastReportService.getReport(reportRequest, PermissionUtils.getUser(request).name)
            addChart(document, writer, data, format, "impression")
            addChart(document, writer, data, format, "click")
            addChart(document, writer, data, format, "ctr")
            //add table report
            val formatter = new DecimalFormat("#,###")
            var title: ArrayBuffer[String] = new ArrayBuffer[String]
            title += ("Time", "Impression", "Click", "Ctr")
            var formater: SimpleDateFormat= null
            format match {
                case "hour" => formater = new SimpleDateFormat("hh:mm")
                case _ => formater = new SimpleDateFormat("dd-MM-yyyy")
            }
            var listdata: ArrayBuffer[String] = new ArrayBuffer[String]
            var totalImpression: Double = 0
            var totalClick: Double = 0
            var totalCtr: Double = 0
            for (i <- 0 until data.graph.points.size) {
                totalImpression += data.graph.points.get(i).impression
                totalClick += data.graph.points.get(i).click
                totalCtr += data.graph.points.get(i).ctr
                //add time
                listdata += (formater.format(new java.util.Date(data.graph.points.get(i).date)))
                //add data impression
                listdata += (formatter.format(data.graph.points.get(i).impression.toLong))
                //add data click
                listdata += (formatter.format(data.graph.points.get(i).click.toLong))
                //add data ctr
                listdata += (Math.round(data.graph.points.get(i).ctr * 10000).toInt/10000.0).toString
            }
            listdata += ("TOTAL", formatter.format(totalImpression.toLong), formatter.format(totalClick.toLong), (Math.round(totalCtr * 10000).toInt / 10000.0).toString)
            var colWidth = Array(1, 1, 1, 1)
            var table: PdfPTable = createTable(title.toArray, listdata.toArray, colWidth)
            table.setSpacingBefore(30)
            table.setSpacingAfter(50)
            document.add(table)

            //add table order
            if (data.compareRange == null) {
                title = new ArrayBuffer[String]
                title += ("Orders", "Impression", "Click", "CTR (%)", "Spent (1000VND)")
                val listOrder = new ArrayBuffer[String]
                var impression: String = ""
                var click: String = ""
                var ctr: String = ""
                var spent: String = ""
                for (i <- 0 until data.items.size){
                    for (j <- 0 until data.items.get(i).properties.size){
                        data.items.get(i).properties.get(j).label match {
                            case "impression" => impression = formatter.format(data.items.get(i).properties.get(j).value.toLong)
                            case "click" => click = formatter.format(data.items.get(i).properties.get(j).value.toLong)
                            case "ctr" => ctr = (Math.round(data.items.get(i).properties.get(j).value * 10000).toInt / 10000.0).toString
                            case "spent" => spent = formatter.format(data.items.get(i).properties.get(j).value.toLong)
                            case _ =>
                        }
                    }
                    listOrder += (data.items.get(i).name, impression, click, ctr, spent)
                }
                for (i <- 0 until data.summary.properties.size) {
                    data.summary.properties.get(i).label match {
                        case "impression" => impression = formatter.format(data.summary.properties.get(i).value.toLong)
                        case "click" => click = formatter.format(data.summary.properties.get(i).value.toLong)
                        case "ctr" => ctr = (Math.round(data.summary.properties.get(i).value * 10000).toInt / 10000.0).toString
                        case "spent" => spent = formatter.format(data.summary.properties.get(i).value.toLong)
                        case _ =>
                    }
                }
                listOrder += ("TOTAL", impression, click, ctr, spent)
                colWidth = Array(2, 1, 1, 1, 1)
                table = createTable(title.toArray, listOrder.toArray, colWidth, 5)
                table.setSpacingBefore(20f)
                document.add(table)
            } else
                document.add(createCompareTable(data))
        }
        catch {
            case e: Exception => {
                e.printStackTrace
            }
        }
        document.close
    }

    def addChart(document: Document, writer: PdfWriter, data: NewReportResponse, format: String, kind: String) {
        val width: Float = 500
        val height: Float = 300
        val chart = createLineChart(data, format, kind)
        val contentByte: PdfContentByte = writer.getDirectContent
        val template: PdfTemplate = contentByte.createTemplate(width, height + 70)
        val graphics2d: Graphics2D = template.createGraphics(width, height + 70, new DefaultFontMapper)
        val rectangle2d: Rectangle2D = new Rectangle2D.Double(0, 0, width, height)
        chart.draw(graphics2d, rectangle2d)
        graphics2d.dispose
        val chartImage: Image = Image.getInstance(template)
        document.add(chartImage)
    }

    def createLineChart(datas: NewReportResponse, format: String, kind: String): JFreeChart = {
        val data = datas.graph
        val sdf: SimpleDateFormat = new SimpleDateFormat("dd/MM/yyyy")
        val dataset: TimeSeriesCollection  = new TimeSeriesCollection
        dataset.setDomainIsPointsInTime(true)
        var s1: TimeSeries = null
        var s2: TimeSeries = null
        var time1 = sdf.format(new java.util.Date(datas.range.from))
        var time2 = ""
        if (time1.equals(sdf.format(new java.util.Date(datas.range.to))))
            time1 = " [" + time1 + "]"
        else time1 = " [" + time1 + " - " + sdf.format(new java.util.Date(datas.range.to)) + "]"
        if (datas.compareRange != null) {
            time2 = sdf.format(new java.util.Date(datas.compareRange.from))
            if (time2.equals(sdf.format(new java.util.Date(datas.compareRange.to))))
                time2 = " [" + time2 + "]"
            else time2 = " [" + time2 + " - " + sdf.format(new java.util.Date(datas.compareRange.to)) + "]"
        }

        format match {
            case "hour" => {
                s1 = new TimeSeries(kind + time1, classOf[Hour])
                s2 = new TimeSeries(kind + time2, classOf[Hour])
            }
            case _ => {
                s1 = new TimeSeries(kind + time1, classOf[Day])
                s2 = new TimeSeries(kind + time2, classOf[Day])
            }
        }
        for (i <- 0 until data.points.size){
            var value = 0.0
            var compareValue = 0.0
            kind match {
                case "impression" => {
                    value = data.points.get(i).impression
                    compareValue = data.comparePoints.get(i).impression
                }
                case "click" => {
                    value = data.points.get(i).click
                    compareValue = data.comparePoints.get(i).click
                }
                case "ctr" => {
                    value = data.points.get(i).ctr
                    compareValue = data.comparePoints.get(i).ctr
                }
            }
            format match {
                case "hour" => {
                    s1.addOrUpdate(new Hour(new java.util.Date(data.points.get(i).date)), value)
                    if (data.comparePoints != null && data.comparePoints.size > 0) {
                        s2.addOrUpdate(new Hour(new java.util.Date(data.points.get(i).date)), compareValue)
                    }
                }
                case _ => {
                    s1.addOrUpdate(new Day(new java.util.Date(data.points.get(i).date)), value)
                    if (data.comparePoints != null && data.comparePoints.size > 0) {
                        s2.addOrUpdate(new Day(new java.util.Date(data.points.get(i).date)), compareValue)
                    }
                }
            }
        }
        dataset.addSeries(s1)
        if (datas.compareRange != null) dataset.addSeries(s2)

        val chart: JFreeChart = ChartFactory.createTimeSeriesChart(kind, "", "", dataset, true, true, true)
        val plot: XYPlot = chart.getPlot.asInstanceOf[XYPlot]
        plot.setBackgroundPaint(Color.white)
        plot.setDomainGridlinePaint(Color.GRAY)
        plot.setRangeGridlinePaint(Color.GRAY)
        plot.setAxisOffset(new RectangleInsets(0, 0, 0, 0))
        plot.setDomainCrosshairVisible(true)
        plot.setRangeCrosshairVisible(true)

        val axis: DateAxis = plot.getDomainAxis.asInstanceOf[DateAxis]
        format match {
            case "hour" => axis.setDateFormatOverride(new SimpleDateFormat("h-mm"))
            case _ =>  axis.setDateFormatOverride(new SimpleDateFormat("d-MMM"))
        }
        //if (data.points.size > 10) axis.setVerticalTickLabels(true)
        val renderer: XYLineAndShapeRenderer = plot.getRenderer.asInstanceOf[XYLineAndShapeRenderer]
        renderer.setBaseShapesVisible(true)
        renderer.setBaseShapesFilled(true)
        return chart
    }


    def createTable(title: Array[String], data: Array[String], colWidth: Array[Int], highlight: Int = 0, widthPercentage: Int = 100): PdfPTable = {
        val numCol = title.length
        val font: BaseFont = BaseFont.createFont(this.getClass().getResource("/").getPath() + "font/arial.ttf", BaseFont.WINANSI, true)
        val bgcolorTitle = BaseColor.LIGHT_GRAY
        val bgcolorRow = new BaseColor(236, 245, 247)
        def cellDefault = {
            val cell = new PdfPCell()
            cell.setHorizontalAlignment(Element.ALIGN_LEFT)
            cell.setPadding(8)
            cell.setBorderColor(new BaseColor(221, 221, 221))
            cell
        }

        val table: PdfPTable = new PdfPTable(numCol)
        table.setWidthPercentage(widthPercentage)//width 100%
        table.setWidths(colWidth)//set width for col
        // add title
        var cell: PdfPCell = null
        for (i <- title){
            cell = cellDefault
            cell.setPhrase(new Phrase(i, new Font(font, 12f)))
            cell.setBackgroundColor(bgcolorTitle)
            cell.setHorizontalAlignment(Element.ALIGN_CENTER)
            table.addCell(cell)
        }

        for (i <- 0 until data.size){
            cell = cellDefault
            if (highlight != 0 && i % highlight == 0){
                cell.setPhrase(new Phrase(data(i), new Font(font, -1, -1, new BaseColor(0, 136, 204))))
            } else cell.setPhrase(new Phrase(data(i), new Font(font)))

            if (i % (numCol * 2) < numCol) cell.setBackgroundColor(bgcolorRow)
            table.addCell(cell)
        }
        table
    }

    def createCompareTable(datas: NewReportResponse): PdfPTable = {
        val sdf: SimpleDateFormat = new SimpleDateFormat("dd/MM/yyyy")
        val formatter = new DecimalFormat("#,###")
        val basefont: BaseFont = BaseFont.createFont(this.getClass().getResource("/").getPath() + "font/arial.ttf", BaseFont.WINANSI, true)
        val bgcolorTitle = BaseColor.LIGHT_GRAY
        val bgcolorRow = new BaseColor(236, 245, 247)
        def cellDefault(text: String = "", color: BaseColor = null, bgColor: BaseColor = null) = {
            val cell = new PdfPCell()
            val font = new Font(basefont, -1, -1, color)
            cell.setPhrase(new Phrase(text, font))
            cell.setHorizontalAlignment(Element.ALIGN_LEFT)
            cell.setPadding(8)
            cell.setBorderColor(new BaseColor(221, 221, 221))
            if (bgColor != null) cell. setBackgroundColor(bgColor)
            cell
        }

        val table: PdfPTable = new PdfPTable(6)
        table.setWidthPercentage(100)
        table.setWidths(Array(4,40,17,17,17,17))
        for (i <- Array("#","Orders","Impression","Click","CTR (%)","Spent (1000VND)")){
            val cell = cellDefault(i)
            cell.setBackgroundColor(bgcolorTitle)
            cell.setHorizontalAlignment(Element.ALIGN_CENTER)
            table.addCell(cell)
        }
        var count = 1
        var impression: ReportItemProperty = null
        var click: ReportItemProperty = null
        var ctr: ReportItemProperty = null
        var spent: ReportItemProperty = null
        val time: String = sdf.format(new java.util.Date(datas.range.from)) + " -> " + sdf.format(new java.util.Date(datas.range.to))
        val compareTime: String = sdf.format(new java.util.Date(datas.compareRange.from)) + " -> " + sdf.format(new java.util.Date(datas.compareRange.to))
        for (i <- 0 until datas.items.size){
            val item = datas.items.get(i)
            table.addCell(cellDefault(count.toString,null,bgcolorRow))
            table.addCell(cellDefault(item.name, new BaseColor(0, 136, 204),bgcolorRow))
            table.addCell(cellDefault("",null,bgcolorRow)); table.addCell(cellDefault("",null,bgcolorRow)); table.addCell(cellDefault("",null,bgcolorRow)); table.addCell(cellDefault("",null,bgcolorRow));
            table.addCell(cellDefault())
            table.addCell(cellDefault(time))
            for (j <- 0 until item.properties.size) {
                item.properties.get(j).label match {
                    case "impression" => impression = item.properties.get(j)
                    case "click" => click = item.properties.get(j)
                    case "ctr" => ctr = item.properties.get(j)
                    case "spent" => spent = item.properties.get(j)
                    case _ =>
                }
            }
            table.addCell(cellDefault(formatter.format(impression.value.toInt)))
            table.addCell(cellDefault(formatter.format(click.value.toInt)))
            table.addCell(cellDefault(Math.round(ctr.value).toString))
            table.addCell(cellDefault(formatter.format(spent.value.toInt)))
            //compare
            table.addCell(cellDefault())
            table.addCell(cellDefault(compareTime))
            table.addCell(cellDefault(formatter.format(impression.compareValue.toInt)))
            table.addCell(cellDefault(formatter.format(click.compareValue.toInt)))
            table.addCell(cellDefault(Math.round(ctr.compareValue).toString))
            table.addCell(cellDefault(formatter.format(spent.compareValue.toInt)))
            //change
            table.addCell(cellDefault())
            table.addCell(cellDefault("%Change"))
            table.addCell(cellDefault(impression.change.toInt + "%"))
            table.addCell(cellDefault(click.change.toInt + "%"))
            table.addCell(cellDefault(ctr.change.toInt + "%"))
            table.addCell(cellDefault(spent.change.toInt + "%"))

            count += 1
        }
        for (i <- 0 until datas.summary.properties.size) {
            datas.summary.properties.get(i).label match {
                case "impression" => impression = datas.summary.properties.get(i)
                case "click" => click = datas.summary.properties.get(i)
                case "ctr" => ctr = datas.summary.properties.get(i)
                case "spent" => spent = datas.summary.properties.get(i)
                case _ =>
            }
        }
        //total
        table.addCell(cellDefault("", null, bgcolorRow))
        table.addCell(cellDefault("TOTAL", null, bgcolorRow))
        table.addCell(cellDefault(impression.change + "%\n" + impression.value.toLong + " vs " + impression.compareValue.toLong, null, bgcolorRow))
        table.addCell(cellDefault(click.change + "%\n" + click.value.toLong + " vs " + click.compareValue.toLong, null, bgcolorRow))
        table.addCell(cellDefault(ctr.change + "%\n" + Math.round(ctr.value * 10000).toInt / 10000.0 + " vs " + Math.round(ctr.compareValue * 10000).toInt / 10000.0, null, bgcolorRow))
        table.addCell(cellDefault(spent.change + "%\n" + spent.value.toLong + " vs " + spent.compareValue.toLong, null, bgcolorRow))
        table
    }

}

class ExportReportServlet extends HandlerContainerServlet{
    def factory(): BaseHandler = new ExportReportHandler(Environment)
}
*/
//class ReportServlet extends HandlerContainerServlet{
//    def factory(): BaseHandler = new ReportHandler(Environment)
//}

class FastReportServlet extends HandlerContainerServlet{
    def factory(): BaseHandler = new FastReportHandler(Environment)
}


