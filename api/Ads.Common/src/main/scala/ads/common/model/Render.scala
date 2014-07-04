package ads.common.model

import ads.common.SecurityUtils
import java.io.{StringWriter, File}
import com.netflix.config.DynamicPropertyFactory
import com.github.mustachejava.{Mustache, DefaultMustacheFactory}
import java.util.concurrent.{ConcurrentHashMap, ConcurrentLinkedQueue}
import scala.concurrent.ops._

object Config{
    //val domain = "http://210.211.108.205:8080"
    //val jsDomain  = "http://210.211.108.205"
    val factory = DynamicPropertyFactory.getInstance()
    val frequencyDir = factory.getStringProperty("freq.dir", "C:/Data_bk/feq_temp")
    val retargetingPrDir = factory.getStringProperty("retargeting.pr.dir", "C:/Data_bk/retargeting_pr")
    val frequencyRemotes = factory.getStringProperty("freq.remotes", "127.0.0.1;")
    val jsDomain = factory.getStringProperty("url.js.domain", "http://localhost:1795")
    val hostDomain = factory.getStringProperty("host.domain", "http://localhost:8080")
    val pathLog = factory.getStringProperty("path.log.data", "C:/Data_bk/")
    val cachedLog = factory.getStringProperty("report.cached.path", "C:/Data_bk/cachedLog/cachedLog.txt")
    val zingTVReportLog = factory.getStringProperty("report.zingtv.path", "C:/Data_bk/cachedLog/ZingTV")
    val zingTVCRMReportLog = factory.getStringProperty("crmreport.zingtv.path", "C:/Data_bk/cachedLog/ZingTVCRM")
    val fastCachedLog = factory.getStringProperty("report.fastcached.path", "C:/Data_bk/cachedLog/fastCachedLog.txt")
    val uploadDir = factory.getStringProperty("upload.dir", "")
    val pathTemplate = factory.getStringProperty("path.template", "D:/projects/123click_new/servers/Ads.Common/out")
    val uploadDomain = factory.getStringProperty("upload.domain", "")
    val serverDomain = factory.getStringProperty("url.domain", "http://localhost:8080")
    val portMaster = factory.getStringProperty("port_master", "8080")
    val portSlave = factory.getStringProperty("port_slave", "8082")
    val remoteAddr = factory.getStringProperty("remote_address", "10.30.46.61_3,10.30.46.61_4")
    val remoteClientData = factory.getStringProperty("remote_client","crm_4")
    val urlDeliverTracking = factory.getStringProperty("url.url_deliver_tracking", "")
    val hitTrack = factory.getStringProperty("url.track.hit", "http://api.adtimaserver.vn/track/hit.gif")
    val videoTrack = factory.getStringProperty("url.track.video", "http://api.adtimaserver.vn/zad/vtrack")
    val clickTrack = factory.getStringProperty("url.track.click", "http://api.adtimaserver.vn/track/click")
    val prTrack = factory.getStringProperty("url.track.pr", "http://api.adtimaserver.vn/zad/prtrack")
    val mapRemoteClient = new ConcurrentHashMap[String, Int]()
    for(data <- remoteClientData.getValue.split(",") if remoteClientData.getValue.split(",").size > 0) {
        println("Map : " + data.split("_")(0) + " : " + data.split("_")(1))
        mapRemoteClient.put("remote_cookies=" + data.split("_")(0), data.split("_")(1).toInt)
    }

    val serverIp = System.getProperty("server_prefix","58728080");
    val rootUserName = factory.getStringProperty("root.username","root,phuongdm,habns")
    val isWindow = System.getProperty("os.name").toLowerCase.indexOf("win") >= 0
    val pathData = factory.getStringProperty("path.database","C:/Data_bk/db/")
    val zooKeeperAddr = factory.getStringProperty("zookeeper_address", "127.0.0.1:2181")
    val compressFolder = factory.getStringProperty("compress.path", "C:/Data_bk/log/compress/")

    val isMaster = "true".equals(System.getProperty("server_is_master","true"))
    System.out.println("isMaster:"+isMaster)
    //factory.getBooleanProperty("is.master", true)
    val showLog = "true".equals(System.getProperty("show_log","false"))
    val storeDB = "true".equals(System.getProperty("store_db","false"))
    val masterPort = factory.getIntProperty("master.port", 9999)
    val masterIp = factory.getStringProperty("master.ip", "10.30.58.205")

    val geopipDatabase = factory.getStringProperty("geoip_database","C:\\\\GeoLiteCity.dat")

    val secretKeyZNews = factory.getStringProperty("secretkey.zingnews","4gdg4fds25f34")
    val urlCreateArticleZNews = factory.getStringProperty("url.create.zingnews","http://admin.news.zing.vn/api/Adtimapr/newpost.aspx")
    val urlReportArticleZNews = factory.getStringProperty("url.report.zingnews","http://admin.news.zing.vn/api/Adtimapr/report.aspx")

    val ctrDefault = factory.getFloatProperty("abc", 0.003.toFloat)
    val fullHostDomain = if (hostDomain.getValue.startsWith("//")) "http:" + hostDomain.getValue else hostDomain.getValue

    val proxyHost = factory.getStringProperty("proxy.host","10.30.12.30")
    val proxyPort = factory.getIntProperty("proxy.port",81)

    val dataPublisher = factory.getStringProperty("publishers", "zingnews_71,zingnews_105,zingnews_110")
    val mapPublishers : Map[String,Int] = dataPublisher.get().split(",").map{a=>(a.split("\\_")(0),a.split("\\_")(1).toInt)}.toMap

    val maxCacheSize = factory.getIntProperty("max.cache.size", 32000)
    val cacheExpire = factory.getIntProperty("cache.expire", 30*24*3600)

    val secretKeyForUpload = factory.getStringProperty("secretkey.upload","adtima_123456")

    val smtpHost = factory.getStringProperty("mail.smtp.host", "10.30.12.52")
    val smtpPort = factory.getStringProperty("mail.smtp.port", "25")
    val smtpUser = factory.getStringProperty("mail.user", "noti@adtima.vn")
    val smtpPassword = factory.getStringProperty("mail.password", "")
    val isAuth = factory.getBooleanProperty("mail.isauth", false)
    val articlePublisherURL = factory.getStringProperty("article.manange.url", "http://manage.adtima.vn/#/websites/siteId/zoneId/articleId/setting")
    val articleSaleURL = factory.getStringProperty("article.sale.url", "http://manage.adtima.vn/#/orders/orderId/campaignId/articleId/setting-pr")
    val mailSubject = factory.getStringProperty("mail.subject", "Adtima PR Article is ACTION [ITEMNAME]")

    // Frequency Capping Config
    val numOfServers = factory.getIntProperty("frequency_capping_num_server", 6)
    val timeUpdateStatusPr = factory.getIntProperty("time_update_status_pr", 30)

    // config stream.adtimaserver.vn
    val streamAdtimaServerInUse = factory.getBooleanProperty("stream.adtima.server.in.use", true).getValue
    val streamAdtimaServerByProxy = factory.getBooleanProperty("stream.adtima.server.proxy.use", true).getValue
    val streamAdtimaServerProxyHost = factory.getStringProperty("stream.adtima.server.proxy.host", "10.30.58.36").getValue
    val streamAdtimaServerProxyPort = factory.getIntProperty("stream.adtima.server.proxy.port", 81).getValue
    val streamAdtimaServerHost= factory.getStringProperty("stream.adtima.server.host", "120.138.68.165").getValue
    val streamAdtimaServerPort = factory.getIntProperty("stream.adtima.server.port", 21).getValue
    val streamAdtimaServerDomain = factory.getStringProperty("stream.adtima.server.domain", "stream.adtimaserver.vn").getValue
    val streamAdtimaServerUsername = factory.getStringProperty("stream.adtima.server.username", "adtima").getValue
    val streamAdtimaServerPassword = factory.getStringProperty("stream.adtima.server.password", "V9htUmFb03+h4m8l").getValue

    // config image magick
    val imageMagickPath = factory.getStringProperty("image.magick.path", "/usr/bin").getValue
    val isResizeOverlayBanner = factory.getBooleanProperty("overlay.banner.is.resize", false).getValue

//    val sizes = factory.getStringProperty("overlay.banner.sizes", "").getValue
//    var overlayBannerThumbSizes = List[OverlayBannerSize]()
//    val arr = sizes.split(",")
//    for (str <- arr) {
//        overlayBannerThumbSizes = overlayBannerThumbSizes :+ OverlayBannerSize(str)
//    }


    //create folder log and cachedLog if not exist
    var file: File = new File(fastCachedLog.getValue)
    file.getParentFile.mkdirs()
    //create folder compress log if not exist
    file = new File(compressFolder.getValue)
    file.mkdirs()

    // jsoup
    System.setProperty("http.proxyHost", proxyHost.getValue);
    System.setProperty("http.proxyPort", proxyPort.getValue.toString);
}

class TemplateEngine(){
    var factory = new DefaultMustacheFactory()
    val cache = new ConcurrentHashMap[String,Mustache]()
    val queue = new ConcurrentLinkedQueue[StringWriter]()
    var workingDirectory: File = null

    spawn {
        while(true) {
            Thread.sleep(5*60*1000)
            factory = new DefaultMustacheFactory()
            cache.clear()
        }
    }

    def convertJavaList(list: List[Map[String, Any]]): java.util.ArrayList[java.util.HashMap[String, Any]] = {
        val javaList = new java.util.ArrayList[java.util.HashMap[String, Any]]
        list.foreach{ e =>
            javaList.add(convertJavaMap(e))
        }
        javaList
    }

    def convertJavaMap(map: Map[String, Any]): java.util.HashMap[String, Any] = {
        val javaMap = new java.util.HashMap[String, Any]
        for (entry <- map) {
            if (entry._2.isInstanceOf[List[Map[String, Any]]]) {
                javaMap.put(entry._1, convertJavaList(entry._2.asInstanceOf[List[Map[String, Any]]]))
            } else {
                javaMap.put(entry._1, entry._2)
            }
        }
        javaMap
    }

    def layout(path: String, map: Map[String,Any]):String = {

        var file = path
        if (workingDirectory != null) file = workingDirectory.getPath + "/" + path

        if (!cache.containsKey(file)){
            cache.put(file, factory.compile(file))
        }

        val javaMap = convertJavaMap(map)

        var w = queue.poll()
        if (w == null) w = new StringWriter()
        else w.getBuffer.setLength(0)

        val m = cache.get(file)
        m.execute(w, javaMap)
        val s = w.toString()
        queue.add(w)
        s
    }
}

object RenderTemplate {
    val VIDEOAD_LINEAR = "videoad-linear"
    val VIDEOAD_LINEAR_WRAPPER = "videoad-linear-wrapper"
    val VIDEOAD_NONLINEAR = "videoad-nonlinear"
    val VIDEOAD_NONLINEAR_WRAPPER = "videoad-nonlinear-wrapper"
}

object Render {

    val engine = new TemplateEngine(){
        workingDirectory = new File(Config.pathTemplate.getValue)
    }

    def renderZone(zone: Zone): String ={
        if (zone == null) return ""

        val zoneId = SecurityUtils.encode(zone.id)
        val inline = ""
        val renderKind = if (zone.renderKind != null && zone.renderKind.length > 0) s"""z2-type="${zone.renderKind}" """ else ""
        engine.layout("templates/zone.mustache",
            Map(
                "domain" -> Config.jsDomain.getValue,
                "width" -> zone.width,
                "height" -> zone.height,
                "id" -> zoneId,
                "z2-type" -> renderKind,
                "inline" -> inline))
    }

    def renderTrack(domain: String, zaFile: String, params: String): String = {
        engine.layout("templates/track.mustache",
            Map(
                "domain" -> domain,
                //"za_file" -> zaFile,
                "params" -> params
            ))
    }
    def renderIEHtml(map:Map[String,Any]):String = engine.layout("templates/iebanner.mustache",map)
    def renderExHtml(map:Map[String,Any]):String = engine.layout("templates/exbanner.mustache",map)
    def renderExpandableJs(map:Map[String,Any]):String = engine.layout("templates/richmedia_js.mustache",map)
    def renderBalloonJs(map:Map[String,Any]):String = engine.layout("templates/balloon_js.mustache",map)
    def renderAJs(map:Map[String,Any]):String = engine.layout("templates/ajs.mustache",map)
    def renderHtml(map: Map[String,Any]): String = engine.layout("templates/banner.mustache", map)
    def renderVideo(map: Map[String,Any]): String = engine.layout("templates/videoad.mustache", map)
    def renderVideoLinear(map: Map[String,Any]): String = engine.layout("templates/videoad_linear.mustache", map)
    def renderVideoLinearWrapper(map: Map[String,Any]): String = engine.layout("templates/videoad_linear_wrapper.mustache", map)
    def renderVideoNonLinear(map: Map[String,Any]): String = engine.layout("templates/videoad_nonlinear.mustache", map)
    def renderVideoNonLinearWrapper(map: Map[String,Any]): String = engine.layout("templates/videoad_nonlinear_wrapper.mustache", map)
    def renderVideoLinearCompanion(map: Map[String, Any]): String = engine.layout("templates/videoad_linear_companion.mustache", map)
    def renderVideoLinearWrapperCompanion(map: Map[String, Any]): String = engine.layout("templates/videoad_linear_wrapper_companion.mustache", map)
    def renderVideoCompanionLink(map: Map[String, Any]): String = engine.layout("templates/videoad_companion_link.mustache", map)
    def renderVideoCompanion(map: Map[String, Any]): String = engine.layout("templates/videoad_companion.mustache", map)
    def renderEcommerceBanner(map: Map[String,Any]): String = engine.layout("templates/ecommerce_banner.mustache", map)
    def renderConversion(map: Map[String,Any]): String = engine.layout("templates/conversion.mustache", map)
    def renderCrossdomain(map:Map[String,Any]):String = engine.layout("templates/crossdomain.mustache",map)
    def renderProxy(map:Map[String,Any]):String = engine.layout("templates/proxy.mustache",map)
    def renderVideoPauseAd(map: Map[String,Any]): String = engine.layout("templates/videoad_pause.mustache", map)
    def renderSkipAd(map:Map[String,Any]):String = engine.layout("templates/skipad.mustache",map)
    def renderPrBanner1(map:Map[String,Any]):String = engine.layout("templates/prbanner_1.mustache",map)
    def renderPrBanner2(map:Map[String,Any]):String = engine.layout("templates/prbanner_2.mustache",map)
    def renderPrBanner3(map:Map[String,Any]):String = engine.layout("templates/prbanner_3.mustache",map)
    def renderRegion(map:Map[String,Any]):String = engine.layout("templates/region.mustache",map)
}
