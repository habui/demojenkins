package ads.website.modules

import ads.common.{Reflect, IItem}
import ads.common.database.{IDataService, AbstractDelegateDataService}
import ads.common.model.{ZoneModel, Zone, ZoneToZoneGroup, ZoneGroup}
import java.util
import scala.collection.mutable.ArrayBuffer
import ads.common.services.IZoneService

class ZoneGroupModel(var id: Int, var siteId: Int, var name: String, var zoneCount: Int, var zones: Array[Int], var disable: Boolean = false) extends IItem

object defaultZoneGroupModel{
    def apply() = new ZoneGroupModel(0, 0, "", 0, null)
}

trait IZoneGroupModelService extends IDataService[ZoneGroupModel]{
    def getZones(groupId: Int): Array[ZoneModel]
}

class ZoneGroupModelService
(env: {
    val zoneGroupService: IDataService[ZoneGroup]
    val zoneToZoneGroupService: IDataService[ZoneToZoneGroup]
    val zoneModelService: IDataService[ZoneModel]
    val zoneService: IZoneService
})
    extends AbstractDelegateDataService[ZoneGroup, ZoneGroupModel](env.zoneGroupService)
    with IZoneGroupModelService{

    val linkService = env.zoneToZoneGroupService

    def toModel(item: ZoneGroup): ZoneGroupModel ={

        val zones = linkService.listByReferenceId(id = item.id).data.map(z=>z.zoneId).toArray

        new ZoneGroupModel(item.id, item.siteId, item.name, linkService.countByRef(item.id), zones)
    }


    override def afterSave(model: ZoneGroupModel) {
        val zones = linkService.listByReferenceId(id = model.id).data
        val ids = zones.map(z=>z.zoneId).toList
        if (model.zones != null){
            for (zone <- model.zones){
                if (!ids.contains(zone)) linkService.save(new ZoneToZoneGroup(0,zone,model.id))
            }
        }
        for (zone <- zones){
            if (!model.zones.contains(zone.zoneId)) linkService.remove(zone.id)
        }
    }

    def fromModel(model: ZoneGroupModel): ZoneGroup = {
        new ZoneGroup(model.id, model.siteId, model.name, model.disable)
    }

    def getZones(groupId: Int): Array[ZoneModel] = {
        val list = env.zoneToZoneGroupService.listByReferenceId(id = groupId).data.map(l=>l.zoneId).toList
        val r = env.zoneModelService.listByIds(list)
        return r
    }

    override def search(query: String, pId: Int, isDisable: Boolean, take: Int = 10) = {
        if(pId > 0) {
            loadAll().toList.filter(p=>Reflect.getProperties(p.getClass).get("name").get.get(p).toString.toLowerCase.contains(query.toLowerCase)).toList.filter(p=>(p.siteId == pId && p.disable == isDisable)).take(take).toList
        } else {
            loadAll().toList.filter(p=>Reflect.getProperties(p.getClass).get("name").get.get(p).toString.toLowerCase.contains(query.toLowerCase)).filter(p=>(p.disable == isDisable)).take(take).toList
        }
    }

    override def beforeUpdate(instance: ZoneGroupModel) = {
        val enables = linkService.listByReferenceId(id = instance.id).data.toList
        val disables = linkService.listDisable(0,Int.MaxValue).data.filter(p=>p.zoneGroup == instance.id).toList

        instance.zones.foreach(zid => { //enable item
            disables.find(p=>p.zoneId == zid && p.zoneGroup == instance.id) match {
                case Some(p) => linkService.enable(p.id)
                case None =>{
                    enables.find(p=>p.zoneId == zid) match {
                        case Some(p) => {                            
                        }
                        case None => {
                            linkService.save(new ZoneToZoneGroup(0, zid, instance.id))
                        }
                    }
                }
            }
        })
        
        enables.foreach(p=>{
            if(!instance.zones.contains(p.zoneId)) linkService.disable(p.id)
        })
    }

    override def disable(id: Int) = {
        super.disable(id)
        val links = linkService.listByReferenceId(id=id)
        links.data.foreach(l=>linkService.disable(l.id))
    }

    override def enable(id: Int) = {
        super.enable(id)

        val disableLinks = linkService.listDisable(0,Int.MaxValue).data
        disableLinks.filter(p=>p.zoneGroup == id).foreach(l=>{
            val zone = env.zoneService.load(l.zoneId)
            if(zone != null && !zone.disable) linkService.enable(l.id)
        })
    }
}