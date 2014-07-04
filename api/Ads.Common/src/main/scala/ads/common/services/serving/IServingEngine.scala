package ads.common.services.serving

import ads.common.model._
import com.netflix.config.DynamicPropertyFactory
import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import java.io.{FileWriter, BufferedWriter, PrintWriter}
import scala.collection.mutable

class AdsRequest(
                             val site: Int,
                             val zoneIds: List[Int],
                             val url: String,
                             val ip: String,
                             val cookie: String,
                             val viewed: List[Int],
                             val items: Int = 1,
                             val companionTargeting: String,
                             val listValues: mutable.Map[String, List[String]] = null
                             )

class CompanionAdsResponse(
                              val data: mutable.HashMap[Int, Array[Banner]]
                              )

trait IServingEngine {
    def serve(request: AdsRequest): AdsResponse
    def serveMulti(request: AdsRequest): AdsResponse
    def serveVideo(request: AdsRequest): VideoAdsResponse
    def servePauseAdVideo(request: AdsRequest, json: String): VideoAdsResponse
    def serveCompanion(request: AdsRequest) : CompanionAdsResponse
    def serveVideoZingTv(request: AdsRequest, json: String): ZingTVVideoAdsResponse
    def getPrData(request: AdsRequest, model: PrBannerModel): Array[ArticleModel]
    def run()
}

object Const{
    val totalShares: Int = 1000*1000
}

object Configs{
    val UploadUrl = DynamicPropertyFactory.getInstance().getStringProperty("urls.upload", "http://localhost/uploads")
}


class CachedLogService (path: String){

    val buf = new ArrayBuffer[(Int,String,Double)]()

    def readAll(f: (Int,String,Double)=>Unit){
        for (line <- Source.fromFile(path).getLines()){
            val seqs = line.split(',')
            val time = seqs(0).toInt
            val key = seqs(1).toString
            val value = seqs(2).toDouble
            f(time,key,value)
        }
    }

    def save(time: Int, key: String, value: Double){
        buf += ((time,key,value))
    }

    def flush(){
        val s = new PrintWriter(new BufferedWriter(new FileWriter(path, true)))
        for ((time,key,value) <- buf){
            s.println(s"$time,$key,$value")
        }
        s.close()
        buf.clear()
    }
}

