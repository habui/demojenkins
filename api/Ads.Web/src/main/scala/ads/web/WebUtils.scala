package ads.web

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64
import javax.servlet.http.{Cookie, HttpServletRequest, HttpServletResponse}
import ads.common.Syntaxs._
import com.google.gson.Gson
import java.lang.reflect.Type
import org.apache.commons.lang.{RandomStringUtils, StringEscapeUtils, StringUtils}
import com.netflix.config.DynamicPropertyFactory
import ads.common.{JsonExt, SecurityUtils}
import java.util.{Calendar, UUID}
import java.text.SimpleDateFormat
import java.util.regex.Pattern
import org.apache.commons.lang.text.StrTokenizer
import ads.common.model._
import java.util
import java.io.{FileInputStream, File}

object MacroInsertion {

    def replace(source: String, targetString: String, replace: String) = {
        var result: StringBuilder = new StringBuilder(source)
        val l = targetString.length
        var index = result.indexOf(targetString)
        while (index > -1) {
            result = result.replace(index, index + l, replace)
            index = result.indexOf(targetString,index)
        }
        result.toString
    }

    def insert(source: String): String = {
        if (source == null) return source
        val time: Calendar = Calendar.getInstance()
        var result = replace(source, "{TIMESTAMP}", time.getTime.getTime.toString)
        result = replace(result, "{RANDOM}", UUID.randomUUID().toString.replaceAll("-", ""))
        result = replace(result, "{RANDOMNUMBER}", new util.Random().nextInt(Int.MaxValue).toString)
        result = replace(result, "{HHMMSS}", (new java.text.SimpleDateFormat("HHmmss")).format(time.getTime))
        result = replace(result, "%7BTIMESTAMP%7D", time.getTime.getTime.toString)
        result = replace(result, "%7BRANDOM%7D", UUID.randomUUID().toString.replaceAll("-", ""))
        result = replace(result, "%7BRANDOMNUMBER%7D", new util.Random().nextInt(Int.MaxValue).toString)
        result = replace(result, "%7BHHMMSS%7D", (new java.text.SimpleDateFormat("HHmmss")).format(time.getTime))
        result
    }

    def insertBanner(banner: BannerModel): BannerModel = {
        banner.targetUrl = insert(banner.targetUrl)
        if (banner.thirdPartyImpressionUrl != null)
            banner.thirdPartyImpressionUrl = banner.thirdPartyImpressionUrl.map(i => insert(i)).toList
        banner match {
            case htmlBanner: HtmlBannerModel => htmlBanner.embeddedHtml = insert(htmlBanner.embeddedHtml)
            case mediaBanner: MediaBannerModel => {
                mediaBanner.bannerFile = insert(mediaBanner.bannerFile)
                mediaBanner.bannerFileFallback = insert(mediaBanner.bannerFileFallback)
            }
            case expand: ExpandableBannerModel => {
                expand.backupFile = insert(expand.backupFile)
                expand.expandFile = insert(expand.expandFile)
                expand.standardFile = insert(expand.standardFile)
                expand.tvcFile = insert(expand.tvcFile)
            }
            case balloon: BalloonBannerModel => {
                balloon.backupFile = insert(balloon.backupFile)
                balloon.expandFile = insert(balloon.expandFile)
                balloon.standardFile = insert(balloon.standardFile)
                balloon.tvcFile = insert(balloon.tvcFile)
            }
            case tvc: NetworkTVCBannerModel => tvc.tvcFile = insert(tvc.tvcFile)
            case _ =>
        }
        banner
    }

    def insertBanner(banner: Banner): Banner = {
        banner.targetUrl = insert(banner.targetUrl)
        if (banner.thirdPartyImpressionUrl != null)
            banner.thirdPartyImpressionUrl = banner.thirdPartyImpressionUrl.map(i => insert(i)).toList
        banner.extra = insert(banner.extra)
        banner
    }
}

object WebUtils {
    val cookieName = "_adtima_140514"
    val window = 15 * 60
    val trackingImage = Base64.decode("R0lGODlhAQABAPAAAP///wAAACwAAAAAAQABAEACAkQBADs=")


    def renderTrackingImage(response: HttpServletResponse): Try[_] = renderImage(response, trackingImage)

    implicit class ResponseContainer(val response: HttpServletResponse) extends AnyVal {
        def renderJs(js: String) = {
            response.setStatus(HttpServletResponse.SC_OK)
            response.setCharacterEncoding("utf-8")
            response.setContentType("text/javascript; charset=utf-8")
            response.getWriter.println(js)
            response.getWriter.close()
        }
        def renderJson(js: String) = {
            response.setStatus(HttpServletResponse.SC_OK)
            response.setCharacterEncoding("utf-8")
            response.setContentType("application/json; charset=utf-8")
            response.getWriter.println(js)
            response.getWriter.close()
        }
        def renderHtml(content: String) = {
            response.setStatus(HttpServletResponse.SC_OK)
            response.setCharacterEncoding("utf-8")
            response.setContentType("text/html; charset=utf-8")
            response.getWriter.println(content)
            response.getWriter.close()
        }
        def renderXml(content: String) = {
            response.setStatus(HttpServletResponse.SC_OK)
            response.setCharacterEncoding("utf-8")
            response.setContentType("text/xml; charset=utf-8")
            response.getWriter.println(content)
            response.getWriter.close()
        }
    }
    def convertSecondToHMS(second:Int):String = {
        if (second <= 0) return "00:00:00"
        val hours = second / 3600
        val remainder = second % 3600
        val minutes = remainder / 60
        val seconds = second % 60
        return f"$hours%02d:$minutes%02d:$seconds%02d"
    }
    def parseStringToArray(value:String):Array[String] = {
        return value.split(",")
    }
    def renderImage(response: HttpServletResponse, content: Array[Byte]): Try[_] =
        Try {
            response.setHeader("Cache-Control", "private, no-cache, no-cache=Set-Cookie, proxy-revalidate")
            response.setHeader("Pragma", "no-cache")
            response.setHeader("P3P","CP=NOI ADM DEV PSAi COM NAV OUR OTRo STP IND DEM")

            response.setCharacterEncoding("utf-8")
            response.setContentType("image/gif")

            response.getOutputStream.write(content)
            response.getOutputStream.close()
        }


    def wrapDocumentWrite(html: String) = s"document.write(\'$html');"

    val gson = JsonExt.gson

    def fromJson[T](clazz: Class[T], json: String): T = gson.fromJson[T](json, clazz)

    def fromJson[T](classType: Type, json: String): T = gson.fromJson[T](json, classType)

    def toRawJson[T](instance: T): String = gson.toJson(instance)

    def toJson[T](instance: T): String = "{\"data\":" + s"${gson.toJson(instance)}}"

    def timeToString(time  : Long) : String = {
        new java.util.Date(time).toLocaleString
//        val simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy' 'HH:MM:ss")
//        simpleDateFormat.format(date)
    }

    def normalizeJsParam(s: String) =
        StringUtils.replaceEachRepeatedly(s, Array("'", "\""), Array("", "")) |>
            StringEscapeUtils.escapeHtml

    def escapeJs(s: String) = StringEscapeUtils.escapeJavaScript(s)

    def escapeHtml(s: String) = StringEscapeUtils.escapeHtml(s)

    def getRemoteAddress(request: HttpServletRequest): String = {
        request.getHeader("X-FORWARDED-FOR") ?? request.getRemoteAddr
    }

    def getSessionId(request: HttpServletRequest): Try[String] = {
        //        request.getCookies.find(p => p.getName == cookieName).map(c=>c.getValue) match{
        //            case Some(s)=>succeed(s)
        //            case None => fail("cookie_not_found")
        //        }
        val v = request.getParameter("X-sessionId")
        if (v == null) return fail("session_not_found")
        else return succeed(v)
    }

    def getCookie(request: HttpServletRequest): String = request.getCookies.find(p => p.getName == cookieName).map(c => c.getValue) getOrElse null

    def setCookie(response: HttpServletResponse, value: String) {
        val cookie = new Cookie(cookieName, value)
        cookie.setPath("/")
        cookie.setMaxAge(Int.MaxValue)
        response.addCookie(cookie)
    }

    def getOrSetCookie(request: HttpServletRequest, response: HttpServletResponse):String={
        var cookie = getCookie(request)
        if (cookie == null){
            cookie = UUID.randomUUID().toString
            setCookie(response, cookie)
        }
        cookie
    }

    def setSessionId(response: HttpServletResponse, value: String) {
        response.setHeader("X-sessionId", value)
        //        val cookie = new Cookie(cookieName, value)
        //        cookie.setMaxAge(window)
        //        response.addCookie(cookie)
    }

    def clearSessionId(response: HttpServletResponse) {
        //        val cookie = new Cookie(cookieName, "")
        //        cookie.setMaxAge(0)
        //        response.addCookie(cookie)
    }

    def checkFlash(request: HttpServletRequest): Boolean = true

    def getUserAgent(request: HttpServletRequest) : String = {
        request.getHeader("User-Agent") match {
//            case s if s.toLowerCase().indexOf("windows phone os") >= 0 => "windowsphone"
            case s if s.toLowerCase().indexOf("android") >= 0 => "android"
            case s if s.toLowerCase.matches(".*(iphone|ipod|ipad).*") => "ios"
            case _ => "others"
        }
    }

    def downloadFile(response: HttpServletResponse, path: String) {
        val f = new File(path)
        response.setContentLength(f.length.toInt)
        if (!f.exists) return
        response.setContentType("application/force-download")
        response.setHeader("Content-Transfer-Encoding", "binary")
        response.setHeader("Content-Disposition","attachment; filename=\"" + f.getName+ "\"")
        val in = new FileInputStream(f)
        val out = response.getOutputStream
        val buf = new Array[Byte](1024)
        var count: Int = in.read(buf, 0, 1024)
        while(count != -1){
            out.write(buf, 0, count)
            count = in.read(buf, 0, 1024)
        }
        out.flush()
        in.close()
        out.close()
    }
}

object AdsUtils {

    def getTrack(zone: String, item: String, token: String, encrypted: String): String = {
        return s"<img src='${Config.urlDeliverTracking.get()}/reports/track.gif?zone=${zone}&item=${item}&token=${token}&encrypted=${encrypted}' />"
    }

    def getTrackSingleQuote(zoneIdEncoded: String, itemIdEncoded: String, token: String): String = {
        if (!zoneIdEncoded.isEmpty) {
            if (!itemIdEncoded.isEmpty) {
                return s"<img src='${Config.urlDeliverTracking.get()}/report/zads-1x1.gif?zid=$zoneIdEncoded&ziid=$itemIdEncoded&ztok=$token&t=${System.currentTimeMillis()}'>"
            }
            return s"<img src='${Config.urlDeliverTracking.get()}/report/zads-1x1.gif?zid=$zoneIdEncoded&ztok=$token&t=${System.currentTimeMillis()}'>"
        }
        ""
    }

    def wrapClick(click: String, img: String) = s"<a href='$click' target='_blank'><img src='$img' /></a>"

    def wrapImg(url: String) = s"<img src='$url' style='display:none;'/>"
    def wrapItemImage(itemLink:String, mediaURL:String,itemTarget:String,itemAlt:String,itemTitle:String):String = {
        return s"<a class='adtimaLink' target='$itemTarget' href='$itemLink'><img class='adtimaImage' src='$mediaURL' alt='$itemAlt' title='$itemTitle' ></a>"
    }
    def wrapItemFlash(zoneId:String,itemLink:String,mediaURL:String,itemTarget:String,itemWidth:Int,itemHeight:Int,imageUrlFallback: String):String = {
        var flashVars:String = s"clickTARGET=$itemTarget&amp;zid=$zoneId&amp;"
        flashVars += s"clickTAG=${java.net.URLEncoder.encode(MacroInsertion.insert(itemLink))}"
        //var html:String = s"<object classid='clsid:d27cdb6e-ae6d-11cf-96b8-444553540000' codebase='http://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0' border='0' width='$itemWidth' height='$itemHeight'>"
        var html:String = s"<object type='application/x-shockwave-flash' data='$mediaURL' codebase='http://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0' border='0' width='$itemWidth' height='$itemHeight'>"
        var tmp = ""
        if(imageUrlFallback != null) {
            tmp = wrapClick(itemLink, imageUrlFallback)
        }
        html += s"<param name='movie' value='$mediaURL'>"
        html += s"<param name='AllowScriptAccess' value='always'>"
        html += s"<param name='quality' value='High'>"
        html += s"<param name='wmode' value='transparent'>"
        html += s"<param name='FlashVars' value='$flashVars'>"
        html += s"<embed width='$itemWidth' height='$itemHeight' flashvars='$flashVars' allowscriptaccess='always' wmode='transparent' quality='high' name='ZingAds' id='ZingAds' src='$mediaURL' type='application/x-shockwave-flash'>"
        html += s"$tmp</object>"
        html
    }
    def wrapItemRichMedia(zoneId:String,itemLink:String,mediaURL:String,itemTarget:String,itemWidth:Int,itemHeight:Int):String = {
        var flashVars:String = s"clickTARGET=$itemTarget&amp;zid=$zoneId&amp;"
        flashVars += s"click=$itemLink&amp;clickTAG=$itemLink"
        var html:String = "<object classid='clsid:d27cdb6e-ae6d-11cf-96b8-444553540000' codebase='http://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0' border='0' width='$itemWidth' height='$itemHeight'>"
        html+= s"<param name='movie' value='$mediaURL'>"
        html+= s"<param name='AllowScriptAccess' value='always'>"
        html+= s"<param name='quality' value='High'>"
        html+= s"<param name='wmode' value='transparent'>"
        html+= s"<param name='FlashVars' value='$flashVars@'>"
        return html + s"<embed width='$itemWidth' height='$itemHeight' flashvars='$flashVars' allowscriptaccess='always' wmode='transparent' quality='high' name='ZingAds' id='ZingAds' src='$mediaURL' type='application/x-shockwave-flash'></object>"
    }
    def wrapItemVideo(zoneId:String,itemLink:String,videoURL:String,itemTarget:String,itemWidth:Int,itemHeight:Int):String = {
        val player:String = "";
        var flashVars:String = s"clickTARGET=$itemTarget&amp;zid=$zoneId&amp;video$videoURL&amp;"
        flashVars += s"click=$itemLink&amp;clickTAG=$itemLink"
        var html:String = s"<object classid='clsid:d27cdb6e-ae6d-11cf-96b8-444553540000' codebase='http://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0' border='0' width='$itemWidth' height='$itemHeight'>"
        html += s"<param name='movie' value='$player'>"
        html += s"<param name='AllowScriptAccess' value='always'>"
        html += s"<param name='quality' value='High'>"
        html += s"<param name='wmode' value='transparent'>"
        html += s"<param name='FlashVars' value='$flashVars'>"
        return html + s"<embed width='@itemWidth' height='@itemHeight' flashvars='@flashVars' allowscriptaccess='always' wmode='transparent' quality='high' name=ZingAds' id='ZingAds' src='$player' type='application/x-shockwave-flash'></object>"
    }
    def wrapItemExpandable(zoneid:String,bannerId:String,urlJs:String,trackClick:String,trackSubClick:String,trackCollapse:String,trackExpand:String,osync:Boolean):String = {
        val prefix = RandomStringUtils.randomAlphabetic(6)
        val sync = osync match {
            case true => "var o_syn_id = Date.time();\n"
            case false=> ""
        }
        val html = s"<div>\n" +
            "<script type='text/javascript' language='javascript'>\n" +
            s"${sync}"+
            s"var o_tracker_url_${prefix}='${trackClick}';\n" +
            "var o_tracker_target='{target}';\n" +
            s"var subClickUrlTracking_${prefix}='${trackSubClick}';\n" +
            s"var collapseUrlTracking_${prefix}='${trackCollapse}';\n" +
            s"var expandUrlTracking_${prefix}='${trackExpand}';\n" +
            s"var zid_${prefix}='$zoneid';\n" +
            "</script>\n" +
            s"<script src='${Config.serverDomain.getValue}/richmedia_js.js?id=${bannerId}&p=${prefix}&zid=$zoneid' type='text/javascript'></script>\n" +
            "</div>"
        html
    }
    def wrapItemBalloon(zoneid:String,bannerId:String,urlJs:String,trackClick:String,trackSubClick:String,trackCollapse:String,trackExpand:String):String = {
        val prefix = RandomStringUtils.randomAlphabetic(6)
        val html = s"<div>\n" +
            "<script type='text/javascript' language='javascript'>\n" +
            s"var o_tracker_url_${prefix}='${trackClick}';\n" +
            "var o_tracker_target='{target}';\n" +
            s"var subClickUrlTracking_${prefix}='${trackSubClick}';\n" +
            s"var collapseUrlTracking_${prefix}='${trackCollapse}';\n" +
            s"var expandUrlTracking_${prefix}='${trackExpand}';\n" +
            "</script>\n" +
            s"<script src='${Config.serverDomain.getValue}/balloon_js.js?id=${bannerId}&p=${prefix}' type='text/javascript'></script>\n" +
            "</div>"
        html
    }
    def wrapItemBalloonVast(zoneid:String,banner: BannerModel,urlJs:String,trackClick:String,trackSubClick:String,trackCollapse:String,trackExpand:String):String = {
        var vast = ""
        var expandFile = ""
        var width = 0
        var height = 0
        var barWidth = 0
         if(banner.isInstanceOf[BalloonBannerModel]) {
            val item = banner.asInstanceOf[BalloonBannerModel]
            if(item.iTVCExtension == 3) {
                vast = item.standardFile
                expandFile = item.expandFile
                width = item.expandWidth
                height = item.expandHeight
                barWidth = item.barWidth
            }
        } else {
            val item = banner.asInstanceOf[NetworkBalloonBannerModel]
            if(item.iTVCExtension == 3) {
                vast = item.standardFile
                expandFile = item.expandFile
            }
        }
        val html = "<script src=\"http://static.adtimaserver.vn/resource/js/balloon/swfobject.js\"></script>\n" +
            "<script src=\"http://static.adtimaserver.vn/resource/js/balloon/jquery.min.js\"></script>" +
            "\n\n<style>\n" +
            "#banner-standard{position:fixed; right: 0px; bottom: 0px; width: "+ width +"px; overflow: hidden;}\n" +
            "#banner_1{float:right;}\n</style>\n\n\t" +
            "<div id=\"banner-standard\">\n\t\t" +
            "<div id=\"b1\">\n\t\t\t" +
            "<script type=\"text/javascript\">\n\t\t\t\t" +
            "var params = {\n\t\t\t\t\tquality: \"high\",\n\t\t\t\t\tscale: \"noscale\",\n\t\t\t\t\tallowscriptaccess: \"always\",\n\t\t\t\t\twmode: \"transparent\"\n\t\t\t\t};\n\t\t\t\tvar flashvars = {\n\t\t\t\t\tprefix:\"banner_1\",\n\t\t\t\t\tautoExpandBalloon:0,\n\t\t\t\t\tintroText:\"Example Intro\",\n\t\t\t\t\tvastXML:\""+ vast +"\"\n\t\t\t\t};\n\t\t\t\t" +
            "var attributes = {\n\t\t\t\t\tid: \"banner_1\",\n\t\t\t\t\tname: \"banner_1\"\n\t\t\t\t};\n\t\t\t\t" +
            "swfobject.embedSWF(\""+ expandFile +"\", \"b1\",  \""+ width +"\", \""+ height +"\", \"10.0.0\", \"http://static.adtimaserver.vn/resource/js/balloon/expressInstall.swf\", flashvars, params, attributes);\n\t\t\t" +
            "</script>\n\t\t" +
            "</div>\n\t" +
            "</div>\n\t\n" +
            "<script>\n" +
            "function Close_Balloon_banner_1(){ $('#banner-standard').css('visibility', 'hidden');}\n" +
            "function Hide_Balloon_banner_1(){ $('#banner-standard').css('width', '"+ barWidth +"px');}\n" +
            "function Show_Balloon_banner_1(){ $('#banner-standard').css('width', '"+ width +"px');}\n" +
            "</script>"
        html
    }
    def wrapItemPopup(zoneid:String,content:String,urlJs:String,trackClick:String):String = {

        val click = if(trackClick != "#") "target='_blank' onclick=\"closepopup('"+zoneid+"')\"" else "onclick=\"closepopup('"+zoneid+"'); return false;\""

        val html =
            s"<div>\n" +
            s"<script src='${urlJs}/resource/js/adtima_ads_popup.js' type='text/javascript'></script>\n" +
            "<div id='"+zoneid+"' class='adtima_popup'> <a href='"+trackClick+"' " + click + "><span id=\"adtima_close_button\">X</span></a>" +
            s"$content"+
            s"</div><div id='adtima_bg' class='adtima_popup_bg'></div> "+
            s"<script>openpopup('$zoneid');</script>"+
            "</div>"
        html
    }
    def getTrackUrl(action: String, map: Map[String, String], redirectUrl: String = null): String = {
        val encoded = SecurityUtils.encodeParams(map)
        val urlTrack = action match {
            case "display" => Config.hitTrack.getValue
            case "click" => Config.clickTrack.getValue
        }
        val ext = if (redirectUrl != null) s"&redirectUrl=${java.net.URLEncoder.encode(redirectUrl)}" else ""
        return s"$urlTrack?params=$encoded" + ext
    }
    def getTrackVideoUrl(action:String,map: Map[String,String]): String = {
        val encoded = SecurityUtils.encodeParams(map)
        val urlTrack = Config.videoTrack.getValue + "/track?event=" + action
        return s"$urlTrack&params=$encoded"
    }

    def getTrackPRUrl(action:String,map: Map[String,String], isFullUrl: Boolean = true): String = {
        val encoded = SecurityUtils.encodeParams(map)
        var urlTrack = Config.prTrack.getValue + "/track?event=" + action
        if (isFullUrl && urlTrack.startsWith("//"))
            urlTrack = "http:" + urlTrack
        return s"$urlTrack&params=$encoded"
    }
}

object IpUtils {
    val _255 = "(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)"
    val pattern = Pattern.compile("^(?:" + _255 + "\\.){3}" + _255 + "$");
    def longToIpV4(longIp: Long): String = {
        val octet3 = ((longIp >> 24) % 256)
        val octet2 = ((longIp >> 16) % 256);
        val octet1 = ((longIp >> 8) % 256);
        val octet0 = ((longIp) % 256);
        return octet3 + "." + octet2 + "." + octet1 + "." + octet0
    }
    def ipV4ToLong(ip: String): Long = {
        val octets = ip.split("\\.")
        return (octets(0).toLong << 24) + (Integer.parseInt(octets(1)) << 16) +
            (Integer.parseInt(octets(2)) << 8) + Integer.parseInt(octets(3))
    }
    def isIPv4Private(ip: String) : Boolean = {
        val longIp = ipV4ToLong(ip)
        return (longIp >= ipV4ToLong("192.168.0.0") && longIp <= ipV4ToLong("192.168.255.255"))
    }
    def isIPv4Valid(ip: String) : Boolean = {
        return pattern.matcher(ip).matches()
    }
    def getIpFromRequest(request: HttpServletRequest) : String = {
        var ip: String = request.getHeader("x-forwarded-for")
        var found = false
        if (ip != null) {
            val tokenizer = new StrTokenizer(ip, ",")
            var flag = true
            while (tokenizer.hasNext() && flag) {
                ip = tokenizer.nextToken().trim();
                if (isIPv4Valid(ip) && !isIPv4Private(ip)) {
                    found = true
                    flag = false
                }
            }
        }
        if (!found) {
            ip = request.getRemoteAddr()
        }
        return ip
    }
}