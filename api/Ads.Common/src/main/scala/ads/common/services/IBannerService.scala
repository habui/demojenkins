package ads.common.services

import ads.common.database._
import ads.common.model._
import ads.common.Reflect
import scala.concurrent.Future
import scala.concurrent.ops._
import scala.util.control.NonFatal
import ads.common.Syntaxs._


/**
 * Created by stroumphs on 11/25/13.
 */
trait IBannerService extends IDataService[Banner] {
    def searchItems(query: String, pId: Int, searchBy: String) : List[Banner]
}

class BannerService(env:{
    val bannerDataDriver : IDataDriver[Banner]
    val campaignService: IDataService[Campaign]
    val newBookServiceRef: Future[IDataService[NewBookRecord]]
    val actionLogServiceRef : Future[IActionLogService]
}) extends InMemoryDataService[Banner](b=>b.campaignId, env.bannerDataDriver,env.actionLogServiceRef) with IBannerService {
    def searchItems(query: String, pId: Int, searchBy: String): List[Banner] = {
        if(pId > 0) {
            val listItems = loadAll().toList.filter(p=>{
                val name = Reflect.getProperties(p.getClass).get("name").get.get(p)
                if(name != null) {
                    name.toString.toLowerCase.contains(query.toLowerCase)
                } else {
                    false
                }}).toList
            searchBy match {
                case "order" => {
                    val listCampaignId = env.campaignService.loadAll().toList.filter(c=>c.orderId == pId).toList.map(c=>c.id).toList.distinct
                    listItems.filter(b=>listCampaignId.contains(b.campaignId)).toList.take(10)
                }
                case "campaign" => {
                    listItems.filter(b=>b.campaignId == pId).toList.take(10)
                }
                case "zone" => {
                    val service = env.newBookServiceRef.value.get.get
                    val listItemIds = service.loadAll().toList.filter(z=>(z.zoneId == pId)).toList.map(z=>z.itemId).toList.distinct
                    listItems.filter(b=>listItemIds.contains(b.id)).toList.take(10)
                }
                case _ => {
                    listItems.toList.take(10)
                }
            }

        } else {
            //loadAll().toList.filter(p=>Reflect.getProperties(p.getClass).get("name").get.get(p).toString.toLowerCase.contains(query.toLowerCase)).take(10).toList
            super.search(query, pId)
        }
    }
}

trait IBanner2Service extends IDataService[Banner]{

}

class Banner2Service(env:{
    val banner2DataDriver : IDataDriver[Banner]
    val actionLogServiceRef : Future[IActionLogService]
}) extends InMemoryDataService[Banner](b=>b.campaignId, env.banner2DataDriver,env.actionLogServiceRef) with IBanner2Service{
    spawn {
        while(true) {
            Thread.sleep(30 * 1000)
            try {
                //            println("----------Update banner2service -------------")
                for (item <- env.banner2DataDriver.loadAll()) {
                    if (all.containsKey(item.id)){
                        val x = all.get(item.id)
                        all.remove(item.id)
                        removeIndex(x)
                    }
                    all.put(item.id, item)
                    index(item)
                }
                //            println("------------End------------------------------")
            }
            catch {
                case NonFatal(e) => printException(e, System.out)
                case x: Throwable => printException(x, System.out)
            }

        }
    }
}

