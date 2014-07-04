package ads.website

import org.scalatest.FunSuite
import ads.website.modules.serving.LogRecord
import ads.common.Syntaxs._
import java.util.Date
import scala.collection.mutable
import java.io.{FileWriter, BufferedWriter, PrintWriter}

/**
 * Created by tungvc2 on 2/14/14.
 */
class RenderTimeTest extends FunSuite{
    /*
    class Key(val siteId: Int, val zoneId: Int, val cookie: String){
        override def hashCode(): Int = {
            val x = (siteId * 13) ^ (zoneId * 23) ^ cookie.hashCode
            x
        }

        override def equals(obj: scala.Any): Boolean =
            if (super.equals(obj)) true
            else
            if (!obj.isInstanceOf[Key]) false
            else {
                val y = obj.asInstanceOf[Key]
                return siteId == y.siteId && zoneId == y.zoneId && cookie.equals(y.cookie)
            }
    }

    class Info(var time: Long, var renderTime: Long = 0)
    val map = new mutable.HashMap[Key, Info]()
    val zingFile = new PrintWriter(new BufferedWriter(new FileWriter("D:/log/1zing.txt", true)))
    val baoMoiFile = new PrintWriter(new BufferedWriter(new FileWriter("D:/log/1baomoi.txt", true)))
    //select file log at 7h00 14-2-2014
    val iterator: Iterator[LogRecord] = Environment.log.readAll(new Date(2014 - 1900, 1, 14, 7, 0), 3600*1000)
    using (iterator){
        for (item <- iterator){
            if (item != null){
                if (item.siteId == 1 || item.siteId == 6) {
                    val key = new Key(item.siteId, item.zoneId, item.cookie)
                    item.kind match {
                        case "render" =>
                            map.get(key) match{
                                case Some(x) => {
                                    x.time = item.time
                                }
                                case None => map.put(key, new Info(item.time))
                            }
                        case "impression" => {
                            map.get(key) match{
                                case Some(x) => {
                                    val renderTime = item.time - x.time
                                    if (renderTime > 0)
                                        if (key.siteId == 1) zingFile.println(s"${key.zoneId},${renderTime}")
                                        else baoMoiFile.println(s"${key.zoneId},${renderTime}")
                                    map.remove(key)
                                }
                                case None =>
                            }
                        }
                        case _ =>
                    }
                }
            }
        }
    }
    zingFile.close()
    baoMoiFile.close()
    */
}
