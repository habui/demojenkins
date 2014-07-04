package ads.common

import org.scalatest.FunSuite
import ads.common.Syntaxs._
import java.util.concurrent.{ConcurrentSkipListMap, ConcurrentHashMap}
import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._
import java.util.{Date, Comparator}
import scala.pickling._
import binary._


class XXX(x: Int, y: Int, z: Int)

class PickleSpecs extends FunSuite{
    test("binary"){
        val obj = new XXX(1,2,3)
        val buf = new ByteArray(1024)
        obj.pickleTo(buf)
    }
}

class LogIteratorSpecs extends FunSuite{
    test("hello"){
        println(System.currentTimeMillis() > 1384146660000L)
        println(new Date(1384146660000L))
        System.currentTimeMillis()  |> println
    }
}

class LanguageSpecs extends FunSuite{


    def formatMoney(x: Long): String  = {
        var s = ""
        var n = x
        var count = 0
        while (n > 0){
            val mod = n % 10
            if (count % 3 == 0 & count > 0){
                s = "." + s
            }
            s = mod + s
            n = n / 10
            count += 1
        }
        s
    }

    test("tuple_hash_code_and_equal"){
        println(SecurityUtils.encode(10001))
    }


    test("getOrAdd"){
        val map = new ConcurrentHashMap[Int,ArrayBuffer[Int]]()
        val l = map.getOrAdd(1, id=>new ArrayBuffer[Int]())
        l += 1
        map.getOrAdd(1, id=>new ArrayBuffer[Int]()) += 2
        map.getOrAdd(1, id=>new ArrayBuffer[Int]()) += 3

        val second = map.getOrAdd(1, id=>new ArrayBuffer[Int]())

        assert(second.size == 3)
        assert(second(0) == 1)
        assert(second(1) == 2)
        assert(second(2) == 3)
    }

    class SComperator extends Comparator[Int]{
        def compare(o1: Int, o2: Int): Int = o1 - o2
    }

    test("map"){
        val map = new ConcurrentSkipListMap[Int,Int](new SComperator())
        for (i <- 0 until 100) map.put(i, i)

        val it = map.values().iterator()

        for (x <- it) println(x)
    }

    test("is"){

        
        println(classOf[List[Int]] == classOf[List[String]])

        val intList = new java.util.ArrayList[Int](10)

        intList.add(0)

        val xxxList = intList.asInstanceOf[(java.util.ArrayList[String])]
        xxxList.add("hello")

        println(xxxList == intList)
    }
}

class SecuritySpecs extends FunSuite {

    test("encode"){
        val code = SecurityUtils.encode(10000)
        println("encode: " + code)
    }

    test("simple data") {
        val encoded = SecurityUtils.encodeData(Array("hello", "world"))
        println(encoded)

        val decoded = SecurityUtils.decodeData(encoded)
        println(decoded)

        assert(decoded.isInstanceOf[Success[Array[String]]])

        decoded match {
            case Success(s) => {
                s.reduce((x, y) => x + ";" + y) |> println
                assertResult("hello")(s(0))
                assertResult("world")(s(1))
            }
            case _ => fail("decode failed!")
        }
    }

    test("speed") {
        val encoded = SecurityUtils.encodeData(Array("hello", "world"))
        val loop = 1000000
        val time = measureTimeNano{
            //for (i <- 0 until loop) Utils.encodeData(Array("hello #" + i, "world # " + i))
            for (i <- 0 until loop) SecurityUtils.decodeData(encoded)
        }

        println(s"${time/1000000} ms")
        println(s"speed: ${loop * 1000L * 1000 / time} K/sec")
    }

    test("simple attack") {

        //        val encoded = Utils.encodeData(Array("hello", "world"))
        //        println(encoded)
        //
        //        var count = 0
        //
        //        for (j <- 0 until encoded.length) {
        //            for (i <- 0 until 255) {
        //                val hack = encoded.substring(0, j) + (i.toChar) + encoded.substring(j + 1)
        //
        //                if (hack != encoded) {
        //                    val decoded = Utils.decodeData(hack)
        //
        //                    decoded match {
        //                        case Success(s) => {
        //                            if (s.length > 0) {
        //                                count = count + 1
        //                                //s.reduce((x, y) => x + "|" + y) |> println
        //                                //assert(decoded.isInstanceOf[Failure])
        //                            }
        //                        }
        //                        case _ => {}
        //                    }
        //
        //                    //assertResult(true)(decoded.isInstanceOf[Failure])
        //                }
        //            }
        //        }
        //
        //        println(s"hits: $count")

    }
}