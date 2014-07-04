package ads.web.mvc

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import java.lang.reflect.Method
import java.util.Date
import java.{util, lang}
import ads.web.{IpUtils, WebUtils, Invoke}
import ads.common.services._
import ads.common.{SecurityUtils, JsonExt, UserContext, SecurityContext}
import ads.common.model._
import scala.collection.mutable.ArrayBuffer
import ads.common.Syntaxs.Failure
import scala.Some
import ads.common.Syntaxs.Success

object SessionServiceInBase
{
    private var sessionService: ISessionService = null

    def Set(value: ISessionService) = {sessionService = value}

    def Get() = sessionService
}

abstract class BaseHandler {
    def process(request: HttpServletRequest, response: HttpServletResponse)
}

class SmartDispatcherHandler  extends BaseHandler {
    val sessionService = SessionServiceInBase.Get()
    val methodMaps: Map[String, Method] =
        this.
            getClass.
            getMethods.
            filter(m => m.getAnnotation(classOf[Invoke]) != null).
            map(m => (m.getName.toLowerCase, m)).
            toMap

    def filter(request: HttpServletRequest, response: HttpServletResponse): Boolean = true

    override def process(request: HttpServletRequest, response: HttpServletResponse) {
        val uri = request.getRequestURI.toLowerCase
        val methodName = uri.substring(request.getServletPath.length + 1).toLowerCase
        SessionInfo.sessionId = request.getHeader("X-sessionId")
        methodMaps.get(methodName) match {
            case Some(method) => {
                invokeMethod(method, request, response)
                SecurityContext.remove()
                UserContext.remove()
                return
            }
            case None => {
                SecurityContext.remove()
                UserContext.remove()
            }
        }
        response.sendError(HttpServletResponse.SC_BAD_REQUEST)
    }

    def validateUser(request: HttpServletRequest) : Boolean = {
        val url = request.getRequestURI.toLowerCase
        lazy val remoteAddr = IpUtils.getIpFromRequest(request)
        lazy val addrs = Config.remoteAddr.get().split(",")
        lazy val index = addrs.indexOf(remoteAddr)
        if(url.equals("/rest/session/login") || !url.startsWith("/rest") || url.startsWith("/rest/conversion/conversion")) return true
        if(url.equals("/rest/misc/upload")) {
            val time = request.getParameter("time")
            val hash = request.getParameter("hash")
            if(SecurityUtils.md5Hash(time + SecurityUtils.md5Hash(Config.secretKeyForUpload.get())).equals(hash)) return true
        }
        val sessionId = request.getHeader("X-sessionId")
        if(sessionId != null) {
            val user = sessionService.getSession(sessionId).tryGet(session => session.user).getOrElse(null)
            if(user != null) {
                UserContext.set(user)
                return true
            }
        }

        if(index >= 0) {
            val headerCookies = request.getHeader("cookie")
            if(headerCookies != null && Config.mapRemoteClient.keySet().contains(headerCookies)) {
                val clientUserId = Config.mapRemoteClient.get(headerCookies)
                UserContext.set(new User(clientUserId, 0, "RemoteClient", "", ""))
                return true
            }
        }

        return false
    }


    def invokeMethod(method: Method, request: HttpServletRequest, response: HttpServletResponse) {
        val types = method.getParameterTypes
        val args = new Array[Object](types.length)
        val invocation = method.getAnnotation(classOf[Invoke])
        val names = invocation.Parameters.split(",").map(f=>f.trim)
        request.setCharacterEncoding("UTF-8")


        if(!validateUser(request)) {
            return new Fail("",401).apply(request, response)
        }

        for (i <- 0 until types.length) {
            args(i) = convert(types(i), request, response, names(i)).asInstanceOf[Object]
        }

        if (!invocation.bypassFilter()){
            if (!filter(request, response)) return
        }

        val result =
            args.length match {
                case 0 => method.invoke(this)
                case 1 => method.invoke(this, args(0))
                case 2 => method.invoke(this, args(0), args(1))
                case 3 => method.invoke(this, args(0), args(1), args(2))
                case 4 => method.invoke(this, args(0), args(1), args(2), args(3))
                case 5 => method.invoke(this, args(0), args(1), args(2), args(3), args(4))
                case 6 => method.invoke(this, args(0), args(1), args(2), args(3), args(4), args(5))
                case 7 => method.invoke(this, args(0), args(1), args(2), args(3), args(4), args(5),args(6))
                case 8 => method.invoke(this, args(0), args(1), args(2), args(3), args(4), args(5),args(6),args(7))
                case 9 => method.invoke(this, args(0), args(1), args(2), args(3), args(4), args(5),args(6),args(7),args(8))
                case 10 => method.invoke(this, args(0), args(1), args(2), args(3), args(4), args(5),args(6),args(7),args(8),args(9))
            }

        val action = result match {
            case x: Action => x
            case Success(t) => new Json(t)
            case Failure((m: String, code: Int))=>new Fail(m,code)
            case Failure(m: String) => new Fail(m, HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
            case y => Text(y)
        }

        action.apply(request, response)
    }

    def convert(target: Class[_], request: HttpServletRequest, response: HttpServletResponse, name: String): Any = {
        val values = request.getParameterValues(name)
        if (values != null) {
            val r = convertSimple(target, values)
            if (r != null) return r
        }
        else {
            name match {
                case "request" => return request
                case "response" => return response
            }
        }

        convertJson(target, request, name)
    }

    def convertJson(target: Class[_], request: HttpServletRequest, name: String): Any = {
        WebUtils.fromJson(target, request.getParameter(name))
    }

    def convertSimple(target: Class[_], values: Array[String]): Any = {
        val v: String = values(0)
        if (target == classOf[Date]) return new Date(v.trim.toLong)
        if (target == classOf[Boolean]) return new lang.Boolean(v.equalsIgnoreCase("true"))
        if (target == classOf[Int]) return new Integer(v.trim.toInt)
        if (target == classOf[String]) return v.trim
//        if (target == classOf[String]) return v.trim |> (URLDecoder.decode(_,"utf-8")) //URLDecoder.decode(values(0).trim, "utf-8")
//        if (target == classOf[List[Int]]) {
//            if (v == "" || v == null) return new Array[Int](0).toList
//            return v.split(",").map(f=>f.trim.toInt).toList
//        }
        if (target == classOf[List[String]]){
//            if (v.trim == "" || v == null) return new Array[String](0).toList
            if (v.trim == "" || v == null) return null
            return v.split(",").toList
        }
        if (target == classOf[Array[Int]]) {
//            if (v.trim == "" || v == null) return new Array[Int](0)
            if (v.trim == "" || v == null) return null
            return v.split(",").map(f=>f.trim.toInt).toArray
        }

        if (target == classOf[Array[String]]) {
//            if (v.trim == "" || v == null) return new Array[String](0)
            if (v.trim == "" || v == null) return null
            return v.split(",").toArray
        }
        if (target == classOf[Long]) return new lang.Long(v.trim.toLong)
        if (target == classOf[Double]) return v.toDouble
        if (target == classOf[Float]) return v.toFloat
        if (target == classOf[util.HashMap[String, String]]) return JsonExt.gson.fromJson(v.trim, classOf[java.util.HashMap[String,String]])

        if(target == classOf[Array[VariableTargeting]]) {
            val variables = new ArrayBuffer[VariableTargeting]()
            for(s<-values) {
                var temp = s
                if(!(s.startsWith("[") && s.endsWith("]"))) {
                    temp = "["+ s +"]"
                }
                val items  = JsonExt.gson.fromJson(temp, classOf[Array[VariableTargeting]]).asInstanceOf[Array[VariableTargeting]]
                for(item <- items) if(item != null && item.key != null && item.value != null) variables += item
            }
            if(variables.length > 0) return variables.toArray
            else return new Array[VariableTargeting](0)
        }
        if(target == classOf[Array[VideoZoneTimeSegment]]) {
            val variables = new ArrayBuffer[VideoZoneTimeSegment]()
            for(s<-values) {
                var temp = s
                if(!(s.startsWith("[") && s.endsWith("]"))) {
                    temp = "["+ s +"]"
                }
                val items  = JsonExt.gson.fromJson(temp, classOf[Array[VideoZoneTimeSegment]]).asInstanceOf[Array[VideoZoneTimeSegment]]
                for(item <- items) if(item != null && item.spend > 0 && item.start >= 0) variables += item
            }
            if(variables.length > 0) return variables.toArray
            else return new Array[VideoZoneTimeSegment](0)
        }
        if(target == classOf[Array[ApproveItem]]) {
            val variables = new ArrayBuffer[ApproveItem]()
            for(s<-values) {
                var temp = s
                if(!(s.startsWith("[") && s.endsWith("]"))) {
                    temp = "["+ s +"]"
                }
                val items  = JsonExt.gson.fromJson(temp, classOf[Array[ApproveItem]]).asInstanceOf[Array[ApproveItem]]
                for(item <- items) if(item != null && item.zoneId > 0 && item.itemId > 0) variables += item
            }
            if(variables.length > 0) return variables.toArray
            else return new Array[ApproveItem](0)
        }
        if(target == classOf[Array[GeoTargeting]]) {
            val variables = new ArrayBuffer[GeoTargeting]()
            for(s<-values) {
                var temp = s
                if(!(s.startsWith("[") && s.endsWith("]"))) {
                    temp = "["+ s +"]"
                }
                val items  = JsonExt.gson.fromJson(temp, classOf[Array[GeoTargeting]]).asInstanceOf[Array[GeoTargeting]]
                for(item <- items) if(item != null && item.value != null && item.value.length > 0) variables += item
            }
            if(variables.length > 0) return variables.toArray
            else return new Array[GeoTargeting](0)
        }
        null
    }

    def readFromRequest[T](request: HttpServletRequest, instance: T): T = {
        for (m <- instance.getClass.getMethods) {
            var name = m.getName
            if (name.endsWith("_$eq") && m.getParameterTypes.length == 1) {
                name = name.substring(0, name.length - 4)

                val p = request.getParameterValues(name)
                if (p != null) {
                    m.invoke(instance, convertSimple(m.getParameterTypes()(0), p).asInstanceOf[Object])
                }
            }
        }
        instance
    }

    def success() = Text("{\"result\": \"success\"}")
    def failure(message: String = "wonk_dog") = Text(s"failure: $message")
}
