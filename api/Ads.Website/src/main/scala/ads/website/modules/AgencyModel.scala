package ads.website.modules

import ads.common.model._
import ads.common.database.{PagingResult, AbstractDelegateDataService, IDataService}
import ads.web.{Invoke, HandlerContainerServlet}
import ads.web.mvc.{Text, Json, BaseHandler}
import ads.website.handler.RestHandler
import ads.website.{PermissionUtils, Environment}
import ads.common.services._
import javax.servlet.http.HttpServletRequest
import ads.common.{UserContext, SecurityContext}
import ads.common.Syntaxs.{Failure, Success, fail, Try}
import java.util

class AgencyModel(id:Int,
                  name:String,
                  email:String,
                  password:String,
                  ownerId: Int,
                  assignedWebsiteCount : Int,
                  assignedOrderCount : Int,
                  disable: Boolean = false
) extends UserModel(id, ownerId, name, email, password, assignedWebsiteCount, assignedOrderCount, disable)

object defaultAgencyModel {
    def apply() = new AgencyModel(0,"","","",0,0,0)
}

trait IAgencyModelService extends IDataService[UserModel]{
    def listByUser(userId: Int,from: Int, count: Int, sortBy: String, direction: String): PagingResult[UserModel]

}
class AgencyModelService(env:{
    val userRoleService: IUserRoleService
    val userService: IUserService
}) extends AbstractDelegateDataService[User, UserModel](env.userService) with IAgencyModelService{
    def toModel(item:User):UserModel = {
        val assignedWebsiteCount = env.userRoleService.countBy(classOf[Website].getSimpleName.toLowerCase,item.id)
        val assignedOrderCount = env.userRoleService.countBy(classOf[Order].getSimpleName.toLowerCase,item.id)
        new UserModel(item.id,item.ownerId,item.name,item.email,item.password,assignedWebsiteCount,assignedOrderCount)
    }
    def fromModel(item:UserModel):User = {
        new User(item.id,0,item.name,item.password,item.email)
    }
    def listByUser(userId: Int, from: Int, count: Int, sortBy: String, direction: String): PagingResult[UserModel] = {
        var all = env.userService.list(0, Int.MaxValue, sortBy, direction).data.toList.map(u=>toModel(u))
        all = all.filter(p=>p.ownerId == userId)
        val result = all.slice(from, from + count)
        new PagingResult[UserModel](result.toArray, all.size)
    }
}

class AgencyRestHandler(env:{val agencyModelService: IAgencyModelService
    val userModelService : IDataService[UserModel]
    val userRoleService: IUserRoleService
    val userService: IUserService
}) extends RestHandler[UserModel, User](()=> defaultAgencyModel(), env.userModelService, env.userService) {

    override def getItemFromModel(item: UserModel) : Any = env.userService.load(item.id)

    @Invoke(Parameters = "request")
    override def save(request: HttpServletRequest): Any = {
        val instance = readInstance(request)
        val s = beforeSave(request, instance)
        if (!s.isSuccess) return s
        instance.ownerId = PermissionUtils.getUser(request).id
        instance.id = modelService.save(instance)
        afterSave(instance)
        Json(instance)
    }

    @Invoke(Parameters = "request,id")
    override def update(request: HttpServletRequest, id: Int): Any = {
        val old = modelService.load(id)
        if (old != null) {
            val instance = readFromRequest[UserModel](request, old)
            val s = beforeUpdate(request, instance)
            if (!s.isSuccess) return s
            if(instance.password == null || instance.password.equals("")){
                instance.password = old.password
            }
            modelService.update(instance)
            afterUpdate(old, instance)
            Text("{\"result\": \"success\"}")
        }
        else {
            Text("{\"result\":\"error\", \"message\": \"not found!\"}")
        }
    }


    override def beforeSave(request: HttpServletRequest, instance: UserModel): Try[Unit]={
        if (instance.name == null || instance.name.equals("") || instance.password == null || instance.password.equals(""))
            return fail("User name or password can't be empty!")

        if (env.userService.findByName(instance.name) != null  || Config.rootUserName.get.split(",").contains(instance.name))
            return fail("{\"result\" : \"failed\", \"message\" : \"User '" + instance.name+ " already exist\"}")

        super.beforeSave(request, instance)
    }

    override def beforeUpdate(request: HttpServletRequest,instance: UserModel): Try[Unit] = {
        if (instance.name == null || instance.name.equals("") || instance.password == null || instance.password.equals("")) return fail("User name or password can't be empty!")
        val old = env.userService.load((instance.id))
        if(old.name != instance.name) return fail("Can't change username")
        val u = env.userService.findByName(instance.name)
        if (u != null && u.id != instance.id) return fail(s"User '${instance.name}' already exist!")
        instance.password = instance.password.hashCode().toString
        super.beforeUpdate(request, instance)
    }

    def readInstance(request: HttpServletRequest): AgencyModel = {
        readFromRequest[AgencyModel](request, new AgencyModel(0,"","","",0,0,0))
    }

    @Invoke(Parameters = "request,from,count")
    override def list(request: HttpServletRequest, from: Int, count: Int) = {
        SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request,item, Permission.WEBSITE_VIEW_INFO))
        var sortBy = request.getParameter("sortBy")
        var direction = request.getParameter("dir")
        if(sortBy != null && sortBy.length > 0){
            if(direction != "asc") direction = "desc"
        }
        else if (direction == "asc") sortBy = "id"
        else {
            sortBy = "id"
            direction = "desc"
        }

        val user = PermissionUtils.getUser(request)
        val data = env.agencyModelService.listByUser(user.id, from, count, sortBy, direction)
        Json(data)
    }

    @Invoke(Parameters = "request,obj,ids")
    def countAssigned(request: HttpServletRequest, obj: String, ids: Array[Int]) = {
        val rs: Array[Map[Int, Int]] = Array.fill(ids.length)(null)
        val objName = obj.toLowerCase
        if(objName.equals("website") || objName.equals("order")){
            SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request,item, Permission.WEBSITE_VIEW_INFO))
            val user = PermissionUtils.getUser(request)
            for(i <- 0 until ids.length){
                env.userRoleService.findBy(objName, ids(i)) match {
                    case Success(roles) => {
                        val rolesAgency = roles.toList.filter(r=>env.userService.load(r.userId).ownerId == user.id).toArray
                        rs.update(i, Map(ids(i) -> rolesAgency.length))
                    }
                    case Failure(cause) => {
                        rs.update(i, Map(ids(i) -> 0))
                    }
                }
            }
            Json(rs)
        }else {
            Text("invalid : " + obj)
        }
    }
}
class AgencyServlet extends HandlerContainerServlet {
    def factory():BaseHandler = new AgencyRestHandler(Environment)
}
