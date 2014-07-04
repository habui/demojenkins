package ads.website.modules.serving

import ads.web.mvc.BaseHandler
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import ads.web.{HandlerContainerServlet, WebUtils}
import ads.common.Syntaxs._
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.Date
import ads.common.model.Config
import scala.concurrent.ops._
import scala.collection.mutable.ArrayBuffer
import java.io.{FileWriter, BufferedWriter, File, PrintWriter}
import scala.util.control.NonFatal
import ads.website.Environment


trait ILogUserAgent {
    def log(cookie: String, userAgent: String, data: String)

    var masterLog: IMasterLogUserAgent
}

trait IMasterLogUserAgent{
    def remoteFlush(list: ArrayBuffer[UserAgentRecord])
}

class UserAgentRecord(val cookie: String, val userAgent: String, val data: String)

class TrackUserAgentServlet extends HandlerContainerServlet {
    def factory() : BaseHandler = return new TrackUserAgentHandler(Environment)
}

class TrackUserAgentHandler(env: {val logUserAgent: ILogUserAgent}) extends BaseHandler {
    def process(request: HttpServletRequest, response: HttpServletResponse): Unit = {
        val data = request.getParameter("data") ?? ""
        val agent = request.getHeader("User-Agent") ?? ""
        val cookie = WebUtils.getOrSetCookie(request, response)
        env.logUserAgent.log(cookie, agent, java.net.URLDecoder.decode(data))
    }
}

class LogUserAgent extends ILogUserAgent with IMasterLogUserAgent{
    val _queue = new ConcurrentLinkedQueue[UserAgentRecord]()

    val master = Config.isMaster
    val masterPort = Config.masterPort.getValue
    var masterIp = Config.masterIp.getValue

    var masterLog: IMasterLogUserAgent = null

    spawn {
        val list = new ArrayBuffer[UserAgentRecord]()
        var limit = 1000
        while (true) {
            if (_queue.size() > limit + 1000) limit += 1000
            while (_queue.peek() != null && list.size < limit) {
                val item = _queue.poll()
                list += item
            }
            if (list.size > 0) {
                flush(list)
                list.clear()
            }
            Thread.sleep(5 * 1000)
        }
    }

    def getPathFile(time: Date): String = Config.pathLog.getValue + f"/UserAgent/UserAgent_${time.getYear + 1900}_${time.getMonth}%02d_${time.getDate}%02d.log"

    def openFile(): PrintWriter = {
        val time = new Date()
        val s = getPathFile(time)
        new File(s).getParentFile.mkdirs
        new PrintWriter(new BufferedWriter(new FileWriter(s, true)))
    }

    def openPendingFile(): PrintWriter = {
        val time = new Date()
        val s = getPathFile(time) + s".pending.${time.getTime}"
        new File(s).getParentFile.mkdirs
        new PrintWriter(new BufferedWriter(new FileWriter(s, true)))
    }

    var _current: String = ""

    def remoteFlush(list: ArrayBuffer[UserAgentRecord]){
        for (r <- list) _queue.offer(r)
    }

    val pendingLogs = new ArrayBuffer[UserAgentRecord]()

    def printLog(s: PrintWriter, record: UserAgentRecord){
        s.println(s"${record.cookie}<adtima>${record.userAgent}<adtima>${record.data}")
    }

    def flush(list: ArrayBuffer[UserAgentRecord]) {
        if (master) {
            val s = openFile()
            for (record <- list) {
                printLog(s, record)
            }
            s.close()
        } else { //slave
        var flushed = false
            try{
                for (x <- list) pendingLogs += x
                masterLog.remoteFlush(pendingLogs)
                flushed = true
                pendingLogs.clear()
            }
            catch{
                case NonFatal(e) => printException(e, System.out)
                case x: Throwable => printException(x, System.out)
            }

            if (!flushed){
                println("error while flush log2 to master")

                if (!Environment.isReconnectLog2Service)
                {
                    println("Reconnect to log2 service at ip " + Config.masterIp.getValue + ":" + Config.masterPort.getValue)
                    spawn {
                        if (!Environment.isReconnectLog2Service) {
                            Environment.isReconnectLog2Service = true
                            Environment.connectLogUserAgentService(Environment.logUserAgent)
                        }
                    }
                }
            }
        }
    }

    def log(cookie: String, userAgent: String, data: String): Unit = {
        val record = new UserAgentRecord(cookie, userAgent, data)
        _queue.offer(record)
    }
}
