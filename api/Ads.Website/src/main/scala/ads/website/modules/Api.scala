package ads.website.modules

import ads.web.{WebUtils, Invoke}
import ads.common.Syntaxs._
import ads.web.mvc.{Action, SmartDispatcherHandler, Text}
import java.util.Date
import ads.common.database.IDataService
import ads.common.{Reflect}
import java.util
//import ads.common.services.report.{ReportRecord, IReportService}
import ads.common.model.{EcommerceBannerModel, BannerModel}

class Api(env: {
    val bannerModelService: IDataService[BannerModel]
    //val reportService: IReportService
}) extends SmartDispatcherHandler {

    class Item(var title: String, var imageUrl: String, var price: Int, var link: String, var desc: String, var shopName: String, var id: Int = 0)

    class Result(var result: String, var itemId: Int, var reason: String)

    class LogResult(var result: String, var data: java.util.ArrayList[LogRecord], var reason: String = null)

    class LogRecord(var date: String, var ip: String)

    class ClickRecord(var itemId: Int, var click: Int, var impression: Int)

    @Invoke(Parameters = "data,click,startDate,endDate,campId")
    def createItem(data: String, click: Int, startDate: String, endDate: String, campId: Int) = {
        parseItem(data) + parseDate(startDate) + parseDate(endDate) match {
            case Success(((item, start), end)) => {
                val banner = new EcommerceBannerModel(0, campId, WebUtils.escapeHtml(item.title), item.link, null, "Running", click, start.getTime, end.getTime, Reflect.toMap(item))
                val id = env.bannerModelService.save(banner)

                Text(WebUtils.toRawJson(new Result("OK", id, null)))
            }
            case Failure(s) => Text(WebUtils.toRawJson(new Result("Failed", -1, s.toString)))
        }
    }

    @Invoke(Parameters = "itemId,action")
    def control(itemId: Int, action: String): Try[String] = {

        val old = env.bannerModelService.load(itemId)
        if (old != null && old.isInstanceOf[EcommerceBannerModel]) {
            val banner = old.asInstanceOf[EcommerceBannerModel]

            if (banner.status == "Stopped") {
                return fail(WebUtils.toRawJson(new Result("Failed", itemId, "already stopped!")))
            }

            action match {
                case "Stop" => banner.status = "Stopped"
                case "Resume" => banner.status = "Running"
                case "Pause" => banner.status = "Paused"
            }

            env.bannerModelService.update(banner)

            return succeed(WebUtils.toRawJson(new Result("OK", itemId, null)))
        }
        else {
            return fail(WebUtils.toRawJson(new Result("Failed", itemId, "not found!")))
        }
    }


    @Invoke(Parameters = "data,click,startDate,endDate,campId")
    def updateItem(data: String, click: Int, startDate: String, endDate: String, campId: Int): Action = {
        parseItem(data) + parseDate(startDate) + parseDate(endDate) match {
            case Success(((item, start), end)) => {
                val old = env.bannerModelService.load(item.id)

                if (old == null) {
                    return Text(WebUtils.toRawJson("Failure", item.id, "item not found!"))
                }

                val m = Reflect.toMap(item)
                val banner = new EcommerceBannerModel(item.id, campId, WebUtils.escapeHtml(item.title), item.link, null, "Running", click, start.getTime, end.getTime, m)

                env.bannerModelService.update(banner)

                Text(WebUtils.toRawJson(new Result("OK", item.id, null)))
            }
            case Failure(s) => Text(WebUtils.toRawJson(new Result("Failed", -1, s.toString)))
        }
    }

    def parseItem(data: String) = Try {
        WebUtils.fromJson(classOf[Item], data)
    }

    def parseDate(str: String): Try[Date] = Try {
        val part = str.split("-");
        val date = new Date(part(0).toInt - 1900, part(1).toInt - 1, part(2).toInt, part(3).toInt, part(4).toInt)
        date
    }


//    @Invoke(Parameters = "startDate,endDate,campId")
//    def getClicks(startDate: String, endDate: String, campId: Int): Action = {
//        val list = env.bannerModelService.loadAll().filter(p => p.isInstanceOf[EcommerceBannerModel]).filter(p => p.campaignId == campId).toList
//        if (list == null || list.size == 0) {
//            return Text("null")
//        }
//        else {
//            val from = parseDate(startDate).getOrElse(new Date()).getTime
//            val to = parseDate(endDate).getOrElse(new Date()).getTime
//            val duration = to - from
//
//            val arr = list.map(item=>env.reportService.getInfo(item.id, from, duration)).toList
//
//            return Text(WebUtils.toRawJson(arr))
//        }
//    }
}
