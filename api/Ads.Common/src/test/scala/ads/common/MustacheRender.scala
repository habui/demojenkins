package ads.common

import org.scalatest.FunSuite
import ads.common.model.{Zone, Render}
import scala.io.Source
import java.util.Scanner
import ads.common.database.{PagingResult, IDataService}
import ads.common.services.CachedLog
import scala.collection.mutable.ArrayBuffer
import java.io.{FileWriter, BufferedWriter, PrintWriter}

/**
 * Created by vietnt on 1/2/14.
 */
class MustacheRender extends FunSuite{

    test("simple"){
        println (Render.renderZone(new Zone(1,10,"hello", null, null,100,100,100,1024,768,"", "","")))
    }
}

class LoadTest extends FunSuite{
    test("load"){
        val file =Source.fromFile("C:\\Users\\vietnt\\Downloads\\2013-12-31\\2013-12-31\\access.0.log")

        var c = 0
        val time = Syntaxs.measureTime{
            for (line <- file.getLines()) c += 1
        }
        println (s"lines = $c, time = $time, speed = ${c/time} K/sec")
    }
}