package ads.website

import org.scalatest.FunSuite
import ads.website.modules.{BookingService, BookingHandler}
import ads.common.model._
import java.util
//import ads.common.services.report.MockReportService
import ads.common.Syntaxs._
import ads.common.{SecurityUtils}
import scala.collection.immutable.TreeMap
import java.util.Date
import ads.common.database.{SqlDataService, IDataService, InMemoryDataService}
import ads.common.services.ActionLogService
import ads.web.WebUtils

class Gson_extreme_cases extends FunSuite{
    test("new concurrent skip list map") {
        lazy val zoneToZoneGroupDataService: IDataService[ZoneToZoneGroup] = new SqlDataService[ZoneToZoneGroup](()=>new ZoneToZoneGroup(0,0,0))
        val map = new InMemoryDataService[ZoneToZoneGroup](z=>z.zoneGroup, zoneToZoneGroupDataService,Environment.actionLogServiceRef)

        map.countByRef(19)
        map.listByReferenceId(id=19)
    }


    test("test merge booking") {
        val bookingService = new BookingService(Environment)
        val listBookRecord = new util.ArrayList[BookRecord]()
        listBookRecord.add(new BookRecord(0,1,10,100,1383868800000L,1384819200000L))
//        listBookRecord.add(new BookRecord(0,1,10,80,969763500000L,970022700000L))
        listBookRecord.add(new BookRecord(0,1,10,75,1384819200000L,1385510400000L))
        listBookRecord.add(new BookRecord(0,1,10,75,1385510400000L,1385942400000L))
        //val rs = bookingService.mergeBookRecord(listBookRecord)
    }

    test("extreme"){
    }
}

class Load extends FunSuite{

    test("report "){
        println(new Date().getTime/3600/1000*3600*1000)
    }

    test("xxx"){
        val params = SecurityUtils.decodeData("sJ5TGz6pO9yOu0meij05QqPNgPEuRqTSWUslem3wvv_CGwVj4XosjN_oVjZl0o3Y1Dl9a2selCChjk3vMbCdHgi8j4KHLNE2CGHHQUdF3jovgMdtVh9NRnImIwD42Br3r9OPqRmvsIzb514-KJW-zVJASD4").getOrElse(null)
        var map = new TreeMap[String, String]()
        for (i <- 0 until params.length / 2) {
            map = map + ((params(i * 2), params(i * 2 + 1)))
        }
        for (e <- map) println(e)

        val token = map("token")
        val time = SecurityUtils.decodeTimeToken(token)

        println(time)
    }
}

class Yield_case extends FunSuite{

    class Point (var x : Int, var y: Int, var z : Int)

    def from(n: Int): Stream[Int] =
        Stream.cons(n, from(n + 1))

    def sieve(s: Stream[Int]): Stream[Int] =
        Stream.cons(s.head, sieve(s.tail filter { _ % s.head != 0 }))

    def primes = sieve(from(2))


    test("yield"){
        val p = new Point(1,2,3)
        println (WebUtils.toRawJson(p))
    }
}