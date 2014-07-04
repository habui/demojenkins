package ads.website.modules

import ads.web.mvc.{Json, SmartDispatcherHandler, BaseHandler}
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import ads.web.{Invoke, HandlerContainerServlet}
import ads.website.Environment
import ads.website.modules.serving.{TrackKind, ICompressService, ILog}
import java.util.{Calendar, Date}
import ads.common.Syntaxs._
import java.io._
import ads.common.model.Config
import scala.concurrent.ops._
import java.util
import com.google.common.hash.{PrimitiveSink, Funnel, BloomFilter}


object UniqueUser {
    var isRun = false
    var progress = 0.0
    //val filter = Set(12,5,1,82,7,81,10,231,83,14,13,6,11)
    def run(pathResult: String, startDate: Date, duration: Int, times: Set[String], filter: Set[Int]) {
        if (isRun) return
        isRun = true
        val now = System.currentTimeMillis()
        var progressName = pathResult + "--0%"
        new File(progressName).createNewFile()
        try {
            val user: util.HashSet[String] = new util.HashSet()
            val max = duration*24
            for (i <- 0 until duration*24) {
                val current = startDate + i.hour
                val iterator = Environment.log.readAll(current, 3600*1000)
                using (iterator){
                    for (item <- iterator; if item != null; if (filter == null || filter.contains(item.siteId))){
                        user.add(item.cookie)
                    }
                }
                val oldFile = new File(progressName)
                progressName = pathResult + s"--${(i+1)*100/max}%"
                oldFile.renameTo(new File(progressName))
                if (((i + 1) % 24 == 0) && times.contains(((i + 1) / 24).toString)) {
                    new File(pathResult).getParentFile.mkdirs
                    val pw = new PrintWriter(new BufferedWriter(new FileWriter(progressName, true)))
                    pw.println(s"${(i+1)/24} --- ${user.size}")
                    pw.println("time: " + (System.currentTimeMillis() - now))
                    println(s"${(i+1)/24} --- ${user.size}")
                    println("time: " + (System.currentTimeMillis() - now))
                    pw.close()
                }

                println(s"Unit user ${(i+1)*100/max}%\n${user.size()} keys")
                user.clear()
            }
            new File(progressName).renameTo(new File(pathResult))
        }catch {
            case ex: Throwable => {
                isRun = false
                printException(ex, System.out)
            }
        }
        isRun = false
    }

    def run2(pathResult: String, startDate: Date, duration: Int, times: Set[String], filter: Set[Int]) {
        if (isRun) return
        isRun = true
        val now = System.currentTimeMillis()
        var progressName = pathResult + "--0%"
        new File(progressName).createNewFile()
        val result = new Array[Int](101)
        try {
            val user: util.HashMap[String, Int] = new util.HashMap[String, Int]()
            val max = duration*24
            for (i <- 0 until duration*24) {
                val current = startDate + i.hour
                val iterator = Environment.log.readAll(current, 3600*1000)
                using (iterator){
                    for (item <- iterator; if item != null; if filter.contains(item.siteId); if item.kind == TrackKind.Impression){
                        user.putOrUpdate(item.cookie, 1, v => v + 1)
                        val value = user.get(item.cookie)
                        if (value < 101) {
                            result(value) += 1
                        }
                    }
                }
                //                    spawn{
                //                        env.compressService.deleteLog(current.getTime, current.getTime + 1)
                //                    }
                val oldFile = new File(progressName)
                progressName = pathResult + s"--${(i+1)*100/max}%"
                oldFile.renameTo(new File(progressName))
                if (((i + 1) % 24 == 0) && times.contains(((i + 1) / 24).toString)) {
                    new File(pathResult).getParentFile.mkdirs
                    val pw = new PrintWriter(new BufferedWriter(new FileWriter(progressName, true)))
                    pw.println("time: " + (System.currentTimeMillis() - now))
                    pw.println(s"${(i+1)/24} --- ${user.size}")
                    for (x <- 0 until result.length) {
                        pw.println(f"$x%03d,${result(x)}")
                    }
                    println("time: " + (System.currentTimeMillis() - now))
                    pw.close()
                }

                println(s"Unit user ${(i+1)*100/max}%")
            }
            new File(progressName).renameTo(new File(pathResult))
            user.clear()
        }catch {
            case ex: Throwable => {
                isRun = false
                printException(ex, System.out)
            }
        }
        isRun = false
    }

    class UniqueUserInfo(var bloom: BloomFilter[String], var size: Int = 0)

    def runByBloomFilter(pathResult: String, startDate: Date, duration: Int, times: Set[String], filterSite: Set[Int], filterCamp: Set[Int], receiver: String) {
        val maxImps = 2
        val stringFunnel = new Funnel[String]() {
            def funnel(p1: String, p2: PrimitiveSink) = {
                p2.putString(p1)
            }
        }
        if (isRun) return
        try {
            isRun = true
            val now = System.currentTimeMillis()
            progress = 0
            var progressName = pathResult + "--0%"
            new File(progressName).getParentFile.mkdirs()
            new File(progressName).createNewFile()
            val impression = new Array[UniqueUserInfo](maxImps)
            for (i <- 0 until maxImps)
                impression(i) = new UniqueUserInfo(BloomFilter.create(stringFunnel, 120*1000*1000, 0.01))

            val max = duration*24
            for (i <- 0 until max) {
                val current = startDate + i.hour
                val iterator = Environment.log.readAll(current, 3600*1000)
                using (iterator){
                    for (item <- iterator; if isRun; if item != null; if (filterSite == null || filterSite.contains(item.siteId));
                         if (filterCamp == null || filterCamp.contains(item.campId)); if item.kind == TrackKind.Impression){
                        var j = 0
                        while (j < maxImps && impression(j).bloom.mightContain(item.cookie)) j += 1
                        if (j < maxImps) {
                            impression(j).bloom.put(item.cookie)
                            impression(j).size += 1
                        }
                    }
                }
                val oldFile = new File(progressName)
                progress = (i+1)*100/max
                progressName = pathResult + s"--${progress}%"
                oldFile.renameTo(new File(progressName))
                if (((i + 1) % 24 == 0 && times.contains(((i + 1) / 24).toString)) || i == max - 1) {
                    new File(pathResult).getParentFile.mkdirs
                    val pw = new PrintWriter(new BufferedWriter(new FileWriter(progressName, true)))
                    pw.println("Run time: " + (System.currentTimeMillis() - now) + "ms")
                    pw.println(s"${(i+1)/24}day(s) --- ${impression(1).size} unique user")
                    pw.close()
                }
            }
            new File(progressName).renameTo(new File(pathResult))
            val subject = s"[Adtima Report] Report unique visitor from ${startDate.toLocaleString}, duration ${duration} day(s)"
            new MailService().send(Config.smtpUser.getValue, receiver, subject, "", pathResult)
        }catch {
            case ex: Throwable => {
                isRun = false
                progress = -1
                printException(ex, System.out)
            }
        }
        isRun = false
        progress = -1
    }

}

class UniqueUserHandler(env: {
    val log: ILog
    val compressService: ICompressService
}) extends SmartDispatcherHandler {
    val APIKEY = "b6e79326f581877ceec931e35f29f2d3"

    @Invoke(Parameters = "request,response,apikey,from,duration,times,filtersite,filtercamp,email")
    def run(request: HttpServletRequest, response: HttpServletResponse, apiKey: String, from: Long, duration: Int, times: String, filtersite: String, filtercamp: String, email: String) {
        val duration = request.getParameter("duration").toInt
        val times = request.getParameter("times").split(",").toSet
        val listFilterSite = if (filtersite != null && filtersite != "") filtersite.split(",").map(_.toInt).toSet else null
        val listFilterCamp = if (filtercamp != null && filtercamp != "") filtercamp.split(",").map(_.toInt).toSet else null
        if (APIKEY != apiKey) {
            response.setStatus(500)
            response.getWriter.print("API KEY incorrect!!!")
            return
        }
        val startDate = new Date(from)
        if (new Date() < (startDate + (duration).day)) {
            response.setStatus(500)
            response.getWriter.print("Time wrong!!!")
            return
        }
        val now = System.currentTimeMillis()
        val pathResult = Config.pathLog.getValue + f"/UniqueUser/${now}@${from}_${duration}_${request.getParameter("times")}.txt"

        if (!UniqueUser.isRun) {
            spawn {
                UniqueUser.runByBloomFilter(pathResult, startDate, duration, times, listFilterSite, listFilterCamp, email)
            }
            response.getWriter.print(s"Result will return at <a href=${pathResult}>${pathResult}</a> when done.")
        }
        else response.getWriter.print("Please wait for another thread finish!!!")
    }

    @Invoke(Parameters = "request,response,apikey")
    def stop(request: HttpServletRequest, response: HttpServletResponse, apiKey: String) {
        if (APIKEY != apiKey) {
            response.setStatus(401)
            return
        }
        UniqueUser.isRun = false
        UniqueUser.progress = -1
    }

    @Invoke(Parameters = "request,response")
    def getStatus(request: HttpServletRequest, response: HttpServletResponse) = {
        var progress = -1.0
        if (UniqueUser.isRun) progress = UniqueUser.progress
        Json(progress.toString)
    }


    @Invoke(Parameters = "request,response,apiKey,from,duration,itemId,kind,email")
    def getCookie(request: HttpServletRequest, response: HttpServletResponse, apiKey: String, from: Long, duration: Int, itemId: Int, kind: String, email: String) {
        if (APIKEY != apiKey) {
            response.setStatus(500)
            response.getWriter.print("API KEY incorrect!!!")
            return
        }
        println(s"Start get cookie from ${new Date(from)}, duration $duration day(s), itemId $itemId, kind $kind")
        spawn {
            val now = System.currentTimeMillis()
            val pathResult = Config.pathLog.getValue + f"/UniqueUser/${now}@${from}_${duration}_${itemId}_${kind}.txt"
            try {
                var progress = 0
                var progressName = pathResult + "--0%"
                new File(progressName).getParentFile.mkdirs()
                new File(progressName).createNewFile()
                val max = duration*24
                val startDate = new Date(from)
                var cookies: Map[String, String] = Map()
                for (i <- 0 until max) {
                    val current = startDate + i.hour
                    val iterator = Environment.log.readAll(current, 3600*1000)
                    using (iterator){
                        for (item <- iterator; if item != null; if item.itemId == itemId; if item.kind == kind){
                            if (!cookies.contains(item.cookie)) cookies += ((item.cookie, item.cookie))
                        }
                    }
                    val oldFile = new File(progressName)
                    progress = (i+1)*100/max
                    progressName = pathResult + s"--${progress}%"
                    oldFile.renameTo(new File(progressName))
                }
                val pw = new PrintWriter(new BufferedWriter(new FileWriter(progressName, true)))
                pw.println(s"${cookies.size} cookies")
                for (cookie <- cookies.keys) {
                    pw.println(cookie)
                }
                pw.close()
                new File(progressName).renameTo(new File(pathResult))
                val subject = s"[Adtima Report] Report unique visitor from ${startDate.toLocaleString}, duration ${duration} day(s)"
                new MailService().send(Config.smtpUser.getValue, email, subject, "", pathResult)
            }catch {
                case ex: Throwable => {
                    printException(ex, System.out)
                }
            }
        }
    }

    @Invoke(Parameters = "request,apiKey")
    def clearLog(request: HttpServletRequest,apiKey: String) = {
        val kind = request.getParameter("kind")
        if (kind != null) {
            kind match {
                case "trash" => {
                    val folder = new File(Config.pathLog.getValue + "/UniqueUser")
                    val listFile = folder.list().filter(_.endsWith("%"))
                    for (filePath <- listFile)
                        new File(filePath).delete()
                }
                case "all" => {
                    val folder = new File(Config.pathLog.getValue + "/UniqueUser")
                    val listFile = folder.list()
                    for (filePath <- listFile)
                        new File(filePath).delete()
                }
                case "file" => {
                    new File(Config.pathLog.getValue + "/UniqueUser/" + request.getParameter("fileName")).delete()
                }
            }
        }
    }
}

class UniqueUserServlet extends HandlerContainerServlet {
    def factory(): BaseHandler = return new UniqueUserHandler(Environment)
}
