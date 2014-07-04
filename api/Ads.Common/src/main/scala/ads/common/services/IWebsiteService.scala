package ads.common.services

import ads.common.database.{IDataDriver, PagingResult, InMemoryDataService, IDataService}
import ads.common.model.{Zone, Website}
import java.util.concurrent.ConcurrentHashMap
import scala.collection.mutable.ArrayBuffer
import ads.common.Syntaxs._
import scala.concurrent.Future

/**
 * Created by stroumphs on 11/8/13.
 */

trait IWebsiteService extends IDataService[Website]{
    def listByType(from: Int, count: Int, types : String) : PagingResult[Website]
}

class WebsiteService(env:{
    val websiteDataDriver : IDataDriver[Website]
    val zoneService : IDataService[Zone]
    val actionLogServiceRef :Future[IActionLogService]
})
    extends InMemoryDataService[Website](u=>u.ownerId, env.websiteDataDriver,env.actionLogServiceRef ) with IWebsiteService{

    lazy val reviewTypeIndex = new ConcurrentHashMap[String, ArrayBuffer[Int]]()

    def listByType(from: Int = 0, count: Int = Int.MaxValue, types : String) : PagingResult[Website] = {
        if(types == null) return new PagingResult(new Array[Website](0), 0)

        val zoneTypes = types.toLowerCase.split(",")
        if(zoneTypes.length == 0) return new PagingResult(new Array[Website](0), 0)
        val sites = list(0,Int.MaxValue)

        if(sites == null || sites.data.length <= 0) return new PagingResult(new Array[Website](0), 0)

        val rs = sites.data.filter(s=>isType(s.id, zoneTypes)).toList
        val size = Math.min(count, rs.size - from)
        val arr = rs.slice(from, from + size).toArray

        new PagingResult(arr, rs.size)
    }

    def isType(siteId: Int, zoneTypes: Array[String]): Boolean = {
        val zones = env.zoneService.listByReferenceId(0,Int.MaxValue, siteId)
        if(zones == null && zones.data.length < 0) return false

        zones.data.filter(z=>{
            var flag = true
            var i = 0
            while(i < zoneTypes.length && flag){
                if(!z.runningMode.contains(zoneTypes(i))){
                    flag = false
                }
                i += 1
            }
            flag
        }).toList.length > 0
    }

}
