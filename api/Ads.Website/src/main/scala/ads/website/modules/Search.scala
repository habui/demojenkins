package ads.website.modules

import ads.web.{Invoke, HandlerContainerServlet}
import ads.web.mvc.{Json, SmartDispatcherHandler, BaseHandler}
import ads.website.{PermissionUtils, Environment}
import ads.common.database.IDataService
import ads.common.model.{Permission, CampaignModel}
import scala.collection.mutable.ArrayBuffer
import ads.website.modules.serving.InfoKind
import ads.common.{SecurityUtils, SecurityContext}
import org.apache.http.HttpRequest
import javax.servlet.http.HttpServletRequest

class SearchModel(var id: Int, var name: String, var kind: String)

class SearchRestHandler(env: {
    val websiteModelService: IWebsiteModelService
    val zoneModelService: IZoneModelService
    val zoneGroupModelService: IZoneGroupModelService
    val orderModelService: IOrderModelService
    val campaignModelService: IDataService[CampaignModel]
    val bannerModelService: IBannerModelService
})
extends SmartDispatcherHandler  {
    @Invoke(Parameters = "request,key")
    def search(request: HttpServletRequest, key: String) = {

        val result = new ArrayBuffer[SearchModel]()
        SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request,item, Permission.WEBSITE_VIEW_INFO))
        env.websiteModelService.search(key, 0, false, Int.MaxValue).map(i => result += new SearchModel(i.id, i.name, InfoKind.Website))
        env.zoneModelService.search(key, 0, false, Int.MaxValue).map(i => result += new SearchModel(i.id, i.name, InfoKind.Zone))
        env.zoneGroupModelService.search(key, 0, false, Int.MaxValue).map(i => result += new SearchModel(i.id, i.name, InfoKind.ZoneGroup))
        SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request,item, Permission.ORDER_VIEW_INFO))
        env.orderModelService.search(key, 0, false, Int.MaxValue).map(i => result += new SearchModel(i.id, i.name, InfoKind.Order))
        env.campaignModelService.search(key, 0, false, Int.MaxValue).map(i => result += new SearchModel(i.id, i.name, InfoKind.Campaign))
        env.bannerModelService.search(key, 0, false, Int.MaxValue).map(i => result += new SearchModel(i.id, i.name, InfoKind.Item))

        if (key.length > 2 && key.charAt(0) == '@') {
            try {
                val id = key.substring(2)
                key.charAt(1) match {
                    case 'w' => result += new SearchModel(id.toInt, env.websiteModelService.load(id.toInt).name, InfoKind.Website)
                    case 'z' => result += new SearchModel(id.toInt, env.zoneModelService.load(id.toInt).name, InfoKind.Zone)
                    case 'g' => result += new SearchModel(id.toInt, env.zoneGroupModelService.load(id.toInt).name, InfoKind.ZoneGroup)
                    case 'o' => result += new SearchModel(id.toInt, env.orderModelService.load(id.toInt).name, InfoKind.Order)
                    case 'c' => result += new SearchModel(id.toInt, env.campaignModelService.load(id.toInt).name, InfoKind.Campaign)
                    case 'i' => result += new SearchModel(id.toInt, env.bannerModelService.load(id.toInt).name, InfoKind.Item)
                    case 'x' => {
                        val decodeId = SecurityUtils.decode(id)
                        result += new SearchModel(decodeId, env.zoneModelService.load(decodeId).name, InfoKind.Zone)
                    }
                }
            }
        }
        Json(result)
    }

}

class SearchServlet extends HandlerContainerServlet {
    def factory(): BaseHandler = new SearchRestHandler(Environment)
}