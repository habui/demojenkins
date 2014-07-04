package ads.website

import javax.servlet.http.HttpServletRequest
import ads.common.model._
import ads.common.database.AbstractDelegateDataService
import ads.web.mvc.Text
import ads.common.{BypassSecurityContext, UserContext}
import ads.web.WebUtils
import ads.website.modules.{UserModel, AgencyModel, WebsiteModel}

object PermissionUtils{
    lazy val websiteService = Environment.websiteService
    lazy val userRoleService = Environment.userRoleService
    lazy val campaignService = Environment.campaignService
    lazy val orderService = Environment.orderService
    lazy val roleService = Environment.roleService
    lazy val sessionService = Environment.sessionService
    lazy val userService = Environment.userService

    val editFunction = Array("a")
    val viewFunction = Array("list")
    val approveFunction = Array("c")

    def checkRoles(roles : Array[Long], permission: Long) : Boolean = roles.map(r=>r&permission).sum > 0

    def getUser(request: HttpServletRequest) : User = {
        val sessionId = request.getHeader("X-sessionId")
        if(sessionId == null) {
            val user = UserContext.get()
            if(user != null) return user
            return null.asInstanceOf[User]
        }
        sessionService.getSession(sessionId).tryGet(session => session.user).getOrElse(null)
    }

    def checkPermission(request: HttpServletRequest, instance: Any, inputPermission: Long) : Boolean = {
        var permission = inputPermission
        if(BypassSecurityContext.get()) return true
        val user = getUser(request)

        if(user == null) return false
        if(instance == null) return false

        //check permission before save
        instance match {
            case p @ (_: WebsiteModel | _: AgencyModel | _: UserModel) => {
                permission match {
                    case Permission.WEBSITE_CREATE => return user.ownerId == 0
                    case _ => true
                }
            }
            case _ =>
        }


        if(Config.rootUserName.get.split(",").contains(user.name)) {
            return true
        }

        val value = instance match {
            case p : ZoneGroup => {
                val item = instance.asInstanceOf[ZoneGroup]
                if(item == null) null.asInstanceOf[Website]
                else {
                    BypassSecurityContext.set()
                    val temp = websiteService.load(item.siteId)
                    BypassSecurityContext.remove()
                    temp
                }
            }
            case p : Zone => {
                val item = instance.asInstanceOf[Zone]
                if(item == null) null.asInstanceOf[Website]
                else{
                    BypassSecurityContext.set()
                    val temp = websiteService.load(item.siteId)
                    BypassSecurityContext.remove()
                    temp
                }
            }
            case p : Website => {
                instance.asInstanceOf[Website]
            }
            case p : Order => {
                instance.asInstanceOf[Order]
            }
            case p : Banner => {
                val item = instance.asInstanceOf[Banner]
                if(item == null) null.asInstanceOf[Order]
                    
                else {
                    var temp = null.asInstanceOf[Order]
                    BypassSecurityContext.set()
                    val campaignItem = campaignService.load(item.campaignId)
                    if(campaignItem == null) null.asInstanceOf[Order]
                    else temp = orderService.load(campaignItem.orderId)
                    BypassSecurityContext.remove()
                    temp
                }
            }
//            case p : Article => {
//                val item = instance.asInstanceOf[Article]
//                if(item == null) null.asInstanceOf[Order]
//
//                else {
//                    BypassSecurityContext.set()
//                    //check website permission
//                    val website =  websiteService.load(item.websiteId)
//                    if(website != null) return false
//                    if(user.id == website.ownerId) return true
//                    val userRole = userRoleService.findBy("website", user.id, website.id).getOrElse(new UserRole(0,0,"",0,new Array[Int](0)))
//                    if(userRole.roles == null) userRole.roles = new Array[Int](0)
//                    val newPermission = permission match {
//                        case Permission.ORDER_VIEW_INFO => Permission.WEBSITE_VIEW_INFO
//                        case Permission.ORDER_EDIT => Permission.WEBSITE_PR_EDITOR
//                        case _ => permission
//                    }
//                    if(PermissionUtils.checkRoles(userRole.roles.map(r=>roleService.load(r).permissions), newPermission)) {
//                        BypassSecurityContext.remove()
//                        return true
//                    }
//
//                    //get order when don't have website permission
//                    var order = null.asInstanceOf[Order]
//                    val campaignItem = campaignService.load(item.campaignId)
//                    if(campaignItem == null) null.asInstanceOf[Order]
//                    else order = orderService.load(campaignItem.orderId)
//                    BypassSecurityContext.remove()
//                    order
//                }
//            }
            case p : Campaign => {
                val item = instance.asInstanceOf[Campaign]
                if(item == null) null.asInstanceOf[Order]
                else {
                    BypassSecurityContext.set()
                    val temp = orderService.load(item.orderId)
                    BypassSecurityContext.remove()
                    temp
                }
            }
            case p : User => instance.asInstanceOf[User]
            case p : UserRole => instance.asInstanceOf[UserRole]
            case p : ActionLog => return false
            case p : Agency => instance.asInstanceOf[Agency]
            case _ => {
                return true
            }
        }

        value match {
            case null => return false
            case p : Website => {
                if(user.id == value.asInstanceOf[Website].ownerId) return true
                val userRole = userRoleService.findBy("website", user.id, value.asInstanceOf[Website].id).getOrElse(new UserRole(0,0,"",0,new Array[Int](0)))
                if(userRole.roles == null) userRole.roles = new Array[Int](0)
                val newPermission = permission match {
                    case Permission.ORDER_VIEW_INFO => Permission.WEBSITE_VIEW_INFO
                    case Permission.ORDER_EDIT => Permission.WEBSITE_EDIT
                    case Permission.ORDER_REPORT => Permission.WEBSITE_REPORT
                    case Permission.WEBSITE_CREATE => return true
                    case _ => permission
                }
                PermissionUtils.checkRoles(userRole.roles.map(r=>roleService.load(r).permissions), newPermission)
            }
            case p : Order => {
                if(user.id == value.asInstanceOf[Order].ownerId) return true
                val userRole = userRoleService.findBy("order", user.id, value.asInstanceOf[Order].id).getOrElse(new UserRole(0,0,"",0,new Array[Int](0)))
                if(userRole.roles == null) userRole.roles = new Array[Int](0)
                val newPermission = permission match {
                    case Permission.WEBSITE_VIEW_INFO => Permission.ORDER_VIEW_INFO
                    case Permission.WEBSITE_EDIT => Permission.ORDER_EDIT
                    case Permission.WEBSITE_REPORT => Permission.ORDER_REPORT
                    case Permission.WEBSITE_APPROVE => return true
                    case Permission.WEBSITE_BOOKING => Permission.ORDER_EDIT
                    case Permission.WEBSITE_CREATE => return true
                    case _ => permission
                }
                PermissionUtils.checkRoles(userRole.roles.map(r=>roleService.load(r).permissions), newPermission) ||
                {
                    if(instance.isInstanceOf[Banner] && instance.asInstanceOf[Banner].kind == BannerKind.Article) {
                        val banner = instance.asInstanceOf[Banner]
                        val model = WebUtils.fromJson(classOf[ArticleModel], banner.extra)

                        val website = websiteService.load(model.websiteId)
                        if(website != null && website.ownerId == user.id) true
                        else {
                            val userRole = userRoleService.findBy("website", user.id, model.websiteId).getOrElse(new UserRole(0,0,"",0,new Array[Int](0)))
                            if(userRole.roles == null) userRole.roles = new Array[Int](0)
                            PermissionUtils.checkRoles(userRole.roles.map(r=>roleService.load(r).permissions), newPermission)
                        }
                    } else false
                }
            }

            case p : UserRole => {
                if(p.objName.toLowerCase == "website") {
                    val item = websiteService.load(p.itemId)
                    if(item == null) return false
                    if(user.id == item.ownerId) return true
                    if(p.userId == user.id) return false

                    val victimRoles = userRoleService.findBy("website", p.userId, p.itemId).getOrElse(new UserRole(0,0,"",0,new Array[Int](0)))
                    if(victimRoles.roles.contains(5) && victimRoles.userId == user.id) return false

                    val userRole = userRoleService.findBy("website", user.id, p.itemId).getOrElse(new UserRole(0,0,"",0,new Array[Int](0)))
                    val per = PermissionUtils.checkRoles(userRole.roles.map(r=>roleService.load(r).permissions), Permission.WEBSITE_VIEW_INFO)
                    per
                }
                else if(p.objName.toLowerCase == "order") {
                    val item = orderService.load(p.itemId)
                    if(item == null) return false
                    if(user.id == item.ownerId) return true
                    if(p.userId == user.id) return false

                    val victimRoles = userRoleService.findBy("order", p.userId, p.itemId).getOrElse(new UserRole(0,0,"",0,new Array[Int](0)))
                    if(victimRoles.roles.contains(8) && victimRoles.userId == user.id) return false

                    val userRole = userRoleService.findBy("order", user.id, p.itemId).getOrElse(new UserRole(0,0,"",0,new Array[Int](0)))
                    PermissionUtils.checkRoles(userRole.roles.map(r=>roleService.load(r).permissions), Permission.ORDER_VIEW_INFO)
                } else false
            }

            case p : User => {
                if (permission == Permission.ROOT_PERMISSION) {
                    false
                } else if(permission == Permission.WEBSITE_EDIT)
                    user.id == p.id
                else true
            }

            case p : Agency => user.ownerId == 0
            case _ => true
        }
    }
}
