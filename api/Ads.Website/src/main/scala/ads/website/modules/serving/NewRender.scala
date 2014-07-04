package ads.website.modules.serving

import ads.common.services.serving.{AdsRequest, IServingEngine}
import ads.common.database.IDataService
import ads.website.modules._
import ads.web.mvc.BaseHandler
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import ads.common.SecurityUtils
import ads.web.{MacroInsertion, AdsUtils, WebUtils, HandlerContainerServlet}
import ads.common.model._
import ads.common.Syntaxs._
import WebUtils._
import java.net.URLDecoder
import scala.collection.JavaConversions._
import ads.website.{ModelCache, Environment}
import scala.collection.mutable.ArrayBuffer
import java.util
import org.apache.commons.lang.StringEscapeUtils
import java.util.concurrent.{ThreadLocalRandom, ConcurrentHashMap}
import ads.common.geoip.GeoipService


class MultiRenderHandler (env: {
    val serving: IServingEngine
    val zoneModelService: IZoneModelService
    val zoneService: IDataService[Zone]
    val campaignService: IDataService[Campaign]
    val campaignModelService: IDataService[CampaignModel]
    val bannerModelService: IBannerModelService
    val bannerCache : ModelCache[BannerModel]
    val zoneCache : ModelCache[ZoneModel]
    val log: ILog})
    extends RenderJsHandler(env) {
    class MultiRenderData(val zid: String, var bannerId: String = "", var data: String = "", var width: String = "0", var height: String = "0", var autoLayout: String = "false")

    val bannerCache = env.bannerCache
    val zoneCache = env.zoneCache


    override def internalProcess(request: HttpServletRequest, response: HttpServletResponse): Try[Unit] = Try {
        val arrayZone = request.getParameter("zones").split(",").map(z => SecurityUtils.decode(z)).toList
        var agent = request.getParameter("agent")
        val channel = request.getParameter("channel") ?? ""
        var dynamic = request.getParameter("dynamic") ?? ""
        val vs = request.getParameter("vs") ?? ""
        val dyValues = vs.split(";")
        val listValues =  new util.HashMap[String, List[String]]()
        val url = ""
        val ip = WebUtils.getRemoteAddress(request)
        val cookie = WebUtils.getOrSetCookie(request, response)
        var siteId = 0
        var listRender = new ArrayBuffer[MultiRenderData]()
        val location = GeoipService.getGeoipClient.getLocation(ip)
        val city = if (location != null) location.city else ""
        val task = new util.Random().nextInt(9998) + 1
        val companionTargeting = request.getParameter("postid") ?? null

        dyValues.map(item => {
            val t = item.split("=")
            if (t.size == 2) {
                listValues.put(t(0).toLowerCase, t(1).split(",").toList.map(b => b.toLowerCase))
            }
        })

        val req = new AdsRequest(0, arrayZone, url, ip, cookie, null, 1, companionTargeting, listValues ++ Map("os" -> List(WebUtils.getUserAgent(request))))
        val data = env.serving.serveCompanion(req).data


        for((zid, result) <- data if data != null) {
            val render = new MultiRenderData(SecurityUtils.encode(zid))

            val zone = zoneCache.get(zid)

            if(zone != null) {
                try {
                    dynamic = request.getParameter("zdyn")
                    if(dynamic !=null)
                        dynamic = URLDecoder.decode(dynamic,"UTF-8")
                }
                var orderId = 0
                var campId = 0
                var bannerId = 0
                siteId = if (zone != null) zone.siteId else 0
                val zoneGroupIds: Array[Int] = if (zone != null) zone.groups else Array.empty
                if(result != null && result.length > 0) {

                    val banner = bannerCache.get(result(0).id)

                    render.width = banner.width.toString
                    render.height = banner.height.toString

                    if(banner != null) {
                        render.bannerId = SecurityUtils.encode(banner.id)
                        //MacroInsertion.insertBanner(banner)

                        bannerId = banner.id
                        campId = banner.campaignId
                        orderId = env.campaignService.load(banner.campaignId).orderId


                        val default = Map("zoneId" -> SecurityUtils.encode(zone.id),
                            "itemId" -> SecurityUtils.encode(banner.id),
                            "token" -> SecurityUtils.generateTimeToken(),
                            "ip" -> ip)
                        lazy val deliveryUrl = AdsUtils.getTrackUrl("display",
                            default + ("track" -> "impression"))
                        lazy val clickUrl = AdsUtils.getTrackUrl("click",
                            default + ("track" -> "click"), banner.targetUrl)

                        lazy val trackSubClick = AdsUtils.getTrackUrl("click",
                            default + ("track" -> "trackSubClick"))
                        lazy val trackCollapse = AdsUtils.getTrackUrl("click",
                            default + ("track" -> "trackCollapse"))
                        lazy val trackExpand = AdsUtils.getTrackUrl("click",
                            default + ("track" -> "trackExpand"))
                        var thirdparty = ""
                        if(banner.thirdPartyImpressionUrl != null && banner.thirdPartyImpressionUrl.length > 0) {
                            for (i <- banner.thirdPartyImpressionUrl)
                                thirdparty += AdsUtils.wrapImg(i)
                        }

                        banner.kind match {
                            case BannerKind.Media | BannerKind.NetworkMedia =>
                                if (agent != "ie") render.data = renderBanner(zone, banner, dynamic, channel, WebUtils.checkFlash(request), deliveryUrl, clickUrl, thirdparty)
                                else render.data = SecurityUtils.encode(banner.id)
                            case BannerKind.Ecommerce => {
                                val list = env.bannerModelService.listByIds(result.map(b=>b.id).toList)
                                render.data = renderJs(zone, list, dynamic, channel, ip, zone.id == 10000)
                            }
                            case BannerKind.Expandable | BannerKind.NetworkExpandable => {
                                render.data = renderExandabled(zone,banner,deliveryUrl, clickUrl, trackSubClick, trackCollapse, trackExpand, thirdparty)
                                render.autoLayout = "true"
                            }
                            case BannerKind.Html | BannerKind.NetworkHtml => {
                                val b = banner.asInstanceOf[HtmlBannerModel]
                                render.autoLayout = b.autoLayout.toString
                                if (agent != "ie")
                                    render.data = renderJsHtml(zone, banner, deliveryUrl, dynamic, channel)
                                else if(b.autoLayout)
                                    render.data = renderAutoLayout(zone, b.id, b.embeddedHtml, deliveryUrl)
                                else render.data = SecurityUtils.encode(banner.id)
                            }
                            case BannerKind.Popup | BannerKind.NetworkPopup => {
                                render.data = renderPopup(zone,banner,deliveryUrl, clickUrl, thirdparty)
                                render.autoLayout = "true"
                            }
                            case BannerKind.Balloon | BannerKind.NetworkBalloon => {
                                render.data = renderBalloon(zone,banner,deliveryUrl, clickUrl, trackSubClick, trackCollapse, trackExpand, thirdparty)
                                render.autoLayout = "true"
                            }
                            case BannerKind.PrBanner | BannerKind.NetworkPrBanner => {
                                render.data = renderJsPrBanner(zone,banner,ip,req)
                            }
                            case _ => render.data = SecurityUtils.encode(banner.id)
                        }

                        render.data = MacroInsertion.insert(render.data)

                        listRender += render
                    }
//                    env.log.log(LogKind.Normal, task, city, siteId, zoneGroupIds, zone.id, orderId, campId, bannerId, WebUtils.getRemoteAddress(request).replace(',','|'), cookie, TrackKind.Impression, System.currentTimeMillis(), 0, 0)
//                    if(banner.isInstanceOf[INetwork] && banner.asInstanceOf[INetwork].rateUnit == RateUnit.CPC && ThreadLocalRandom.current().nextLong(100000) < 0.0015*100000) {
//                        env.log.log(LogKind.Normal, task, city, siteId, zoneGroupIds, zone.id, orderId, campId, bannerId, WebUtils.getRemoteAddress(request).replace(',','|'), cookie, TrackKind.Click, System.currentTimeMillis(), 0, 0)
//                    }
                }
                env.log.log(LogKind.Normal, task, city, siteId, zoneGroupIds, zone.id, orderId, campId, bannerId, WebUtils.getRemoteAddress(request).replace(',','|'), cookie, "render", System.currentTimeMillis(), 0, 0)
            }
        }
        env.log.log(LogKind.Normal, task, city, siteId, Array.empty, 0, 0, 0, 0, WebUtils.getRemoteAddress(request).replace(',','|'), cookie, "pageview", System.currentTimeMillis(), 0, 0)
        if (agent != "ie") agent = ""
        ("ZADS.renderMultiBanner"+agent.toUpperCase()+"(" + WebUtils.toRawJson(listRender) + ");") |> response.renderJs
    }

    def renderAutoLayout(zone: ZoneModel,bannerId : Int, content: String, deliveryUrl: String) :String = {
        val linkTrack = AdsUtils.wrapImg(deliveryUrl)
        return content.concat(linkTrack)
    }

    def renderExandabled(zone: ZoneModel, banner: BannerModel, deliveryUrl: String, clickUrl: String, trackSubClick: String, trackCollapse: String, trackExpand: String, thirdparty: String):String = {
        var osync = false
        val camp = env.campaignModelService.load(banner.campaignId)
        val item = banner.asInstanceOf[ExpandableBannerModel]
        if(camp.isInstanceOf[CampaignModel] && camp.asInstanceOf[CampaignModel].companion && item.expandDirection == 2)
            osync = true
        val embedded = AdsUtils.wrapItemExpandable(SecurityUtils.encode(zone.id),SecurityUtils.encode(banner.id),Config.serverDomain.getValue,clickUrl,trackSubClick,trackCollapse,trackExpand,osync).concat(AdsUtils.wrapImg(deliveryUrl)).concat(thirdparty)
        embedded
    }

    def renderJsPrBanner(zone: ZoneModel, banner: BannerModel, ip: String, request: AdsRequest) : String = {
        val data = renderPrBanner(zone,banner,ip,request)
        val content = data |> StringEscapeUtils.escapeJavaScript
        s"parent.ZADS.M2.fillInlineHTMLContent('${SecurityUtils.encode(zone.id)}'," + '"' + content + '"' + s",${banner.width}, ${banner.height})"
    }

    def renderJsHtml(zone: ZoneModel, banner: BannerModel, deliveryUrl: String, dynamic: String, channel: String): String = {
        val media = banner.asInstanceOf[HtmlBannerModel]
        val content: String = media.embeddedHtml.concat(AdsUtils.wrapImg(deliveryUrl));
        if (media.autoLayout == false)
            renderBannerHtml(banner, SecurityUtils.encode(zone.id), content)
        else content
    }

    def renderBalloon(zone: ZoneModel, banner: BannerModel, deliveryUrl: String, clickUrl: String, trackSubClick: String, trackCollapse: String, trackExpand: String, thirdparty: String): String = {
        var typeBaloon = 0
        val item = banner.asInstanceOf[BalloonBannerModel]
        if(item.iTVCExtension == 3) typeBaloon = 1
        var embedded = ""
        if(typeBaloon == 1)
            embedded = AdsUtils.wrapItemBalloonVast(SecurityUtils.encode(zone.id),banner,Config.serverDomain.getValue,clickUrl,trackSubClick,trackCollapse,trackExpand).concat(AdsUtils.wrapImg(deliveryUrl)).concat(thirdparty)
        else
            embedded = AdsUtils.wrapItemBalloon(SecurityUtils.encode(zone.id),SecurityUtils.encode(banner.id),Config.serverDomain.getValue,clickUrl,trackSubClick,trackCollapse,trackExpand).concat(AdsUtils.wrapImg(deliveryUrl)).concat(thirdparty)
        embedded
    }
    def renderPopup(zone: ZoneModel, banner: BannerModel, deliveryUrl: String, clickUrl: String, thirdparty: String): String = {
        var content:String = ""
        val media = banner.asInstanceOf[PopupBannerModel]
        if (media.bannerFile.endsWith(".swf"))
            content = AdsUtils.wrapItemFlash(SecurityUtils.encode(zone.id),clickUrl,media.bannerFile,"_blank",media.width,media.height,media.bannerFileFallback) + AdsUtils.wrapImg(deliveryUrl)
        else if (media.bannerFile.endsWith(".mp4"))
            content = AdsUtils.wrapItemVideo(SecurityUtils.encode(zone.id),clickUrl,media.bannerFile,"_blank",media.width,media.height) + AdsUtils.wrapImg(deliveryUrl)
        else
            content = AdsUtils.wrapItemImage(clickUrl,media.bannerFile,"_blank","","") + AdsUtils.wrapImg(deliveryUrl)
        val trackClick = if (media.actionCloseBtn != null && media.actionCloseBtn == "move_to_target") clickUrl else "#"
        val embedded = AdsUtils.wrapItemPopup(SecurityUtils.encode(zone.id),content,Config.jsDomain.getValue,trackClick).concat(AdsUtils.wrapImg(deliveryUrl)).concat(thirdparty)
        embedded
    }

    def renderBanner(zone: ZoneModel, banner: BannerModel, dynamic: String, channel: String, flashSupport: Boolean, deliveryUrl: String, clickUrl: String, thirdparty: String): String = {
        val media = banner.asInstanceOf[MediaBannerModel]
        var content:String = "";
        if (media.bannerFile.endsWith(".swf"))
            content = AdsUtils.wrapItemFlash(SecurityUtils.encode(zone.id),clickUrl,media.bannerFile,"_blank",media.width,media.height,media.bannerFileFallback) + AdsUtils.wrapImg(deliveryUrl)
        else if (media.bannerFile.endsWith(".mp4"))
            content = AdsUtils.wrapItemVideo(SecurityUtils.encode(zone.id),clickUrl,media.bannerFile,"_blank",media.width,media.height) + AdsUtils.wrapImg(deliveryUrl)
        else
            content = AdsUtils.wrapItemImage(clickUrl,media.bannerFile,"_blank","","") + AdsUtils.wrapImg(deliveryUrl)
        content = content + thirdparty
        return renderHtml(banner, zone, channel, content, dynamic)
    }

    def renderBannerHtml(banner: BannerModel, zoneIdEncoded: String, embedded: String): String = {
        val content = embedded |> StringEscapeUtils.escapeJavaScript
        val html = banner.asInstanceOf[HtmlBannerModel]
        if (html.inline) s"parent.ZADS.M2.fillInlineHTMLContent('$zoneIdEncoded'," + '"' + content + '"' + s",${banner.width}, ${banner.height})"
        else s"parent.ZADS.M2.fillAdsContent('$zoneIdEncoded'," + '"' + content + '"' + s",${banner.width}, ${banner.height})"
    }

    def renderHtml(banner: BannerModel, zone: ZoneModel, channel: String, embedded: String, dynamic: String): String = {
        val jsParam = parseDynamicParameters(dynamic)
        val content = Render.renderHtml(
            Map("url_static" -> urlStatic.get(),
                "java_click_version" -> javaClickVersion.get(),
                "java_ld_version" -> javaClickVersion.get(),
                "css_click_version" -> cssClickVersion.get(),
                "html_proxy_version" -> htmlProxyVersion.get(),
                "channelUrl" -> channel,
                "item_content" -> embedded,
                "width" -> zone.width,
                "height" -> (zone.height + 10),
                "jsParam" -> jsParam)) |> StringEscapeUtils.escapeJavaScript
        s"parent.ZADS.M2.fillAdsContent('${SecurityUtils.encode(zone.id)}'," + '"' + content + '"' + s",${banner.width}, ${banner.height})"
    }
}

class MultiRenderServlet extends HandlerContainerServlet {
    def factory() :BaseHandler = return new MultiRenderHandler(Environment)
}
