package ads.website.modules

import ads.common.{IItem}
import ads.common.database.{AbstractDelegateDataService,IDataService}
import ads.common.model.{Order, Role, Permission, Website}
import ads.web.mvc.{Json, Text, BaseHandler}
import ads.website.handler.RestHandler
import ads.web.{Invoke, HandlerContainerServlet}
import ads.common.services.IRoleService
import ads.website.Environment

/**
 * Created by quangnbh on 10/22/13.
 */
class RoleModel(    var id : Int,
                    var name : String,
                    var description : String,
                    var disable: Boolean = false
                   )    extends IItem

object defaultRoleModel  {
    def apply() = new RoleModel(0,"","")
}

trait IRoleModelService extends IDataService[RoleModel]{
    def init () : Any
    def get(name : String , objName : String) : Role

}

class RoleModelService (env : {
    val roleService : IDataService[Role]
}) extends AbstractDelegateDataService[Role,RoleModel](env.roleService) {

    def fromModel(model : RoleModel) : Role =
        new Role(model.id, model.name, model.description,0,"",model.disable)

    def toModel(r : Role) : RoleModel =
        new RoleModel(r.id,r.name,r.description)
}


class RoleRestHandler(env : {
    val roleModelService : IDataService[RoleModel]
    val roleService : IRoleService
}) extends RestHandler[RoleModel,Role](() => defaultRoleModel(),env.roleModelService,env.roleService) {
    @Invoke
    def init () : Any = {
        val rs = env.roleService ;
        val r: Role = rs.load(1)
        if(r == null){
            val websiteName : String = classOf[Website].getSimpleName.toLowerCase
            rs.save(
                new Role(1,"VIEWER",
                    "Can view all info of the assigned website (Name, zones, zone groups, linked item)",
                    Permission.WEBSITE_VIEW_INFO,websiteName
                ));
            rs.save(
                new Role(2,"REPORTER",
                    "Can view reports, and configuration data...",
                    Permission.WEBSITE_VIEW_INFO | Permission.WEBSITE_REPORT,websiteName)
            );
            rs.save(
                new Role(3,"SALE MAN",
                    ": Can book any available zones on the assigned website...",
                    Permission.WEBSITE_VIEW_INFO | Permission.WEBSITE_BOOKING,websiteName)
            );
            rs.save(
                new Role(4,"APPROVAL",
                    "Can approve any waiting ads on the assigned website...",
                    Permission.WEBSITE_VIEW_INFO | Permission.WEBSITE_APPROVE,websiteName)
            );
            rs.save(
                new Role(5,"ADMIN",
                    "Admin of website...",
                    Permission.WEBSITE_ALL_PERMISSIONS ^ Permission.WEBSITE_OWN,websiteName)
            );
            rs.save(
                new Role(6,"OWNER",
                    "Owner of website...",
                    Permission.WEBSITE_ALL_PERMISSIONS ,websiteName)
            );

            rs.save(
                new Role(7,"REPORTER",
                    "order reporter...",
                    Permission.ORDER_VIEW_INFO | Permission.ORDER_REPORT ,classOf[Order].getSimpleName().toLowerCase)
            );
            rs.save(
                new Role(8,"STANDARD USER",
                    "order standard user...",
                    Permission.ORDER_ALL_PERMISSIONS ,classOf[Order].getSimpleName().toLowerCase)
            );
            Text("{\"result\": \"success\"}")
        }else {
            Text("{\"result\": \"failed\",\"message\" : \"Already init\"}")
        }
    }

    @Invoke(Parameters = "obj")
    def listByObject(obj : String): Any = {
        Json(env.roleService.listByObject(obj))
    }
}

class RoleServlet extends HandlerContainerServlet {
    def factory() = new RoleRestHandler(Environment)
}