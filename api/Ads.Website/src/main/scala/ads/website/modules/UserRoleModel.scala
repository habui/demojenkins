package ads.website.modules

/**
 * Created by quangnbh on 10/22/13.
 */

import ads.common.{SecurityContext, IItem}
import ads.common.database.{PagingResult, InMemoryDataService, AbstractDelegateDataService, IDataService}
import ads.common.model._
import ads.web.{Invoke, HandlerContainerServlet}
import ads.website.handler.RestHandler
import ads.web.mvc.{SmartDispatcherHandler, Json, BaseHandler, Text}
import java.util.concurrent.ConcurrentHashMap
import javax.servlet.http.HttpServletRequest
import ads.common.Syntaxs._
import ads.common.services.{IRoleService, ISessionService, IUserService, IUserRoleService}
import java.util
import com.google.gson.internal.LinkedTreeMap
import scala.collection.JavaConverters._
import ads.website.{PermissionUtils, Environment}
import ads.common.Syntaxs.Failure
import ads.common.Syntaxs.Success
import ads.common.Syntaxs.Failure
import ads.common.Syntaxs.Success
import scala.Array
import ads.common.Syntaxs.Failure
import ads.common.Syntaxs.Success

class UserRoleModel(
    var id : Int,
    var name : String,
    var email : String,
    var assignedWebsiteCount : Int = 0,
    var assignedOrderCount : Int = 0,
    var disable: Boolean = false
) extends IItem

object defaultUserRoleModel {
    def apply() = new UserRoleModel(0,"","",0,0)
}


class UserRoleModelService(env : {
    val userRoleService : IDataService[UserRole]

}) extends  AbstractDelegateDataService[UserRole,UserRoleModel](env.userRoleService) {
    def getRoles[T <: IItem](userName : String) : Array[Role] = Array()
    def fromModel(model : UserRoleModel) = {
        new UserRole(model.id,0,"",0,null,model.disable)
    }

    def toModel(ur : UserRole) = {
        new UserRoleModel(0,"","",0,0)
    }

}
class SimpleUserRoleModel(var id : Int, var name : String)

class UserRoleRestHandler(env : {
    val userRoleService : IUserRoleService
    val userModelService : IDataService[UserModel]
    val roleService : IDataService[Role]
    val websiteService : IDataService[Website]
    val orderService : IDataService[Order]
    val userService: IUserService

})  extends SmartDispatcherHandler{

    def beforeSet(request: HttpServletRequest, obj: String, itemIds: Array[Int]) : Try[Unit] = {
        itemIds.foreach(id=>{
            val item = obj match {
                case "website" => env.websiteService.load(id)
                case "order" => env.orderService.load(id)
                case _ => null
            }
            if(item == null) return fail("Item not found")
            if(!PermissionUtils.checkPermission(request,item,obj.toLowerCase match {
                case "website" => Permission.WEBSITE_MANAGE_PERMISSION
                case "order" => Permission.ORDER_EDIT
                case _ => 0
            } )) fail("You don't have permission")
        })
        succeed()
    }


    //set role for website/order (use in assign website/order)
    @Invoke(Parameters = "request,obj,id,data")
    def set(request : HttpServletRequest, obj : String, id : Int, data : Any ): Any = {
        val objName = obj.toLowerCase
        val dt = data.asInstanceOf[util.ArrayList[LinkedTreeMap[String,Any]]]
        if(objName.equals("website") || objName.equals("order")){
            val s = beforeSet(request, objName, Array(id))
            if (!s.isSuccess) return s

            if(dt.size() == 0) {
                val roles = env.userRoleService.findBy(objName,id).getOrElse(null)
                if(roles != null) {
                    roles.foreach(p=>env.userRoleService.remove(p.id))
                }
            }

            val result : Array[Map[Int,String]] = Array.fill(dt.size())(null)

            for(i <- 0 until dt.size()) {
                val obj  : LinkedTreeMap[String,Any]  = dt.get(i)
                val userId : Int = obj.get("userid").asInstanceOf[Double].toInt
                val userRoles : Array[Int]   = obj.get("roles").asInstanceOf[util.ArrayList[Double]].asScala.toList.map(d => d.toInt).toArray;

                env.userRoleService.findBy(objName,userId,id,true) match {
                    case Success(ur) => {
                        var ret : Boolean  = false
                        if(userRoles != null && userRoles.length > 0){
                            ur.roles = userRoles
                            if(ur.disable) ur.disable = false
                            ret =  env.userRoleService.update(ur)
                        }else {
                            env.userRoleService.remove(ur.id)
                            ret  = true
                        }
                        if(ret) result.update(i,Map(userId -> "ok")) else result.update(i,Map(userId -> "failed"))
                    }
                    case Failure(cause) => {
                        if(userRoles != null && userRoles.length > 0){
                            env.userRoleService.save(new UserRole(0,userId,objName,id,userRoles))
                            result.update(i,Map(userId -> "ok"))
                        }
                    }
                }
                Json(result)
            }
        }
    }

    //set role for user(use in Admin)
    @Invoke(Parameters = "request,obj,userId,data")
    def setByUser(request : HttpServletRequest, obj : String, userId : Int, data : Any ): Any = {
        val objName = obj.toLowerCase
        val dt = data.asInstanceOf[util.ArrayList[LinkedTreeMap[String,Any]]]
        if(objName.equals("website") || objName.equals("order")){

            val s = beforeSet(request, objName, dt.toArray.map(d=>{
                val item = d.asInstanceOf[LinkedTreeMap[String,Any]]
                objName match {
                    case "website" => item.get("websiteId").asInstanceOf[Double].toInt
                    case "order" => item.get("orderId").asInstanceOf[Double].toInt
                }
            }))
            if (!s.isSuccess) return s

            val result : Array[Map[Int,String]] = Array.fill(dt.size())(null)

            for(i <- 0 until dt.size()) {
                val obj  : LinkedTreeMap[String,Any]  = dt.get(i)
                var itemId : Int= -1
                if(objName.equals("website")){
                    itemId = obj.get("websiteId").asInstanceOf[Double].toInt
                }
                if(objName.equals("order")){
                    itemId =  obj.get("orderId").asInstanceOf[Double].toInt
                }
                val itemRoles : Array[Int]   = obj.get("roles").asInstanceOf[util.ArrayList[Double]].asScala.toList.map(d => d.toInt).toArray

                env.userRoleService.findBy(objName,userId,itemId) match {
                    case Success(ur) => {
                        ur.roles = itemRoles
                        result.update(i,Map(itemId -> "failed"))
                        if(env.userRoleService.update(ur)){
                            result.update(i,Map(itemId -> "ok"))
                        };
                    }
                    case Failure(cause) => {
                        env.userRoleService.save(new UserRole(0,userId,objName,itemId,itemRoles))
                        result.update(i,Map(userId -> "ok"))
                    }
                }
                Json(result)
            }
        }

    }

    //get role for website/order
    @Invoke(Parameters = "request,obj,id")
    def getAll(request : HttpServletRequest, obj : String, id : Int): Any = {

        val objName = obj.toLowerCase
        if(objName.equals("website") || objName.equals("order")){
            SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request,item, Permission.WEBSITE_MANAGE_PERMISSION))
            env.userRoleService.findBy(objName,id) match {
                case Success(roles) => {
                    val ret  = Array.fill[Any](roles.length)(null)
                    for(i <- 0 until roles.length) {
                        val ur = roles(i)
                        val aMap = new util.HashMap[String, Any]()
                        val userroles = if(ur.roles != null) ur.roles.toList else List()
                        val listRole : Array[Role] = env.roleService.listByIds(userroles)
//                        val roleNames : Array[AnyRef] = listRole.map(r => {
//                            val al = new {val id = r.id; val name = r.name}
//                            al
//                        })
                        val roleNames = listRole.map(r => new SimpleUserRoleModel(r.id,r.name))
                        val u = env.userModelService.load(ur.userId)
                        aMap.put("user",u)
                        aMap.put("roles",roleNames)
                        ret.update(i,aMap)
                    }
                    Json(ret)
                }
                case Failure(cause) => {
                    Text("Not found")
                }
            }
        }else {
            Text("invalid : " + obj)
        }
    }

    //get role for website/order
    @Invoke(Parameters = "request,obj,id,from,count")
    def get(request : HttpServletRequest, obj : String, id : Int,from : Int, count : Int): Any = {
        val objName = obj.toLowerCase
        if(objName.equals("website") || objName.equals("order")){
            SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request,item, Permission.WEBSITE_MANAGE_PERMISSION))
            env.userRoleService.findBy(objName,id,from,count) match {
                case Success(pageResult) => {
                    val roles = pageResult.data
                    if(roles == null){
                        return Json(new PagingResult[Any](Array(),pageResult.total))
                    }
                    val ret  = Array.fill[Any](roles.length)(null)
                    for(i <- 0 until roles.length) {
                        val ur = roles(i)
                        val aMap = new util.HashMap[String, Any]()
                        val listRole : Array[Role] = env.roleService.listByIds(ur.roles.toList)
                        val roleNames = listRole.map(r => new SimpleUserRoleModel(r.id,r.name))
                        val u = env.userModelService.load(ur.userId)
                        aMap.put("user",u)
                        aMap.put("roles",roleNames)
                        ret.update(i,aMap)
                    }
                    Json(new PagingResult[Any](ret,pageResult.total))
                }
                case Failure(cause) => {
                    Json(new PagingResult[Any](new Array[Any](0),0))
//                    Text("Not found")
                }
            }
        }else {
            Text("invalid : " + obj)
        }
    }

    //get role for user
    @Invoke(Parameters = "request,obj,userId,from,count")
    def getByUser(request : HttpServletRequest, obj : String, userId : Int,from : Int, count : Int): Any = {
        val objName = obj.toLowerCase
        if(objName.equals("website") || objName.equals("order")){
            SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request,item, Permission.WEBSITE_VIEW_INFO))
            env.userRoleService.findByUser(objName,userId,from,count) match {
                case Success(pageResult) => {
                    val roles = pageResult.data
                    if(roles == null){
                        return Json(new PagingResult[Any](Array(),pageResult.total))
                    }
                    val ret  = Array.fill[Any](roles.length)(null)
                    for(i <- 0 until roles.length) {
                        val ur = roles(i)
                        val aMap = new util.HashMap[String, Any]()
                        val listRole : Array[Role] = if(ur.roles != null) env.roleService.listByIds(ur.roles.toList) else null
                        val roleNames = if(listRole!=null) listRole.map(r => new SimpleUserRoleModel(r.id,r.name)) else null
                        var item : IItem  = null.asInstanceOf[IItem]
                        if(objName.equals("website")){
                            item = env.websiteService.load(ur.itemId)
                        }
                        if(objName.equals("order")){
                            item  = env.orderService.load(ur.itemId)
                        }
                        aMap.put("item",item)
                        aMap.put("roles",roleNames)
                        ret.update(i,aMap)
                    }
                    Json(new PagingResult[Any](ret,pageResult.total))
                }
                case Failure(cause) => {
                    Text("Not found")
                }
            }
        }else {
            Text("invalid : " + obj)
        }

    }

    def getPermission(user: User, obj: String, id: Int) : Long = {
        if(user == null) return 0
        if(Config.rootUserName.get.split(",").contains(user.name)) {
            return Permission.ROOT_PERMISSION
        }
        val userRole = env.userRoleService.findBy(obj, user.id, id).getOrElse(new UserRole(0,0,"",0,new Array[Int](0)))
        if(userRole.roles == null) userRole.roles = new Array[Int](0)
        val roles = env.roleService.listByIds(userRole.roles.toList).map(r=>r.permissions)
        var rs: Long = 0
        roles.foreach(r=>rs = (rs | r))
        //check owner
        obj match {
            case "website" => {
                val item = env.websiteService.load(id)
                if(item != null && user.id == item.ownerId) {
                    rs = rs | Permission.WEBSITE_ALL_PERMISSIONS
                }
            }
            case "order" => {
                val item = env.orderService.load(id)
                if(item != null && user.id == item.ownerId) {
                    rs = rs | Permission.ORDER_ALL_PERMISSIONS
                }
            }
        }
        rs
    }


    @Invoke(Parameters = "request,obj,id")
    def getCurrentPermission(request:HttpServletRequest, obj: String, id: Int) : Any = {
        val user = PermissionUtils.getUser(request)
        Text("{\"permission\": \""+ getPermission(user, obj, id) +"\"}")

    }

    @Invoke(Parameters = "request,obj,ids")
    def getCurrentPermissions(request:HttpServletRequest, obj: String, ids: Array[Int]) : Any = {
        val user = PermissionUtils.getUser(request)
        var rs = "{\"data\": ["
        ids.foreach(id => {
            val permission = getPermission(user, obj, id)
            rs += "{\"id\":"+ id + ",\"permission\":" + permission + "},"
        })
        if(ids.length > 0) {
            rs = rs.dropRight(1)
        }
        rs += "]}"
        return Text(rs)
    }


    @Invoke(Parameters = "request,obj,id")
    def getByAgency(request: HttpServletRequest, obj: String, id: Int): Any ={
        val objName = obj.toLowerCase
        if(objName.equals("website") || objName.equals("order")){
            SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request,item, Permission.WEBSITE_VIEW_INFO))
            val user = PermissionUtils.getUser(request)
            env.userRoleService.findBy(objName, id) match {
                case Success(roles) => {
                    val rolesAgency = roles.toList.filter((r)=>env.userService.load(r.userId).ownerId == user.id).toArray
                    val ret  = Array.fill[Any](rolesAgency.length)(null)
                    for(i <- 0 until rolesAgency.length) {
                        val ur = rolesAgency(i)
                        val aMap = new util.HashMap[String, Any]()
                        val userroles = if(ur.roles != null) ur.roles.toList else List()
                        val listRole : Array[Role] = env.roleService.listByIds(userroles)
                        val roleNames = listRole.map(r => new SimpleUserRoleModel(r.id,r.name))
                        val u = env.userModelService.load(ur.userId)
                        aMap.put("user",u)
                        aMap.put("roles",roleNames)
                        ret.update(i,aMap)
                    }
                    Json(ret)
                }
                case Failure(cause) => {
                    Text("Not found")
                }
            }
        }else {
            Text("invalid : " + obj)
        }
    }
    //get role website/order for current user
    @Invoke(Parameters = "request,obj,id")
    def getByObject(request: HttpServletRequest, obj: String, id: Int): Any = {
        val objName = obj.toLowerCase
        if(objName.equals("website") || objName.equals("order")){
            val user = PermissionUtils.getUser(request)
            env.userRoleService.findByUser(objName, user.id) match {
                case Success(roles) => {
                    val uroles = roles.filter(r=>r.itemId == id)
                    if (uroles.length == 0) return Json(null)
                    val urole = uroles(0)
                    val userroles = if(urole.roles != null) urole.roles.toList else List()
                    val listRole : Array[Role] = env.roleService.listByIds(userroles)
                    val roleNames = listRole.map(r => new SimpleUserRoleModel(r.id,r.name))
                    Json(roleNames)
                }
                case Failure(cause) => {
                    Text("Not found")
                }
            }
        }else {
            Text("invalid : " + obj)
        }
    }

    @Invoke(Parameters = "request,obj,ids")
    def getByObjectIds(request: HttpServletRequest, obj: String, ids: Array[Int]): Any={
        if (ids == null)
            return fail("null")
        val objName = obj.toLowerCase
        if(objName.equals("website") || objName.equals("order")){
            val user = PermissionUtils.getUser(request)
            env.userRoleService.findByUser(objName, user.id) match {
                case Success(roles) => {
                    val uroles = roles.filter(r=>ids.contains(r.itemId))
                    if (uroles.length == 0) return Json(null)
                    val res = new util.HashMap[Int, Any]()
                    for(i <- 0 until uroles.length){
                        val urole = uroles(i)
                        val userroles = if(urole.roles != null) urole.roles.toList else List()
                        val listRole : Array[Role] = env.roleService.listByIds(userroles)
                        val roleNames = listRole.map(r => new SimpleUserRoleModel(r.id,r.name))
                        res.put(urole.itemId, roleNames)
                    }
                    Json(res)
                }
                case Failure(cause) => {
                    Text("Not found")
                }
            }
        }else {
            Text("invalid : " + obj)
        }
    }
}

class UserRoleServlet() extends HandlerContainerServlet {
    def factory(): BaseHandler = new UserRoleRestHandler(Environment)
}
