package ads.website.modules.serving

import ads.common.services.serving.{AdsRequest, IServingEngine}
import ads.common.database.{InMemoryDataService, IDataService}
import ads.website.modules._
import ads.web.mvc.{SmartDispatcherHandler, Json, Text, BaseHandler}
import com.netflix.config.DynamicPropertyFactory
import javax.servlet.http.{HttpServletResponse, HttpServletRequest, Cookie}
import ads.common.{Base64, SecurityUtils}
import ads.web._
import ads.common.model._
import ads.common.Syntaxs._
import WebUtils._
import org.apache.commons.lang.{StringEscapeUtils, RandomStringUtils}
import java.net.URLDecoder
import java.io.{File, IOException}
import scala.collection.JavaConversions._
import ads.website.{ModelCache, Environment}
import ads.common.Syntaxs.Success
import scala.collection.mutable.ArrayBuffer
import java.util.{UUID, Calendar}
import org.apache.commons.codec.binary
import java.util
import scala.collection.mutable
import ads.common.services.{IConversionService, ICampaignService, IBannerService}
import scala.util.control.NonFatal
import ads.common.Syntaxs.Failure
import ads.common.Syntaxs.Success
import ads.common.services.report.AdType
import ads.common.geoip.GeoipService
import com.maxmind.geoip.Location
import ads.common.sessions.{SiteZoneFreqStore, Session}
import com.google.gson.reflect.TypeToken
import collection.JavaConversions._


class RenderVideoHandler(env: {
    val serving: IServingEngine
    val zoneModelService: IZoneModelService
    val zoneCache: ModelCache[ZoneModel]
    val campaignModelService: IDataService[CampaignModel]
    val bannerModelService: IBannerModelService}) extends RenderJsHandler(env) {
    override def process(request: HttpServletRequest, response: HttpServletResponse): Unit = Try {

        //        val zoneId = SecurityUtils.decode(request.getParameter("zid"))
        val arrayZone = request.getParameter("zid").split(",").map(z => SecurityUtils.decode(z)).toList
        val zevent = request.getParameter("zevent") ?? ""
        val channel = request.getParameter("channel") ?? ""
        val vid = request.getParameter("zvid") ?? "0"
        val companionTargeting = request.getParameter("postid") ?? null
        lazy val data = request.getParameter("data") ?? ""
        val playerWidth = (request.getParameter("playerWidth") ?? "0").toInt
        val playerHeight = (request.getParameter("playerHeight") ?? "0").toInt

        val url = ""
        val ip = WebUtils.getRemoteAddress(request)
        //        if (zoneId > 0) {
        if (arrayZone.length > 0) {
            val zone = env.zoneCache.get(arrayZone(0))
            if (zone != null) {
                var dynamic: String = ""
                //                val req = new AdsRequest(zone.siteId, List(zone.id), url, ip, WebUtils.getOrSetCookie(request, response), null, 1, mutable.Map("os" -> List(WebUtils.getUserAgent(request))))
                val req = new AdsRequest(zone.siteId, arrayZone, url, ip, WebUtils.getOrSetCookie(request, response), null, 1, companionTargeting, mutable.Map("os" -> List(WebUtils.getUserAgent(request))))
                try {
                    dynamic = request.getParameter("zdyn")
                    if (dynamic != null) {
                        dynamic = URLDecoder.decode(dynamic, "UTF-8")
                    }
                } catch {
                    case ioe: Throwable => ioe.printStackTrace()
                }
                if (zevent.equals("pause")) {
                    val result = serving.servePauseAdVideo(req, new String(org.apache.commons.codec.binary.Base64.decodeBase64(data))).data
                    if (result != null && result.getOrElse("nonLinear", new Array(0)).length > 0 && result("nonLinear")(0).length > 0) {
                        val banner = result("nonLinear")(0)(0)
                        renderPauseAd(zone, vid, banner, dynamic, channel, ip, WebUtils.checkFlash(request)) |>
                            response.renderJson
                    }
                } else if (data.length > 0) {
                    val result = serving.serveVideoZingTv(req, new String(org.apache.commons.codec.binary.Base64.decodeBase64(data))).data
                    renderZingTVVideo(arrayZone.map(z => env.zoneCache.get(z)).toArray, vid, result, dynamic, channel, ip, WebUtils.checkFlash(request), playerWidth, playerHeight) |>
                        response.renderJson
                } else {
                    val result = serving.serveVideo(req).data
                    renderVideo(zone, vid, result, dynamic, channel, ip, WebUtils.checkFlash(request), playerWidth, playerHeight) |>
                        response.renderJson
                }
            }
        }
    }

    def findSuitableSize(playerWidth: Int, playerHeight: Int, itemWidth: Int, itemHeight: Int): OverlayBannerSize = {
        if (playerWidth == 0 || playerHeight == 0 || playerWidth > itemWidth)
            return new OverlayBannerSize(itemWidth, itemHeight)

        if (playerWidth <  450)
            return new OverlayBannerSize(300, 60)

        if (playerWidth < 468)
            return new OverlayBannerSize(450, 50)

        if (playerWidth < 480)
            return new OverlayBannerSize(468, 60)

        if (playerWidth < 728)
            return  new OverlayBannerSize(480, 70)

        return new OverlayBannerSize(itemWidth, itemHeight)
    }

    def renderPauseAd(zone: ZoneModel, vid: String, banner: BannerModel, dynamic: String, channel: String, ip: String, vertical: Boolean): String = {
        val default = Map("zoneId" -> SecurityUtils.encode(zone.id),
            "itemId" -> SecurityUtils.encode(banner.id),
            "token" -> SecurityUtils.generateTimeToken(),
            "vid" -> vid,
            "adType" -> AdType.Pause.toString,
            "ip" -> ip)

        lazy val deliveryUrl = AdsUtils.getTrackVideoUrl("display", default + ("track" -> TrackKind.Impression))
        lazy val clickUrl = AdsUtils.getTrackVideoUrl("click", default + ("track" -> TrackKind.Click))
        lazy val creativeUrl = AdsUtils.getTrackVideoUrl("creative", default + ("track" -> TrackKind.Creative))
        lazy val firstQuartileUrl = AdsUtils.getTrackVideoUrl("firstQuartile", default + ("track" -> TrackKind.FirstQuartile))
        lazy val midPointUrl = AdsUtils.getTrackVideoUrl("midPoint", default + ("track" -> TrackKind.MidPoint))
        lazy val thirdQuartileUrl = AdsUtils.getTrackVideoUrl("thirdQuartile", default + ("track" -> TrackKind.ThirdQuartile))
        lazy val completeUrl = AdsUtils.getTrackVideoUrl("complete", default + ("track" -> TrackKind.Complete))
        lazy val closeUrl = AdsUtils.getTrackVideoUrl("close", default + ("track" -> TrackKind.Close))
        lazy val pauseUrl = AdsUtils.getTrackVideoUrl("pause", default + ("track" -> TrackKind.Pause))
        lazy val resumeUrl = AdsUtils.getTrackVideoUrl("resume", default + ("track" -> TrackKind.Resume))
        lazy val fullscreenUrl = AdsUtils.getTrackVideoUrl("fullscreen", default + ("track" -> TrackKind.Fullscreen))
        lazy val muteUrl = AdsUtils.getTrackVideoUrl("mute", default + ("track" -> TrackKind.Mute))
        lazy val unmuteUrl = AdsUtils.getTrackVideoUrl("unmute", default + ("track" -> TrackKind.Unmute))

        val item = banner.asInstanceOf[NetworkPauseAdBannerModel]

        var mediaUrl = ""
        var filetype = ""
        if (item.extendURL == null || item.extendURL.isEmpty) {
            mediaUrl = item.bannerFile
            if (mediaUrl.endsWith(".swf")) {
                filetype = "application/x-shockwave-flash"
            } else {
                filetype = "image/jpeg"
            }

        } else {
            mediaUrl = item.extendURL
        }

        val result = Map("targetUrl" -> clickUrl,
            "tracking" -> deliveryUrl,
            "fileType" -> filetype,
            "title" -> banner.name,
            "desc" -> "",
            "width" -> banner.width,
            "height" -> banner.height,
            "recommendedDuration" -> "",
            "mediaUrl" -> mediaUrl,
            "linkTrackClick" -> clickUrl,
            "linkTrackImpress" -> deliveryUrl,
            "linkTrackCreativeView" -> creativeUrl,
            "linkTrackFirstQuartile" -> firstQuartileUrl,
            "linkTrackMidPoint" -> midPointUrl,
            "linkTrackThirdQuartile" -> thirdQuartileUrl,
            "linkTrackComplete" -> completeUrl,
            "linkTrackClose" -> closeUrl,
            "linkTrackPause" -> pauseUrl,
            "linkTrackResume" -> resumeUrl,
            "linkTrackFullscreen" -> fullscreenUrl,
            "linkTrackMute" -> muteUrl,
            "linkTrackUnmute" -> unmuteUrl
        )
        Render.renderVideoPauseAd(result)
    }

    def renderVideo(zone: ZoneModel, vid: String, map: Map[String, Array[Array[BannerModel]]], dynamic: String, channel: String, ip: String, vertical: Boolean, playerWidth: Int, playerHeight: Int): String = {
        //var showAfterSeconds = 0d
        val videoZone = zone.asInstanceOf[VideoZoneModel]
        var contentList = new ArrayBuffer[Any]()
        if (map != null) {
            map.foreach {
                case (key, valueList) => {

                    var startTime = videoZone.midStartTime
                    var index = 0
                    for (bannerList <- valueList) {

                        for (banner <- bannerList) {
                            val default = Map("zoneId" -> SecurityUtils.encode(zone.id),
                                "itemId" -> SecurityUtils.encode(banner.id),
                                "token" -> SecurityUtils.generateTimeToken(),
                                "vid" -> vid,
                                "ip" -> ip)

                            if ("nonLinear".equals(key)) {
                                var showAfter = 0
                                var duration = 10
                                if (videoZone.timeSegments.length > index) {
                                    showAfter = videoZone.timeSegments(index).start
                                    duration = videoZone.timeSegments(index).spend
                                }

                                contentList += renderNonLinearAd(config = default, overlayItem = banner.asInstanceOf[NetworkOverlayBannerModel], start = showAfter, duration = duration, playerWidth, playerHeight)
                                index = index + 1
                            } else {
                                // VIDEO AD LINEAR
                                val item = banner.asInstanceOf[NetworkTVCBannerModel]
                                var itemPosition = "\"position\":\"pre-roll\""
                                if (key == "mid") {
                                    itemPosition = "\"position\":\"mid-roll\",\"startTime\":\"" + WebUtils.convertSecondToHMS(startTime) + "\"";
                                } else if (key == "pre") {
                                    itemPosition = "\"position\":\"pre-roll\""
                                } else if (key == "post") {
                                    itemPosition = "\"position\":\"post-roll\""
                                }
                                var duration = 10
                                if (item.duration > 0) {
                                    duration = item.duration.toInt
                                }
                                //showAfterSeconds = (duration / 3)

                                contentList += renderLinearAd(config = default, tvcItem = banner.asInstanceOf[NetworkTVCBannerModel], position = itemPosition, duration = duration)
                            }
                        }
                        if (key == "mid") {
                            startTime += videoZone.midTimeScheduled
                        }

                    }
                }
            }
        }

        var vads: String = ""
        for (content <- contentList) {
            vads += content.toString + ","
        }
        vads = vads.substring(0, vads.length - 1)

        var companionZones = ""
        //return Render.renderVideo(Map("vads" -> vads, "showAfterSeconds" -> showAfterSeconds))
        return Render.renderVideo(Map("vads" -> vads, "regions" -> companionZones))
    }

    def renderRegions(zones: Array[ZoneModel]): String = {
        var rs = ""
        val index = 0
        for (zone <- zones) {
            if (zone.renderKind != null && zone.renderKind.compareTo(EZoneRenderKind.BANNER_SKIN) == 0) {
                // banner skin
                val map = Map("id" -> "companion-1600x560-static-1", "width" -> zone.width, "height" -> zone.height, "index" -> index)
                rs += Render.renderRegion(map) + ","
            } else {
                val map = Map("id" -> SecurityUtils.encode(zone.id), "width" -> zone.width, "height" -> zone.height, "index" -> index)
                rs += Render.renderRegion(map) + ","
            }
        }
        if (rs.length > 0) rs.dropRight(1) else rs
    }

    def renderZingTVVideo(zones: Array[ZoneModel], vid: String, list: Map[String, Array[(BannerModel, Int, Int, Map[Int, BannerModel])]], dynamic: String, channel: String, ip: String, vertical: Boolean, playerWidth: Int, playerHeight: Int): String = {
        var items = new ArrayBuffer[Any]()
        var lastStart = -1
        var lastDuration = 0
        if (list != null) {
            for ((key, value) <- list) {
                for (v <- value) {
                    val banner = v._1
                    var start = v._2
                    val duration = v._3
                    val companionData = if (v._4 != null) v._4 else Map[Int, BannerModel]()
                    if(banner.kind == BannerKind.NetworkOverlayBanner) {
                        if(lastStart + lastDuration > start) {
                            start = lastStart + lastDuration
                        }
                        lastStart = start
                        lastDuration = duration
                    }
                    val data = renderZingTVItem(zones(0).id, banner, duration, start, vid, ip, key, companionData, playerWidth, playerHeight)
                    if (data.length > 0) items += data
                }
            }
        }


        var vads: String = ""
        for (item <- items) {
            vads += item.toString + ","
        }

        if (vads.length > 0) vads = vads.dropRight(1)

        val regions = renderRegions(zones.slice(1, zones.length))
        //val content = Render.renderVideo(Map("vads" -> vads, "showAfterSeconds" -> 0))
        val content = Render.renderVideo(Map("vads" -> vads, "regions" -> regions))
        content
    }

    // ---- COMPANION ADS ----
    def renderCompanionLink(tvcItemId: Int, videoZoneId: Int, videoId: String, bannerCompanions: List[BannerModel], mapBannerToZone: Map[Int, Int], position: String): String = {
        var companions = List[java.util.Map[String, String]]()
        bannerCompanions.foreach {
            banner => {
                companions = companions :+ mapAsJavaMap(Map("itemId" -> banner.id.toString, "zoneId" -> mapBannerToZone(banner.id).toString))
            }
        }
        val videoCompanionInfo = new VideoCompanionInfo(tvcItemId.toString, videoId, companions, videoZoneId.toString)
        val data = SecurityUtils.encodeData(Array(WebUtils.toRawJson(videoCompanionInfo)))
        val companionLink = s"${Config.hostDomain.getValue}/zad/videocomp?vcompanion=${data}"
        val mapData = Map(
            "itemPosition" -> position,
            "companionLink" -> companionLink)
        Render.renderVideoCompanionLink(mapData)
    }


    // ---- [COMPANION ADS] ----
    def renderNonLinearAd(config: Map[String, String], overlayItem: NetworkOverlayBannerModel, start: Int, duration: Int, playerWidth: Int, playerHeight: Int): String = {
        var content = ""

        var mediaUrl, filetype = ""
        if (overlayItem.extendURL == null || overlayItem.extendURL.isEmpty) {
            mediaUrl = overlayItem.bannerFile

            filetype = "image/jpg"
            if (mediaUrl.endsWith(".swf")) {
                filetype = "application/x-shockwave-flash"
            }
        } else {
            mediaUrl = overlayItem.extendURL
        }

        val deliveryUrl = AdsUtils.getTrackVideoUrl("display", config + ("track" -> TrackKind.Impression))
        val clickUrl = AdsUtils.getTrackVideoUrl("click", config + ("track" -> TrackKind.Click))
        val creativeUrl = AdsUtils.getTrackVideoUrl("creative", config + ("track" -> TrackKind.Creative))
        val firstQuartileUrl = AdsUtils.getTrackVideoUrl("firstQuartile", config + ("track" -> TrackKind.FirstQuartile))
        val midPointUrl = AdsUtils.getTrackVideoUrl("midPoint", config + ("track" -> TrackKind.MidPoint))
        val thirdQuartileUrl = AdsUtils.getTrackVideoUrl("thirdQuartile", config + ("track" -> TrackKind.ThirdQuartile))
        val completeUrl = AdsUtils.getTrackVideoUrl("complete", config + ("track" -> TrackKind.Complete))
        val closeUrl = AdsUtils.getTrackVideoUrl("close", config + ("track" -> TrackKind.Close))
        val pauseUrl = AdsUtils.getTrackVideoUrl("pause", config + ("track" -> TrackKind.Pause))
        val resumeUrl = AdsUtils.getTrackVideoUrl("resume", config + ("track" -> TrackKind.Resume))
        val fullscreenUrl = AdsUtils.getTrackVideoUrl("fullscreen", config + ("track" -> TrackKind.Fullscreen))
        val muteUrl = AdsUtils.getTrackVideoUrl("mute", config + ("track" -> TrackKind.Mute))
        val unmuteUrl = AdsUtils.getTrackVideoUrl("unmute", config + ("track" -> TrackKind.Unmute))
        val itemPosition = "\"startTime\":\"" + WebUtils.convertSecondToHMS(start) + "\""

        var impressionTrackList = List(Map("linkTrackImpress" -> deliveryUrl))
        if (overlayItem.thirdPartyImpressionUrl != null && overlayItem.thirdPartyImpressionUrl.size > 0) {
            for (impressionTrackUrl <- overlayItem.thirdPartyImpressionUrl) {
                impressionTrackList = impressionTrackList :+ Map("linkTrackImpress" -> impressionTrackUrl)
            }
        }


        val result = Map("recommendedDuration" -> duration,
            "itemPosition" -> itemPosition,
            "impression" -> impressionTrackList,
            "linkTrackClick" -> clickUrl,

            "creativeView" -> List(Map("linkTrackCreativeView" -> creativeUrl)),
            "firstQuartile" -> List(Map("linkTrackFirstQuartile" -> firstQuartileUrl)),
            "midpoint" -> List(Map("linkTrackMidPoint" -> midPointUrl)),
            "thirdQuartile" -> List(Map("linkTrackThirdQuartile" -> thirdQuartileUrl)),
            "complete" -> List(Map("linkTrackComplete" -> completeUrl)),
            "close" -> List(Map("linkTrackClose" -> closeUrl)),
            "pause" -> List(Map("linkTrackPause" -> pauseUrl)),
            "resume" -> List(Map("linkTrackResume" -> resumeUrl)),
            "fullscreen" -> List(Map("linkTrackFullscreen" -> fullscreenUrl)),
            "mute" -> List(Map("linkTrackMute" -> muteUrl)),
            "unmute" -> List(Map("linkTrackUnmute" -> unmuteUrl))
        )

        if (overlayItem.wrapper) {
            // WRAPPER
            val overlayWrapperMap = Map("video_vast_extlink" -> overlayItem.extendURL)
            val mapData = result ++ overlayWrapperMap
            content = Render.renderVideoNonLinearWrapper(mapData)
        } else {
            // NON WRAPPER
            val bannerSize = findSuitableSize(playerWidth, playerHeight, overlayItem.width, overlayItem.height)
            if (bannerSize.width != overlayItem.width || bannerSize.height != overlayItem.height) {
                if (filetype.compareTo("application/x-shockwave-flash") == 0 && overlayItem.bannerFileFallback.length > 0) {
                    filetype = "image/jpg"
                    mediaUrl = overlayItem.bannerFileFallback
                }
                mediaUrl = mediaUrl.replaceAll("\\.[\\w]*$", s"_${bannerSize.width}x${bannerSize.height}$$0")
            }
            val overlayMap = Map("title" -> overlayItem.name, "desc" -> "", "width" -> bannerSize.width, "height" -> bannerSize.height, "fileType" -> filetype, "mediaUrl" -> mediaUrl)
            val mapData = result ++ overlayMap
            content = Render.renderVideoNonLinear(mapData)
        }
        content
    }

    def renderLinearAd(config: Map[String, String], tvcItem: NetworkTVCBannerModel, position: String, duration: Int): String = {
        var content = ""

        var mediaUrl, filetype, apiFramework = ""
        if (tvcItem.extendURL.isEmpty) {
            mediaUrl = tvcItem.tvcFile
            apiFramework = "apiFramework='VPAID'"
            filetype = "video/x-mp4"
        } else {
            mediaUrl = tvcItem.extendURL
        }

        val deliveryUrl = AdsUtils.getTrackVideoUrl("display", config + ("track" -> TrackKind.Impression))
        val clickUrl = AdsUtils.getTrackVideoUrl("click", config + ("track" -> TrackKind.Click))
        val creativeUrl = AdsUtils.getTrackVideoUrl("creative", config + ("track" -> TrackKind.Creative))
        val firstQuartileUrl = AdsUtils.getTrackVideoUrl("firstQuartile", config + ("track" -> TrackKind.FirstQuartile))
        val midPointUrl = AdsUtils.getTrackVideoUrl("midPoint", config + ("track" -> TrackKind.MidPoint))
        val thirdQuartileUrl = AdsUtils.getTrackVideoUrl("thirdQuartile", config + ("track" -> TrackKind.ThirdQuartile))
        val completeUrl = AdsUtils.getTrackVideoUrl("complete", config + ("track" -> TrackKind.Complete))
        val closeUrl = AdsUtils.getTrackVideoUrl("close", config + ("track" -> TrackKind.Close))
        val pauseUrl = AdsUtils.getTrackVideoUrl("pause", config + ("track" -> TrackKind.Pause))
        val resumeUrl = AdsUtils.getTrackVideoUrl("resume", config + ("track" -> TrackKind.Resume))
        val fullscreenUrl = AdsUtils.getTrackVideoUrl("fullscreen", config + ("track" -> TrackKind.Fullscreen))
        val muteUrl = AdsUtils.getTrackVideoUrl("mute", config + ("track" -> TrackKind.Mute))
        val unmuteUrl = AdsUtils.getTrackVideoUrl("unmute", config + ("track" -> TrackKind.Unmute))

        // third party tracking
        var thirdPartyImpressionTrackingUrl, thirdPartyCompleteTrackingUrl, thirdPartyClickTrackingUrl = ""
        if (tvcItem.thirdParty != null) {
            thirdPartyImpressionTrackingUrl = tvcItem.thirdParty.impression
            thirdPartyCompleteTrackingUrl = tvcItem.thirdParty.complete
            thirdPartyClickTrackingUrl = tvcItem.thirdParty.click
        }

        var impressionTracking = List(Map("linkTrackImpress" -> deliveryUrl))
        if (!thirdPartyImpressionTrackingUrl.isEmpty)
            impressionTracking = List(Map("linkTrackImpress" -> deliveryUrl), Map("linkTrackImpress" -> thirdPartyImpressionTrackingUrl))

        var completeTracking = List(Map("linkTrackComplete" -> completeUrl))
        if (!thirdPartyCompleteTrackingUrl.isEmpty)
            completeTracking = List(Map("linkTrackComplete" -> completeUrl), Map("linkTrackComplete" -> thirdPartyCompleteTrackingUrl))

        var clickTracking = List(Map("linkTrackClick" -> clickUrl))
        if (!thirdPartyClickTrackingUrl.isEmpty)
            clickTracking = List(Map("linkTrackClick" -> clickUrl), Map("linkTrackClick" -> thirdPartyClickTrackingUrl))

        val result = Map(
            "itemPosition" -> position,
            "impression" -> impressionTracking,

            "creativeView" -> List(Map("linkTrackCreativeView" -> creativeUrl)),
            "start" -> List(),
            "firstQuartile" -> List(Map("linkTrackFirstQuartile" -> firstQuartileUrl)),
            "midpoint" -> List(Map("linkTrackMidPoint" -> midPointUrl)),
            "thirdQuartile" -> List(Map("linkTrackThirdQuartile" -> thirdQuartileUrl)),
            "complete" -> completeTracking,
            "close" -> List(Map("linkTrackClose" -> closeUrl)),
            "pause" -> List(Map("linkTrackPause" -> pauseUrl)),
            "resume" -> List(Map("linkTrackResume" -> resumeUrl)),
            "fullscreen" -> List(Map("linkTrackFullscreen" -> fullscreenUrl)),
            "mute" -> List(Map("linkTrackMute" -> muteUrl)),
            "unmute" -> List(Map("linkTrackUnmute" -> unmuteUrl))
        )

        if (tvcItem.wrapper) {
            val mapData = result ++ Map("video_vast_extlink" -> tvcItem.extendURL, "click" -> clickTracking)
            content = Render.renderVideoLinearWrapper(mapData)
        } else {
            val linearMap = Map("title" -> tvcItem.name, "desc" -> "", "linkTrackClick" -> clickUrl, "link3rdTrackClick" -> thirdPartyClickTrackingUrl,
                "width" -> tvcItem.width, "height" -> tvcItem.height, "fileType" -> filetype, "apiFramework" -> apiFramework, "mediaUrl" -> mediaUrl)
            val mapData = result ++ linearMap
            content = Render.renderVideoLinear(mapData)
        }
        content
    }

    def renderZingTVItem(zoneId: Int, banner: BannerModel, duration: Int, start: Int, vid: String, ip: String, key: String, companionData: Map[Int, BannerModel], playerWidth: Int, playerHeight: Int) = {
        var content = ""
        val default = Map("zoneId" -> SecurityUtils.encode(zoneId),
            "itemId" -> SecurityUtils.encode(banner.id),
            "token" -> SecurityUtils.generateTimeToken(),
            "vid" -> vid,
            "ip" -> ip)

        if ("nonLinear".equals(key)) {
            content = renderNonLinearAd(default, banner.asInstanceOf[NetworkOverlayBannerModel], start, duration, playerWidth, playerHeight)
        } else {
            // LINEAR AD
            var itemPosition = "\"position\":\"pre-roll\""

            var trackInfo = default
            if (key == "mid") {
                itemPosition = "\"position\":\"mid-roll\",\"startTime\":\"" + WebUtils.convertSecondToHMS(start) + "\""
                trackInfo += (("adType" -> AdType.Midroll.toString))
            } else if (key == "pre") {
                itemPosition = "\"position\":\"pre-roll\""
                trackInfo += (("adType" -> AdType.Preroll.toString))
            } else if (key == "post") {
                itemPosition = "\"position\":\"post-roll\""
                trackInfo += (("adType" -> AdType.Postroll.toString))
            }
            if (companionData.size > 0) {
                val banners = companionData.values.toList
                val mapCompanionZones = companionData.map(c => (c._2.id, c._1)).toMap
                content = renderCompanionLink(banner.id, zoneId, vid, banners, mapCompanionZones, itemPosition)
            } else {
                content = renderLinearAd(trackInfo, banner.asInstanceOf[NetworkTVCBannerModel], itemPosition, duration)
            }


        }
        content
    }
}

class RenderVideoCompanionHandler(env: {
    val serving: IServingEngine
    val zoneModelService: IZoneModelService
    val zoneCache: ModelCache[ZoneModel]
    val campaignModelService: IDataService[CampaignModel]
    val bannerModelService: IBannerModelService}) extends RenderJsHandler(env) {

    override def process(request: HttpServletRequest, response: HttpServletResponse): Unit = Try {

        val videoCompanionParam = request.getParameter("vcompanion") ?? ""
        if (videoCompanionParam.isEmpty) {
            return
        }

        val decodeParams = SecurityUtils.decodeData(videoCompanionParam).getOrElse(null)
        if (decodeParams == null || decodeParams.size == 0) {
            return
        }

        val json = decodeParams(0)
        val videoCompanion = WebUtils.fromJson[VideoCompanionInfo](new TypeToken[VideoCompanionInfo]() {}.getType, json)

        val ip = WebUtils.getRemoteAddress(request)
        if (videoCompanion.tvcId == 0) {
            return
        }

        val default = Map("zoneId" -> SecurityUtils.encode(videoCompanion.videoZoneId.toInt),
            "itemId" -> SecurityUtils.encode(videoCompanion.tvcId.toInt),
            "token" -> SecurityUtils.generateTimeToken(),
            "vid" -> videoCompanion.videoId,
            "ip" -> ip)
        var bannerCompanionIds = List[Int]()
        var mapCompanionZones = Map[Int, Int]()


        for (i <- 0 to videoCompanion.companions.size - 1) {
            val map = videoCompanion.companions(i)
            bannerCompanionIds = bannerCompanionIds ::: List(map.get("itemId").toInt)
            mapCompanionZones = mapCompanionZones ++ Map(map("itemId").toInt -> map("zoneId").toInt)
        }

        var item = env.bannerModelService.load(videoCompanion.tvcId.toInt)
        val banners = env.bannerModelService.listByIds(bannerCompanionIds).toList
        if (item.isInstanceOf[NetworkTVCBannerModel]) {
            val tvcItem = item.asInstanceOf[NetworkTVCBannerModel]
            renderLinearAndCompanionAd(default, tvcItem, banners, mapCompanionZones) |> response.renderXml

        }

    }

    def renderLinearAndCompanionAd(config: Map[String, String], tvcItem: NetworkTVCBannerModel, banners: List[BannerModel], mapCompanionZones: Map[Int, Int]): String = {
        // tvcItem is inline, not wrapper
        var content = ""

        var mediaUrl, filetype, apiFramework = ""
        if (tvcItem.extendURL.isEmpty) {
            mediaUrl = tvcItem.tvcFile
            apiFramework = "apiFramework='VPAID'"
            filetype = "video/x-mp4"
        } else {
            mediaUrl = tvcItem.extendURL
        }

        val deliveryUrl = AdsUtils.getTrackVideoUrl("display", config + ("track" -> TrackKind.Impression))
        val clickUrl = AdsUtils.getTrackVideoUrl("click", config + ("track" -> TrackKind.Click))
        val creativeUrl = AdsUtils.getTrackVideoUrl("creative", config + ("track" -> TrackKind.Creative))
        val firstQuartileUrl = AdsUtils.getTrackVideoUrl("firstQuartile", config + ("track" -> TrackKind.FirstQuartile))
        val midPointUrl = AdsUtils.getTrackVideoUrl("midPoint", config + ("track" -> TrackKind.MidPoint))
        val thirdQuartileUrl = AdsUtils.getTrackVideoUrl("thirdQuartile", config + ("track" -> TrackKind.ThirdQuartile))
        val completeUrl = AdsUtils.getTrackVideoUrl("complete", config + ("track" -> TrackKind.Complete))
        val closeUrl = AdsUtils.getTrackVideoUrl("close", config + ("track" -> TrackKind.Close))
        val pauseUrl = AdsUtils.getTrackVideoUrl("pause", config + ("track" -> TrackKind.Pause))
        val resumeUrl = AdsUtils.getTrackVideoUrl("resume", config + ("track" -> TrackKind.Resume))
        val fullscreenUrl = AdsUtils.getTrackVideoUrl("fullscreen", config + ("track" -> TrackKind.Fullscreen))
        val muteUrl = AdsUtils.getTrackVideoUrl("mute", config + ("track" -> TrackKind.Mute))
        val unmuteUrl = AdsUtils.getTrackVideoUrl("unmute", config + ("track" -> TrackKind.Unmute))

        // third party tracking
        var thirdPartyImpressionTrackingUrl, thirdPartyCompleteTrackingUrl, thirdPartyClickTrackingUrl = ""
        if (tvcItem.thirdParty != null) {
            thirdPartyImpressionTrackingUrl = tvcItem.thirdParty.impression
            thirdPartyCompleteTrackingUrl = tvcItem.thirdParty.complete
            thirdPartyClickTrackingUrl = tvcItem.thirdParty.click
        }

        var impressionTracking = List(Map("linkTrackImpress" -> deliveryUrl))
        if (!thirdPartyImpressionTrackingUrl.isEmpty)
            impressionTracking = List(Map("linkTrackImpress" -> deliveryUrl), Map("linkTrackImpress" -> thirdPartyImpressionTrackingUrl))

        var completeTracking = List(Map("linkTrackComplete" -> completeUrl))
        if (!thirdPartyCompleteTrackingUrl.isEmpty)
            completeTracking = List(Map("linkTrackComplete" -> completeUrl), Map("linkTrackComplete" -> thirdPartyCompleteTrackingUrl))

        var clickTracking = List(Map("linkTrackClick" -> clickUrl))
        if (!thirdPartyClickTrackingUrl.isEmpty)
            clickTracking = List(Map("linkTrackClick" -> clickUrl), Map("linkTrackClick" -> thirdPartyClickTrackingUrl))

        val companionAds = renderCompanionAd(banners, mapCompanionZones, config("ip"), config("vid"))

        val dataTemplates = Map(
            "impression" -> impressionTracking,

            "creativeView" -> List(Map("linkTrackCreativeView" -> creativeUrl)),
            "start" -> List(),
            "firstQuartile" -> List(Map("linkTrackFirstQuartile" -> firstQuartileUrl)),
            "midpoint" -> List(Map("linkTrackMidPoint" -> midPointUrl)),
            "thirdQuartile" -> List(Map("linkTrackThirdQuartile" -> thirdQuartileUrl)),
            "complete" -> completeTracking,
            "close" -> List(Map("linkTrackClose" -> closeUrl)),
            "pause" -> List(Map("linkTrackPause" -> pauseUrl)),
            "resume" -> List(Map("linkTrackResume" -> resumeUrl)),
            "fullscreen" -> List(Map("linkTrackFullscreen" -> fullscreenUrl)),
            "mute" -> List(Map("linkTrackMute" -> muteUrl)),
            "unmute" -> List(Map("linkTrackUnmute" -> unmuteUrl)),
            "companionAds" -> companionAds
        )

        if (tvcItem.wrapper) {
            val mapData = dataTemplates ++ Map("video_vast_extlink" -> tvcItem.extendURL, "click" -> clickTracking)
            content = Render.renderVideoLinearWrapperCompanion(mapData)
        } else {
            val linearMap = Map("duration" -> WebUtils.convertSecondToHMS(tvcItem.duration.toInt),"title" -> tvcItem.name, "desc" -> "", "linkTrackClick" -> clickUrl, "link3rdTrackClick" -> thirdPartyClickTrackingUrl,
                "width" -> tvcItem.width, "height" -> tvcItem.height, "fileType" -> filetype, "apiFramework" -> apiFramework, "mediaUrl" -> mediaUrl)
            val mapData = dataTemplates ++ linearMap
            content = Render.renderVideoLinearCompanion(mapData)
        }

        content
    }

    def renderCompanionAd(banners: List[BannerModel], mapZone: Map[Int, Int], ip: String, videoId: String): String = {
        if (banners.isEmpty)
            return ""

        var listCompanion = List[Map[String, Any]]()
        for (banner <- banners) {
            val info = Map("zoneId" -> SecurityUtils.encode(mapZone(banner.id)),
                "itemId" -> SecurityUtils.encode(banner.id),
                "token" -> SecurityUtils.generateTimeToken(),
                "vid" -> videoId,
                "ip" -> ip)

            val linkTrackCreativeView = AdsUtils.getTrackUrl("display", info + ("track" -> TrackKind.Impression))

            var map = Map[String, Any](
                "isStatic" -> false,
                "isHtml" -> false,
                "isIframe" -> false,
                "isTrackClick" -> false,
                "isAdParam" -> false,
                "width" -> banner.width,
                "height" -> banner.height,
                "linkTrackCreativeView" -> linkTrackCreativeView)

            if (banner.kind.equals(BannerKind.Media) || banner.kind.equals(BannerKind.NetworkMedia)) {
                // MEDIA
                val mediaBanner = if (banner.kind.equals(BannerKind.Media)) banner.asInstanceOf[MediaBannerModel] else banner.asInstanceOf[NetworkMediaBannerModel]
                val linkTrackClick = AdsUtils.getTrackUrl("click", info + ("track" -> TrackKind.Click), banner.targetUrl)

                var fileType = "image/jpeg"
                if (mediaBanner.bannerFile.endsWith(".swf")) {
                    fileType = "application/x-shockwave-flash"
                    map +=("isAdParam" -> true, "adParameters" -> s"clickTag=$linkTrackClick")
                }
                map +=("isStatic" -> true, "staticResource" -> mediaBanner.bannerFile, "staticType" -> fileType, "isTrackClick" -> true, "linkTrackClick" -> linkTrackClick)
            } else if (banner.kind.equals(BannerKind.Html) || banner.kind.equals(BannerKind.NetworkHtml)) {
                // HTML
                val htmlBanner = if (banner.kind.equals(BannerKind.Html)) banner.asInstanceOf[HtmlBannerModel] else banner.asInstanceOf[NetworkHtmlBannerModel]
                map +=("htmlResource" -> htmlBanner.embeddedHtml, "isHtml" -> true)
            }
            listCompanion = listCompanion :+ map
        }
        val mapData = Map("companions" -> listCompanion)
        Render.renderVideoCompanion(mapData)
    }
}

class RenderMultiBannerHandler(env: {
    val serving: IServingEngine
    val zoneModelService: IZoneModelService
    val zoneService: IDataService[Zone]
    val campaignService: IDataService[Campaign]
    val campaignModelService: IDataService[CampaignModel]
    val bannerModelService: IBannerModelService
    val log: ILog})
    extends RenderJsHandler(env) {

    class MultiRenderData(val zid: String, var data: String = "", var width: String = "0", var height: String = "0", var autoLayout: String = "false")

    override def internalProcess(request: HttpServletRequest, response: HttpServletResponse): Try[Unit] = Try {
        val arrayZone = request.getParameter("zones").split(",").map(z => SecurityUtils.decode(z)).toList
        var agent = request.getParameter("agent")
        val channel = request.getParameter("channel") ?? ""
        var dynamic = request.getParameter("dynamic") ?? ""
        val vs = request.getParameter("vs") ?? ""
        val companionTargeting = request.getParameter("postid") ?? null
        val dyValues = vs.split(";")
        val listValues: mutable.Map[String, List[String]] = mutable.Map()
        val location = GeoipService.getGeoipClient.getLocation(WebUtils.getRemoteAddress(request))
        val city = if (location != null) location.city else ""
        val task = new util.Random().nextInt(9998) + 1
        dyValues.map(item => {
            val t = item.split("=")
            if (t.size == 2) {
                listValues put(t(0).toLowerCase, t(1).split(",").toList.map(b => b.toLowerCase))
            }
        })
        //        dyValues.map( item => { val t = item.split("="); if(t.size == 2) { listValues.put(t(0),t(1)) } })
        val url = ""
        val ip = WebUtils.getRemoteAddress(request)
        val cookie = WebUtils.getOrSetCookie(request, response)
        var siteId = 0
        var listRender = new ArrayBuffer[MultiRenderData]()
        val req = new AdsRequest(0, arrayZone, url, ip, WebUtils.getOrSetCookie(request, response), null, 1, companionTargeting, listValues ++ Map("os" -> List(WebUtils.getUserAgent(request))))
        val data = env.serving.serveCompanion(req).data

        for ((zid, result) <- data if data != null) {
            val render = new MultiRenderData(SecurityUtils.encode(zid))
            val zone = env.zoneModelService.load(zid)
            //            val req = new AdsRequest(zone.siteId,zone.id,url,ip,WebUtils.getCookie(request),null,1,listValues.toMap)
            if (zone != null) {
                try {
                    dynamic = request.getParameter("zdyn")
                    if (dynamic != null) {
                        dynamic = URLDecoder.decode(dynamic, "UTF-8")
                    }
                } catch {
                    case ioe: IOException =>
                }
                //            val result = serving.serve(req)
                var orderId = 0
                var campId = 0
                var bannerId = 0
                siteId = if (zone != null) zone.siteId else 0
                val zoneGroupIds: Array[Int] = if (zone != null) zone.groups else Array.empty

                if (result != null && result != null && result.length > 0) {
                    val banner = env.bannerModelService.load(result(0).id)
                    render.width = banner.width.toString
                    render.height = banner.height.toString
                    if (banner != null) {
                        MacroInsertion.insertBanner(banner)
                        bannerId = banner.id
                        campId = banner.campaignId
                        orderId = env.campaignService.load(banner.campaignId).orderId
                        if (agent != "ie") {
                            banner.kind match {
                                case BannerKind.Media | BannerKind.NetworkMedia =>
                                    render.data = renderBanner(zone, banner, dynamic, channel, ip, WebUtils.checkFlash(request))
                                case BannerKind.Ecommerce => {
                                    val list = env.bannerModelService.listByIds(result.map(b => b.id).toList)
                                    render.data = renderJs(zone, list, dynamic, channel, ip, zone.id == 10000)
                                }
                                case BannerKind.Expandable | BannerKind.NetworkExpandable => {
                                    render.data = renderExandabled(zone, banner, ip)
                                    render.autoLayout = "true"
                                }
                                case BannerKind.Html | BannerKind.NetworkHtml => {
                                    val b = banner.asInstanceOf[HtmlBannerModel]
                                    render.autoLayout = b.autoLayout.toString
                                    render.data = renderJsHtml(zone, banner, dynamic, channel, ip, WebUtils.checkFlash(request))
                                }
                                case BannerKind.Popup | BannerKind.NetworkPopup => {
                                    render.autoLayout = "true"
                                    render.data = renderPopup(zone, banner, ip)
                                }
                                case BannerKind.Balloon | BannerKind.NetworkBalloon => {
                                    render.data = renderBalloon(zone, banner, ip)
                                    render.autoLayout = "true"
                                }
                            }
                            agent = ""
                        } else {
                            // agent IE
                            banner.kind match {
                                case BannerKind.Html | BannerKind.NetworkHtml => {
                                    val b = banner.asInstanceOf[HtmlBannerModel]
                                    if (b.autoLayout) {
                                        render.data = renderAutoLayout(zone, b.id, b.embeddedHtml, ip)
                                        render.autoLayout = "true"
                                    } else render.data = SecurityUtils.encode(banner.id)
                                }
                                case BannerKind.Expandable | BannerKind.NetworkExpandable => {
                                    render.data = renderExandabled(zone, banner, ip)
                                    render.autoLayout = "true"
                                }
                                case BannerKind.Popup | BannerKind.NetworkPopup => {
                                    render.autoLayout = "true"
                                    render.data = renderPopup(zone, banner, ip)
                                }
                                case BannerKind.Balloon | BannerKind.NetworkBalloon => {
                                    render.data = renderBalloon(zone, banner, ip)
                                    render.autoLayout = "true"
                                }
                                case _ => render.data = SecurityUtils.encode(banner.id)
                            }
                        }
                        listRender += render
                    }
                }
                env.log.log(LogKind.Normal, task, city, siteId, zoneGroupIds, zone.id, orderId, campId, bannerId, WebUtils.getRemoteAddress(request).replace(',', '|'), cookie, TrackKind.Render, System.currentTimeMillis(), 0, 0)
            }
        }
        env.log.log(LogKind.Normal, task, city, siteId, Array.empty, 0, 0, 0, 0, WebUtils.getRemoteAddress(request).replace(',', '|'), cookie, TrackKind.PageView, System.currentTimeMillis(), 0, 0)
        ("ZADS.renderMultiBanner" + agent.toUpperCase() + "(" + WebUtils.toRawJson(listRender) + ");") |> response.renderJs
    }

    def renderAutoLayout(zone: ZoneModel, bannerId: Int, content: String, ip: String): String = {
        val default = Map("zoneId" -> SecurityUtils.encode(zone.id),
            "itemId" -> SecurityUtils.encode(bannerId),
            "token" -> SecurityUtils.generateTimeToken(),
            "ip" -> ip)
        val deliveryUrl = AdsUtils.getTrackUrl("display",
            default + ("track" -> TrackKind.Impression))
        val linkTrack = AdsUtils.wrapImg(deliveryUrl)
        return content.concat(linkTrack)
    }

    def renderExandabled(zone: ZoneModel, banner: BannerModel, ip: String): String = {
        val default = Map("zoneId" -> SecurityUtils.encode(zone.id),
            "itemId" -> SecurityUtils.encode(banner.id),
            "token" -> SecurityUtils.generateTimeToken(),
            "ip" -> ip)
        val deliveryUrl = AdsUtils.getTrackUrl("display",
            default + ("track" -> TrackKind.Impression))

        val clickUrl = AdsUtils.getTrackUrl("click",
            default + ("track" -> TrackKind.Click), banner.targetUrl)
        val trackSubClick = AdsUtils.getTrackUrl("click",
            default + ("track" -> TrackKind.SubClick))
        val trackCollapse = AdsUtils.getTrackUrl("click",
            default + ("track" -> TrackKind.Collapse))
        val trackExpand = AdsUtils.getTrackUrl("click",
            default + ("track" -> TrackKind.Expand))
        var thirdparty = ""
        if (banner.thirdPartyImpressionUrl != null && banner.thirdPartyImpressionUrl.length > 0) {
            if (banner.thirdPartyImpressionUrl(0) != null) {
                thirdparty = AdsUtils.wrapImg(banner.thirdPartyImpressionUrl(0))
            }
        }
        var osync = false
        banner.kind match {
            case BannerKind.Expandable => {
                val camp = env.campaignModelService.load(banner.campaignId)
                val item = banner.asInstanceOf[ExpandableBannerModel]
                if (camp.isInstanceOf[BookingCampaignModel]) {
                    val campBooking = camp.asInstanceOf[BookingCampaignModel]
                    if (campBooking.companion && item.expandDirection == 2) {
                        osync = true
                    }
                }
            }
            case BannerKind.NetworkExpandable => {
                val camp = env.campaignModelService.load(banner.campaignId)
                val item = banner.asInstanceOf[NetworkExpandableBannerModel]
                if (camp.isInstanceOf[NetworkCampaignModel]) {
                    val campBooking = camp.asInstanceOf[NetworkCampaignModel]
                    if (campBooking.companion && item.expandDirection == 2) {
                        osync = true
                    }
                }
            }
        }

        val embedded = AdsUtils.wrapItemExpandable(SecurityUtils.encode(zone.id), SecurityUtils.encode(banner.id), Config.serverDomain.getValue, clickUrl, trackSubClick, trackCollapse, trackExpand, osync).concat(AdsUtils.wrapImg(deliveryUrl)).concat(thirdparty)
        embedded
    }

    def renderBalloon(zone: ZoneModel, banner: BannerModel, ip: String): String = {
        val default = Map("zoneId" -> SecurityUtils.encode(zone.id),
            "itemId" -> SecurityUtils.encode(banner.id),
            "token" -> SecurityUtils.generateTimeToken(),
            "ip" -> ip)
        val deliveryUrl = AdsUtils.getTrackUrl("display",
            default + ("track" -> TrackKind.Impression))

        val clickUrl = AdsUtils.getTrackUrl("click",
            default + ("track" -> TrackKind.Click), banner.targetUrl)
        val trackSubClick = AdsUtils.getTrackUrl("click",
            default + ("track" -> TrackKind.SubClick))
        val trackCollapse = AdsUtils.getTrackUrl("click",
            default + ("track" -> TrackKind.Collapse))
        val trackExpand = AdsUtils.getTrackUrl("click",
            default + ("track" -> TrackKind.Expand))
        var thirdparty = ""
        if (banner.thirdPartyImpressionUrl != null && banner.thirdPartyImpressionUrl.length > 0) {
            if (banner.thirdPartyImpressionUrl(0) != null) {
                thirdparty = AdsUtils.wrapImg(banner.thirdPartyImpressionUrl(0))
            }
        }
        var typeBaloon = 0
        if (banner.isInstanceOf[BalloonBannerModel]) {
            val item = banner.asInstanceOf[BalloonBannerModel]
            if (item.iTVCExtension == 3) {
                typeBaloon = 1
            }
        } else {
            val item = banner.asInstanceOf[NetworkBalloonBannerModel]
            if (item.iTVCExtension == 3) {
                typeBaloon = 1
            }
        }
        var embedded = ""
        if (typeBaloon == 1) {
            embedded = AdsUtils.wrapItemBalloonVast(SecurityUtils.encode(zone.id), banner, Config.serverDomain.getValue, clickUrl, trackSubClick, trackCollapse, trackExpand).concat(AdsUtils.wrapImg(deliveryUrl)).concat(thirdparty)
        } else {
            embedded = AdsUtils.wrapItemBalloon(SecurityUtils.encode(zone.id), SecurityUtils.encode(banner.id), Config.serverDomain.getValue, clickUrl, trackSubClick, trackCollapse, trackExpand).concat(AdsUtils.wrapImg(deliveryUrl)).concat(thirdparty)
        }
        embedded
    }

    def renderPopup(zone: ZoneModel, banner: BannerModel, ip: String): String = {
        val default = Map("zoneId" -> SecurityUtils.encode(zone.id),
            "itemId" -> SecurityUtils.encode(banner.id),
            "token" -> SecurityUtils.generateTimeToken(),
            "ip" -> ip)
        val deliveryUrl = AdsUtils.getTrackUrl("display",
            default + ("track" -> TrackKind.Impression))

        val clickUrl = AdsUtils.getTrackUrl("click",
            default + ("track" -> TrackKind.Click), banner.targetUrl)

        var thirdparty = ""
        if (banner.thirdPartyImpressionUrl != null && banner.thirdPartyImpressionUrl.length > 0) {
            if (banner.thirdPartyImpressionUrl(0) != null) {
                thirdparty = AdsUtils.wrapImg(banner.thirdPartyImpressionUrl(0))
            }
        }
        var content: String = ""
        banner.kind match {
            case BannerKind.Popup => {
                val media = banner.asInstanceOf[PopupBannerModel]

                if (media.bannerFile.endsWith(".swf"))
                    content = AdsUtils.wrapItemFlash(SecurityUtils.encode(zone.id), clickUrl, media.bannerFile, "_blank", media.width, media.height, media.bannerFileFallback)
                else if (media.bannerFile.endsWith(".mp4"))
                    content = AdsUtils.wrapItemVideo(SecurityUtils.encode(zone.id), clickUrl, media.bannerFile, "_blank", media.width, media.height)
                else
                    content = AdsUtils.wrapItemImage(clickUrl, media.bannerFile, "_blank", "", "")

            }
            case BannerKind.NetworkPopup => {
                val media = banner.asInstanceOf[NetworkPopupBannerModel]

                if (media.bannerFile.endsWith(".swf"))
                    content = AdsUtils.wrapItemFlash(SecurityUtils.encode(zone.id), clickUrl, media.bannerFile, "_blank", media.width, media.height, media.bannerFileFallback)
                else if (media.bannerFile.endsWith(".mp4"))
                    content = AdsUtils.wrapItemVideo(SecurityUtils.encode(zone.id), clickUrl, media.bannerFile, "_blank", media.width, media.height)
                else
                    content = AdsUtils.wrapItemImage(clickUrl, media.bannerFile, "_blank", "", "")

            }
        }

        val embedded = AdsUtils.wrapItemPopup(SecurityUtils.encode(zone.id), content, Config.jsDomain.getValue, clickUrl).concat(AdsUtils.wrapImg(deliveryUrl)).concat(thirdparty)
        embedded
    }

    def render(zone: ZoneModel, banner: BannerModel, dynamic: String, channel: String, ip: String, flashSupport: Boolean): String = {
        val default = Map("zoneId" -> SecurityUtils.encode(zone.id),
            "itemId" -> SecurityUtils.encode(banner.id),
            "token" -> SecurityUtils.generateTimeToken(),
            "ip" -> ip)

        val deliveryUrl = AdsUtils.getTrackUrl("display",
            default + ("track" -> TrackKind.Impression))

        val clickUrl = AdsUtils.getTrackUrl("click",
            default + ("track" -> TrackKind.Click), banner.targetUrl)

        banner.kind match {
            case BannerKind.Media => {
                val media = banner.asInstanceOf[MediaBannerModel]
                var content: String = "";
                if (media.bannerFile.endsWith(".swf"))
                    content = AdsUtils.wrapItemFlash(SecurityUtils.encode(zone.id), clickUrl, media.bannerFile, "_blank", media.width, media.height, media.bannerFileFallback) + AdsUtils.wrapImg(deliveryUrl)
                else if (media.bannerFile.endsWith(".mp4"))
                    content = AdsUtils.wrapItemVideo(SecurityUtils.encode(zone.id), clickUrl, media.bannerFile, "_blank", media.width, media.height) + AdsUtils.wrapImg(deliveryUrl)
                else
                    content = AdsUtils.wrapItemImage(clickUrl, media.bannerFile, "_blank", "", "") + AdsUtils.wrapImg(deliveryUrl)
                return content
            }
            case BannerKind.NetworkMedia => {
                val media = banner.asInstanceOf[NetworkMediaBannerModel]
                var content: String = "";
                if (media.bannerFile.endsWith(".swf"))
                    content = AdsUtils.wrapItemFlash(SecurityUtils.encode(zone.id), clickUrl, media.bannerFile, "_blank", media.width, media.height, media.bannerFileFallback) + AdsUtils.wrapImg(deliveryUrl)
                else if (media.bannerFile.endsWith(".mp4"))
                    content = AdsUtils.wrapItemVideo(SecurityUtils.encode(zone.id), clickUrl, media.bannerFile, "_blank", media.width, media.height) + AdsUtils.wrapImg(deliveryUrl)
                else
                    content = AdsUtils.wrapItemImage(clickUrl, media.bannerFile, "_blank", "", "") + AdsUtils.wrapImg(deliveryUrl)
                return content
            }
        }
    }

    def renderBannerIE(banner: BannerModel, zone: ZoneModel, zoneIdEncoded: String, channel: String, embedded: String, dynamic: String): String = {
        val jsParam = parseDynamicParameters(dynamic)
        val style = s"<style>.containerdiv { float: left; position: relative; } .cornerimage { position: absolute; top:${zone.height}px; right: 0; }</style>";
        val content = Render.renderIEHtml(
            Map("url_static" -> urlStatic.get(),
                "java_click_version" -> javaClickVersion.get(),
                "java_ld_version" -> javaClickVersion.get(),
                "css_click_version" -> cssClickVersion.get(),
                "html_proxy_version" -> htmlProxyVersion.get(),
                "channelUrl" -> channel,
                "item_content" -> style.concat(embedded),
                "width" -> zone.width,
                "height" -> (zone.height),
                "jsParam" -> jsParam))
        content
    }

    def renderBannerHtml(zone: ZoneModel, banner: BannerModel, dynamic: String, channel: String, ip: String, flashSupport: Boolean): String = {
        val default = Map("zoneId" -> SecurityUtils.encode(zone.id),
            "itemId" -> SecurityUtils.encode(banner.id),
            "token" -> SecurityUtils.generateTimeToken(),
            "ip" -> ip)

        val deliveryUrl = AdsUtils.getTrackUrl("display",
            default + ("track" -> TrackKind.Impression))

        banner.kind match {
            case BannerKind.Html => {
                val media = banner.asInstanceOf[HtmlBannerModel]
                val embedded = media.embeddedHtml;
                val jsParam = parseDynamicParameters(dynamic)
                val content = Render.renderIEHtml(
                    Map("url_static" -> urlStatic.get(),
                        "java_click_version" -> javaClickVersion.get(),
                        "java_ld_version" -> javaClickVersion.get(),
                        "css_click_version" -> cssClickVersion.get(),
                        "html_proxy_version" -> htmlProxyVersion.get(),
                        "channelUrl" -> channel,
                        "item_content" -> embedded.concat(AdsUtils.wrapImg(deliveryUrl)),
                        "width" -> zone.width,
                        "height" -> (zone.height),
                        "jsParam" -> jsParam))
                return util.Arrays.toString(binary.Base64.encodeBase64(embedded.getBytes()));
                //return content
            }
            case BannerKind.NetworkHtml => {
                val media = banner.asInstanceOf[HtmlBannerModel]
                val embedded = media.embeddedHtml;
                val jsParam = parseDynamicParameters(dynamic)
                val content = Render.renderIEHtml(
                    Map("url_static" -> urlStatic.get(),
                        "java_click_version" -> javaClickVersion.get(),
                        "java_ld_version" -> javaClickVersion.get(),
                        "css_click_version" -> cssClickVersion.get(),
                        "html_proxy_version" -> htmlProxyVersion.get(),
                        "channelUrl" -> channel,
                        "item_content" -> embedded.concat(AdsUtils.wrapImg(deliveryUrl)),
                        "width" -> zone.width,
                        "height" -> (zone.height),
                        "jsParam" -> jsParam))
                return embedded
                //return content
            }
        }
    }

    def renderBanner(zone: ZoneModel, banner: BannerModel, dynamic: String, channel: String, ip: String, flashSupport: Boolean): String = {

        val default = Map("zoneId" -> SecurityUtils.encode(zone.id),
            "itemId" -> SecurityUtils.encode(banner.id),
            "token" -> SecurityUtils.generateTimeToken(),
            "ip" -> ip)

        val deliveryUrl = AdsUtils.getTrackUrl("display",
            default + ("track" -> TrackKind.Impression))

        val clickUrl = AdsUtils.getTrackUrl("click",
            default + ("track" -> TrackKind.Click), banner.targetUrl)
        var thirdparty = ""
        if (banner.thirdPartyImpressionUrl != null && banner.thirdPartyImpressionUrl.length > 0) {
            if (banner.thirdPartyImpressionUrl(0) != null) {
                thirdparty = AdsUtils.wrapImg(banner.thirdPartyImpressionUrl(0))
            }
        }
        banner.kind match {
            case BannerKind.Media => {
                val media = banner.asInstanceOf[MediaBannerModel]
                //if (media.bannerFile.endsWith(".swf")) return ""
                //val content = AdsUtils.wrapClick(clickUrl, media.bannerFile) + AdsUtils.wrapImg(deliveryUrl)
                var content: String = "";
                if (media.bannerFile.endsWith(".swf"))
                    content = AdsUtils.wrapItemFlash(SecurityUtils.encode(zone.id), clickUrl, media.bannerFile, "_blank", media.width, media.height, media.bannerFileFallback) + AdsUtils.wrapImg(deliveryUrl)
                else if (media.bannerFile.endsWith(".mp4"))
                    content = AdsUtils.wrapItemVideo(SecurityUtils.encode(zone.id), clickUrl, media.bannerFile, "_blank", media.width, media.height) + AdsUtils.wrapImg(deliveryUrl)
                else
                    content = AdsUtils.wrapItemImage(clickUrl, media.bannerFile, "_blank", "", "") + AdsUtils.wrapImg(deliveryUrl)
                content = content + thirdparty
                return renderHtml(banner, zone, SecurityUtils.encode(zone.id), channel, content, dynamic)
            }
            case BannerKind.NetworkMedia => {
                val media = banner.asInstanceOf[NetworkMediaBannerModel]
                //if (media.bannerFile.endsWith(".swf")) return ""
                //val content = AdsUtils.wrapClick(clickUrl, media.bannerFile) + AdsUtils.wrapImg(deliveryUrl)
                var content: String = "";
                if (media.bannerFile.endsWith(".swf"))
                    content = AdsUtils.wrapItemFlash(SecurityUtils.encode(zone.id), clickUrl, media.bannerFile, "_blank", media.width, media.height, media.bannerFileFallback) + AdsUtils.wrapImg(deliveryUrl)
                else if (media.bannerFile.endsWith(".mp4"))
                    content = AdsUtils.wrapItemVideo(SecurityUtils.encode(zone.id), clickUrl, media.bannerFile, "_blank", media.width, media.height) + AdsUtils.wrapImg(deliveryUrl)
                else
                    content = AdsUtils.wrapItemImage(clickUrl, media.bannerFile, "_blank", "", "") + AdsUtils.wrapImg(deliveryUrl)
                content = content + thirdparty
                return renderHtml(banner, zone, SecurityUtils.encode(zone.id), channel, content, dynamic)
            }
        }
    }
}

class RenderJsHandler(env: {
    val serving: IServingEngine
    val zoneModelService: IZoneModelService
    val campaignModelService: IDataService[CampaignModel]
    val bannerModelService: IBannerModelService})
    extends BaseHandler {

    val factory = DynamicPropertyFactory.getInstance()
    val urlStatic = factory.getStringProperty("banner.url_static", "http://localhost/")
    val javaClickVersion = factory.getStringProperty("version.java_click_version", "1.0.0.0")
    val cssClickVersion = factory.getStringProperty("version.css_click_version", "1.0.0.0")
    val htmlProxyVersion = factory.getStringProperty("version.html_proxy_version", "1.0.0.0")

    val serving = env.serving
    val zoneService = env.zoneModelService

    override def process(request: HttpServletRequest, response: HttpServletResponse) {
        internalProcess(request, response) match {
            case Failure(cause) =>
                cause match {
                    case x: Exception => response.renderHtml(s"<!-- ${x.getMessage} \r\n ${x.getStackTraceString} \r\n  ${x.getCause}-->")
                    case _ => response.renderHtml(s"<!-- $cause -->")
                }
            case _ =>
        }
    }

    def internalProcess(request: HttpServletRequest, response: HttpServletResponse): Try[Unit] = Try {
        val zoneId = SecurityUtils.decode(request.getParameter("zid"))
        val channel = request.getParameter("channel") ?? ""
        val dynamic = request.getParameter("dynamic") ?? ""
        val url = ""
        val companionTargeting = request.getParameter("postid") ?? null

        val ip = WebUtils.getRemoteAddress(request)

        val zone = zoneId match {
            case 10000 => new BannerZoneModel(10000, 0, "123_mua", null, null, 0, 0, 0, 0, 0, false, 190, 663, null)
            case 10001 => new BannerZoneModel(10001, 0, "123_mua", null, null, 0, 0, 0, 0, 0, false, 980, 185, null)
            case 10002 => new BannerZoneModel(10002, 0, "123_mua", null, null, 0, 0, 0, 0, 0, false, 468, 90, null)
            case _ => env.zoneModelService.load(zoneId)
        }

        val items = zoneId match {
            case 10000 => 3
            case 10001 => 4
            case _ => 1
        }

        //val zone = env.zoneService.load(zoneId)

        val req = new AdsRequest(
            zone.siteId,
            List(zone.id),
            url,
            ip,
            WebUtils.getOrSetCookie(request, response),
            null,
            items,
            companionTargeting,
            mutable.Map("os" -> List(WebUtils.getUserAgent(request))))

        val result = serving.serve(req)


        if (result.banner == null) {
            throw new NullPointerException("Serving return null!")
        }

        val banner = env.bannerModelService.load(result.banner(0).id)
        MacroInsertion.insertBanner(banner)
        if (banner.isInstanceOf[MediaBannerModel] || banner.isInstanceOf[NetworkMediaBannerModel]) {
            renderJs(zone, banner, dynamic, channel, ip, WebUtils.checkFlash(request)) |>
                response.renderJs
        } else if (banner.isInstanceOf[EcommerceBannerModel]) {
            val list = env.bannerModelService.listByIds(result.banner.map(b => b.id).toList)
            renderJs(zone, list, dynamic, channel, ip, zone.id == 10000) |> response.renderJs
        } else if (banner.isInstanceOf[HtmlBannerModel] || banner.isInstanceOf[NetworkHtmlBannerModel]) {
            renderJsHtml(zone, banner, dynamic, channel, ip, WebUtils.checkFlash(request)) |>
                response.renderJs
        } else if (banner.isInstanceOf[ExpandableBannerModel]) {
            renderJsExpandable(zone, banner, dynamic, channel, ip, WebUtils.checkFlash(request)) |>
                response.renderJs
        }
    }

    def renderExpandable(zone: ZoneModel, banner: BannerModel, dynamic: String, channel: String, ip: String, flashSupport: Boolean): String = {
        val default = Map("zoneId" -> SecurityUtils.encode(zone.id),
            "itemId" -> SecurityUtils.encode(banner.id),
            "token" -> SecurityUtils.generateTimeToken(),
            "ip" -> ip)
        val deliveryUrl = AdsUtils.getTrackUrl("display",
            default + ("track" -> TrackKind.Impression))

        val clickUrl = AdsUtils.getTrackUrl("click",
            default + ("track" -> TrackKind.Click), banner.targetUrl)
        val trackSubClick = AdsUtils.getTrackUrl("click",
            default + ("track" -> TrackKind.SubClick))
        val trackCollapse = AdsUtils.getTrackUrl("click",
            default + ("track" -> TrackKind.Collapse))
        val trackExpand = AdsUtils.getTrackUrl("click",
            default + ("track" -> TrackKind.Expand))
        var osync = false
        val camp = env.campaignModelService.load(banner.campaignId)
        val item = banner.asInstanceOf[ExpandableBannerModel]
        if (camp.isInstanceOf[BookingCampaignModel]) {
            val campBooking = camp.asInstanceOf[BookingCampaignModel]
            if (campBooking.companion && item.expandDirection == 2) {
                osync = true
            }
        }
        val embedded = AdsUtils.wrapItemExpandable(SecurityUtils.encode(zone.id), SecurityUtils.encode(banner.id), Config.serverDomain.getValue, clickUrl, trackSubClick, trackCollapse, trackExpand, osync).concat(AdsUtils.wrapImg(deliveryUrl)) |> StringEscapeUtils.escapeJavaScript
        s"parent.ZADS.M2.fillInlineHTMLContent('${SecurityUtils.encode(zone.id)}'," + '"' + embedded + '"' + s",${banner.width}, ${banner.height})"


    }

    def renderJsExpandable(zone: ZoneModel, banner: BannerModel, dynamic: String, channel: String, ip: String, flashSupport: Boolean): String = {
        val default = Map("zoneId" -> SecurityUtils.encode(zone.id),
            "itemId" -> SecurityUtils.encode(banner.id),
            "token" -> SecurityUtils.generateTimeToken(),
            "ip" -> ip)

        val deliveryUrl = AdsUtils.getTrackUrl("display", default + ("track" -> TrackKind.Impression))
        val clickUrl = AdsUtils.getTrackUrl("click", default + ("track" -> TrackKind.Click), banner.targetUrl)

        val collapseUrl = AdsUtils.getTrackUrl("click", default + ("track" -> TrackKind.Collapse))
        val subClickUrl = AdsUtils.getTrackUrl("click", default + ("track" -> TrackKind.SubClick))
        val expandUrl = AdsUtils.getTrackUrl("click", default + ("track" -> TrackKind.Expand))
        banner.kind match {
            case BannerKind.Expandable => {
                val item = banner.asInstanceOf[ExpandableBannerModel]
                val camp = env.campaignModelService.load(banner.campaignId)
                var osync = false
                if (camp.isInstanceOf[BookingCampaignModel]) {
                    val campBooking = camp.asInstanceOf[BookingCampaignModel]
                    if (campBooking.companion && item.expandDirection == 2) {
                        osync = true
                    }
                }
                val content = AdsUtils.wrapItemExpandable(SecurityUtils.encode(zone.id), SecurityUtils.encode(item.id), Config.serverDomain.getValue, clickUrl, subClickUrl, collapseUrl, expandUrl, osync).concat(AdsUtils.wrapImg(deliveryUrl)) |> StringEscapeUtils.escapeJavaScript
                return renderBannerJsExpandable(banner, zone, SecurityUtils.encode(zone.id), channel, content, dynamic)
            }
            case BannerKind.NetworkExpandable => {
                val item = banner.asInstanceOf[NetworkExpandableBannerModel]
                val camp = env.campaignModelService.load(banner.campaignId)
                var osync = false
                if (camp.isInstanceOf[NetworkCampaignModel]) {
                    val networkCampaign = camp.asInstanceOf[NetworkCampaignModel]
                    if (networkCampaign.companion && item.expandDirection == 2) {
                        osync = true
                    }
                }
                val content = AdsUtils.wrapItemExpandable(SecurityUtils.encode(zone.id), SecurityUtils.encode(item.id), Config.serverDomain.getValue, clickUrl, subClickUrl, collapseUrl, expandUrl, osync).concat(AdsUtils.wrapImg(deliveryUrl)) |> StringEscapeUtils.escapeJavaScript
                return renderBannerJsExpandable(banner, zone, SecurityUtils.encode(zone.id), channel, content, dynamic)
            }
        }
    }

    def renderJs(zone: ZoneModel, list: Array[BannerModel], dynamic: String, channel: String, ip: String, vertical: Boolean): String = {
        val items = list.map(banner => {
            val default = Map("zoneId" -> SecurityUtils.encode(zone.id),
                "itemId" -> SecurityUtils.encode(banner.id),
                "token" -> SecurityUtils.generateTimeToken(),
                "ip" -> ip)

            val deliveryUrl = AdsUtils.getTrackUrl("display",
                default + ("track" -> TrackKind.Impression))

            val clickUrl = AdsUtils.getTrackUrl("click", default + ("track" -> TrackKind.Click), banner.targetUrl)

            banner.kind match {
                case BannerKind.Ecommerce => {
                    val item = banner.asInstanceOf[EcommerceBannerModel]

                    var result = Map("targetUrl" -> clickUrl, "tracking" -> AdsUtils.wrapImg(deliveryUrl))
                    for (entry <- item.properties.entrySet()) {
                        if (entry.getKey == "price") {
                            result = result + ("price" -> formatMoney(entry.getValue.toLong))
                        }
                        else result = result + (entry.getKey -> entry.getValue)
                    }
                    result
                }
                case _ => ???
            }
        })

        val content = Render.renderEcommerceBanner(Map("items" -> items, "domain" -> urlStatic.get(), "vertical" -> vertical)) |> StringEscapeUtils.escapeJavaScript

        renderEcommerceHtml(SecurityUtils.encode(zone.id), channel, content, dynamic, zone.width, zone.height)
    }

    def renderBannerJsExpandable(banner: BannerModel, zone: ZoneModel, zoneIdEncoded: String, channel: String, embedded: String, dynamic: String): String = {
        s"parent.ZADS.M2.fillInlineHTMLContent('$zoneIdEncoded'," + '"' + embedded + '"' + s",${banner.width}, ${banner.height})"
    }

    def renderBannerExpandable(banner: BannerModel, zone: ZoneModel, zoneIdEncoded: String, channel: String, embedded: String, dynamic: String): String = {
        val jsParam = parseDynamicParameters(dynamic)
        val style = s"<style>.containerdiv { float: left; position: relative; } .cornerimage { position: absolute; top:${zone.height}px; right: 0; }</style>";
        val content = Render.renderIEHtml(
            Map("url_static" -> urlStatic.get(),
                "java_click_version" -> javaClickVersion.get(),
                "java_ld_version" -> javaClickVersion.get(),
                "css_click_version" -> cssClickVersion.get(),
                "html_proxy_version" -> htmlProxyVersion.get(),
                "channelUrl" -> channel,
                "item_content" -> style.concat(embedded),
                "width" -> zone.width,
                "height" -> (zone.height),
                "jsParam" -> jsParam))
        content
    }

    def renderJs(zone: ZoneModel, banner: BannerModel, dynamic: String, channel: String, ip: String, flashSupport: Boolean): String = {

        val default = Map("zoneId" -> SecurityUtils.encode(zone.id),
            "itemId" -> SecurityUtils.encode(banner.id),
            "token" -> SecurityUtils.generateTimeToken(),
            "ip" -> ip)

        val deliveryUrl = AdsUtils.getTrackUrl("display",
            default + ("track" -> TrackKind.Impression))

        val clickUrl = AdsUtils.getTrackUrl("click",
            default + ("track" -> TrackKind.Click), banner.targetUrl)

        banner.kind match {
            case BannerKind.Media => {
                val media = banner.asInstanceOf[MediaBannerModel]
                //if (media.bannerFile.endsWith(".swf")) return ""
                //val content = AdsUtils.wrapClick(clickUrl, media.bannerFile) + AdsUtils.wrapImg(deliveryUrl)
                var content: String = "";
                if (media.bannerFile.endsWith(".swf"))
                    content = AdsUtils.wrapItemFlash(SecurityUtils.encode(zone.id), clickUrl, media.bannerFile, "_blank", media.width, media.height, media.bannerFileFallback) + AdsUtils.wrapImg(deliveryUrl)
                else if (media.bannerFile.endsWith(".mp4"))
                    content = AdsUtils.wrapItemVideo(SecurityUtils.encode(zone.id), clickUrl, media.bannerFile, "_blank", media.width, media.height) + AdsUtils.wrapImg(deliveryUrl)
                else
                    content = AdsUtils.wrapItemImage(clickUrl, media.bannerFile, "_blank", "", "") + AdsUtils.wrapImg(deliveryUrl)
                return renderHtml(banner, zone, SecurityUtils.encode(zone.id), channel, content, dynamic)
            }
            case BannerKind.NetworkMedia => {
                val media = banner.asInstanceOf[NetworkMediaBannerModel]
                //if (media.bannerFile.endsWith(".swf")) return ""
                //val content = AdsUtils.wrapClick(clickUrl, media.bannerFile) + AdsUtils.wrapImg(deliveryUrl)
                var content: String = "";
                if (media.bannerFile.endsWith(".swf"))
                    content = AdsUtils.wrapItemFlash(SecurityUtils.encode(zone.id), clickUrl, media.bannerFile, "_blank", media.width, media.height, media.bannerFileFallback) + AdsUtils.wrapImg(deliveryUrl)
                else if (media.bannerFile.endsWith(".mp4"))
                    content = AdsUtils.wrapItemVideo(SecurityUtils.encode(zone.id), clickUrl, media.bannerFile, "_blank", media.width, media.height) + AdsUtils.wrapImg(deliveryUrl)
                else
                    content = AdsUtils.wrapItemImage(clickUrl, media.bannerFile, "_blank", "", "") + AdsUtils.wrapImg(deliveryUrl)
                return renderHtml(banner, zone, SecurityUtils.encode(zone.id), channel, content, dynamic)
            }


            //case HtmlBanner(id, campaiginId, _, targetUrl, _, width, height, html) => ""
            //            case MediaBannerModel(id, camId, _, targetUrl, _, width, height, bannerFile, fallBack, _) => {
            //                if (bannerFile.endsWith(".swf")) {
            //                    // flash
            //                    return ""
            //                }
            //                else {
            //                    val content = AdsUtils.wrapClick(clickUrl, bannerFile) + AdsUtils.wrapImg(deliveryUrl)
            //                    return renderHtml(banner, zone, SecurityUtils.encode(zone.id), channel, content, dynamic)
            //                }
            //            }
        }
    }

    def renderPrBanner(zone: ZoneModel, banner: BannerModel, ip: String, request: AdsRequest): String = {

        val default = Map("zoneId" -> SecurityUtils.encode(zone.id),
            "itemId" -> SecurityUtils.encode(banner.id),
            "token" -> SecurityUtils.generateTimeToken(),
            "ip" -> ip)

        val deliveryUrl = AdsUtils.getTrackUrl("display",
            default + ("track" -> TrackKind.Impression))

        banner.kind match {
            case BannerKind.PrBanner | BannerKind.NetworkPrBanner => {
                val model = banner.asInstanceOf[PrBannerModel]
                val data = serving.getPrData(request, model)
                val map: mutable.HashMap[String, Any] = new mutable.HashMap[String, Any]
                map put("url_static", urlStatic.getValue)
                var count = 1
                for (b <- data) {
                    try {
                        if (count == 1) {
                            if (b.photos.length > 0) map put("image_url" + count, b.photos(0))
                            map put("description" + count, b.summary)
                        }
                        val default = Map("zoneId" -> SecurityUtils.encode(zone.id),
                            "itemId" -> SecurityUtils.encode(b.id),
                            "token" -> SecurityUtils.generateTimeToken(),
                            "ip" -> ip)
                        val trackingClick = AdsUtils.getTrackUrl("click", default + ("track" -> TrackKind.Click), b.publishedUrl)

                        map put("title" + count, b.name)
                        map put("link" + count, trackingClick)
                        count += 1
                    } catch {
                        case x: Throwable => x.printStackTrace()
                    }

                }
                val content: String = count - 1 match {
                    case 0 | 1 => Render.renderPrBanner1(map.toMap)
                    case 2 => Render.renderPrBanner2(map.toMap)
                    case 3 => Render.renderPrBanner3(map.toMap)
                }
                content.concat(AdsUtils.wrapImg(deliveryUrl))
            }
        }
    }

    def renderJsHtml(zone: ZoneModel, banner: BannerModel, dynamic: String, channel: String, ip: String, flashSupport: Boolean): String = {

        val default = Map("zoneId" -> SecurityUtils.encode(zone.id),
            "itemId" -> SecurityUtils.encode(banner.id),
            "token" -> SecurityUtils.generateTimeToken(),
            "ip" -> ip)

        val deliveryUrl = AdsUtils.getTrackUrl("display",
            default + ("track" -> TrackKind.Impression))

        banner.kind match {
            case BannerKind.Html | BannerKind.NetworkHtml => {
                val media = banner.asInstanceOf[HtmlBannerModel]
                val content: String = media.embeddedHtml.concat(AdsUtils.wrapImg(deliveryUrl))
                if (media.autoLayout == false) {
                    return renderBannerHtml(banner, zone, SecurityUtils.encode(zone.id), channel, content, dynamic)
                } else {
                    return content
                }
            }
        }
    }

    def renderInlineHtml(banner: Banner, zone: ZoneModel, zoneIdEncoded: String, embedded: String): String = {
        val content = WebUtils.escapeJs(embedded)
        s"parent.ZADS.M2.fillInlineHTMLContent('$zoneIdEncoded','$content',${zone.width},${zone.height});"
    }

    def renderEcommerceHtml(zoneIdEncoded: String, channel: String, embedded: String, dynamic: String, w: Int, h: Int): String = {
        s"parent.ZADS.M2.fillAdsContent('$zoneIdEncoded'," + '"' + embedded + '"' + s",$w, $h)"

        //val s = s"<!DOCTYPE html PUBLIC ${"}-//W3C//DTD XHTML 1.0 Transitional//EN${"} ${"}http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd${"}><html xmlns=${"}http://www.w3.org/1999/xhtml${"}><head><meta http-equiv=${"}Content-Type${"} content=${"}text/html; charset=utf-8${"} /><title>Zing Ads</title><link rel=${"}stylesheet${"} type=${"}text/css${"} href=${"}${url_static}/css/click.css?v=${css_click_version}${"}/><script type=${"}text/javascript${"} src=${"}${url_static}/js/lb.js?v=${java_ld_version}${"}></script><script type=${"}text/javascript${"} src=${"}${url_static}/js/click.js?v=${java_click_version}${"}></script><script type=${"}text/javascript${"}>var param_init_body = ${"}${channelUrl}${"};var xd_url = ${"}${url_static}/html/xd_proxy-${html_proxy_version}.html#?=${"};${jsParam}</script></head><body onload='parent.resize($width,$height)'><div class=${"}z2ads_box${"}><div class=${"}z2ads_contentimg${"}>${item_content}</div></div></body></html>"
        //val s = s"<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Transitional//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd'><html xmlns='http://www.w3.org/1999/xhtml'><head><meta http-equiv='Content-Type' content='text/html; charset=utf-8' /><title>Zing Ads</title><link rel='stylesheet' type='text/css' href='${url_static}/css/click.css?v=${css_click_version}'/><script type='text/javascript' src='${url_static}/js/lb.js?v=${java_ld_version}'></script><script type='text/javascript' src='${url_static}/js/click.js?v=${java_click_version}'></script><script type='text/javascript'>var param_init_body = '${channelUrl}';var xd_url = '${url_static}/html/xd_proxy-${html_proxy_version}.html#?=';${jsParam}</script></head><body onload='parent.resize($width,$height)'><div class='z2ads_box'><div class='z2ads_contentimg'>${item_content}</div></div></body></html>"
        //val content = WebUtils.escapeJs(s)

        //return s"parent.ZADS.M2.fillPopupAdsContent('$zoneIdEncoded',$content,${banner.width}, ${banner.height}})"

        //return s"parent.ZADS.M2.fillAdsContent('$zoneIdEncoded'," + '"' + content + '"' + s",${banner.width}, ${banner.height})"
    }

    def renderHtml(banner: BannerModel, zone: ZoneModel, zoneIdEncoded: String, channel: String, embedded: String, dynamic: String): String = {
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

        s"parent.ZADS.M2.fillAdsContent('$zoneIdEncoded'," + '"' + content + '"' + s",${banner.width}, ${banner.height})"

        //val s = s"<!DOCTYPE html PUBLIC ${"}-//W3C//DTD XHTML 1.0 Transitional//EN${"} ${"}http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd${"}><html xmlns=${"}http://www.w3.org/1999/xhtml${"}><head><meta http-equiv=${"}Content-Type${"} content=${"}text/html; charset=utf-8${"} /><title>Zing Ads</title><link rel=${"}stylesheet${"} type=${"}text/css${"} href=${"}${url_static}/css/click.css?v=${css_click_version}${"}/><script type=${"}text/javascript${"} src=${"}${url_static}/js/lb.js?v=${java_ld_version}${"}></script><script type=${"}text/javascript${"} src=${"}${url_static}/js/click.js?v=${java_click_version}${"}></script><script type=${"}text/javascript${"}>var param_init_body = ${"}${channelUrl}${"};var xd_url = ${"}${url_static}/html/xd_proxy-${html_proxy_version}.html#?=${"};${jsParam}</script></head><body onload='parent.resize($width,$height)'><div class=${"}z2ads_box${"}><div class=${"}z2ads_contentimg${"}>${item_content}</div></div></body></html>"
        //val s = s"<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Transitional//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd'><html xmlns='http://www.w3.org/1999/xhtml'><head><meta http-equiv='Content-Type' content='text/html; charset=utf-8' /><title>Zing Ads</title><link rel='stylesheet' type='text/css' href='${url_static}/css/click.css?v=${css_click_version}'/><script type='text/javascript' src='${url_static}/js/lb.js?v=${java_ld_version}'></script><script type='text/javascript' src='${url_static}/js/click.js?v=${java_click_version}'></script><script type='text/javascript'>var param_init_body = '${channelUrl}';var xd_url = '${url_static}/html/xd_proxy-${html_proxy_version}.html#?=';${jsParam}</script></head><body onload='parent.resize($width,$height)'><div class='z2ads_box'><div class='z2ads_contentimg'>${item_content}</div></div></body></html>"
        //val content = WebUtils.escapeJs(s)

        //return s"parent.ZADS.M2.fillPopupAdsContent('$zoneIdEncoded',$content,${banner.width}, ${banner.height}})"

        //return s"parent.ZADS.M2.fillAdsContent('$zoneIdEncoded'," + '"' + content + '"' + s",${banner.width}, ${banner.height})"
    }

    def renderBannerHtml(banner: BannerModel, zone: ZoneModel, zoneIdEncoded: String, channel: String, embedded: String, dynamic: String): String = {
        val content = embedded |> StringEscapeUtils.escapeJavaScript

        banner.kind match {
            case BannerKind.Html => {
                val html = banner.asInstanceOf[HtmlBannerModel];
                if (html.inline) s"parent.ZADS.M2.fillInlineHTMLContent('$zoneIdEncoded'," + '"' + content + '"' + s",${banner.width}, ${banner.height})"
                else s"parent.ZADS.M2.fillAdsContent('$zoneIdEncoded'," + '"' + content + '"' + s",${banner.width}, ${banner.height})"
            }
            case BannerKind.NetworkHtml => {
                val html = banner.asInstanceOf[NetworkHtmlBannerModel];
                if (html.inline) s"parent.ZADS.M2.fillInlineHTMLContent('$zoneIdEncoded'," + '"' + content + '"' + s",${banner.width}, ${banner.height})"
                else s"parent.ZADS.M2.fillAdsContent('$zoneIdEncoded'," + '"' + content + '"' + s",${banner.width}, ${banner.height})"
            }
        }

        //val html = banner.asInstanceOf[HtmlBannerModel]
        //if (html.inline) s"parent.ZADS.M2.fillInlineHTMLContent('$zoneIdEncoded'," + '"' + content + '"' + s",${banner.width}, ${banner.height})"
        //else s"parent.ZADS.M2.fillAdsContent('$zoneIdEncoded'," + '"' + content + '"' + s",${banner.width}, ${banner.height})"
    }

    def parseDynamicParameters(dynamic: String): String = {
        if (dynamic == null) return "";
        val parts = dynamic.split("\\|")
        val sb = new StringBuilder()

        //for (part <- parts){
        for (i <- 0 until parts.length) {
            val part = parts(i)

            if (part != null && part.length > 0) {
                val pair = part.split("=")
                if (pair != null && pair.length == 2) {
                    val key = WebUtils.normalizeJsParam(pair(0))
                    val value = WebUtils.normalizeJsParam(pair(1))

                    sb.append(s"zads_${key}=${"}$value${"}")

                    if (i == parts.length - 1)
                        sb.append(";")
                    else
                        sb.append(",")
                }
            }
        }

        return sb.toString()
    }

    def renderRichMedia(zoneIdEncoded: String, expandWidth: Int, expandHeight: Int, banner: Banner) {
        //s"parent.ZADS.M2.fillRichMediaContent('$zoneIdEncoded', ${banner.zoneType}, ${banner.itemType}, ${banner.zonePosition}, ${banner.zoneCollapse}, $expandWidth, $expandHeight, ${data.zoneWidth}, ${data.zoneHeight}, ${data.itemDuration}, '$embeded'"
    }
}

class RenderCrossDomainHandler(env: {
}) extends BaseHandler {
    override def process(request: HttpServletRequest, response: HttpServletResponse): Unit = Try {
        val content = Render.renderCrossdomain(Map("url_static" -> "url"))
        content |> response.renderXml
    }
}

class RenderProxyHandler(env: {}) extends BaseHandler {
    override def process(request: HttpServletRequest, response: HttpServletResponse): Unit = Try {
        val content = Render.renderProxy(Map("url_static" -> "url"))
        content |> response.renderHtml
    }
}

class RenderBannerHtmlHandler(env: {
    val serving: IServingEngine
    val zoneModelService: IZoneModelService
    val bannerModelService: IBannerModelService
    val campaignModelService: IDataService[CampaignModel]
    val campaignService: IDataService[Campaign]
}) extends RenderHandler(env) {
    override def process(request: HttpServletRequest, response: HttpServletResponse): Unit = Try {
        val bannerId: Int = SecurityUtils.decode(request.getParameter("id"))
        val zoneId: Int = SecurityUtils.decode(request.getParameter("zid"))
        if (bannerId > 0 && zoneId > 0) {
            val banner = env.bannerModelService.load(bannerId)
            val zone = env.zoneModelService.load(zoneId)
            if (banner != null) {
                MacroInsertion.insertBanner(banner)
                var dynamic: String = "";
                val channel: String = request.getParameter("channel") ?? ""
                val ip = WebUtils.getRemoteAddress(request)
                try {
                    dynamic = request.getParameter("zdyn")
                    if (dynamic != null) {
                        dynamic = URLDecoder.decode(dynamic, "UTF-8")
                    }
                } catch {
                    case ioe: IOException =>
                }
                if (banner.isInstanceOf[MediaBannerModel] || banner.isInstanceOf[NetworkMediaBannerModel]) {
                    render(zone, banner, dynamic, channel, ip, WebUtils.checkFlash(request)) |>
                        response.renderHtml

                } else if (banner.isInstanceOf[HtmlBannerModel] || banner.isInstanceOf[NetworkHtmlBannerModel]) {
                    renderBannerHtml(zone, banner, dynamic, channel, ip, WebUtils.checkFlash(request)) |>
                        response.renderHtml
                } else if (banner.isInstanceOf[ExpandableBannerModel]) {
                    renderExpandable(zone, banner, dynamic, channel, ip, WebUtils.checkFlash(request)) |>
                        response.renderHtml
                }
            }
        }
    }
}

class TestServingHandler(env: {
    val serving: IServingEngine
    val zoneModelService: IZoneModelService
    val bannerModelService: IBannerModelService
    val campaignModelService: IDataService[CampaignModel]
    val campaignService: IDataService[Campaign]
}) extends RenderJsHandler(env) {
    override def process(request: HttpServletRequest, response: HttpServletResponse): Unit = Try {
        val skey = request.getParameter("skey")
        if ("dfgd345dfgdfg345dfgdfgdfg2gfffdd7744@--fff".equals(skey)) {
            var out = ""
            val ip = WebUtils.getRemoteAddress(request)
            val zid = SecurityUtils.decode(request.getParameter("zid"))
            val companionTargeting = request.getParameter("postid") ?? null

            val zone = env.zoneModelService.load(zid)
            if (zone != null) {
                val url = ""
                val req = new AdsRequest(zone.siteId, List(zone.id), url, ip, WebUtils.getOrSetCookie(request, response), null, 1, companionTargeting, mutable.Map("os" -> List(WebUtils.getUserAgent(request))))
                val result = serving.serve(req)
                out += result.toString + "<br />"
                val banners = result.banner
                out += "banner size: " + banners.length + "<br />"
                for (banner <- banners) {
                    out += "item: " + banner.id + "<br />"
                }
            } else {
                out += "zone id in valid."
            }
            out |> response.renderHtml
        } else {
            response.sendRedirect("/")
        }
    }
}

class RenderAjsHandler(env: {
    val bannerModelService: IDataService[BannerModel]
    val zoneModelService: IZoneModelService
}) extends BaseHandler {
    val factory = DynamicPropertyFactory.getInstance()
    val urlStatic = factory.getStringProperty("banner.url_static", "http://localhost/")

    override def process(request: HttpServletRequest, response: HttpServletResponse): Unit = Try {
        val zoneId: Int = SecurityUtils.decode(request.getParameter("zid"))
        if (zoneId > 0) {
            val zone = env.zoneModelService.load(zoneId)
            if (zone != null) {
                renderAJs(zone) |> response.renderJs
            }
        }
    }

    def renderAJs(zone: ZoneModel): String = {
        val urlStatic = factory.getStringProperty("banner.url_static", "http://staging.static.adtimaserver.vn")
        val prefix = RandomStringUtils.randomAlphabetic(8)
        val content = Render.renderAJs(
            Map(
                "url_static" -> urlStatic.getValue,
                "zone_id" -> SecurityUtils.encode(zone.id),
                "width" -> zone.width,
                "height" -> zone.height,
                "prefix" -> prefix
            )
        )
        content
    }
}

class RenderBannerExpandableHandler(env: {
    val bannerModelService: IDataService[BannerModel]
}) extends BaseHandler {
    val factory = DynamicPropertyFactory.getInstance()
    val urlStatic = factory.getStringProperty("banner.url_static", "http://dev.api.adtimaserver.vn/")

    override def process(request: HttpServletRequest, response: HttpServletResponse): Unit = Try {


        val bannerId = SecurityUtils.decode(request.getParameter("id"))
        val zoneId = request.getParameter("zid")
        val prefix = request.getParameter("p")
        if (bannerId > 0) {
            val banner = env.bannerModelService.load(bannerId)
            if (banner.isInstanceOf[ExpandableBannerModel]) {
                val item = banner.asInstanceOf[ExpandableBannerModel]
                renderJs(item, prefix, zoneId) |> response.renderJs
            }
        }
    }

    def renderJs(banner: ExpandableBannerModel, prefix: String, zoneId: String): String = {
        var tvcFile = ""
        val displayStyle = banner.displayStyle match {
            case 1 => ""
        }

        if (banner.tvcFile != null && banner.iTVCExtension == ExpandTVCExtension.VIDEO_INTEGRATED) {
            tvcFile = banner.tvcFile
        }
        val content = Render.renderExpandableJs(
            Map("zoneId" -> zoneId,
                "height" -> banner.height,
                "width" -> banner.width,
                "fullheight" -> banner.expandHeight,
                "fullwidth" -> banner.expandWidth,
                "url_standard" -> banner.standardFile,
                "url_expand" -> banner.expandFile,
                "prefix" -> prefix,
                "display" -> displayStyle,
                "url_video" -> tvcFile,
                "url_static_richmedia" -> urlStatic.getValue.concat("/resource/richmedia/"),
                "url_backup" -> banner.backupFile))
        content
    }
}

class RenderBannerBalloonHandler(env: {
    val bannerModelService: IDataService[BannerModel]
}) extends BaseHandler {
    val factory = DynamicPropertyFactory.getInstance()
    val urlStatic = factory.getStringProperty("banner.url_static", "http://localhost/")

    override def process(request: HttpServletRequest, response: HttpServletResponse): Unit = Try {


        val bannerId = SecurityUtils.decode(request.getParameter("id"))
        val prefix = request.getParameter("p")
        if (bannerId > 0) {
            val banner = env.bannerModelService.load(bannerId)
            if (banner.isInstanceOf[BalloonBannerModel]) {
                val item = banner.asInstanceOf[BalloonBannerModel]
                renderJs(item, prefix) |> response.renderJs
            }
        }
    }

    def renderJs(banner: BalloonBannerModel, prefix: String): String = {
        var tvcFile = ""
        val displayStyle = ""

        if (banner.tvcFile != null && banner.iTVCExtension == ExpandTVCExtension.VIDEO_INTEGRATED) {
            tvcFile = banner.tvcFile
        }
        val content = Render.renderBalloonJs(
            Map("height" -> banner.height,
                "width" -> banner.width,
                "topheight" -> banner.barHeight,
                "topwidth" -> banner.barWidth,
                "fullheight" -> banner.expandHeight,
                "fullwidth" -> banner.expandWidth,
                "url_standard" -> banner.standardFile,
                "url_expand" -> banner.expandFile,
                "prefix" -> prefix,
                "display" -> displayStyle,
                "url_video" -> tvcFile,
                "url_static_richmedia" -> urlStatic.getValue.concat("/resource/richmedia/"),
                "url_backup" -> banner.backupFile))
        content
    }
}

class RenderHandler(env: {
    val serving: IServingEngine
    val zoneModelService: IZoneModelService
    val bannerModelService: IBannerModelService
    val campaignModelService: IDataService[CampaignModel]
    val campaignService: IDataService[Campaign]
}) extends RenderJsHandler(env) {


    override def process(request: HttpServletRequest, response: HttpServletResponse): Unit = Try {
        val zoneId: Int = SecurityUtils.decode(request.getParameter("zid"))
        val channel: String = request.getParameter("channel") ?? ""
        val companionTargeting = request.getParameter("postid") ?? null
        val url = ""


        val ip = WebUtils.getRemoteAddress(request)
        if (zoneId > 0) {
            val zone = env.zoneModelService.load(zoneId)
            var dynamic: String = "";
            val req = new AdsRequest(zone.siteId, List(zone.id), url, ip, WebUtils.getOrSetCookie(request, response), null, 1, companionTargeting, mutable.Map("os" -> List(WebUtils.getUserAgent(request))))
            try {
                dynamic = request.getParameter("zdyn")
                if (dynamic != null) {
                    dynamic = URLDecoder.decode(dynamic, "UTF-8")
                }
            } catch {
                case ioe: IOException =>
            }
            val result = serving.serve(req)
            val banner = env.bannerModelService.load(result.banner(0).id)
            MacroInsertion.insertBanner(banner)
            if (banner.isInstanceOf[MediaBannerModel] || banner.isInstanceOf[NetworkMediaBannerModel]) {
                render(zone, banner, dynamic, channel, ip, WebUtils.checkFlash(request)) |>
                    response.renderHtml

            } else if (banner.isInstanceOf[HtmlBannerModel] || banner.isInstanceOf[NetworkHtmlBannerModel]) {
                renderBannerHtml(zone, banner, dynamic, channel, ip, WebUtils.checkFlash(request)) |>
                    response.renderHtml
            } else if (banner.isInstanceOf[ExpandableBannerModel]) {
                renderExpandable(zone, banner, dynamic, channel, ip, WebUtils.checkFlash(request)) |>
                    response.renderHtml
            }
        } else {

        }
    }

    def render(zone: ZoneModel, banner: BannerModel, dynamic: String, channel: String, ip: String, flashSupport: Boolean): String = {
        val default = Map("zoneId" -> SecurityUtils.encode(zone.id),
            "itemId" -> SecurityUtils.encode(banner.id),
            "token" -> SecurityUtils.generateTimeToken(),
            "ip" -> ip)

        val deliveryUrl = AdsUtils.getTrackUrl("display",
            default + ("track" -> TrackKind.Impression))

        val clickUrl = AdsUtils.getTrackUrl("click",
            default + ("track" -> TrackKind.Click), banner.targetUrl)
        var thirdparty = ""
        if (banner.thirdPartyImpressionUrl != null && banner.thirdPartyImpressionUrl.length > 0) {
            if (banner.thirdPartyImpressionUrl(0) != null) {
                thirdparty = AdsUtils.wrapImg(banner.thirdPartyImpressionUrl(0))
            }
        }
        banner.kind match {
            case BannerKind.Media => {
                val media = banner.asInstanceOf[MediaBannerModel]
                var content: String = "";
                if (media.bannerFile.endsWith(".swf"))
                    content = AdsUtils.wrapItemFlash(SecurityUtils.encode(zone.id), clickUrl, media.bannerFile, "_blank", media.width, media.height, media.bannerFileFallback) + AdsUtils.wrapImg(deliveryUrl)
                else if (media.bannerFile.endsWith(".mp4"))
                    content = AdsUtils.wrapItemVideo(SecurityUtils.encode(zone.id), clickUrl, media.bannerFile, "_blank", media.width, media.height) + AdsUtils.wrapImg(deliveryUrl)
                else
                    content = AdsUtils.wrapItemImage(clickUrl, media.bannerFile, "_blank", "", "") + AdsUtils.wrapImg(deliveryUrl)
                //val content = AdsUtils.wrapClick(clickUrl, media.bannerFile) + AdsUtils.wrapImg(deliveryUrl)
                content = content + thirdparty
                return renderBanner(banner, zone, SecurityUtils.encode(zone.id), channel, content, dynamic)
            }
            case BannerKind.NetworkMedia => {
                val media = banner.asInstanceOf[NetworkMediaBannerModel]
                var content: String = "";
                if (media.bannerFile.endsWith(".swf"))
                    content = AdsUtils.wrapItemFlash(SecurityUtils.encode(zone.id), clickUrl, media.bannerFile, "_blank", media.width, media.height, media.bannerFileFallback) + AdsUtils.wrapImg(deliveryUrl)
                else if (media.bannerFile.endsWith(".mp4"))
                    content = AdsUtils.wrapItemVideo(SecurityUtils.encode(zone.id), clickUrl, media.bannerFile, "_blank", media.width, media.height) + AdsUtils.wrapImg(deliveryUrl)
                else
                    content = AdsUtils.wrapItemImage(clickUrl, media.bannerFile, "_blank", "", "") + AdsUtils.wrapImg(deliveryUrl)
                //val content = AdsUtils.wrapClick(clickUrl, media.bannerFile) + AdsUtils.wrapImg(deliveryUrl)
                content = content + thirdparty
                return renderBanner(banner, zone, SecurityUtils.encode(zone.id), channel, content, dynamic)
            }
        }
    }

    def renderBannerHtml(zone: ZoneModel, banner: BannerModel, dynamic: String, channel: String, ip: String, flashSupport: Boolean): String = {
        val default = Map("zoneId" -> SecurityUtils.encode(zone.id),
            "itemId" -> SecurityUtils.encode(banner.id),
            "token" -> SecurityUtils.generateTimeToken(),
            "ip" -> ip)

        val deliveryUrl = AdsUtils.getTrackUrl("display",
            default + ("track" -> TrackKind.Impression))

        banner.kind match {
            case BannerKind.Html => {
                val media = banner.asInstanceOf[HtmlBannerModel]
                val embedded = media.embeddedHtml;
                val jsParam = parseDynamicParameters(dynamic)
                val content = Render.renderIEHtml(
                    Map("url_static" -> urlStatic.get(),
                        "java_click_version" -> javaClickVersion.get(),
                        "java_ld_version" -> javaClickVersion.get(),
                        "css_click_version" -> cssClickVersion.get(),
                        "html_proxy_version" -> htmlProxyVersion.get(),
                        "channelUrl" -> channel,
                        "item_content" -> embedded.concat(AdsUtils.wrapImg(deliveryUrl)),
                        "width" -> zone.width,
                        "height" -> (zone.height),
                        "jsParam" -> jsParam))
                return content
            }
            case BannerKind.NetworkHtml => {
                val media = banner.asInstanceOf[NetworkHtmlBannerModel]
                val embedded = media.embeddedHtml;
                val jsParam = parseDynamicParameters(dynamic)
                val content = Render.renderIEHtml(
                    Map("url_static" -> urlStatic.get(),
                        "java_click_version" -> javaClickVersion.get(),
                        "java_ld_version" -> javaClickVersion.get(),
                        "css_click_version" -> cssClickVersion.get(),
                        "html_proxy_version" -> htmlProxyVersion.get(),
                        "channelUrl" -> channel,
                        "item_content" -> embedded.concat(AdsUtils.wrapImg(deliveryUrl)),
                        "width" -> zone.width,
                        "height" -> (zone.height),
                        "jsParam" -> jsParam))
                return content
            }
        }
    }

    def renderBanner(banner: BannerModel, zone: ZoneModel, zoneIdEncoded: String, channel: String, embedded: String, dynamic: String): String = {
        val jsParam = parseDynamicParameters(dynamic)
        val style = s"<style>.containerdiv { float: left; position: relative; } .cornerimage { position: absolute; top:${zone.height}px; right: 0; }</style>";
        val content = Render.renderIEHtml(
            Map("url_static" -> urlStatic.get(),
                "java_click_version" -> javaClickVersion.get(),
                "java_ld_version" -> javaClickVersion.get(),
                "css_click_version" -> cssClickVersion.get(),
                "html_proxy_version" -> htmlProxyVersion.get(),
                "channelUrl" -> channel,
                "item_content" -> style.concat(embedded),
                "width" -> zone.width,
                "height" -> (zone.height),
                "jsParam" -> jsParam))
        content
    }

}

class TrackVideoHandler(env: {
    val track: ITrack
    val bannerService: IBannerService
    val campaignService: ICampaignService
    val conversionService: IConversionService
    val zoneModelService: IZoneModelService
    val zoneCache: ModelCache[ZoneModel]
    val bannerCache: ModelCache[BannerModel]
    val siteCache: ModelCache[WebsiteModel]
    val log: ILog
}) extends SecureHandler {


    def process(request: HttpServletRequest, response: HttpServletResponse, params: Map[String, String]): Try[Any] =
        Try {
            var track = params("track")
            val zoneId = params("zoneId") |> SecurityUtils.decode
            val itemId = params("itemId") |> SecurityUtils.decode
            val ip = params("ip")
            val timeToken = params("token") |> SecurityUtils.decodeTimeToken
            val cookie = WebUtils.getOrSetCookie(request, response)
            val token = cookie + "@" + itemId
            val videoId = if (params.contains("vid")) params("vid").toInt else 0
            val adType = if (params.contains("adType")) params("adType").toInt else 0
            val location = GeoipService.getGeoipClient.getLocation(WebUtils.getRemoteAddress(request))
            val city = if (location != null) location.city else ""
            val task = new util.Random().nextInt(9998) + 1
            val now = System.currentTimeMillis()

            val banner = env.bannerService.load(itemId)
            if (ip != WebUtils.getRemoteAddress(request) && banner.kind != BannerKind.Tracking) {
                response.setStatus(500)
                return fail(500)
            }
            val orderId = env.campaignService.load(banner.campaignId).orderId
            val campId = banner.campaignId
            //            val zone = env.zoneModelService.load(zoneId)
            val zone = env.zoneCache.get(zoneId)
            val siteId = if (zone != null) zone.siteId else 0
            val zoneGroupIds: Array[Int] = if (zone != null) zone.groups else Array.empty
            var url = banner.targetUrl

            timeToken match {
                case Success(time) => {
                    if (track == TrackKind.Click) {
                        try {
                            //banner = MacroInsertion.insertBanner(banner)
                            url = banner.targetUrl
                            url = MacroInsertion.insert(url)
                            //set cookie for conversion
                            val cookie = new Cookie("123click_conversion_" + orderId, zoneId + "@" + itemId)
                            val conversion = env.conversionService.listByReferenceId(0, 1, orderId)
                            if (conversion != null && conversion.data.length > 0) {
                                if (conversion.data(0).windows > 0) {
                                    cookie.setMaxAge(conversion.data(0).windows * 3600 * 24)
                                    cookie.setPath("/")
                                    response.addCookie(cookie)
                                }
                            } else println(s"conversion of order ${orderId} not found !!")
                        } catch {
                            case ex: Exception => //System.out.println("Get conversion:"+ ex.getMessage);
                        }
                        response.sendRedirect(url)
                    }
                    else if (track == TrackKind.Impression) {
                        env.track.trackToken(token, time)
                        //TODO : write session impression
                        FrequencyUtils.writeFreq(cookie, banner.id, zone, now)
                    } else if (track == TrackKind.FirstQuartile) {

                    } else if (track == TrackKind.MidPoint) {

                    } else if (track == TrackKind.ThirdQuartile) {

                    } else if (track == TrackKind.Complete) {

                    } else if (track == TrackKind.Close) {

                    } else if (track == TrackKind.Pause) {

                    } else if (track == TrackKind.Resume) {

                    } else if (track == TrackKind.Fullscreen) {

                    } else if (track == TrackKind.Mute) {

                    } else if (track == TrackKind.Unmute) {

                    }
                    if (track == TrackKind.Click) {
                        if (!env.track.verifyToken(token, time)) {
                            track = TrackKind.Spam
                        }
                    }
                    env.log.log(LogKind.Normal, task, city, siteId, zoneGroupIds, zoneId, orderId, campId, itemId, WebUtils.getRemoteAddress(request).replace(',', '|'), cookie, track, System.currentTimeMillis(), 0, videoId, adType)

                }
                case _ => response.setStatus(500)
            }
        }
}

/*
class TrackHandler(env: {
    val track: ITrack
    val bannerService: IBannerService
    val campaignService: ICampaignService
    val conversionService: IConversionService
    val zoneModelService: IZoneModelService
    val bannerModelService: IBannerModelService
    val log: ILog}) extends SecureHandler {
    def process(request: HttpServletRequest, response: HttpServletResponse, params: Map[String, String]): Try[Any] =
        Try {
//            try{
                var track = params("track")
                val zoneId = params("zoneId") |> SecurityUtils.decode
                val itemId = params("itemId") |> SecurityUtils.decode
                val ip = params("ip")
                val timeToken = params("token") |> SecurityUtils.decodeTimeToken
                val cookie = WebUtils.getOrSetCookie(request, response)
                val token = cookie + "@" + itemId
                val time = System.currentTimeMillis()

                var banner = env.bannerService.load(itemId)
                if (ip != WebUtils.getRemoteAddress(request) && banner.kind != BannerKind.Tracking) {
                    val redirectUrl = request.getParameter("redirectUrl")
                    if (redirectUrl != null && !redirectUrl.equals(""))
                        response.sendRedirect(redirectUrl)
                    response.setStatus(500)
                    return fail(500)
                }

                val orderId = env.campaignService.load(banner.campaignId).orderId
                val campId = banner.campaignId
                val zone = env.zoneModelService.load(zoneId)
                val siteId = if (zone != null) zone.siteId else 0
                val zoneGroupIds: Array[Int] = if (zone != null) zone.groups else Array.empty
                var url = banner.targetUrl

                val time = timeToken match {
                    case Success(time) => time
                    case _ => -1
                }
                if (time != -1 || banner.kind.equals(BannerKind.Tracking)) {
                    if (track == TrackKind.Click) {
                        //set cookie for
                        try {
                            banner = MacroInsertion.insertBanner(banner)
                            url = banner.targetUrl
                            val cookie = new Cookie("123click_conversion_" + orderId, zoneId + "@" + itemId)
                            val conversion = env.conversionService.listByReferenceId(0, 1, orderId)
                            if (conversion != null && conversion.data.length > 0) {
                                if (conversion.data(0).windows > 0) {
                                    cookie.setMaxAge(conversion.data(0).windows * 3600 * 24)
                                    cookie.setPath("/")
                                    response.addCookie(cookie)
                                }
                            } else println(s"conversion of order ${orderId} not found !!")
                        } catch{
                            case ex:Exception => //System.out.println("Get conversion:"+ ex.getMessage);
                        }
                        if (banner.kind.equals(BannerKind.Tracking) && params.contains("pixel")) {
                            WebUtils.renderTrackingImage(response)
                        } else{
                            response.sendRedirect(url)
                        }

                    }
                    else if (track == TrackKind.Impression) {
                        if (banner.kind.equals(BannerKind.Tracking) && !params.contains("pixel")) {
                            response.sendRedirect(env.bannerModelService.load(itemId).asInstanceOf[TrackingBanner].bannerFile)
                        }else if (params.contains("redirect"))
                            response.sendRedirect(params("redirect"))
                        else {
                            val redirectUrl = request.getParameter("redirectUrl")
                            if (redirectUrl != null && !redirectUrl.equals(""))
                                response.sendRedirect(redirectUrl)
                            env.track.trackToken(token, time)
                            WebUtils.renderTrackingImage(response)
                        }

                        //TODO : write session impression
                        writeImpressionToHyperdex(request,banner.id, time)
                    }
                    if (track == TrackKind.Click) {
                        if (!env.track.verifyToken(token, time)) {
                            track = TrackKind.Spam
                        }
                    }
                    env.log.log(LogKind.Normal, task, city, siteId, zoneGroupIds, zoneId, orderId, campId, itemId, WebUtils.getRemoteAddress(request).replace(',','|'), cookie, track, time, 0, 0)
                }
//            } catch {
//                case ex: Exception => {
//                    val redirectUrl = request.getParameter("redirectUrl")
//                    if (redirectUrl != null && !redirectUrl.equals(""))
//                        response.sendRedirect(redirectUrl)
//                    println(s"Request exception: ${request.getRequestURL}/${request.getQueryString}\nip: ${WebUtils.getRemoteAddress(request)}")
//                    println(ex.getMessage)
//                }
//            }
        }
}
*/

class TrackHandler(env: {
    val track: ITrack
    val bannerService: IBannerService
    val campaignService: ICampaignService
    val conversionService: IConversionService
    val zoneModelService: IZoneModelService
    val bannerModelService: IBannerModelService
    val bannerCache: ModelCache[BannerModel]
    val zoneCache: ModelCache[ZoneModel]
    val siteCache: ModelCache[WebsiteModel]
    val log: ILog}) extends SecureHandler {
    def process(request: HttpServletRequest, response: HttpServletResponse, params: Map[String, String]): Try[Any] =
        Try {
            try {
                var track = params("track")
                val zoneId = params("zoneId") |> SecurityUtils.decode
                val itemId = params("itemId") |> SecurityUtils.decode
                val ip = params("ip")
                val timeToken = params("token") |> SecurityUtils.decodeTimeToken
                val cookie = WebUtils.getOrSetCookie(request, response)
                val token = cookie + "@" + itemId
                val videoId = if (params.contains("vid")) params("vid").toInt else 0
                val now = System.currentTimeMillis()
                val location = GeoipService.getGeoipClient.getLocation(WebUtils.getRemoteAddress(request))
                val city = if (location != null) location.city else ""
                val task = new util.Random().nextInt(9998) + 1

                val banner = env.bannerService.load(itemId)

                if (ip != WebUtils.getRemoteAddress(request) && banner.kind != BannerKind.Tracking && banner.kind != BannerKind.Article) {
                    val redirectUrl = request.getParameter("redirectUrl")
                    if (redirectUrl != null && !redirectUrl.equals("")) response.sendRedirect(redirectUrl)
                    response.setStatus(500)
                    return fail(500)
                }

                val orderId = env.campaignService.load(banner.campaignId).orderId
                val campId = banner.campaignId
                val zone = env.zoneCache.get(zoneId)
                val siteId = if (zone != null) zone.siteId else 0
                val zoneGroupIds: Array[Int] = if (zone != null && zone.groups != null) zone.groups else Array.empty
                var url = banner.targetUrl

                val time = timeToken match {
                    case Success(time) => time
                    case _ => -1
                }
                if (time != -1 || track == TrackKind.Skip || banner.kind == BannerKind.Article) {
                    track match {
                        case TrackKind.Click => {
                            try {
                                //banner = MacroInsertion.insertBanner(banner)
                                url = banner.targetUrl
                                url = MacroInsertion.insert(url)
                                val cookie = new Cookie("123click_conversion_" + orderId, zoneId + "@" + itemId)
                                val conversion = env.conversionService.listByReferenceId(0, 1, orderId)
                                if (conversion != null && conversion.data.length > 0) {
                                    if (conversion.data(0).windows > 0) {
                                        cookie.setMaxAge(conversion.data(0).windows * 3600 * 24)
                                        cookie.setPath("/")
                                        response.addCookie(cookie)
                                    }
                                } else println(s"conversion of order ${orderId} not found !!")
                            } catch {
                                case ex: Exception => //System.out.println("Get conversion:"+ ex.getMessage);
                            }
                            response.sendRedirect(url)
                            if (!env.track.verifyToken(token, time)) track = TrackKind.Spam
                        }
                        case TrackKind.Impression => {
                            env.track.trackToken(token, time)
                            if (params.contains("redirect") && params("redirect") == "banner") {
                                val redirectUrl = request.getParameter("redirectUrl")
                                if (redirectUrl != null && !redirectUrl.equals(""))
                                    response.sendRedirect(redirectUrl)
                            } else WebUtils.renderTrackingImage(response)
                            //TODO : write session impression
                            FrequencyUtils.writeFreq(cookie, banner.id, zone, now)
                        }
                        case TrackKind.Skip => {
                            val redirectUrl = request.getParameter("redirectUrl")
                            if (redirectUrl != null && !redirectUrl.equals(""))
                                response.sendRedirect(redirectUrl)
                        }
                        case _ =>
                    }
                    env.log.log(LogKind.Normal, task, city, siteId, zoneGroupIds, zoneId, orderId, campId, itemId, WebUtils.getRemoteAddress(request).replace(',', '|'), cookie, track, now, 0, 0)
                }
                else if (banner.kind.equals(BannerKind.Tracking)) {
                    if (params.contains("pixel"))
                        WebUtils.renderTrackingImage(response)
                    else track match {
                        case TrackKind.Click => response.sendRedirect(url)
                        //                        case TrackKind.Impression => response.sendRedirect(env.bannerModelService.load(itemId).asInstanceOf[TrackingBanner].bannerFile)
                        case TrackKind.Impression => response.sendRedirect(env.bannerCache.get(itemId).asInstanceOf[TrackingBanner].bannerFile)
                        case _ =>
                    }
                    env.log.log(LogKind.Normal, task, city, siteId, zoneGroupIds, zoneId, orderId, campId, itemId, WebUtils.getRemoteAddress(request).replace(',', '|'), cookie, track, now, 0, videoId)
                }

                if (params.contains("retargeting")) {
                    val retargeting = params("retargeting") |> SecurityUtils.decode
                    RetargetingUtils.writeRetargetingData(cookie, retargeting)
                }
            }
            catch {
                case NonFatal(e) => {
                    if (e.getCause != null) {
                        e.getCause.printStackTrace(response.getWriter)
                    }
                    else e.printStackTrace(response.getWriter)
                    val redirectUrl = request.getParameter("redirectUrl")
                    if (redirectUrl != null && !redirectUrl.equals(""))
                        response.sendRedirect(redirectUrl)
                    println(s"Request: ${request.getRequestURL}?${request.getQueryString}")
                    throw e
                }
            }
        }
}

class TrackPRHandler(env: {
    val track: ITrack
    val bannerService: IBannerService
    val campaignService: ICampaignService
    val conversionService: IConversionService
    val zoneModelService: IZoneModelService
    val bannerModelService: IBannerModelService
    val bannerCache: ModelCache[BannerModel]
    val zoneCache: ModelCache[ZoneModel]
    val siteCache: ModelCache[WebsiteModel]
    val log: ILog}) extends SecureHandler {
    def process(request: HttpServletRequest, response: HttpServletResponse, params: Map[String, String]): Try[Any] =
        Try {
            try {
                val track = params("track")
                val zoneId = params("zoneId") |> SecurityUtils.decode
                val itemId = params("itemId") |> SecurityUtils.decode
                val retargeting = params("retargeting") |> SecurityUtils.decode
                val cookie = WebUtils.getOrSetCookie(request, response)
                val now = System.currentTimeMillis()
                val location = GeoipService.getGeoipClient.getLocation(WebUtils.getRemoteAddress(request))
                val city = if (location != null) location.city else ""
                val task = new util.Random().nextInt(9998) + 1

                val banner = env.bannerService.load(itemId)
                if (banner.kind != BannerKind.Article) {
                    response.setStatus(500)
                    return fail(500)
                }

                val orderId = env.campaignService.load(banner.campaignId).orderId
                val campId = banner.campaignId
                val zone = env.zoneCache.get(zoneId)
                val siteId = if (zone != null) zone.siteId else 0
                val zoneGroupIds: Array[Int] = if (zone != null) zone.groups else Array.empty

                track match {
                    case TrackKind.Impression => {
                        WebUtils.renderTrackingImage(response)
                        //TODO : write retargeting data
                        if (retargeting > 0)
                            RetargetingUtils.writeRetargetingData(cookie, retargeting)
                    }
                    case _ =>
                }
                env.log.log(LogKind.Normal, task, city, siteId, zoneGroupIds, zoneId, orderId, campId, itemId, WebUtils.getRemoteAddress(request).replace(',', '|'), cookie, track, now, 0, 0)
            }
            catch {
                case NonFatal(e) => {
                    if (e.getCause != null) {
                        e.getCause.printStackTrace(response.getWriter)
                    }
                    else e.printStackTrace(response.getWriter)
                    val redirectUrl = request.getParameter("redirectUrl")
                    if (redirectUrl != null && !redirectUrl.equals(""))
                        response.sendRedirect(redirectUrl)
                    println(s"Request: ${request.getRequestURL}?${request.getQueryString}")
                    throw e
                }
            }
        }
}


class PageviewTrackHandler(env: {val track: ITrack}) extends BaseHandler {
    def process(request: HttpServletRequest, response: HttpServletResponse): Unit = {
        val host = request.getParameter("zdm") ?? ""
        val url = request.getParameter("zpa") ?? ""
        val charset = request.getParameter("zch") ?? ""
        val title = request.getParameter("zt") ?? ""
        val lang = request.getParameter("zla") ?? ""
        val token = WebUtils.getOrSetCookie(request, response)
        //@TODO: logs pageview
        WebUtils.renderTrackingImage(response)
    }
}

class PageviewHitHandler() extends BaseHandler {
    def process(request: HttpServletRequest, response: HttpServletResponse): Unit = {
        val host = request.getParameter("zdm") ?? ""
        val url = request.getParameter("zpa") ?? ""
        val charset = request.getParameter("zch") ?? ""
        val title = request.getParameter("zt") ?? ""
        val lang = request.getParameter("zla") ?? ""
        val params = s"zdm=$host&zpa=$url&zch=$charset&zt=$title&zla=$lang"

        val output = Render.renderTrack(Config.jsDomain.getValue, "", params)

        response.setCharacterEncoding("utf-8")
        response.setContentType("text/html; charset=utf-8")
        val out = response.getOutputStream
        if (out != null) {
            out.println(output)
            out.close()
        }
    }
}

class RenderExtHandler(env: {
    val serving: IServingEngine
    val zoneModelService: IZoneModelService
    val bannerModelService: IBannerModelService
    val websiteModelService: IWebsiteModelService
    val campaignService: ICampaignService
    val log: ILog
}) extends SmartDispatcherHandler {
    val factory = DynamicPropertyFactory.getInstance()
    val urlStatic = factory.getStringProperty("banner.url_static", "http://localhost/")
    val javaClickVersion = factory.getStringProperty("version.java_click_version", "1.0.0.0")
    val cssClickVersion = factory.getStringProperty("version.css_click_version", "1.0.0.0")
    val htmlProxyVersion = factory.getStringProperty("version.html_proxy_version", "1.0.0.0")

    @Invoke(Parameters = "request,response,zoneId,url,flag")
    def skipad(request: HttpServletRequest, response: HttpServletResponse, zoneId: String, url: String, flag: Int) = {
        val zid: Int = SecurityUtils.decode(zoneId)
        val serving = env.serving
        val ip = WebUtils.getRemoteAddress(request)
        val companionTargeting = request.getParameter("postid") ?? null
        var bannerId = 0
        var campId = 0
        var orderId = 0

        if (zid > 0) {
            val zone = env.zoneModelService.load(zid)
            if (zone != null) {
                val req = new AdsRequest(zone.siteId, List(zone.id), url, ip, WebUtils.getOrSetCookie(request, response), null, 1, companionTargeting, mutable.Map("os" -> List(WebUtils.getUserAgent(request))))
                val result = serving.serve(req)
                if (result != null && result.banner != null && result.banner.length > 0) {
                    val banner = env.bannerModelService.load(result.banner(0).id)
                    bannerId = banner.id
                    campId = banner.campaignId
                    orderId = env.campaignService.load(campId).orderId
                    var content = ""
                    val default = Map("zoneId" -> SecurityUtils.encode(zid),
                        "itemId" -> SecurityUtils.encode(banner.id),
                        "token" -> SecurityUtils.generateTimeToken(),
                        "ip" -> ip)

                    lazy val deliveryUrl = AdsUtils.getTrackUrl("display", default + ("track" -> TrackKind.Impression))
                    lazy val clickUrl = AdsUtils.getTrackUrl("click", default + ("track" -> TrackKind.Click), banner.targetUrl)
                    lazy val skipUrl = AdsUtils.getTrackUrl("click", default + ("track" -> TrackKind.Skip), url)

                    banner match {
                        case media: MediaBannerModel => {
                            if (media.bannerFile.endsWith(".swf"))
                                content = AdsUtils.wrapItemFlash(SecurityUtils.encode(zone.id), clickUrl, media.bannerFile, "_blank", media.width, media.height, media.bannerFileFallback)
                            else if (media.bannerFile.endsWith(".mp4"))
                                content = AdsUtils.wrapItemVideo(SecurityUtils.encode(zone.id), clickUrl, media.bannerFile, "_blank", media.width, media.height)
                            else
                                content = AdsUtils.wrapItemImage(clickUrl, media.bannerFile, "_blank", "", "")
                            content += AdsUtils.wrapImg(deliveryUrl)
                        }
                        case b: HtmlBannerModel => content = b.embeddedHtml
                        case _ =>
                    }

                    Render.renderSkipAd(
                        Map("url" -> skipUrl,
                            "content" -> content)
                    ) |> response.renderHtml
                } else if (flag == 0) {
                    response.sendRedirect(url)
                } else response.sendRedirect(s"http://ay.gy/6973258/$url")
            } else response.sendRedirect(url)
            val location = GeoipService.getGeoipClient.getLocation(ip)
            val city = if (location != null) location.city else ""
            val task = new util.Random().nextInt(9998) + 1
            env.log.log(LogKind.Normal, task, city, zone.siteId, zone.groups, zone.id, orderId, campId, bannerId, WebUtils.getRemoteAddress(request).replace(',','|'), WebUtils.getOrSetCookie(request, response), "render", System.currentTimeMillis(), 0, 0)
        } else response.sendRedirect(url)
    }

    @Invoke(Parameters = "request,response,zoneId")
    def html(request: HttpServletRequest, response: HttpServletResponse, zoneId: String) = {
        //Text(Render.renderZone(env.zoneService.load(SecurityUtils.decode(zoneId))))
        val zid: Int = SecurityUtils.decode(zoneId)
        val channel: String = ""
        val url = ""
        val companionTargeting = request.getParameter("postid") ?? null
        val serving = env.serving
        val ip = WebUtils.getRemoteAddress(request)
        val location = GeoipService.getGeoipClient.getLocation(ip)
        val city = if (location != null) location.city else ""
        val task = new util.Random().nextInt(9998) + 1
        if (zid > 0) {
            val zone = env.zoneModelService.load(zid)
            if (zone != null) {
                var dynamic: String = ""
                val req = new AdsRequest(zone.siteId, List(zone.id), url, ip, WebUtils.getOrSetCookie(request, response), null, 1, companionTargeting, mutable.Map("os" -> List(WebUtils.getUserAgent(request))))

                val result = serving.serve(req)
                var banner: BannerModel = null
                var campId: Int = 0
                var orderId: Int = 0
                if (result != null && result.banner != null && result.banner.length > 0) {
                    banner = env.bannerModelService.load(result.banner(0).id)
                    MacroInsertion.insertBanner(banner)
                    val default = Map("zoneId" -> SecurityUtils.encode(zone.id),
                        "itemId" -> SecurityUtils.encode(banner.id),
                        "token" -> SecurityUtils.generateTimeToken(),
                        "ip" -> ip)
                    var deliveryUrl = AdsUtils.getTrackUrl("display", default + ("track" -> TrackKind.Impression))
                    var clickUrl = AdsUtils.getTrackUrl("click", default + ("track" -> TrackKind.Click), banner.targetUrl)
                    if (request.getHeader("X-Forwarded-Proto") == "https") {
                        deliveryUrl = "https" + deliveryUrl.substring(4)
                        clickUrl = "https" + clickUrl.substring(4)
                    }
                    if (banner.isInstanceOf[MediaBannerModel] || banner.isInstanceOf[NetworkMediaBannerModel]) {
                        render(zone, banner, dynamic, channel, deliveryUrl, clickUrl) |> response.renderHtml
                    } else if (banner.isInstanceOf[HtmlBannerModel] || banner.isInstanceOf[NetworkHtmlBannerModel]) {
                        renderBannerHtml(zone, banner, dynamic, channel, deliveryUrl) |> response.renderHtml
                    } else if (banner.isInstanceOf[PrBannerModel] || banner.isInstanceOf[NetworkPrBannerModel]) {
                        renderPrBanner(zone, banner, ip, req) |> response.renderHtml
                    } else {
                        "" |> response.renderHtml
                    }
                    campId = banner.campaignId
                    orderId = env.campaignService.load(banner.campaignId).orderId
                } else {
                    "" |> response.renderHtml
                }
                val bannerId = if (banner != null) banner.id else 0
                val siteId = if (zone != null) zone.siteId else 0
                val zoneGroupIds: Array[Int] = if (zone != null) zone.groups else Array.empty
                env.log.log(LogKind.Normal, task, city, siteId, zoneGroupIds, zid, orderId, campId, bannerId, WebUtils.getRemoteAddress(request).replace(',', '|'), WebUtils.getOrSetCookie(request, response), TrackKind.Render, System.currentTimeMillis(), 0, 0)
            } else {
                System.out.println("Zone id " + zid + " is null")
                "" |> response.renderHtml
            }
        } else {
            "" |> response.renderHtml
        }
    }

    def renderPrBanner(zone: ZoneModel, banner: BannerModel, ip: String, request: AdsRequest): String = {

        val default = Map("zoneId" -> SecurityUtils.encode(zone.id),
            "itemId" -> SecurityUtils.encode(banner.id),
            "token" -> SecurityUtils.generateTimeToken(),
            "ip" -> ip)

        val deliveryUrl = AdsUtils.getTrackUrl("display",
            default + ("track" -> TrackKind.Impression))

        banner.kind match {
            case BannerKind.PrBanner | BannerKind.NetworkPrBanner => {
                val model = banner.asInstanceOf[PrBannerModel]
                val data = env.serving.getPrData(request, model)
                val map: mutable.HashMap[String, Any] = new mutable.HashMap[String, Any]
                map put("url_static", urlStatic.getValue)
                var count = 1
                for (b <- data) {
                    try {
                        if (count == 1) {
                            if (b.photos.length > 0) map put("image_url" + count, b.photos(0))
                            map put("description" + count, b.summary)
                        }
                        val default = Map("zoneId" -> SecurityUtils.encode(zone.id),
                            "itemId" -> SecurityUtils.encode(b.id),
                            "token" -> SecurityUtils.generateTimeToken(),
                            "ip" -> ip)
                        val trackingClick = AdsUtils.getTrackUrl("click", default + ("track" -> TrackKind.Click), b.publishedUrl)

                        map put("title" + count, b.name)
                        map put("link" + count, trackingClick)
                        count += 1
                    } catch {
                        case x: Throwable => x.printStackTrace()
                    }

                }
                val content: String = count - 1 match {
                    case 0 | 1 => Render.renderPrBanner1(map.toMap)
                    case 2 => Render.renderPrBanner2(map.toMap)
                    case 3 => Render.renderPrBanner3(map.toMap)
                }
                content.concat(AdsUtils.wrapImg(deliveryUrl))
            }
        }
    }

    def render(zone: ZoneModel, banner: BannerModel, dynamic: String, channel: String, deliveryUrl: String, clickUrl: String): String = {


        val media = banner.asInstanceOf[MediaBannerModel]
        var content: String = "";
        if (media.bannerFile.endsWith(".swf"))
            content = AdsUtils.wrapItemFlash(SecurityUtils.encode(zone.id), clickUrl, media.bannerFile, "_blank", media.width, media.height, media.bannerFileFallback) + AdsUtils.wrapImg(deliveryUrl)
        else if (media.bannerFile.endsWith(".mp4"))
            content = AdsUtils.wrapItemVideo(SecurityUtils.encode(zone.id), clickUrl, media.bannerFile, "_blank", media.width, media.height) + AdsUtils.wrapImg(deliveryUrl)
        else
            content = AdsUtils.wrapItemImage(clickUrl, media.bannerFile, "_blank", "", "") + AdsUtils.wrapImg(deliveryUrl)

        return renderBanner(banner, zone, SecurityUtils.encode(zone.id), channel, content, dynamic)

    }

    def renderBannerHtml(zone: ZoneModel, banner: BannerModel, dynamic: String, channel: String, deliveryUrl: String): String = {


        val media = banner.asInstanceOf[HtmlBannerModel]
        val embedded = media.embeddedHtml;
        val jsParam = ""
        val content = Render.renderExHtml(
            Map("url_static" -> urlStatic.get(),
                "java_click_version" -> javaClickVersion.get(),
                "java_ld_version" -> javaClickVersion.get(),
                "css_click_version" -> cssClickVersion.get(),
                "html_proxy_version" -> htmlProxyVersion.get(),
                "channelUrl" -> channel,
                "item_content" -> embedded.concat(AdsUtils.wrapImg(deliveryUrl)),
                "width" -> zone.width,
                "height" -> (zone.height),
                "jsParam" -> jsParam))
        return content
    }

    def renderBanner(banner: BannerModel, zone: ZoneModel, zoneIdEncoded: String, channel: String, embedded: String, dynamic: String): String = {
        val jsParam = ""
        val style = s"<style>.containerdiv { float: left; position: relative; } .cornerimage { position: absolute; top:${zone.height}px; right: 0; }</style>";
        val content = Render.renderExHtml(
            Map("url_static" -> urlStatic.get(),
                "java_click_version" -> javaClickVersion.get(),
                "java_ld_version" -> javaClickVersion.get(),
                "css_click_version" -> cssClickVersion.get(),
                "html_proxy_version" -> htmlProxyVersion.get(),
                "channelUrl" -> channel,
                "item_content" -> style.concat(embedded),
                "width" -> zone.width,
                "height" -> (zone.height),
                "jsParam" -> jsParam))
        content
    }

    @Invoke(Parameters = "request,response,zoneId")
    def json(request: HttpServletRequest, response: HttpServletResponse, zoneId: String) = {

        val zoneIdDeCode = SecurityUtils.decode(zoneId)
        val zone = env.zoneModelService.load(zoneIdDeCode)

        val vs = request.getParameter("vs") ?? ""
        val dyValues = vs.split(";")
        val vid = request.getParameter("zvid") ?? "0"
        val location = GeoipService.getGeoipClient.getLocation(WebUtils.getRemoteAddress(request))
        val city = if (location != null) location.city else ""
        val task = new util.Random().nextInt(9998) + 1
        val listValues = new util.HashMap[String, List[String]]()
        dyValues.map(item => {
            val t = item.split("=")
            if (t.size == 2) {
                listValues.put(t(0).toLowerCase, t(1).split(",").toList.map(b => b.toLowerCase))
            }
        })

        val siteId = if (zone != null) zone.siteId else 0
        val zoneGroupIds: Array[Int] = if (zone != null) zone.groups else Array.empty
        val pcount: String = request.getParameter("count")
        var ip: String = request.getParameter("ip")
        if (ip == null || ip.length == 0) {
            ip = WebUtils.getRemoteAddress(request)
        }
        var count: Int = 1
        if (pcount != null && pcount.length > 0) {
            count = pcount.toInt
        }
        val url = ""
        val companionTargeting = request.getParameter("postid") ?? null
        val req = new AdsRequest(zone.siteId, List(zone.id), url, WebUtils.getRemoteAddress(request), WebUtils.getOrSetCookie(request, response), null, count, companionTargeting, listValues ++ Map("os" -> List(WebUtils.getUserAgent(request))))
        val ads = if (count != 1) env.serving.serveMulti(req)
        else env.serving.serve(req)
        val result: ArrayBuffer[BannerJsonModel] = new ArrayBuffer[BannerJsonModel]()
        if (ads.banner != null) {
            val banners = ads.banner
            val default = Map("zoneId" -> zoneId, "token" -> SecurityUtils.generateTimeToken(), "ip" -> ip, "vid" -> vid)
            for (i <- 0 until banners.length) {
                var bannerFile: String = ""
                var format: String = ""
                var embeddedHtml: String = ""
                var currentUsage: Float = 0f
                var bannerFileTracking: String = ""
                var extendData: util.HashMap[String, String] = new util.HashMap[String, String]()

                MacroInsertion.insertBanner(banners(i))
                banners(i).kind match {
                    case BannerKind.Media | BannerKind.NetworkMedia | BannerKind.NetworkOverlayBanner | BannerKind.NetworkPauseAd => {
                        val banner = WebUtils.fromJson(classOf[MediaBannerModel], banners(i).extra)
                        bannerFile = banner.bannerFile
                        format = banner.bannerFile.split('.').last
                        currentUsage = banner.currentUsage
                        extendData = banner.extendData
                    }
                    case BannerKind.Html | BannerKind.NetworkHtml => {
                        val banner = WebUtils.fromJson(classOf[HtmlBannerModel], banners(i).extra)
                        embeddedHtml = banner.embeddedHtml
                        currentUsage = banner.currentUsage
                        extendData = banner.extendData
                    }
                    case BannerKind.Ecommerce => {
                        val banner = WebUtils.fromJson(classOf[EcommerceBannerModel], banners(i).extra)
                        bannerFile = banner.properties.getOrElse("imageUrl", "")
                        format = bannerFile.split('.').last
                        currentUsage = banner.currentUsage
                        extendData = banner.extendData
                    }
                    case BannerKind.Expandable => {
                        //val banner = banners.data(i).asInstanceOf[ExpandableBannerModel]
                    }
                    case BannerKind.NetworkTVC => {
                        //val banner = banners.data(i).asInstanceOf[NetworkTVCBannerModel]
                    }
                    case BannerKind.Tracking => {

                    }
                    case BannerKind.NetworkExpandable => {}
                    case BannerKind.NetworkBalloon => {}
                    case BannerKind.PrBanner | BannerKind.NetworkPrBanner => {
                        val banner = WebUtils.fromJson(classOf[PrBannerModel], banners(i).extra)
                        embeddedHtml = renderPrBanner(zone, banner, ip, req)
                    }
                    case _ =>
                }
                val deliveryUrl = AdsUtils.getTrackUrl("display",
                    default + ("itemId" -> SecurityUtils.encode(banners(i).id)) + ("track" -> TrackKind.Impression))
                val clickUrl = AdsUtils.getTrackUrl("click",
                    default + ("itemId" -> SecurityUtils.encode(banners(i).id)) + ("track" -> TrackKind.Click), banners(i).targetUrl)
                if (bannerFile != "") bannerFileTracking = AdsUtils.getTrackUrl("display",
                    default + ("itemId" -> SecurityUtils.encode(banners(i).id)) + ("redirect" -> "banner") + ("track" -> TrackKind.Impression)) + "&redirectUrl=" + java.net.URLEncoder.encode(bannerFile)
                result += new BannerJsonModel(banners(i).height, banners(i).width, zone.height, zone.width, format, bannerFile, bannerFileTracking, embeddedHtml, currentUsage, deliveryUrl, clickUrl, banners(i).thirdPartyImpressionUrl, extendData, banners(i).kind)

                val campId = banners(i).campaignId
                val orderId = env.campaignService.load(banners(i).campaignId).orderId

                env.log.log(LogKind.Normal, task, city, siteId, zoneGroupIds, zoneIdDeCode, orderId, campId, banners(i).id, WebUtils.getRemoteAddress(request).replace(',', '|'), WebUtils.getOrSetCookie(request, response), TrackKind.Render, System.currentTimeMillis(), 0, 0)

            }
        } else env.log.log(LogKind.Normal, task, city, siteId, zoneGroupIds, zoneIdDeCode, 0, 0, 0, WebUtils.getRemoteAddress(request).replace(',', '|'), WebUtils.getOrSetCookie(request, response), TrackKind.Render, System.currentTimeMillis(), 0, 0)
        Json(result)
    }

    @Invoke(Parameters = "request,websiteId")
    def test(request: HttpServletRequest, websiteId: Int) = {
        val engine = new TemplateEngine() {
            workingDirectory = new File(Config.pathTemplate.getValue)
        }
        var html = engine.layout("templates/script_render.mustache", Map("domain" -> Config.jsDomain.getValue))
        val listZones = env.websiteModelService.load(websiteId).zoneModels
        for (zone <- listZones) {
            val style = "style='background-color:#F6F4F0;width:" + zone.width.toString + ";height:" + zone.height.toString + "'"
            html += engine.layout("templates/test_zone.mustache",
                Map(
                    "domain" -> Config.jsDomain.getValue,
                    "zoneId" -> zone.id,
                    "name" -> zone.name,
                    "width" -> zone.width,
                    "height" -> zone.height,
                    "id" -> SecurityUtils.encode(zone.id),
                    "inline" -> style))
        }
        html
    }

    @Invoke(Parameters = "request,response,zoneId")
    def decodeZone(request: HttpServletRequest, response: HttpServletResponse, zoneId: String) = {
        val zoneIdDeCode = SecurityUtils.decode(zoneId)
        val zone = env.zoneModelService.load(zoneIdDeCode)
        Json(zone)
    }
}


class TrackServlet extends HandlerContainerServlet {
    def factory(): BaseHandler = new TrackHandler(Environment)
}


class TrackVideoServlet extends HandlerContainerServlet {
    def factory(): BaseHandler = new TrackVideoHandler(Environment)
}

class TrackPRServlet extends HandlerContainerServlet {
    def factory(): BaseHandler = new TrackPRHandler(Environment)
}

class PageviewHitServlet extends HandlerContainerServlet {
    def factory() = new PageviewTrackHandler(Environment)
}

class PageviewServlet extends HandlerContainerServlet {
    def factory() = new PageviewHitHandler
}

class RenderJsServlet extends HandlerContainerServlet {
    def factory(): BaseHandler = return new RenderJsHandler(Environment)
}

class RenderServlet extends HandlerContainerServlet {
    def factory(): BaseHandler = return new RenderHandler(Environment)
}

class RenderVideoServlet extends HandlerContainerServlet {
    def factory(): BaseHandler = return new RenderVideoHandler(Environment)
}

class RenderVideoCompanionServlet extends HandlerContainerServlet {
    def factory(): BaseHandler = return new RenderVideoCompanionHandler(Environment)
}

class RenderMultiBannerServlet extends HandlerContainerServlet {
    def factory(): BaseHandler = return new RenderMultiBannerHandler(Environment)
}

class RenderBannerHtmlServlet extends HandlerContainerServlet {
    def factory(): BaseHandler = return new RenderBannerHtmlHandler(Environment)
}

class RenderBannerExpandableServlet extends HandlerContainerServlet {
    def factory(): BaseHandler = return new RenderBannerExpandableHandler(Environment)
}

class RenderBannerBalloonServlet extends HandlerContainerServlet {
    def factory(): BaseHandler = return new RenderBannerBalloonHandler(Environment)
}

class RenderAjsServlet extends HandlerContainerServlet {
    def factory(): BaseHandler = return new RenderAjsHandler(Environment)
}

class TestServingServlet extends HandlerContainerServlet {
    def factory(): BaseHandler = return new TestServingHandler(Environment)
}

class RenderExtServlet extends HandlerContainerServlet {
    def factory(): BaseHandler = return new RenderExtHandler(Environment)
}

class RenderCrossDomainServlet extends HandlerContainerServlet {
    def factory(): BaseHandler = return new RenderCrossDomainHandler(Environment)
}

class RenderProxyServlet extends HandlerContainerServlet {
    def factory(): BaseHandler = return new RenderProxyHandler(Environment)
}
