package ads.common.services

import ads.common.database.{IDataDriver, InMemoryDataService, PagingResult, IDataService}
import ads.common.model._
import scala.concurrent.Future
import scala.collection.JavaConversions._
import scala.concurrent.ops._
import scala.util.control.NonFatal
import ads.common.Syntaxs._

/**
 * Created by quangnbh on 11/28/13.
 */
trait IZoneService extends IDataService[Zone] {
    def listByRefId(refId : Int, kind : String, from : Int, count : Int) : PagingResult[Zone]
    def listByRefIdAndRunningMode(refId : Int, runningMode: String, from : Int, count : Int) : PagingResult[Zone]
    def listBy(refId : Int, kind : String, runningMode : String,from : Int, count : Int) : PagingResult[Zone]
}

class ZoneService(env : {
    val zoneDataDriver : IDataDriver[Zone]
    val actionLogServiceRef : Future[IActionLogService]
    val zoneToZoneGroupService: IDataService[ZoneToZoneGroup]
    val zoneGroupService: IDataService[ZoneGroup]
    val bannerService : IBannerService
    val newBookServiceRef: Future[IDataService[NewBookRecord]]
}) extends InMemoryDataService[Zone](z=>z.siteId,env.zoneDataDriver, env.actionLogServiceRef) with IZoneService {
    lazy val newBookService = env.newBookServiceRef.value.get.get

    def listByRefId(refId : Int, kind : String, from : Int, count : Int) : PagingResult[Zone] = {
        val list = if(refId > 0) listByReferenceId(id = refId) else new PagingResult[Zone](all.values().toIterator.toArray,all.values().size())
        if(list.data != null) {
            val ret = list.data.filter(p=> p.kind.toLowerCase.equals(kind.toLowerCase))
            new PagingResult[Zone](ret.slice(from,count+from),ret.size)
        }else {
            new PagingResult[Zone](list.data,list.total)
        }
    }
    def listBy(refId : Int, kind : String, runningMode : String,from : Int, count : Int) : PagingResult[Zone] =  {
        var arr = if(refId > 0)  listByReferenceId(id= refId).data else all.valuesIterator.toArray
        arr = if(kind!=null || kind.equals("")) arr.filter(z=> z.kind.toLowerCase.equals(kind.toLowerCase)) else arr
        arr = if(runningMode != null && runningMode.equals("")) arr.filter(z => z.runningMode.contains(runningMode)) else arr

        new PagingResult[Zone](arr.slice(from,from + count),arr.size)
    }
    def listByRefIdAndRunningMode(refId : Int, runningMode: String, from : Int, count : Int) : PagingResult[Zone] = {
        val list = listByReferenceId(id= refId)
        if(list.data != null){
           val ret =  runningMode.toLowerCase match {
                case "booking" => {
                    val l = list.data.filter(z => z.runningMode.contains("booking"))
                    if(l!= null){
                        new PagingResult[Zone](l.slice(from,from+count),l.size)
                    }else{
                        new PagingResult[Zone](null,0)
                    }
                }
                case "network" => {
                    val l = list.data.filter(z => z.runningMode != null && z.runningMode.contains("network"))
                    if(l!= null){
                        new PagingResult[Zone](l.slice(from,from+count),l.size)
                    }else{
                        new PagingResult[Zone](null,0)
                    }
                }
                case "tvc" => {
                    val l = list.data.filter(z=> z.kind.equals(ZoneKind.Video))
                    if(l!= null){
                        new PagingResult[Zone](l.slice(from,from+count),l.size)
                    }else{
                        new PagingResult[Zone](null,0)
                    }
                }
                case "banner" => {
                    new PagingResult[Zone](null,0)
                }
                case _ => {
                    new PagingResult[Zone](null,0)
                }
            }
            ret
        }else {
            list
        }
    }

    override def disable(id: Int) {
        super.disable(id)
        val links = env.zoneToZoneGroupService.list(0, Int.MaxValue).data
        links.filter(p=>p.zoneId == id).foreach(p=>env.zoneToZoneGroupService.disable(p.id))

        val itemLinks = newBookService.list(0, Int.MaxValue).data
        itemLinks.filter(p=>p.zoneId == id).foreach(p=>newBookService.disable(p.id))
    }

    override def enable(id: Int) = {
        super.enable(id)
        val disableLinks = env.zoneToZoneGroupService.listDisable(0,Int.MaxValue).data
        disableLinks.filter(p=>p.zoneId == id).foreach(l=>{
            val zoneGroup = env.zoneGroupService.load(l.zoneGroup)
            if(zoneGroup != null && !zoneGroup.disable) env.zoneToZoneGroupService.enable(l.id)
        })

        val disableItemLinks = newBookService.listDisable(0,Int.MaxValue).data
        disableItemLinks.filter(p=>p.zoneId == id).foreach(l=>{
            val item = env.bannerService.load(l.itemId)
            if(item != null && !item.disable) newBookService.enable(l.id)
        })
    }
}

trait IZone2Service extends IDataService[Zone]{
}

class Zone2Service(env:{
    val zone2DataDriver : IDataDriver[Zone]
    val actionLogServiceRef : Future[IActionLogService]
}) extends InMemoryDataService[Zone](b=>b.siteId, env.zone2DataDriver,env.actionLogServiceRef) with IZone2Service{
    spawn {
        while(true) {
            Thread.sleep(30 * 1000)
            try {
                //            println("-------------Update zone2Service------------")
                for (item <- env.zone2DataDriver.loadAll()) {
                    if (all.containsKey(item.id)){
                        val x = all.get(item.id)
                        all.remove(item.id)
                        removeIndex(x)
                    }

                    all.put(item.id, item)
                    index(item)
                }
                //            println("---------------------End--------------------")
            }
            catch {
                case NonFatal(e) => printException(e, System.out)
                case x: Throwable => printException(x, System.out)
            }
        }
    }
}
