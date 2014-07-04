package ads.common.services

import ads.common.database.{InMemoryDataService, IDataDriver, IDataService}
import ads.common.model.{Config, CampaignStatus, Campaign}
import scala.concurrent.Future
import scala.concurrent.ops._
import scala.Array
import scala.util.control.NonFatal
import ads.common.Syntaxs._

trait ICampaignService extends IDataService[Campaign] {

}

class CampaignService(env:{
    val campaignDataDriver : IDataDriver[Campaign]
    val actionLogServiceRef : Future[IActionLogService]
}) extends InMemoryDataService[Campaign](b=>b.orderId, env.campaignDataDriver,env.actionLogServiceRef) with ICampaignService{
    spawn {
        while(Config.isMaster) {
            try {
                val current = System.currentTimeMillis()
                for (item <- env.campaignDataDriver.loadAll()) {
                    if(item.endDate < current && Array(CampaignStatus.RUNNING, CampaignStatus.PENDING, CampaignStatus.PAUSED).contains(item.status)) {
                        item.status = CampaignStatus.EXPIRED
                        update(item)
                    }
                }
            }
            catch {
                case NonFatal(e) => printException(e, System.out)
                case x: Throwable => printException(x, System.out)
            }

            Thread.sleep(30 * 1000)
        }
    }
}