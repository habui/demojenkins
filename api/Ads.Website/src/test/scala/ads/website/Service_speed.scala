package ads.website

import org.scalatest.FunSuite
import ads.common.services.{ZoneToBannerService, IZoneToBannerService, UserRoleService}
import ads.common.model.{ZoneGroup, UserRole}
import ads.common.Syntaxs._
import com.google.gson.Gson
import java.util
import ads.website.modules.{WebsiteModelService, IWebsiteModelService}

/**
 * Created by quangnbh on 11/5/13.
 */


object Test {
    def calculateTime( action: () => Any, tag : String) = {
        val start = System.nanoTime()
        action()
        val end = System.nanoTime();
        println("[" + tag + "] -> " +  (end - start + 0.0)/1000000  + " milli seconds")
    }
}
/*
class Service_speed extends FunSuite{
    test("query time"){
        val uService = new UserRoleService(Environment)
        Test.calculateTime(() => {
            val lsit = uService.findBy("website",10)
        },"FindBy1")

        Test.calculateTime( () => {
           uService.findBy("website",8,10)
        },"FindBy2a")
        Test.calculateTime( () => {
            uService.findBy("website",8,10)
        },"FindBy2b")

        Test.calculateTime(()=>{
            uService.findByUser("website",2)
        },"FindBy3")
    }

    test("approved ads model"){

    }
    test("Gson"){
        val gson = new Gson
        val map = Map(new ZoneGroup(1,1,"a")->4,6->5)
        val map2 = new util.HashMap[Object,Int]()
        map2.put(new ZoneGroup(1,1,"a"),4)
        println(gson.toJson(map2))

        val ret  = gson.fromJson(gson.toJson(map2),classOf[Object])
        println(ret)
    }
}

class WebsiteModelServiceTest extends FunSuite {
    val service : IWebsiteModelService = new WebsiteModelService(Environment)
    test("speed"){
        Test.calculateTime(()=>{
            service.listAllContainAds(0)
        },"listAdsWebsite")
        Test.calculateTime(()=>{
            val ret = service.listAllApproveAds(0)
            //            ret.slice(0,Int.MaxValue)
        },"listAllApproveAds")
        Test.calculateTime(()=>{
            service.listAds(0,0,Int.MaxValue)
        },"listAdsByStatus")

        Test.calculateTime(()=>{
            service.list(0,Int.MaxValue)
        },"listAllWebsiteModel")
    }
}

class ZoneToBannerServiceTest extends FunSuite {
    val service : IZoneToBannerService = new ZoneToBannerService(Environment)
    test("speed test"){
        Test.calculateTime(()=>{
            service.listByStatus(0,0,Int.MaxValue)
        },"listByStatus")
    }

}
*/