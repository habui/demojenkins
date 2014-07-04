package ads.website.modules

import ads.website.handler.RestHandler
import ads.common.model._
import ads.common.database.{AbstractDelegateDataService, PagingResult, IDataService}
import ads.web.{WebUtils, Invoke, HandlerContainerServlet}
import ads.web.mvc.{Text, Action, BaseHandler, Json}
import javax.servlet.http.HttpServletRequest
import ads.common.Syntaxs._
import ads.common.services.{IUserRoleService, IUserService}
import ads.common.{IItem}
import java.util
import ads.website.{PermissionUtils, Environment}
import ads.common.sessions.Session
import scala.collection.mutable


class UserModel(var id : Int,
                var ownerId : Int,
               var name : String,
               var email :String,
               var password : String,
               var assignedWebsiteCount : Int,
               var assignedOrderCount : Int,
               var disable: Boolean = false
) extends IItem
class UserModelService (env:{
    val userService : IUserService
    val userRoleService : IUserRoleService
})
extends AbstractDelegateDataService[User,UserModel](env.userService){
    def toModel(item: User): UserModel = {
        val assignedWebsiteCount = env.userRoleService.countBy(classOf[Website].getSimpleName.toLowerCase,item.id)
        val assignedOrderCount = env.userRoleService.countBy(classOf[Order].getSimpleName.toLowerCase,item.id)
        new UserModel(item.id, item.ownerId,item.name,item.email,"",assignedWebsiteCount,assignedOrderCount)
        //new UserModel(item.id, item.ownerId,item.name,item.email,item.password,assignedWebsiteCount,assignedOrderCount)
    }

    def fromModel(model: UserModel): User = {
        new User(model.id,model.ownerId,model.name,model.email,model.password, model.disable)
    }
}
class UserHandler (env: {
    val userService: IUserService
    val userModelService : IDataService[UserModel]
    val userRoleService : IUserRoleService
    val websiteService : IDataService[Website]
    val orderService : IDataService[Order]
})
    extends RestHandler[UserModel,User](()=>new UserModel(0,0,null,null,null,0,0), env.userModelService, env.userService){

    override def getItemFromModel(item: UserModel) : Any = env.userService.load(item.id)

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
    override def beforeSave(request: HttpServletRequest, instance: UserModel): Try[Unit] = {
        if(!PermissionUtils.checkPermission(request, new User(0, instance.ownerId, instance.name, instance.email, instance.password), Permission.ROOT_PERMISSION)) {
            return fail("You don't have permission")
        }
        if (instance.name == null || instance.name.equals("") || instance.password == null || instance.password.equals(""))
            return fail("User name or password can't be empty!")

        if (env.userService.findByName(instance.name) != null  || Config.rootUserName.get.split(",").contains(instance.name))
            return fail("User " + instance.name+ " already exist")

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

    @Invoke(Parameters = "request,apikey,session")
    def getCapOfCookie(request: HttpServletRequest, apikey: String,session:String) : Any = {
        if(apikey != "a14cada9-8418-4f3f-a06b-51646e732d01") return Json(null)
        val user = PermissionUtils.getUser(request)
        if(user == null || user.id == 0) return Json(null)
        val rs = if(session != null && session.length > 0) {
            val data = Session.store.get(session)
            if(data == null) null
            else {
                val map = new util.HashMap[Int, Array[Int]]
                val times = (data.logs zip data.logs.drop(1)).zipWithIndex.filter(_._2 % 2 == 0).map(_._1).toList
                times.foreach(c => {
                    if(!map.containsKey(c._1)) {
                        map.put(c._1, times.filter(d => d._1 == c._1).map(c=>c._2).toArray)
                    }
                })
                map
            }
        } else {
            null
        }
        Json(rs)
    }
}

class UserServlet extends HandlerContainerServlet {
    def factory(): BaseHandler = new UserHandler(Environment)
}
