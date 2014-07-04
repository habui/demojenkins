package ads.common

import ads.common.collections.{Cube, RadixReportTrie, RadixDictionary}
import org.scalatest.FunSuite
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import ads.common.services.report.{ReportRequest, ReportResponse}
import scala.collection.mutable
import java.util.Date


class CubeTests extends FunSuite{
    class Record (var impression: Int, var click: Int)

    test("values"){
        val cube = new Cube[Int]()

        cube.add(1, Array(("x", 1), ("y",1), ("z", 3)), Array(("group", Array(1,2,3,4))))
        cube.add(2, Array(("x", 1), ("y",1), ("z", 3)))
        cube.add(3, Array(("x", 2), ("y",1), ("z", 3)))
        cube.add(4, Array(("x", 2), ("y",2), ("z", 3)), Array(("group", Array(2))))

        val list1 = cube.fold[ArrayBuffer[Int]](Array(("group",1)), new ArrayBuffer[Int](), (s,r) => s += r)
        assertResult(1)(list1.length)
        assertResult(1)(list1(0))

        val list2 = cube.fold[ArrayBuffer[Int]](Array(("group",2)), new ArrayBuffer[Int](), (s,r) => s += r)
        assertResult(2)(list2.length)
        assertResult(true)(list2.contains(1))
        assertResult(true)(list2.contains(4))

        val x = cube.getValues("x")
        assertResult(2)(x.length)
        assertResult(true)(x.contains(1))
        assertResult(true)(x.contains(2))

        val x2 = cube.getValues(Array(("y",2)), "x")
        assertResult(1)(x2.length)
        assertResult(2)(x2(0))
    }

    test("simple"){
        val cube = new Cube[Record]()

        cube.add(new Record(10000, 1), Array(("order", 1), ("website", 1), ("zone", 1)))
        cube.add(new Record(10000, 1), Array(("order", 1), ("website", 1), ("zone", 2)))
        cube.add(new Record(10000, 1), Array(("order", 1), ("website", 2), ("zone", 3)))
        cube.add(new Record(10000, 1), Array(("order", 2), ("website", 2), ("zone", 3)))

        val x = cube.fold[Int](Array(("order", 1)), 0, (s,r)=>s + r.impression)
        val y = cube.fold[Int](Array(("order", 1), ("website", 2)), 0, (s,r)=>s + r.impression)
        val z = cube.fold[Int](Array(("website", 2)), 0, (s,r)=>s + r.impression)
        val t = cube.fold[Int](Array(("zone", 3)), 0, (s,r)=>s + r.impression)

        val list = cube.fold[ArrayBuffer[Record]](Array(("zone", 3)), new ArrayBuffer[Record](), (s,r)=>s += r)


        assertResult(30000)(x)
        assertResult(10000)(y)
        assertResult(20000)(z)
        assertResult(20000)(t)
    }

    test("load"){
        val cube = new Cube[Record]()

        val time = Syntaxs.measureTime{
            for (i <- 1 to 1000000){
                cube.add(new Record(1000, 1), Array(("website",i/100000), ("zone", i/100), ("order", i/50000)))
            }
        }

        val foldTime = Syntaxs.measureTime { cube.fold[Int](Array(("website",0)), 0, (s, r) => s + r.impression)}

        val s = cube.fold[Int](Array(("zone", 1000), ("website",10)), 0, (s, r) => s + r.impression)
        println(s"insert : $time ms")
        println(s"fold   : $foldTime ms")
        println(s)
    }
}

class RadixDictionaryTests extends FunSuite{


    test("report_trie"){


        val d = new RadixReportTrie()
        d.insert("sgz", Array(1,2,3), 10)
        val r = d.find("sgz", Array(1,2,3))
        assertResult(10)(r.getOrElse(0))

        val insertTime = Syntaxs.measureTime{for (i <- 1 to 1000000) d.insert("a", Array(i/1000000,i/100,i), 1)}
        val findTime = Syntaxs.measureTime{for (i <- 1 to 1000000) d.find("a", Array(i/1000000,i/100,i))}

        val findAllTime = Syntaxs.measureTime{d.findAll("a", Array(0))}
        val sumAllTime = Syntaxs.measureTime{d.sumAll("a", Array(0))}

        println (s"insert: $insertTime ms")
        println (s"find: $findTime ms")
        println (s"findAll: $findAllTime ms")
        println (s"sumAll: $sumAllTime ms")
    }


    test("hoho") {
        val d = new RadixDictionary[Int]()
        d.insert("hello", 1)

        val r = d.find("hello")
        assertResult(1)(r.getOrElse(0))

        val time = Syntaxs.measureTime{for (i <- 0 until 1000000) d.insert(i.toString,i)}
        println(s"insert: $time ms")

        val time2 = Syntaxs.measureTime{for (i <- 0 until 1000000) d.find(i.toString)}
        println(s"find: $time2 ms")

        val findAllTime = Syntaxs.measureTime{ d.findAll("12345")}
        println(s"findAll: $findAllTime ms")

        for (i <- 1 until 10){
            println(s"$i -> ${d.findAll(i.toString).get.length}")
        }
    }
}
