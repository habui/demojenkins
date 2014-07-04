
package ads.website

import org.eclipse.jetty.server.{nio, Server}
import org.eclipse.jetty.server.nio.SelectChannelConnector
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.eclipse.jetty.servlet.ServletHandler
import ads.website.handler.{MiscServlet, ZoneGroupServlet}
import ads.website.modules._
import ads.website.modules.serving._
import scala.concurrent.ops._
import org.eclipse.jetty.util.thread
import ads.common.model.Config
import java.util.{UUID, TimeZone}
import ads.common.sessions.{RetargetingStore, SiteZoneFreqStore, Session}

object Main {

    def main(args: Array[String]) {

        println("******")
        spawn {
            Environment.serving.run()
            //Environment.reportService.run()
            Environment.zoneModelService.load(1)
            Environment.bannerModelService.load(1)
        }

        println(s"Static: ${Config.jsDomain.getValue}")
        println(s"Click : ${Config.clickTrack.getValue}")

        val server = new Server()
        //server.setHandler(new RenderJsHandler())

        val adminConnector = new SelectChannelConnector()
        adminConnector.setPort(Integer.parseInt(Config.portSlave.get()))
        adminConnector.setAcceptors(2)
        adminConnector.setRequestBufferSize(16*1024)
        adminConnector.setResponseBufferSize(128*1024)
        adminConnector.setAcceptQueueSize(100)
        adminConnector.setThreadPool(new thread.QueuedThreadPool(8))


        val connector = new SelectChannelConnector()
        connector.setPort(Integer.parseInt(Config.portMaster.get()))
        connector.setAcceptors(Runtime.getRuntime.availableProcessors())
        connector.setRequestBufferSize(16 * 1024)
        connector.setResponseBufferSize(16 * 1024)
        connector.setAcceptQueueSize(5000)
        connector.setThreadPool(new QueuedThreadPool(Runtime.getRuntime.availableProcessors() * 4))

        server.setConnectors(Array(connector, adminConnector))


        val servlet = new ServletHandler()

        //handler.addServletWithMapping(classOf[SimpleCalc], "/math/*")

        //handler.addServletWithMapping(classOf[com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet], "/hystrix.stream")
        //
        //

        //        handler.addServletWithMapping(classOf[TrackServlet], "/pixel.gif/*")

        servlet.addServletWithMapping(classOf[RenderJsServlet], "/rd.js/*")
        servlet.addServletWithMapping(classOf[SessionServlet], "/rest/session/*")
        servlet.addServletWithMapping(classOf[WebsiteServlet], "/rest/website/*")
        servlet.addServletWithMapping(classOf[ZoneServlet], "/rest/zone/*")
        servlet.addServletWithMapping(classOf[ZoneGroupServlet], "/rest/zone_group/*")
        servlet.addServletWithMapping(classOf[UserServlet], "/rest/user/*")
        servlet.addServletWithMapping(classOf[MiscServlet], "/rest/misc/*")
        servlet.addServletWithMapping(classOf[OrderServlet], "/rest/order/*")
        servlet.addServletWithMapping(classOf[CampaignServlet], "/rest/campaign/*")
        servlet.addServletWithMapping(classOf[BookingServlet], "/rest/book/*")
        servlet.addServletWithMapping(classOf[BannerServlet], "/rest/banner/*")
        servlet.addServletWithMapping(classOf[FastReportServlet], "/rest/report/*")
        servlet.addServletWithMapping(classOf[UserRoleServlet],"/rest/user_role/*")
        servlet.addServletWithMapping(classOf[RoleServlet],"/rest/role/*")
        servlet.addServletWithMapping(classOf[CategoryServlet],"/rest/category/*")
        servlet.addServletWithMapping(classOf[ActionLogServlet],"/rest/action_log/*"    )
        servlet.addServletWithMapping(classOf[ConversionServlet],"/rest/conversion/*")
        servlet.addServletWithMapping(classOf[ArticleServlet], "/rest/article/*")//PR network
        servlet.addServletWithMapping(classOf[UtilsServlet], "/rest/utils/*")//utilities
        servlet.addServletWithMapping(classOf[FastReportServlet], "/rest/fastreport/*")
        servlet.addServletWithMapping(classOf[SearchServlet], "/rest/extend/*")
        servlet.addServletWithMapping(classOf[ConfigTableServlet], "/rest/config/*")

        servlet.addServletWithMapping(classOf[TrackServlet], "/track/*")
        servlet.addServletWithMapping(classOf[TrackUserAgentServlet], "/uatrack/*")
        servlet.addServletWithMapping(classOf[PageviewServlet],"/ztrk")
        servlet.addServletWithMapping(classOf[PageviewHitServlet],"/_za.gif/*")
        servlet.addServletWithMapping(classOf[RenderServlet], "/rd/*")
        servlet.addServletWithMapping(classOf[RenderExtServlet], "/rdext/*")
        servlet.addServletWithMapping(classOf[ApiServlet], "/api/expr/*")
        servlet.addServletWithMapping(classOf[RenderVideoServlet], "/zad/videoad/*")
        servlet.addServletWithMapping(classOf[TrackVideoServlet], "/zad/vtrack/*")
        servlet.addServletWithMapping(classOf[TrackPRServlet], "/zad/prtrack/*")
        servlet.addServletWithMapping(classOf[RenderVideoCompanionServlet], "/zad/videocomp/*")
        servlet.addServletWithMapping(classOf[RenderMultiBannerServlet], "/renders.js/*")
        servlet.addServletWithMapping(classOf[MultiRenderServlet], "/renders2.js/*")
        servlet.addServletWithMapping(classOf[RenderBannerHtmlServlet], "/render.html/*")
        servlet.addServletWithMapping(classOf[RenderBannerExpandableServlet], "/richmedia_js.js/*")
        servlet.addServletWithMapping(classOf[RenderBannerBalloonServlet], "/balloon_js.js/*")
        servlet.addServletWithMapping(classOf[TestServingServlet], "/testserving.html/*")
        //servlet.addServletWithMapping(classOf[ExportReportServlet], "/rest/exportreport/*")
        servlet.addServletWithMapping(classOf[RenderAjsServlet], "/ajs.js/*")
        servlet.addServletWithMapping(classOf[NewBookingServlet], "/rest/new_book/*")
        servlet.addServletWithMapping(classOf[RenderCrossDomainServlet], "/crossdomain.xml")
        servlet.addServletWithMapping(classOf[RenderProxyServlet], "/proxy.html")
        servlet.addServletWithMapping(classOf[UniqueUserServlet], "/rest/uniqueuser/*")
        servlet.addServletWithMapping(classOf[AgencyServlet], "/rest/agency/*")


        //
        //        val context = new WebAppContext()
        //        context.setContextPath("/resource")
        //        context.setBaseResource(new ResourceCollection(Array("/usr/local/nginx/html/resource")))
        //
        //        val handlers = new HandlerList()
        //        handlers.setHandlers(Array(context, servlet))
        //
        //        server.setHandler(handlers)

        server.setHandler(servlet)

        try {
            spawn {
                try {
                    Session.start()
                    SiteZoneFreqStore.start()
                    RetargetingStore.start()
                } catch {
                    case e: Exception => e.printStackTrace()
                }

            }
            server.start()
            server.join()
        }
        catch {
            case e: Exception => e.printStackTrace()
        }
    }
}
