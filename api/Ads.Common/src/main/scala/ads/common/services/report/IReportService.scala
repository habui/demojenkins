package ads.common.services.report

import java.util
import java.util.Random

/*
class ReportRecord(var itemId: Int, var click: Long, var impression: Long, var ctr: Double)

trait IReportService {
    def getReport(request: ReportRequest): ReportResponse
    def getReportOrders(request: ReportRequest): ReportResponse
    def getReportItem(request: ReportRequest): ReportResponse
    def run()
    def getImpression(bannerId: Int, from: Long, duration: Long): Long
    def getInfo(bannerId: Int, from: Long, duration: Long): ReportRecord
    def getCRMReport(request : CRMReportRequest) : CRMReportResponse
}


class MockReportService extends IReportService{
    def getReportItem(request: ReportRequest): ReportResponse = {
        val response = new ReportResponse(
            new Range(1379610000000L, 1382202000000L),
            new Range(1376931600000L, 1379610000000L),
            3,
            null,
            null,
            new Summary(properties = createProperties())
        )
        return response
    }
    def getReportOrders(request: ReportRequest): ReportResponse = {
        val response = new ReportResponse(
            new Range(1379610000000L, 1382202000000L),
            new Range(1376931600000L, 1379610000000L),
            3,
            null,
            null,
            new Summary(properties = createProperties())
        )
        return response
    }
    def createProperties(): util.ArrayList[ReportItemProperty] = {
        val r = new util.ArrayList[ReportItemProperty]()
        r.add(new ReportItemProperty("impr", 120000, 132000, 10))
        r.add(new ReportItemProperty("click", 108, 110, 1.83))
        r.add(new ReportItemProperty("ctr", 0.04, 0.05, 23.39))
        r.add(new ReportItemProperty("spent", 1200000, 1320000, 10))
        return r
    }

    def getReport(request: ReportRequest): ReportResponse = {
        val mockItems = new util.ArrayList[ReportItem]()
        mockItems.add(new ReportItem(
            name = "order 001",
            id = 0,
            properties = createProperties()
        ))
        mockItems.add(new ReportItem(
            name = "order 002",
            id = 0,
            properties = createProperties()
        ))
        mockItems.add(new ReportItem(
            name = "order 003",
            id = 0,
            properties = createProperties()
        ))

        val points = new util.ArrayList[GraphPoint]()
        val comparePoints = new util.ArrayList[GraphPoint]()
        val random = new Random();
        for (i <- 0 until 10) points.add(new GraphPoint(random.nextInt(10), 1379610000000L + 1000*60*60*i))
        for (i <- 0 until 10) comparePoints.add(new GraphPoint(random.nextInt(10) + 5, 1382202000000L + 1000*60*60*i))

        val mockGraph = new ReportGraph(points, comparePoints)

        val response = new ReportResponse(
            new Range(1379610000000L, 1382202000000L),
            new Range(1376931600000L, 1379610000000L),
            3,
            mockItems,
            mockGraph,
            new Summary(properties = createProperties())
        )
        return response
    }

    def run(): Unit = {}

    def getImpression(bannerId: Int, from: Long, duration: Long): Long  = 0

    def getInfo(bannerId: Int, from: Long, duration: Long): ReportRecord = ???

    def getCRMReport(request : CRMReportRequest) : CRMReportResponse = {
        new CRMReportResponse(null)
    }
}
*/