package ads.website

import org.scalatest.FunSuite
import ads.website.modules.serving.{ImpressionTrack2, ITrack, LogIterator, ImpressionTrack}
import java.util.{Hashtable, Date}
import scala.concurrent.ops._
import java.util.concurrent._
import ads.common.Syntaxs._
import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.collection.JavaConversions._

class WohoTest extends FunSuite{
    val path = "C:\\Users\\vietnt\\Downloads\\2013-12-31\\2013-12-31\\access."

    test("top"){

        val map = new Hashtable[String,Int]()
        var total = 0

        for (i <- 0 to 9){
            val file = s"$path$i.log"
            val lines = Source.fromFile(file).getLines()
            for (line <- lines){
                val seqs = line.split(',')
                val time = seqs(0).toLong
                val user = seqs(1)
                val act = seqs(2).substring(3).toInt
                val item = seqs(3).toInt
                val category = seqs(4).toInt

                total += 1

                if (act == 1 && category == 138){
                    val key = user
                    if (map.containsKey(key)){
                        map.put(key, map.get(key) + 1)
                    }
                    else map.put(key, 1)
                }
            }
        }

        val list = new ArrayBuffer[(String,Int)]()
        for (k <- map.entrySet()){
            list += ((k.getKey, k.getValue))
        }

        val top = list.sortBy(e=> - e._2).take(100).toArray

        println(s"total: $total")
        for (e <- top){
            println(s"${e._1} -> ${e._2}")
        }
    }
}

class SpamTest extends FunSuite{

    test("simple"){
        var time = 0L
        val track = new ImpressionTrack2(()=>time)
        val token = "hello@"

        track.trackToken(token, 0)
        assertResult(true)(track.verifyToken(token, 0))
        assertResult(false)(track.verifyToken(token, 0))

        time += 100 * 1000

        assertResult(true)(track.verifyToken(token, 0))
    }

    test("click_without_impression_is_spam"){
        var time = 0L
        val track = new ImpressionTrack2(()=>time)
        val token = "hello@"

        assertResult(false)(track.verifyToken(token, 0))
    }

    test("impression_expired"){
        var time = 0L
        val track = new ImpressionTrack2(()=>time*1000)
        val token = "hello@"

        track.trackToken(token,0)

        time += 1000

      //  assertResult(false)(track.verifyToken(token, 0))
    }

    test("multiple_impression"){
        var time = 0L
        val track = new ImpressionTrack2(()=>time*1000)
        val token = "hello@"

        Thread.sleep(100)

        for (i <- 1 to 20){
            track.trackToken(token,0)
            time += 800
            Thread.sleep(5)
        }

        assertResult(true)(track.verifyToken(token, 0))
    }

    test("load"){

        var time = 0L
        val track = new ImpressionTrack2(()=>time)
        val log = new LogIterator(new Date(113,11,24),10)
        var c = 0L
        var click = 0
        var spam = 0
        val cookies = new ConcurrentHashMap[String,Long]()
        var wtf = 0
        var distance = new ArrayBuffer[Int]()

        val noCookies = new ConcurrentHashMap[String,Long]()
        var resolved = 0

        for (it <- log){
            if (it != null)
            {
                val tk = it.cookie + "@" + it.itemId

                time = it.time
                it.kind match{
                    case "impression" => {
                        cookies.put(tk, time)
                        track.trackToken(it.cookie + "@" + it.itemId, 0)

                        if (noCookies.containsKey(tk)){
                            val x = noCookies.get(tk)
                            if (x > 0){
                                println(s"found impression, time: ${(time - x)/1000} sec")
                                resolved += 1
                                noCookies.put(tk,0)
                                distance.+=:(((time - x)/1000).toInt)
                            }
                        }
                    }
                    case x if x == "click" || x == "click.spam" =>{
                        println(x)
                        val t = cookies.putIfAbsent(tk, time)
                        if (t == 0) {
                            wtf += 1
                            noCookies.put(tk, time)
                        }
                        else{
                            //distance.+=:(((time - t)/1000).toInt)
                        }

                        if (track.verifyToken(it.cookie + "@" + it.itemId, 0)) {
                            click += 1
                            println("ok!")
                        }
                        else spam += 1
                    }
                    case _ =>
                }
                c += 1

                while (track.gc()) {}
            }
        }
        println (s"count: $c")
        println (s"click: $click")
        println (s"spam: $spam")
        println(s"click without cookie: $wtf")
        println(s"resolved: $resolved")

        for (x <- distance) println(x)
    }
}
