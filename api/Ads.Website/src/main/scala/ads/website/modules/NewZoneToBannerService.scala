package ads.website.modules

import scala.collection.mutable.ArrayBuffer
import ads.common.database.{IDataService, PagingResult}
import ads.common.model.{NewBookRecord, ZoneToBanner}

/**
 * Created by stroumphs on 1/8/14.
 */
//trait INewZoneToBannerService {
//    def getLinkedByItem(itemId: Int): ArrayBuffer[Int]
//
//    def listByStatus(status : Int, from: Int, count : Int) : PagingResult[ZoneToBanner]
//
//    def getItemsByZoneId(zoneId: Int) : PagingResult[ZoneToBanner]
//    def getItemsByItemId(itemId : Int) : PagingResult[ZoneToBanner]
//
//    def approve(action : Int, id: Int) : Int
//}
//
//class NewZoneToBannerService(env: {
//    val newBookingService: INewBookingService
//}) extends INewZoneToBannerService{
//    def getLinkedByItem(itemId: Int): ArrayBuffer[Int] = new ArrayBuffer[Int]() ++= env.newBookingService.getLinkedByItem(itemId)
//
//    def listByStatus(status: Int, from: Int, count: Int): PagingResult[ZoneToBanner] = {
//        val data = env.newBookingService.listByStatus(status,from,count)
//        new PagingResult[ZoneToBanner](data.data.map(p=>new ZoneToBanner(p.id,p.zoneId,p.itemId,p.share,p.status)), data.total)
//    }
//
//    def getItemsByZoneId(zoneId: Int): PagingResult[ZoneToBanner] = {
//        val data = env.newBookingService.getItemsByZoneId(zoneId)
//        new PagingResult[ZoneToBanner](data.data.map(p=>new ZoneToBanner(p.id,p.zoneId,p.itemId,p.share,p.status)), data.total)
//    }
//
//    def getItemsByItemId(itemId: Int): PagingResult[ZoneToBanner] = {
//        val data = env.newBookingService.getItemsByItemId(itemId)
//        new PagingResult[ZoneToBanner](data.data.map(p=>new ZoneToBanner(p.id,p.zoneId,p.itemId,p.share,p.status)), data.total)
//    }
//
//    def approve(action: Int, id: Int): Int = env.newBookingService.approve(action, id)
//}
