package ads

import scala.collection.mutable.ArrayBuffer
import java.io._
import org.scalatest.FunSuite
import ads.common.model.PRZoneModel
import ads.website.Environment
import java.util.{TimeZone, Timer, TimerTask, Date}
import scala.collection.mutable
import ads.website.modules.serving.{TrackKind, ReportRecord}
import ads.common.geoip.GeoipService
import ads.web.WebUtils
import ads.common.Syntaxs._
import com.maxmind.geoip.Location
import com.google.common.hash.{PrimitiveSink, Funnel, BloomFilter}
import scala.Predef._
import java.text.SimpleDateFormat
import java.util
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import org.apache.commons.lang.SerializationUtils
import ads.website.modules.UniqueUser.UniqueUserInfo

class Report extends FunSuite{

    class Data(var imp: Long, var click: Long)
    def next() {
        val listKey = new ArrayBuffer[Long]()
        var result: Map[Long, Data] = Map()
        val reader = new BufferedReader(new FileReader("D:\\log\\2014\\server_58728080_2014_03_03_21.log"))
        var line = reader.readLine()
        while(line != null) {
            val parts = line.split(",")
            try {
                val logRecord = parts.size match {
                    case 11 => {
                        val segs = line.split(",")
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
                        val key = time / 1000 / 60 * 1000 * 60
                        if (result.contains(key)) result(key).click += 1
                        else {
                            result += (key -> new Data(0, 1))
                            listKey += key
                        }
                        if (itemId == 780 && kind == "impression") {
                            result(key).imp += 1
                        }
                    }
                    case 12 => { // log record has videoId (subzone)
                    val segs = line.split(",")
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
                        val key = time / 1000 / 60 * 1000 * 60
                        if (result.contains(key)) result(key).click += 1
                        else {
                            result += (key -> new Data(0, 1))
                            listKey += key
                        }
                        if (itemId == 1051 && kind == "impression") {
                            result(key).imp += 1
                        }
                    }
                }
            } catch {
                case ex: Exception => println(s"Error while read log: $line\n${ex.getMessage}\n${ex.getStackTraceString}")
            }
            line = reader.readLine()
        }
        for (i <- listKey) {
            println(f"${new Date(i)}: ${result(i).imp}%10d - ${result(i).click}")
        }
    }

    test("report") {
        next()
        //println(Environment.fastReportService.getImpressionClick(579,System.currentTimeMillis(),System.currentTimeMillis()))
    }

    test("skip line") {
        println(Long.MaxValue)
        val reader = new BufferedReader(new FileReader("D:\\log\\2014\\a.txt"))
        //reader.skip(3)
        var line = reader.readLine()
        println(line.getBytes.length)
        while(line != null) {
            println(line + " -- " + line.length + "--" + line.getBytes.length)
            line = reader.readLine()
        }
    }

    test("concat HashMap") {
        val a = new mutable.HashMap[String, String]()
        a.put("1", "mot")
        a.put("2", "hai")
        val b =  new mutable.HashMap[String, String]()
        b.put("3", "ba")
        b.put("4", "bon")
        a ++= b
        println(a)
    }

    test("Line number reader") {
        val reader = new BufferedReader(new FileReader("D:\\log\\server_58728080_2014_02_24_03.log"))
        //val lnr = new LineNumberReader(reader)
        //reader.skip(3)
        println("abc " + "\r".length)
        var count = 0
        var line = reader.readLine()
        while(line != null) {
            if (line.length != line.getBytes.length) println(line + "\n" + line.length + "\n" + line.getBytes.length)
            count += 1
            line = reader.readLine()
        }
    }

    test("MyTimerTask") {
        class MyTimerTask extends TimerTask {
            def run {
                System.out.println("Timer task started at:" + new Date)
                completeTask
                System.out.println("Timer task finished at:" + new Date)
            }

            private def completeTask {
                try {
                    Thread.sleep(5000)
                }
                catch {
                    case e: InterruptedException => {
                        e.printStackTrace
                    }
                }
            }
        }

        val timerTask: TimerTask = new MyTimerTask
        val timer: Timer = new Timer(true)
        println("now " + new Date())
        timer.scheduleAtFixedRate(timerTask, new Date(System.currentTimeMillis()-5000), 10 * 1000)
        System.out.println("TimerTask started")
        try {
            Thread.sleep(120000)
        }
        catch {
            case e: InterruptedException => {
                e.printStackTrace
            }
        }
        timer.cancel
        System.out.println("TimerTask cancelled")
        try {
            Thread.sleep(30000)
        }
        catch {
            case e: InterruptedException => {
                e.printStackTrace
            }
        }

    }

    test("location") {
        //val location = GeoipService.getGeoipClient.getLocation("")
        //println(s"0,${(location == null) ? "" | "2"},")
        class ABC(val x: Int, y: Int)
        var map = Map[Int, ABC]()
        map += ((5,new ABC(1,1)))
        map += ((6,new ABC(2,2)))
        val a = new ABC(4, 5)
        println(WebUtils.toJson(a))
    }

    test("bloom filter"){
        val stringFunnel = new Funnel[String]() {
            def funnel(p1: String, p2: PrimitiveSink) = {
                p2.putString(p1)
            }
        }

        val bf = BloomFilter.create(stringFunnel, 1000, 0.1)
        println(s"Contain abc: ${bf.mightContain("abc")}")
        bf.put("abc")
        println(s"Contain abc: ${bf.mightContain("abc")}")
//        val objectWriter = new ObjectOutputStream(new FileOutputStream("D:/bloomfilter.obj"));
//        objectWriter.writeObject(bf)
//        objectWriter.flush()
//        objectWriter.close()


        // Deserialize
        val readerObject = new ObjectInputStream(new FileInputStream("D:/bloom.txt"));
        val bloomFilterAfterDes = readerObject.readObject().asInstanceOf[UniqueUserInfo]
        readerObject.close()
        println(bloomFilterAfterDes.size)
        //println(s"Contain abc: ${bloomFilterAfterDes.mightContain("abc")}")
    }

    test("time to string") {
        val date = new Date(1401210000000L)

        val simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy' 'HH:MM:ss")
        println(date.toLocaleString)
    }

    test("serving") {
        val from = 1401987600000L
        val duration = 24 //24h
        val startDate = new Date(from)
        val result: mutable.Map[Int, mutable.Map[Long, Long]] = mutable.Map()
        for (i <- 0 until duration) {
            val current = startDate + i.hour
            val iterator = Environment.log.readAll(current, 3600*1000)
            using (iterator){
                for (item <- iterator; if item != null; if item.zoneId == 179; if item.kind == TrackKind.Impression){
                    if (!result.contains(item.itemId)) result += ((item.itemId, mutable.Map()))
                    val banner = result(item.itemId)
                    val time = item.time / (10L*60*1000)
                    if (!banner.contains(time)) banner += ((time, 0))
                    banner(time) += 1
                }
            }
        }
        val fileOutput = new PrintWriter("D:/log/Report for serving.txt")
        for ((k,v) <- result) {
            val times = v.keySet.toList.sortWith(_ < _)
            fileOutput.println(s"----${k}-----")
            for (time <- times) {
                fileOutput.println(s"${time}: ${v(time)}")
            }
        }
        fileOutput.close()
    }


    test("Get user info") {
        var folder = new File("D:/log/UserAgent")
        var listFile = folder.list().filter(_.startsWith("User"))
        val pw = new PrintWriter("D:/log/UserAgent/UserInfo.txt")
        val map :mutable.Map[String, String] = mutable.Map()
        listFile foreach ((file) => {
            val reader = new BufferedReader(new FileReader("D:/log/UserAgent/" + file))
            var line = reader.readLine()
            while (line != null) {
                val rows = line.split("<adtima>")
                if (rows.length > 2 && !map.contains(rows(0))) {
                    map += ((rows(0),null))
                    try {
                        val obj = WebUtils.fromJson(classOf[LinkedTreeMap[String, String]], rows(rows.length - 1))
                        if (obj.get("id") != null)
                            pw.println(s"${rows(0)},${obj.get("id")},${obj.get("gender")},${obj.get("locale")},${obj.get("birthday")}")
                    } catch {
                        case ex: Throwable => {
                            println(line)
                            //throw ex
                        }
                    }
                }
                line = reader.readLine()
            }
            reader.close()
        })
        //format 2
        folder = new File("D:/log/UserAgent/new user")
        listFile = folder.list().filter(_.startsWith("User"))
        def readline(reader: BufferedReader): String = {
            var line = reader.readLine()
            var result = ""
            while (line != null && line != "}") {
                result += line
                line = reader.readLine()
            }
            if (line != null) result + "}"
            else null
        }
        listFile foreach ((file) => {
            val reader = new BufferedReader(new FileReader("D:/log/UserAgent/new user/" + file))
            var line = readline(reader)
            while (line != null) {
                val rows = line.split("<adtima>")
                if (rows.length > 2 && !map.contains(rows(0))) {
                    map += ((rows(0),null))
                    try {
                        val obj = WebUtils.fromJson(classOf[LinkedTreeMap[String, String]], rows(rows.length - 1))
                        if (obj.get("id") != null)
                            pw.println(s"${rows(0)},${obj.get("id")},${obj.get("gender")},${obj.get("locale")},${obj.get("birthday")}")
                    } catch {
                        case ex: Throwable => {
                            println(line)
                            //throw ex
                        }
                    }
                }
                line = readline(reader)
            }
            reader.close()
        })
        pw.close()
    }

    test("Map user info") {
        class Info(val id: String, val gender: String, val locale: String, val birthday: String)
        val reader = new BufferedReader(new FileReader("D:/log/UserAgent/UserInfo.txt"))
        var line = reader.readLine()
        val map: mutable.Map[String, Info] = mutable.Map()
        while (line != null) {
            val rows = line.split(",")
            if (rows.length == 5)
                map += (rows(0) -> new Info(rows(1), rows(2), rows(3), if (rows(4) != "null") rows(4) else null))
            else println(line)
            line = reader.readLine()
        }

        val folder = new File("D:/log/UserAgent/unit")
        val listFile = folder.list().filter(_.contains("click"))
        listFile foreach ((file) => {
            val pw = new PrintWriter("D:/log/UserAgent/unit/" + file + "_result.txt")
            val reader = new BufferedReader(new FileReader("D:/log/UserAgent/unit/" + file))
            var line = reader.readLine()
            while (line != null) {
                if (map.contains(line)) {
                    val info = map(line)
                    pw.println(s"${line},${info.id},${info.gender},${info.locale},${info.birthday}")
                }
                else pw.println(s"${line}")
                line = reader.readLine()
            }
            reader.close()
            pw.close()
        })
    }
}
