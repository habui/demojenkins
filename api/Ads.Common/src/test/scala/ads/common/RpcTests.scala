package ads.common

import org.scalatest.FunSuite
import ads.common.rpc._
import Syntaxs._
import scala.collection.mutable.ArrayBuffer

class Log(val x: String, val y: String, val z: String)

trait ICalc{
    def sum(x: Int, y: Int): Int
    def sumArray(x: ArrayBuffer[Int]): Int
    def sumLog(x: ArrayBuffer[Log]): Int
}

class Calc extends ICalc{
    def sum(x: Int, y: Int): Int = x + y
    def sumArray(x: ArrayBuffer[Int]): Int = x.fold[Int](0)((s: Int,r: Int)=>s + r)
    def sumLog(x: ArrayBuffer[Log]) = x.length
}

class RpcTests extends FunSuite{
    test("sum"){
        val server = new Thread(new Runnable {
            def run() {
                Rpc.register("calc", new Calc())
                Rpc.startServer(9099)
            }
        })

        server.start()
        Thread.sleep(10)

        val client = new ThriftClient("127.0.0.1", 9099)
        val proxy = ProxyGen.createProxy[ICalc]("calc", client.process)
        val sum = proxy.sum(1,2)

        val buf = new ArrayBuffer[Log]()
        for (i <- 0 until 1000) buf += new Log("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", "yyyyyyyyyyyyyyyyyyyyyyyyyyyy", "zzzzzzzzzzzzzz")

        proxy.sumLog(buf)

        println(s"$sum")

        val time = measureTime{
            for (i <- 0 until 100){
                val sumArr = proxy.sumLog(buf)
            }
        }

        println(s"time: $time")

        //println(s"$sumArr")
    }

}
