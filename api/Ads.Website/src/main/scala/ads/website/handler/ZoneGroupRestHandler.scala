package ads.website.handler

import ads.website.modules.{IZoneGroupModelService, defaultZoneGroupModel, ZoneGroupModel}
import ads.web.{HandlerContainerServlet, Invoke}
import ads.web.mvc.{BaseHandler, Json}
import ads.website.{PermissionUtils, Environment}
import ads.common.model.{ZoneGroup, Website, Permission, ZoneToZoneGroup}
import ads.common.database.IDataService
import ads.common.Syntaxs.{succeed, fail, Try}
import javax.servlet.http.HttpServletRequest
import ads.common.SecurityContext


class ZoneGroupRestHandler(env: {
    val zoneGroupService: IDataService[ZoneGroup]
    val zoneGroupModelService: IZoneGroupModelService
    val websiteService: IDataService[Website]
}) extends RestHandler[ZoneGroupModel, ZoneGroup](() => defaultZoneGroupModel(), env.zoneGroupModelService, env.zoneGroupService) {

    override def getItemFromModel(item: ZoneGroupModel) : Any = env.zoneGroupService.load(item.id)

    override def beforeSave(request: HttpServletRequest, instance: ZoneGroupModel): Try[Unit] = {
	if (instance.name == null || instance.name.equals(""))
            return fail("Name is null")
        if(PermissionUtils.checkPermission(request, env.websiteService.load(instance.siteId), Permission.WEBSITE_EDIT)) succeed()
        else fail("You don't have permission")
    }

    override def beforeUpdate(request: HttpServletRequest, instance: ZoneGroupModel): Try[Unit] = {
        if (instance.name == null || instance.name.equals(""))
            return fail("Name is null")
        super.beforeUpdate(request, instance)
    }
    @Invoke(Parameters = "request,id")
    def getZones(request: HttpServletRequest,id: Int) = {
        SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request,item, Permission.WEBSITE_VIEW_INFO))
        Json(env.zoneGroupModelService.getZones(id))
    }
}

class ZoneGroupServlet extends HandlerContainerServlet {
    def factory(): BaseHandler = new ZoneGroupRestHandler(Environment)
}
