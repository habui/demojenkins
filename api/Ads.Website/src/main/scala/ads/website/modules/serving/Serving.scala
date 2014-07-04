package ads.website.modules.serving

import ads.common.model._
import ads.common.database.IDataService
import ads.common.services.serving.{Const, IServingEngine}
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.{ConcurrentLinkedQueue, ThreadLocalRandom, ConcurrentHashMap}
import scala.concurrent.ops._
import java.util.Date
import scala.collection.mutable.ArrayBuffer
import ads.common.Syntaxs._
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import scala.collection.immutable.{SortedMap, TreeMap}
import ads.web.mvc.BaseHandler
import ads.common.{IDisposable, SecurityUtils}
import scala.util.control.NonFatal
import java.io._
import java.util.zip.ZipFile

//import ads.common.services.report.IReportService
import ads.common.rpc.{ProxyGen, ThriftClient, Rpc}
import ads.website.Environment


trait ITrackLog {
    def log(itemId: Int, zoneId: Int, action: String, money: Int, ip: String, date: Long)
}


abstract class SecureHandler extends BaseHandler {
    def process(request: HttpServletRequest, response: HttpServletResponse) {

        request.getParameter("params").checkEmpty >=>
            SecurityUtils.decodeData >=> {
            params => {
                var map = new TreeMap[String, String]()
                for (i <- 0 until params.length / 2) {
                    map = map + ((params(i * 2), params(i * 2 + 1)))
                }
                process(request, response, map)
            }
        }
        match {
            case Failure(e) => {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)

                if (e != null && e.isInstanceOf[Throwable]){
                    val t = e.asInstanceOf[Throwable]
                    ads.common.Syntaxs.printException(t, System.out)
                }

            }
            case Success(_) => {}
        }
    }

    def process(request: HttpServletRequest, response: HttpServletResponse, params: Map[String, String]): Try[Any]
}


trait ITrack {
    def trackToken(token: String, time: Int)

    def verifyToken(token: String, time: Int): Boolean
}

class LogRecord(val logKind: String, val task: Int, val location: String, val siteId: Int, val zoneGroupIds: Array[Int], val zoneId: Int, val orderId: Int, val campId: Int, val itemId: Int, val ip: String, val cookie: String, val kind: String, val time: Long, val value: Int, var videoId: Int, var adType: Int, val length: Int)

object LogKind {
    val Normal = "[N]"
}

trait ILog {
    def log(logKind: String, task: Int, location: String, siteId: Int, zoneGroupIds: Array[Int], zoneId: Int, orderId: Int, campId: Int,itemId: Int, ip: String, cookie: String, kind: String, time: Long, money: Int, videoId: Int, adType: Int = 0)

    def readAll(from: Date, duration: Long, skip: Long = 0): Iterator[LogRecord]

    var masterLog: IMasterLog

    def lockFile(body: => Any) = synchronized {
        body
    }
}

object LogFile {
    def getPathFile(time: Date): String = Config.pathLog.getValue + f"/${time.getYear + 1900}/${time.getMonth}%02d/${time.getDate}%02d/" + getFileName(time)
    def getFileName(time: Date): String = f"server_${Config.serverIp}_${time.getYear + 1900}_${time.getMonth}%02d_${time.getDate}%02d_${time.getHours}%02d.log"
}

class RangeLogIterator(val start: Date, val duration: Long, val source: Iterator[LogRecord]) extends Iterator[LogRecord] with IDisposable {

    val from = start.getTime
    val to = from + duration
    var done = false

    def hasNext: Boolean = source.hasNext && !done

    def next(): LogRecord = {
        while (source.hasNext && !done) {
            val log = source.next()
            if (log.time >= from) return log
            if (log.time < to) {
                done = true
            }
        }
        null
    }

    def dispose(): Unit = using(source) {}
}

//class LogIterator(var start: Date, val hours: Int, skip: Long = 0) extends Iterator[LogRecord] with IDisposable {
//    val end = minDate(new Date(), start + hours.hour)
//    var _currentTime = start
//
//    while (!fileExists(_currentTime) && !Environment.compressService.fileExists(_currentTime) && _currentTime <= end) {
//        _currentTime = _currentTime + 1.hour
//    }
//    var reader: BufferedReader = null
//    var _hasNext = false
//
//    if (fileExists(start) || (Environment.compressService.fileExists(start) && fileFromZip(start))) {
//        reader = new BufferedReader(new FileReader(LogFile.getPathFile(start)))
//        reader.skip(skip)
//        _hasNext = true
//    }
//
//    //check exist file and unzip
//    def fileFromZip(time: Date): Boolean = {
//        if (Environment.compressService.listFile == null)
//            Environment.compressService.getContentAndDecompress(time.getTime, System.currentTimeMillis - 3600*1000)
//        val fileName = LogFile.getFileName(time)
//        if (Environment.compressService.listFile.contains(fileName)) {
//            while(!Environment.compressService.listFile(fileName)) { // wait for unzip file
//                Thread.sleep(5000)
//            }
//            return true
//        } else return false
//    }
//
//    def fileExists(time: Date): Boolean = {
//        val path = LogFile.getPathFile(time)
//        val file = new File(path)
//        (file.exists() && Environment.compressService.getFileSize(time) <= file.length)
//    }
//
//    def moveNext(): Boolean = {
//        if (reader != null) {
//            reader.close()
//            reader = null
//        }
//
//        _currentTime = _currentTime + 1.hour
//
//        while (_currentTime < end && (!fileExists(_currentTime))) {
//            _currentTime = _currentTime + 1.hour
//        }
//
//        if (fileExists(_currentTime)  || fileFromZip(_currentTime)) {
//            reader = new BufferedReader(new FileReader(LogFile.getPathFile(start)))
//            true
//        } else {
//
//        }
//
//        false
//    }
//
//    def hasNext: Boolean = _hasNext
//
//    def next(): LogRecord = {
//        val line = reader.readLine()
//        if (line == null) {
//            _hasNext = false
//            return null
//        }
//        val length = line.length + 2
//        val segs = line.split(",")
//        try {
//            val logRecord =
//                if (line.startsWith("[")){
//                    val logKind = segs(0).toString
//                    val task = segs(1).toInt
//                    val location = segs(2).toString
//                    val time = segs(3).toLong
//                    val siteId = segs(4).toInt
//                    val zoneGroupIds = segs(5).split("\\|").filter(x=>x != "").foldLeft[ArrayBuffer[Int]](new ArrayBuffer[Int]())(_ += _.toInt).toArray
//                    val zoneId = segs(6).toInt
//                    val orderId = segs(7).toInt
//                    val campId = segs(8).toInt
//                    val itemId = segs(9).toInt
//                    val ip = segs(10)
//                    val cookie = segs(11)
//                    val kind = segs(12)
//                    val value = segs(13).toInt
//                    val videoId = segs(14).toInt
//                    val adType = segs(15).toInt
//                    new LogRecord(logKind, task, location, siteId, zoneGroupIds, zoneId, orderId, campId, itemId, ip, cookie, kind, time, value, videoId, adType, length)
//                } else
//                    segs.size match {
//                       case 11 => {
//                           val time = segs(0).toLong
//                           val siteId = segs(1).toInt
//                           val zoneGroupIds = segs(2).split("\\|").filter(x=>x != "").foldLeft[ArrayBuffer[Int]](new ArrayBuffer[Int]())(_ += _.toInt).toArray
//                           val zoneId = segs(3).toInt
//                           val orderId = segs(4).toInt
//                           val campId = segs(5).toInt
//                           val itemId = segs(6).toInt
//                           val ip = segs(7)
//                           val cookie = segs(8)
//                           val kind = segs(9)
//                           val value = segs(10).toInt
//                           new LogRecord(LogKind.Normal, 0, "", siteId, zoneGroupIds, zoneId, orderId, campId, itemId, ip, cookie, kind, time, value, 0, 0, length)
//                       }
//                       case 12 => { // log record has videoId (subzone)
//                           val time = segs(0).toLong
//                           val siteId = segs(1).toInt
//                           val zoneGroupIds = segs(2).split("\\|").filter(x=>x != "").foldLeft[ArrayBuffer[Int]](new ArrayBuffer[Int]())(_ += _.toInt).toArray
//                           val zoneId = segs(3).toInt
//                           val orderId = segs(4).toInt
//                           val campId = segs(5).toInt
//                           val itemId = segs(6).toInt
//                           val ip = segs(7)
//                           val cookie = segs(8)
//                           val kind = segs(9)
//                           val value = segs(10).toInt
//                           val videoId = segs(11).toInt
//                           new LogRecord(LogKind.Normal, 0, "", siteId, zoneGroupIds, zoneId, orderId, campId, itemId, ip, cookie, kind, time, value, videoId, 0, length)
//                       }
//                       case 13 => { // log record has adType
//                           val time = segs(0).toLong
//                           val siteId = segs(1).toInt
//                           val zoneGroupIds = segs(2).split("\\|").filter(x=>x != "").foldLeft[ArrayBuffer[Int]](new ArrayBuffer[Int]())(_ += _.toInt).toArray
//                           val zoneId = segs(3).toInt
//                           val orderId = segs(4).toInt
//                           val campId = segs(5).toInt
//                           val itemId = segs(6).toInt
//                           val ip = segs(7)
//                           val cookie = segs(8)
//                           val kind = segs(9)
//                           val value = segs(10).toInt
//                           val videoId = segs(11).toInt
//                           val adType = segs(12).toInt
//                           new LogRecord(LogKind.Normal, 0, "", siteId, zoneGroupIds, zoneId, orderId, campId, itemId, ip, cookie, kind, time, value, videoId, adType, length)
//                       }
//                       case _ => {
//                           new LogRecord("", 0, "", 0, Array.empty, 0, 0, 0, 0, "", "", "", -1, 0, 0, 0, length)
//                       }
//                    }
//                    return logRecord
//        } catch {
//            case ex: Exception => println(s"Error while read log: $line")
//        }
//        return new LogRecord("", 0, "", 0, Array.empty, 0, 0, 0, 0, "", "", "", -1, 0, 0, 0, length)
//    }
//
//    def dispose(): Unit = {
//        if (reader != null) {
//            reader.close()
//            reader = null
//        }
//    }
//}

class LogIterator(var start: Date, val hours: Int, skip: Long = 0) extends Iterator[LogRecord] with IDisposable {
    val end = minDate(new Date(), start + hours.hour)
    var _currentTime = start

    while (!fileExists(_currentTime) && !Environment.compressService.fileExists(_currentTime) && _currentTime <= end) {
        _currentTime = _currentTime + 1.hour
    }
    var reader: BufferedReader = null
    var zipFile: ZipFile = null
    var _hasNext = false

    if (fileExists(start)) {
        reader = new BufferedReader(new FileReader(LogFile.getPathFile(start)))
        reader.skip(skip)
        _hasNext = true
    } else if (Environment.compressService.fileExists(start)) {
        zipFile = new ZipFile(Environment.compressService.getPath(start))
        val entries = zipFile.entries
        if (entries.hasMoreElements) {
            val ze = entries.nextElement()
            if (ze.getSize > 0) {
                reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(ze)))
                reader.skip(skip)
                _hasNext = true
            }
        }
    }

    def fileExists(time: Date): Boolean = {
        val path = LogFile.getPathFile(time)
        val file = new File(path)
        (file.exists() && Environment.compressService.getFileSize(time) <= file.length)
    }

    def moveNext(): Boolean = {
        if (reader != null) {
            reader.close()
            reader = null
        }

        _currentTime = _currentTime + 1.hour

        while (_currentTime < end && (!fileExists(_currentTime))) {
            _currentTime = _currentTime + 1.hour
        }

        if (fileExists(_currentTime)) {
            reader = new BufferedReader(new FileReader(LogFile.getPathFile(start)))
            true
        } else if (Environment.compressService.fileExists(start)) {
            zipFile = new ZipFile(Environment.compressService.getPath(start))
            val entries = zipFile.entries
            if (entries.hasMoreElements) {
                val ze = entries.nextElement()
                if (ze.getSize > 0) {
                    reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(ze)))
                    return true
                }
            }
        }
        false
    }

    def hasNext: Boolean = _hasNext

    def next(): LogRecord = {
        val line = reader.readLine()
        if (line == null) {
            _hasNext = false
            return null
        }
        val length = line.length + 1
        val segs = line.split(",")
        try {
            val logRecord =
                if (line.startsWith("[")){
                    val logKind = segs(0)
                    val task = segs(1).toInt
                    val location = segs(2)
                    val time = segs(3).toLong
                    val siteId = segs(4).toInt
                    val zoneGroupIds = segs(5).split("\\|").filter(x=>x != "").foldLeft[ArrayBuffer[Int]](new ArrayBuffer[Int]())(_ += _.toInt).toArray
                    val zoneId = segs(6).toInt
                    val orderId = segs(7).toInt
                    val campId = segs(8).toInt
                    val itemId = segs(9).toInt
                    val ip = segs(10)
                    val cookie = segs(11)
                    val kind = segs(12)
                    val value = segs(13).toInt
                    val videoId = segs(14).toInt
                    val adType = segs(15).toInt
                    new LogRecord(logKind, task, location, siteId, zoneGroupIds, zoneId, orderId, campId, itemId, ip, cookie, kind, time, value, videoId, adType, length)
                } else
                    segs.size match {
                        case 11 => {
                            val time = segs(0).toLong
                            val siteId = segs(1).toInt
                            val zoneGroupIds = segs(2).split("\\|").filter(x=>x != "").foldLeft[ArrayBuffer[Int]](new ArrayBuffer[Int]())(_ += _.toInt).toArray
                            val zoneId = segs(3).toInt
                            val orderId = segs(4).toInt
                            val campId = segs(5).toInt
                            val itemId = segs(6).toInt
                            val ip = segs(7)
                            val cookie = segs(8)
                            val kind = segs(9)
                            val value = segs(10).toInt
                            new LogRecord(LogKind.Normal, 0, "", siteId, zoneGroupIds, zoneId, orderId, campId, itemId, ip, cookie, kind, time, value, 0, 0, length)
                        }
                        case 12 => { // log record has videoId (subzone)
                        val time = segs(0).toLong
                            val siteId = segs(1).toInt
                            val zoneGroupIds = segs(2).split("\\|").filter(x=>x != "").foldLeft[ArrayBuffer[Int]](new ArrayBuffer[Int]())(_ += _.toInt).toArray
                            val zoneId = segs(3).toInt
                            val orderId = segs(4).toInt
                            val campId = segs(5).toInt
                            val itemId = segs(6).toInt
                            val ip = segs(7)
                            val cookie = segs(8)
                            val kind = segs(9)
                            val value = segs(10).toInt
                            val videoId = segs(11).toInt
                            new LogRecord(LogKind.Normal, 0, "", siteId, zoneGroupIds, zoneId, orderId, campId, itemId, ip, cookie, kind, time, value, videoId, 0, length)
                        }
                        case 13 => { // log record has adType
                        val time = segs(0).toLong
                            val siteId = segs(1).toInt
                            val zoneGroupIds = segs(2).split("\\|").filter(x=>x != "").foldLeft[ArrayBuffer[Int]](new ArrayBuffer[Int]())(_ += _.toInt).toArray
                            val zoneId = segs(3).toInt
                            val orderId = segs(4).toInt
                            val campId = segs(5).toInt
                            val itemId = segs(6).toInt
                            val ip = segs(7)
                            val cookie = segs(8)
                            val kind = segs(9)
                            val value = segs(10).toInt
                            val videoId = segs(11).toInt
                            val adType = segs(12).toInt
                            new LogRecord(LogKind.Normal, 0, "", siteId, zoneGroupIds, zoneId, orderId, campId, itemId, ip, cookie, kind, time, value, videoId, adType, length)
                        }
                    }
            return logRecord
        } catch {
            case ex: Exception => println(s"Error while read log: $line")
        }
        return new LogRecord("", 0, "", 0, Array.empty, 0, 0, 0, 0, "", "", "", -1, 0, 0, 0, length)
    }

    def dispose(): Unit = {
        if (reader != null) {
            reader.close()
            reader = null
        }
        if (zipFile != null) {
            zipFile.close()
            zipFile = null
        }
    }
}

trait IMasterLog{
    def remoteFlush(list: ArrayBuffer[LogRecord])
}

class Log extends ILog with IMasterLog{
    val _queue = new ConcurrentLinkedQueue[LogRecord]()
    val start = new Date(2013, 12, 20)
    
    val master = Config.isMaster
    val masterPort = Config.masterPort.getValue
    var masterIp = Config.masterIp.getValue

    var masterLog: IMasterLog = null

    spawn {
        val list = new ArrayBuffer[LogRecord]()
        var limit = 1000

        while (true) {

            println(s"flushing ~~~~ $limit")

            if (_queue.size() > limit + 1000) limit += 1000

            while (_queue.peek() != null && list.size < limit) {
                val item = _queue.poll()
                list += item
            }

            if (list.size > 0) {
                lockFile(flush(list))
                list.clear()
            }

            Thread.sleep(5 * 1000)
        }
    }

    def openFile(): PrintWriter = {
        val time = new Date()
        val s = LogFile.getPathFile(time)
        new File(s).getParentFile.mkdirs
        new PrintWriter(new BufferedWriter(new FileWriter(s, true)))
    }

    def openPendingFile(): PrintWriter = {
        val time = new Date()
        val s = LogFile.getPathFile(time) + s".pending.${time.getTime}"
        new File(s).getParentFile.mkdirs
        new PrintWriter(new BufferedWriter(new FileWriter(s, true)))
    }

    var _current: String = ""
    
    def remoteFlush(list: ArrayBuffer[LogRecord]){
        for (r <- list) _queue.offer(r)
    }

    val pendingLogs = new ArrayBuffer[LogRecord]()

    def printLog(s: PrintWriter, record: LogRecord){
        s.println(s"${record.logKind},${record.task},${record.location},${record.time},${record.siteId},${record.zoneGroupIds.foldLeft[String]("")(_ + _ + "|")},${record.zoneId},${record.orderId},${record.campId},${record.itemId},${record.ip},${record.cookie},${record.kind},${record.value},${record.videoId},${record.adType}")
    }

    def flush(list: ArrayBuffer[LogRecord]) {
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
                println("error while flush log to master")
                println(s"pending logs: ${pendingLogs.size}")

                if (!Environment.isReconnectLogService)
                {
                    println("Reconnect to log service at ip " + Config.masterIp.getValue + ":" + Config.masterPort.getValue)
                    spawn {
                        if (!Environment.isReconnectLogService) {
                            Environment.isReconnectLogService = true
                            Environment.connectLogService(Environment.log)
                        }
                    }
                }
                if (pendingLogs.size > 10000){
                    println(s"log need recompile: " + pendingLogs.size)
                    val s = openPendingFile()
                    for (x <- pendingLogs) printLog(s, x)
                    s.close()
                    pendingLogs.clear()
                }
            }
        }
    }

    def log(logKind: String, task: Int, location: String, siteId: Int, zoneGroupIds: Array[Int], zoneId: Int, orderId: Int, campId: Int,itemId: Int, ip: String, cookie: String, kind: String, time: Long, money: Int, videoId: Int, adType: Int = 0): Unit = {
        val record = new LogRecord(logKind, task, location, siteId, zoneGroupIds, zoneId, orderId, campId, itemId, ip, cookie, kind, time, money, videoId, adType, 0)
        _queue.offer(record)
    }

    def readAll(from: Date, duration: Long, skip: Long = 0): Iterator[LogRecord] = {
        val hours = (duration / 1000 / 3600).toInt
        val remain = duration - hours * 3600L * 1000

        if (remain == 0) return new LogIterator(from, hours, skip)

        return new RangeLogIterator(start, duration, new LogIterator(from, hours + 1))
    }
}

class ImpressionTrack2 (getTime: ()=>Long) extends ITrack {
    val trackTime = 15 * 60 * 1000
    val clickTime = 30 * 1000

    val impression = new LimitedLifeBag[String](trackTime, getTime)
    val click = new LimitedLifeBag[String](clickTime, getTime)

    spawn{
        Thread.sleep(100)

        while (true){
            var i = 0
            var c = 0

            while (impression.gc()) i += 1
            while (click.gc()) c += 1

            Thread.sleep(50)

            //if (i > 0 || c > 0) println(s"collect: $i impression, $c click")
        }
    }

    def gc(): Boolean = {
        val a = impression.gc()
        val b = click.gc()
        a || b
    }

    def mapKey(time: Int) = time / 60

    def trackToken(token: String, time: Int) = impression.track(token)

    def verifyToken(token: String, time: Int): Boolean = {
        if (!impression.contains(token)) {
            //println("no impression")
            return false
        }
        if (click.contains(token)) {
            //println("just click")
            return false
        }
        click.track(token)
        return true
    }
}

class LimitedLifeBag[T](lifeTime: Int, getTime: ()=>Long){

    val map = new ConcurrentHashMap[T,(T,Long)]()
    val queue = new ConcurrentLinkedQueue[(T,Long)]()

    def track(token: T){
        val now = getTime()
        map.put(token,(token,now))
        queue.add((token,now))
    }

    def contains(token: T) : Boolean ={
        val now = getTime()
        val t = map.get(token)

        (t != null) && (t._2 + lifeTime >= now)
    }

    def gc(): Boolean = {
        val now = getTime()
        val t = queue.peek()
        if (t != null && t._2 + lifeTime < now){
            queue.dequeue() match{
                case Some(x)=>{
                    map.tryGetValue(x._1) match{
                        case Some(y)=>{
                            if (y._2 + lifeTime < now) map.remove(x._1)
                        }
                        case None =>
                    }
                }
                case None =>
            }
            true
        }
        else false
    }
}

class ImpressionTrack extends ITrack {
    val trackTime = 15 * 60 * 1000
    val clickTime = 1 * 60 * 1000

    val map = new ConcurrentHashMap[String, (String, Long)]()
    val queue = new ConcurrentLinkedQueue[(String, Long)]()

    def mapKey(time: Int) = time / 60

    def trackToken(token: String, time: Int): Unit = {
        val now = System.currentTimeMillis()

        map.putIfAbsent(token, (null, now))
        queue.enqueue((token, now))

        var free = 0

        while (queue.peek()._2 + trackTime < now
            && !queue.isEmpty
            && free < 10) {
            queue.dequeue()
            free += 1
        }
    }

    def verifyToken(token: String, time: Int): Boolean = {
        val value = map.get(token)
        if (value == null) return false
        if (value._1 == "1" && value._2 + clickTime >= System.currentTimeMillis()) return false

        map.put(token, ("1", System.currentTimeMillis()))
        true
    }
}