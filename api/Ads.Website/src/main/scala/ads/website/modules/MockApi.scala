package ads.website.modules

import ads.web.mvc.{BaseHandler, Text, Action, SmartDispatcherHandler}
import ads.web.{WebUtils, HandlerContainerServlet, Invoke}
import ads.common.Syntaxs._
import java.util.Date
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.JavaConversions._
import java.util
import ads.website.Environment


class MockApi extends SmartDispatcherHandler {

    class Item(var title: String, var imageUrl: String, var price: Int, var id: Int = 0)

    class Result(var result: String, var itemId: Int, var reason: String)

    class LogResult(var result: String, var data: java.util.ArrayList[LogRecord], var reason: String = null)

    class LogRecord(var date: String, var ip: String)

    class ClickRecord(var itemId: Int, var click: Int, var impression: Int)

    val _map = new ConcurrentHashMap[Int, java.util.ArrayList[Int]]()
    val idGen = new AtomicInteger()

    @Invoke(Parameters = "data,click,startDate,endDate,campId")
    def createItem(data: String, click: Int, startDate: String, endDate: String, campId: Int) = {
        parseItem(data) + parseDate(startDate) + parseDate(endDate) match {
            case Success(((item, start), end)) => {

                val list = _map.getOrAdd(campId, (id) => new java.util.ArrayList[Int]())
                val id = idGen.incrementAndGet()
                list.add(id)

                Text(WebUtils.toRawJson(new Result("OK", id, null)))
            }
            case Failure(s) => Text(WebUtils.toRawJson(new Result("Failed", -1, s.toString)))
        }
    }

    @Invoke(Parameters = "data,click,startDate,endDate,campId")
    def updateItem(data: String, click: Int, startDate: String, endDate: String, campId: Int): Action = {
        parseItem(data) + parseDate(startDate) + parseDate(endDate) match {
            case Success(((item, start), end)) => {

                val list = _map.get(campId)
                if (list == null || !list.contains(item.id)) {
                    return Text(WebUtils.toRawJson("Failure", item.id, "item not found!"))
                }
                val id = idGen.incrementAndGet()
                list.add(id)

                Text(WebUtils.toRawJson(new Result("OK", id, null)))
            }
            case Failure(s) => Text(WebUtils.toRawJson(new Result("Failed", -1, s.toString)))
        }
    }

    @Invoke(Parameters = "itemId,action")
    def control(itemId: Int, action: String) = succeed(WebUtils.toRawJson(new Result("OK", itemId, null)))

    @Invoke(Parameters = "startDate,endDate,itemId")
    def getLogs(startDate: String, endDate: String, itemId: Int): Action =
        parseDate(startDate) + parseDate(endDate) match {


            case Success((s, e)) => {

                val r = new util.ArrayList[LogRecord]()
                for (item <- Array(new LogRecord("2013-10-24-19-00", "192.168.1.1"), new LogRecord("2013-10-24-19-01", "192.168.1.2"))) {
                    r.add(item)
                }

                Text(WebUtils.toRawJson(new LogResult("OK", r)))
            }
            case Failure(m) => Text(WebUtils.toRawJson(new LogResult("Failure", null, m.toString)))
        }


    @Invoke(Parameters = "startDate,endDate,campId")
    def getClicks(startDate: String, endDate: String, campId: Int): Action = {
        val list = _map.get(campId)

        if (list != null) {
            val arr = new util.ArrayList[ClickRecord]()
            for (item <- list.map(id => new ClickRecord(id, id + 10, id * 1000))) arr.add(item)
            return Text(WebUtils.toRawJson(arr))
        }
        else return Text("null")
    }

    def parseItem(data: String) = Try {
        WebUtils.fromJson(classOf[Item], data)
    }

    def parseDate(str: String): Try[Date] = Try {
        val part = str.split("-");
        val date = new Date(part(0).toInt, part(1).toInt - 1, part(2).toInt, part(3).toInt, part(4).toInt)
        date
    }
}

class ApiServlet extends HandlerContainerServlet {
    def factory(): BaseHandler = new Api(Environment)
}