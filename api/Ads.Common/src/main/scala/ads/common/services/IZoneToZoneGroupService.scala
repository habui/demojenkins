package ads.common.services

import ads.common.database.{IDataDriver, InMemoryDataService, IDataService}
import ads.common.model._
import scala.concurrent.Future

/**
 * Created by stroumphs on 12/9/13.
 */
//trait IZoneToZoneGroupService extends IDataService[ZoneToZoneGroup]{
//
//}

class ZoneToZoneGroupService (env: {
    val zoneToZoneGroupDataDriver: IDataDriver[ZoneToZoneGroup]
    val actionLogServiceRef : Future[IActionLogService]
})
    extends InMemoryDataService[ZoneToZoneGroup](z=>z.zoneGroup, env.zoneToZoneGroupDataDriver,env.actionLogServiceRef)
//    with IZoneToZoneGroupService {
    {
    override def save(item: ZoneToZoneGroup): Int = {
        var zoneToZoneGroups = loadAll().toList.filter(p=>(p.zoneGroup == item.zoneGroup && p.zoneId == item.zoneId))
        if(zoneToZoneGroups.length > 0) return zoneToZoneGroups(0).id

        zoneToZoneGroups = listDisable(from = 0, count = Int.MaxValue).data.toList
        val existItems = zoneToZoneGroups.find(i=>(i.zoneGroup == item.zoneGroup && i.zoneId == item.zoneId)).toList
        if(existItems.size > 0) {
            existItems.foreach(i=>enable(i.id))
            existItems(0).id
        }
        else super.save(item)
    }
}