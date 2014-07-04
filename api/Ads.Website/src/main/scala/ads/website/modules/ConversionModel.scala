package ads.website.modules

import ads.common.{SecurityUtils, IItem}
import ads.common.database.{AbstractDelegateDataService, IDataService}
import ads.common.model.{Render, Config, Order, Conversion}
import ads.common.services.{CachedLog, IConversionService}
import ads.web.{WebUtils, Invoke, HandlerContainerServlet}
import ads.web.mvc.{Json, BaseHandler,Text}
import javax.servlet.http.{HttpServletResponse, Cookie, HttpServletRequest}
import ads.website.handler.RestHandler
import ads.website.Environment
import ads.website.modules.serving.{LogKind, ILog}
import ads.common.Syntaxs._
import ads.common.geoip.GeoipService
import java.util

class ConversionModel(var id: Int,
                      var name: String,
                      var windows: Int,
                      var value: Double,
                      var updateDate: Long,
                      var trackingStatus: String,
                      var orderId: Int,
                      var disable: Boolean = false
                     ) extends IItem

object defaultConversionModel {
    def apply() = new ConversionModel(0,"",0,1,0,"Unverified",0)
}

trait IConversionModelService extends IDataService[ConversionModel] {
    def genCode(id: Int, value: Double): String
}

class ConversionModelService(env: {
    val conversionService: IConversionService
}) extends AbstractDelegateDataService[Conversion, ConversionModel](env.conversionService) with IConversionModelService {

    def toModel(item: Conversion): ConversionModel = new ConversionModel(item.id, item.name, item.windows, item.value, item.updateDate, item.trackingStatus, item.orderId)

    def fromModel(model: ConversionModel): Conversion = new Conversion(model.id, model.name, model.windows, model.value, model.updateDate, model.trackingStatus, model.orderId, "VND", model.disable)

    def genCode(orderId: Int, value: Double): String = {
        val host =  Config.hostDomain.getValue
        val conversion_js = Config.jsDomain.getValue + "/resource/js/conversion.js"
        Render.renderConversion(Map("host" -> host,
                                    "orderId" -> SecurityUtils.encode(orderId),
                                    "type" -> "Default",
                                    "value" -> value,
                                    "conversion_js" -> conversion_js))
    }
}

class ConversionRestHandler(env: {
    val conversionModelService: IConversionModelService
    val bannerModelService: IBannerModelService
    val zoneModelService: IZoneModelService
    val orderModelService: IOrderModelService
    val conversionService: IConversionService
    val log: ILog
 }) extends RestHandler[ConversionModel,Conversion](()=>defaultConversionModel(), env.conversionModelService, env.conversionService) {

    @Invoke(Parameters = "request,response,orderId,conversionType,value")
    def conversion(request: HttpServletRequest, response: HttpServletResponse, orderId: String, conversionType: String, value: Double) = {
        val location = GeoipService.getGeoipClient.getLocation(WebUtils.getRemoteAddress(request))
        val city = if (location != null) location.city else ""
        val task = new util.Random().nextInt(9998) + 1
        val orderIdDecode = SecurityUtils.decode(orderId)
        val cookies: Array[Cookie] = request.getCookies()
        if (cookies != null && cookies.length > 0) {
            var i = 0
            while (i < cookies.length && !cookies(i).getName().equals(s"123click_conversion_$orderIdDecode")){
                i += 1
            }
            if (i < cookies.length){
                val cookieValue = cookies(i).getValue();
                val strValues: Array[String] = cookieValue.split('@')
                if (strValues.length == 2){
                    val zoneId: Int = strValues(0).toInt
                    val itemId: Int = strValues(1).toInt
                    val zone = env.zoneModelService.load(zoneId)
                    val siteId: Int = zone.siteId
                    val zoneGroupId: Array[Int] = zone.groups
                    val campId: Int = env.bannerModelService.load(itemId).campaignId
                    val key: String = "conversion_@#" + conversionType
                    env.log.log(LogKind.Normal, task, city, siteId, zoneGroupId, zoneId, orderIdDecode, campId, itemId, WebUtils.getRemoteAddress(request).replace(',', '|'), WebUtils.getOrSetCookie(request, response), key, System.currentTimeMillis(), value.toInt, 0)
                    val conversions = env.conversionModelService.listByReferenceId(0,1,orderIdDecode)
                    if (conversions.data.length > 0 && conversions.data(0).trackingStatus.equals("Unverified")) {
                        conversions.data(0).trackingStatus = "Verified"
                        env.conversionModelService.update(conversions.data(0))
                    }
                    Text("Success!!")
                }
            } else Text("Not found cookie for conversion!!")
        } else Text("Not found cookie for conversion!!")
    }

    @Invoke(Parameters = "orderId")
    def genCode(orderId: Int) = {
        val conversion = env.conversionModelService.listByReferenceId(0, 1, orderId).data(0)
        if (conversion.value < 1) conversion.value = 1
        Json(env.conversionModelService.genCode(orderId, conversion.value))
    }

    @Invoke(Parameters = "request")
    def createAllConversion(request: HttpServletRequest) = {
        val listOrder = env.orderModelService.list(0, Int.MaxValue)
        var result = "List conversion create: \n"
        for (order <- listOrder.data) {
            val conversion = env.conversionService.listByReferenceId(0, 1, order.id)
            if (conversion == null || conversion.data.length == 0) {
                env.conversionModelService.save(new ConversionModel(0, order.name, 0, 1.0, System.currentTimeMillis(), "Unverified", order.id))
                result += s"${order.id} -- ${order.name}\n"
            }
        }
        Text(result)
    }
}

class ConversionServlet extends HandlerContainerServlet {
    def factory(): BaseHandler = new ConversionRestHandler(Environment)
}
