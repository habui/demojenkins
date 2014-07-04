package ads.common.services

import ads.common.services.report._
import java.util
import java.util.Date
import javax.servlet.http.HttpServletResponse

trait IFastReportService {
    def getReport(request: ReportRequest, userName: String): NewReportResponse
    def getCRMReport(request : CRMReportRequest): Object
    def getImpressionClick(bannerId: Int, from: Long, to: Long, kind: String): (Long, Long)
    def getZingTVReport(response: HttpServletResponse,range: Range)
    def getZingTVCRMReport(response: HttpServletResponse, date: Long)
    def cacheReportZingTV(date: Date)
    def run()
}

