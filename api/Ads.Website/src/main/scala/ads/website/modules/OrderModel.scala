package ads.website.modules

import ads.common.{SecurityContext, IItem}
import ads.common.database.{PagingResult, IDataService, AbstractDelegateDataService}
import ads.common.model._
import ads.website.handler.RestHandler
import ads.web.{Invoke, HandlerContainerServlet}
import ads.web.mvc.{Text, Json, BaseHandler}
import javax.servlet.http.HttpServletRequest
import ads.website.{PermissionUtils, Environment}
import ads.common.Syntaxs.{succeed, fail, Try}
import ads.common.services.IUserRoleService

class OrderModel(var id: Int,
    var ownerId: Int,
    var name: String,
    var description: String,
    var startDate: Long = System.currentTimeMillis(),
    var endDate: Long = System.currentTimeMillis(),
    var pendingCampaign: Int = 0,
    var runningCampaign: Int = 0,
    var scheduledCampaign : Int = 0,
    var terminatedCampaign : Int = 0,
    var pausedCampaign : Int = 0,
    var outOfBudgetCampaign : Int = 0,
    var expiredCampaign : Int = 0,
    var finishedCampaign : Int = 0,
    var status : String = OrderStatus.RUNNING,
    var disable: Boolean = false
) extends IItem

trait IOrderModelService extends IDataService[OrderModel] {
    def listByFilter(from: Int, count: Int, sortBy: String, direction: String, filter: String) : PagingResult[OrderModel]
    def terminateBook(id: Int) : Unit
}

class OrderModelService (env:{
    val orderService: IDataService[Order]
    val campaignService: IDataService[Campaign]
    val newBookService: IDataService[NewBookRecord]
    val bannerService: IDataService[Banner]})
    extends AbstractDelegateDataService[Order,OrderModel](env.orderService)
    with IOrderModelService{
    def toModel(item: Order): OrderModel = {
        val list = env.campaignService.listByReferenceId(id = item.id).data
        val pending = list.count(p=>p.status == CampaignStatus.PENDING)
        val running = list.count(p=>p.status == CampaignStatus.RUNNING)
        val scheduled = list.count(p=> p.status == CampaignStatus.SCHEDULED)
        val terminated = list.count(p => p.status == CampaignStatus.TERMINATED)
        val paused  = list.count(p=>p.status==CampaignStatus.PAUSED)
        val outOfBudget = list.count(p=>p.status==CampaignStatus.OUT_OF_BUDGET)
        val expired = list.count(p=>p.status == CampaignStatus.EXPIRED)
        val finished = list.count(p=>p.status == CampaignStatus.FINISHED)

        new OrderModel(item.id, item.ownerId, item.name, item.description, 0, 0,
            pending, running,scheduled,terminated,paused,outOfBudget,expired,finished,item.status)
    }

    def fromModel(model: OrderModel): Order = new Order(model.id, model.ownerId, model.name, model.description,model.status, model.disable)

    def listByFilter(from: Int, count: Int, sortBy: String, direction: String, filter: String): PagingResult[OrderModel] = {
        if(filter != "running" && filter != "finished") return list(from,count,sortBy,direction)

        val orders = env.orderService.list(0,Int.MaxValue,sortBy,direction)
        if(orders == null || orders.data.length == 0) return new PagingResult(new Array[OrderModel](0), 0)
        var rs = null.asInstanceOf[List[Order]]
        val time = System.currentTimeMillis()

        if(filter == "running") rs = orders.data.filter(b=>getStateOrder(b,time) == 0).toList
        else rs = orders.data.filter(b=>getStateOrder(b,time) == 1).toList

        val size = Math.min(count, rs.size - from)
        val arr = rs.slice(from, from + size).toArray

        convertRecord(new PagingResult(arr, rs.size))
    }

    def getStateOrder(order: Order, time: Long) : Int = { //1: finished | 0: running | -1 : Nothing
        val campaigns = env.campaignService.listByReferenceId(id = order.id).data
        if(campaigns == null || campaigns.length == 0) -1
        else {
            val minTime = campaigns.map(c=>c.startDate).min
            val maxTime = campaigns.map(c=>c.endDate).max
            if(time < minTime) -1
            else if(time > maxTime) 1
            else 0
        }
    }

    override def disable(id: Int): Unit = {
        super.disable(id)
        env.campaignService.listByReferenceId(id=id).data.foreach(c=>env.campaignService.disable(c.id))
    }

    override def enable(id: Int): Unit = {
        super.enable(id)
        env.campaignService.listDisable(0, Int.MaxValue).data.toList.filter(c=>c.orderId == id).foreach(z=>env.campaignService.enable(z.id))
    }

    def terminateBook(id: Int): Unit = {
        val campaignIds = env.campaignService.listByReferenceId(id = id).data.toList.map(c=>c.id)
        val itemIds = campaignIds.flatMap(cId => env.bannerService.listByReferenceId(id = cId).data.toList.map(i=>i.id))
        env.newBookService.loadAll().toList.filter(b=>itemIds.contains(b.itemId)).foreach(b=>env.newBookService.disable(b.id))
    }
}

class OrderHandler(env:{
    val orderService: IDataService[Order]
    val orderModelService: IOrderModelService
    val campaignService : IDataService[Campaign]
    val conversionService: IDataService[Conversion]
    val userRoleService : IUserRoleService
}) extends RestHandler[OrderModel,Order](()=>new OrderModel(0,0,null,null),env.orderModelService,env.orderService){

//    @Invoke(Parameters = "id,from,count")
//    def getItems(id: Int,from: Int, count: Int) = Json(env.orderModelService.)

    override def getItemFromModel(item: OrderModel) : Any = env.orderService.load(item.id)

    @Invoke(Parameters = "request")
    override def save(request: HttpServletRequest): Any = {
        val instance = readFromRequest[OrderModel](request, new OrderModel(0, 0, null, null))
        val s = beforeSave(request: HttpServletRequest, instance)
        if (!s.isSuccess) return s

        instance.id = modelService.save(instance)

        //set Standard Role of Order for agency user
        val user = PermissionUtils.getUser(request)
        if (user.ownerId != 0)
            env.userRoleService.save(new UserRole(0, user.id, "order", instance.id, Array(8)))
        afterSave(instance)
        Json(instance)
    }

    override def beforeSave(request: HttpServletRequest, instance: OrderModel): Try[Unit] = {
        val user = PermissionUtils.getUser(request)
        if(user == null) return fail("Who are you ???")
        if (instance.name == null || instance.name.equals(""))
            return fail("Name is null")
        if (instance.ownerId == 0)
            return fail("Invalid owner")
        if (user.ownerId != 0)
            instance.ownerId = user.ownerId
        else
            instance.ownerId = user.id
        super.beforeSave(request, instance)
    }


    override def afterSave(instance: OrderModel): Unit = {
        env.conversionService.save(new Conversion(0, instance.name, 0, 1.0, System.currentTimeMillis(), "Unverified", instance.id))
        super.afterSave(instance)
    }

    override def beforeUpdate(request: HttpServletRequest, instance: OrderModel): Try[Unit] = {
        val old = env.orderService.load(instance.id)
        if (instance.name == null || instance.name.equals("") || instance.ownerId == 0 || old == null)
            return fail("Invalid order")
        instance.ownerId = old.ownerId
        if(old.status == OrderStatus.TERMINATED) return fail("Can't edit terminated order")
        super.beforeUpdate(request, instance)
    }

    @Invoke(Parameters = "request,from,count,filterBy")
    def listByFilter(request: HttpServletRequest,from: Int, count: Int, filterBy: String) = {
        SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request,item, Permission.ORDER_VIEW_INFO))
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
        Json(env.orderModelService.listByFilter(from, count, sortBy, direction, filterBy))
    }
    @Invoke(Parameters = "request,id")
    def pause(request:HttpServletRequest, id :Int): Any =  {
        val item = env.orderModelService.load(id)
        if(item != null){
            val s = beforeUpdate(request, item)
            if (!s.isSuccess) return s
            item.status = OrderStatus.PAUSED
            env.orderModelService.update(item)
            val campaigns = env.campaignService.listByReferenceId(id=item.id).data
            campaigns.foreach(c=> {
                c.status = CampaignStatus.PAUSED
                env.campaignService.update(c)
            })
            Text("{\"result\": \"success\"}")
        }else{
            Text("{\"result\":\"error\", \"message\": \"order not found!\"}")
        }
    }
    @Invoke(Parameters = "request,id")
    def resume(request:HttpServletRequest, id :Int): Any =  {
        val item = env.orderModelService.load(id)
        if(item != null){
            val s = beforeUpdate(request, item)
            if (!s.isSuccess) return s
            item.status = OrderStatus.RUNNING
            env.orderModelService.update(item)
            val campaigns = env.campaignService.listByReferenceId(id=item.id).data
            campaigns.foreach(c=> {
                c.status = CampaignStatus.RUNNING
                env.campaignService.update(c)
            })
            Text("{\"result\": \"success\"}")
        }else{
            Text("{\"result\":\"error\", \"message\": \"order not found!\"}")
        }
    }
    @Invoke(Parameters = "request,id,status")
    def setStatus(request:HttpServletRequest, id :Int, status : String): Any = {
        val statuses = Array(OrderStatus.PAUSED, OrderStatus.RUNNING, OrderStatus.TERMINATED)
        if(!statuses.contains(status)) return Text("{\"result\":\"error\", \"message\": \"Invalid status!\"}")
        val item = env.orderModelService.load(id)
        if(item != null){
            val s = beforeUpdate(request, item)
            if (!s.isSuccess) return s
            
            item.status = status ;
            env.orderModelService.update(item)
            val campaigns = env.campaignService.listByReferenceId(id=item.id).data
            campaigns.foreach(c=> {
                c.status = status
                env.campaignService.update(c)
            })
            if(status == OrderStatus.TERMINATED) env.orderModelService.terminateBook(id)
            Text("{\"result\": \"success\"}")
        } else Text("{\"result\":\"error\", \"message\": \"order not found!\"}")
    }
}

class OrderServlet extends HandlerContainerServlet{
    def factory(): BaseHandler = new OrderHandler(Environment)
}
