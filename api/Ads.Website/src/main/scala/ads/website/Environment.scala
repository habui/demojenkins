package ads.website

import ads.common.model._
import ads.common.services.{IFastReportService, ISessionService, SessionService, DataServiceEnv}
import ads.website.modules._
import ads.common.database.IDataService
import javax.jws.WebService
import java.util.concurrent.ConcurrentHashMap

//import ads.common.services.report.{MockReportService, IReportService}
import ads.common.services.serving.IServingEngine
import ads.website.modules.serving._
import ads.serving.ServingEngine
import ads.web.mvc.SessionServiceInBase
import ads.common.rpc.{ProxyGen, ThriftClient, Rpc}
import scala.concurrent.ops._
import scala.collection.mutable.ArrayBuffer
import ads.common.Syntaxs._

trait WebServiceEnv
    extends DatabaseEnv
    with DataServiceEnv{
    lazy val zoneGroupModelService: IZoneGroupModelService = new ZoneGroupModelService(this)
    lazy val zoneModelService: IZoneModelService = new ZoneModelService(this)
    lazy val websiteModelService: IWebsiteModelService = new WebsiteModelService(this)
    lazy val bannerModelService: IBannerModelService = new BannerModelService(this)
    lazy val orderModelService: IOrderModelService = new OrderModelService(this)
    lazy val agencyModelService : IAgencyModelService = new AgencyModelService(this)
    lazy val actionLogModelService : IActionLogModelService = new ActionLogModelService(this)
    lazy val roleModelService : IDataService[RoleModel] = new RoleModelService(this)
    lazy val userRoleModelService : IDataService[UserRoleModel] = new UserRoleModelService(this)
    lazy val campaignModelService: IDataService[CampaignModel] = new CampaignModelService(this)
    lazy val bookingService: IBookingService = new BookingService(this)

    lazy val userModelService : IDataService[UserModel] = new UserModelService(this)
    lazy val conversionModelService: IConversionModelService = new ConversionModelService(this)
    //service for article
    lazy val articleCommentModelService:IDataService[ArticleCommentModel] = new ArticleCommentModelService(this)

    lazy val newBookingService: INewBookingService = new NewBookingService(this)
    lazy val configTableModelService: IConfigTableModelService = new ConfigTableModelService(this)
}

trait IComplexServices{
    //lazy val reportService: IReportService = new ReportService(Environment)
    var fastReportService: IFastReportService = startFastReportService
    lazy val serving: IServingEngine = new ServingEngine(Environment)
    lazy val track: ITrack = new ImpressionTrack2(()=>System.currentTimeMillis())
    lazy val fastCachedLogService: IFastCachedLogService =  new FastCachedLogService(Config.fastCachedLog.get())
    val log: ILog = createLog
    lazy val compressService: ICompressService = new CompressService(Config.compressFolder.getValue)

    var isReconnectLogService = false
    var isReconnectReportService: Boolean = false
    def startFastReportService(): IFastReportService = {
        if (Config.isMaster){
            val fastReportService = new FastReportService(Environment)
            val server = new Thread(new Runnable{
                def run(){
                    Rpc.register("report", fastReportService)
                    println(s"Server report is listening port ${Config.masterPort.getValue}")
//                    Rpc.startServer(Config.masterPort.getValue)
                }
            })
            server.start()
            fastReportService.run()
            fastReportService
        }
        else{
            // client
            connectReportService
            null
        }
    }

    def connectReportService() {
        if (!Environment.isReconnectReportService) {
            Environment.isReconnectReportService = true
            spawn {
                var remoteReport: IFastReportService = null
                var connected = false
                while (!connected){
                    try {
                        val client = new ThriftClient(Config.masterIp.getValue, Config.masterPort.getValue)
                        remoteReport = ProxyGen.createProxy[IFastReportService]("report", client.process)
                        remoteReport.getImpressionClick(0,0,0,"item")
                        println("Connected to report service at ip " + Config.masterIp.getValue + ":" + Config.masterPort.getValue)
                        connected = true
                    }
                    catch {
                        case x : Throwable => {
                            println("Can't connect to report service at ip " + Config.masterIp.getValue + ":" + Config.masterPort.getValue)
                            connected = false
                            Thread.sleep(3000)
                        }
                    }
                }
                Environment.isReconnectReportService = false
                Environment.fastReportService = remoteReport
            }
        }
    }

    def createLog(): ILog = {
        val masterLog: ILog = new Log
        if (Config.isMaster){
            // start server
            val server = new Thread(new Runnable{
                def run(){
                    Rpc.register("log", masterLog)
                    println(s"Service log is listening port ${Config.masterPort.getValue}")
                    Rpc.startServer(Config.masterPort.getValue)
                }
            })
            server.start()
            masterLog
        }
        else{
            // client
            spawn {
                connectLogService(masterLog)
            }
            Thread.sleep(1000)
            masterLog
        }
    }

    def connectLogService(masterLog: ILog) {
        var remotelog: IMasterLog = null
        isReconnectLogService = true
        var connected = false
        while (!connected){
            try
            {
                val client = new ThriftClient(Config.masterIp.getValue, Config.masterPort.getValue)
                remotelog = ProxyGen.createProxy[IMasterLog]("log", client.process)
                remotelog.remoteFlush(new ArrayBuffer[LogRecord]())
                println("Connected to log service at ip " + Config.masterIp.getValue + ":" + Config.masterPort.getValue)
                connected = true
            }
            catch {
                case x : Throwable => {
                    println("Can't connect to log service at ip " + Config.masterIp.getValue + ":" + Config.masterPort.getValue)
                    connected = false
                    Thread.sleep(3000)
                }
            }
        }
        isReconnectLogService = false
        masterLog.masterLog = remotelog
    }


    //Log User Agent
    val logUserAgent: ILogUserAgent = createLogUserAgent
    var isReconnectLog2Service = false

    def createLogUserAgent(): ILogUserAgent = {
        val masterLog: ILogUserAgent = new LogUserAgent
        if (Config.isMaster){
            val server = new Thread(new Runnable{
                def run(){
                    Rpc.register("loguseragent", masterLog)
                    println(s"Service log2 is listening port ${Config.masterPort.getValue}")
                }
            })
            server.start()
            masterLog
        }
        else{
            // client
            spawn {
                connectLogUserAgentService(masterLog)
            }
            Thread.sleep(1000)
            masterLog
        }
    }

    def connectLogUserAgentService(masterLog: ILogUserAgent) {
        var remotelog: IMasterLogUserAgent = null
        isReconnectLog2Service = true
        var connected = false
        while (!connected){
            try
            {
                val client = new ThriftClient(Config.masterIp.getValue, Config.masterPort.getValue)
                remotelog = ProxyGen.createProxy[IMasterLogUserAgent]("loguseragent", client.process)
                remotelog.remoteFlush(new ArrayBuffer[UserAgentRecord]())
                println("Connected to log2 service at ip " + Config.masterIp.getValue + ":" + Config.masterPort.getValue)
                connected = true
            }
            catch {
                case x : Throwable => {
                    println("Can't connect to log2 service at ip " + Config.masterIp.getValue + ":" + Config.masterPort.getValue)
                    connected = false
                    Thread.sleep(3000)
                }
            }
        }
        isReconnectLog2Service = false
        masterLog.masterLog = remotelog
    }

}

class ModelCache[T](load: Int => T)(implicit m: reflect.Manifest[T]) {
    val cacheTime = 10*1000
    val dict = new ConcurrentHashMap[Int, (Long,T)]()

    def get(id: Int) = {
        val now = System.currentTimeMillis()
        val (time, model) = dict.getOrAdd(id, (x) => (now, load(x)))
        if (time + cacheTime < now){
            dict.put(id, (now, load(id)))
        }
        model
    }
}

object Environment
    extends WebServiceEnv
    with IComplexServices {
    SessionServiceInBase.Set(sessionService)

    val bannerCache = new ModelCache[BannerModel](bannerModelService.load)
    val zoneCache = new ModelCache[ZoneModel](zoneModelService.load)
    val siteCache = new ModelCache[WebsiteModel](websiteModelService.load)
}


