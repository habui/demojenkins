package ads.website.modules

import ads.common.{SecurityContext, Reflect, IItem}
import ads.common.database.{InMemoryDataService, IDataDriver, IDataService, AbstractDelegateDataService}
import ads.common.model._
import ads.website.handler.RestHandler
import ads.web.{WebUtils, Invoke, HandlerContainerServlet}
import ads.web.mvc.{Text, Action, Json, BaseHandler}
import scala.collection.mutable.ArrayBuffer
import ads.common.Syntaxs._
import scala.collection.JavaConversions._
import javax.servlet.http.HttpServletRequest
import ads.website.{PermissionUtils, Environment}
import scala.collection.mutable.ArrayBuffer
import java.util.Date
import ads.common.services.{IActionLogService, IBannerService}
import scala.concurrent.Future
import scala.concurrent.ops._
import scala.util.control.NonFatal


//class CampaignModel(var id: Int, var orderId: Int, var name: String, var status: String = "New", var campaignType: String = "BookingDisplay", var unlinkZoneCount: Int = 0, var unlinkItemCount: Int = 0, var startDate: Long = System.currentTimeMillis(), var endDate: Long = System.currentTimeMillis()) extends IItem



class CampaignModelService (env:{
    val campaignService: IDataService[Campaign]
    val newBookingService: INewBookingService
//    val bookService: IDataService[BookRecord]
    val bannerService: IBannerService
    val newBookService: IDataService[NewBookRecord]
}) extends AbstractDelegateDataService[Campaign,CampaignModel](env.campaignService){

    def toModel(item: Campaign): CampaignModel = {
        val banners = env.bannerService.listByReferenceId(id = item.id).data
//        val bannerIds = banners.map(p=>p.id).toList.distinct
        val itemCount = env.bannerService.countByRef(item.id)
        val instance = item.campaignType match {
            case CampaignType.Booking => {
//                val list = env.newBookService.loadAll().filter(b=>bannerIds.contains(b.itemId)).toList
//                val start = list.size match{
//                    case 0 => 0
//                    case _ =>list.map(b=>b.from).min
//                }
//                val end = list.size match {
//                    case 0 => 0
//                    case _ => list.map(b=>b.to).max
//                }
                val instance = WebUtils.fromJson(classOf[BookingCampaignModel], item.extra)
                if(instance != null){
                    instance.id = item.id
//                    instance.startDate = start
//                    instance.endDate = end
                    instance.status = item.status
                }
                instance
            }
            case CampaignType.Network =>{

                val instance = WebUtils.fromJson(classOf[NetworkCampaignModel],item.extra)
                if(instance != null){
                    instance.id = item.id
                    instance.status = item.status
                }
                instance
            }
            case CampaignType.NetworkTVC => {
                val instance = WebUtils.fromJson(classOf[NetworkTVCCampaignModel],item.extra)
                if(instance !=null){
                    instance.id = item.id
                    instance.status = item.status
                }
                instance
            }
	//pr network
            case CampaignType.PRNetwork => {
                val instance = WebUtils.fromJson(classOf[PRNetworkCampaignModel],item.extra)
                if(instance !=null){
                    instance.id = item.id
                    instance.status = item.status
                }
                instance
            }
        }
        instance.itemCount = itemCount
        instance
    }

    def fromModel(model: CampaignModel): Campaign = {
        model.campaignType match {
            case CampaignType.Booking => {
                val m = model.asInstanceOf[BookingCampaignModel]
                val json = WebUtils.toRawJson(m)
                val status = if(model.status.equals("")) CampaignStatus.RUNNING else model.status
                new Campaign(model.id,model.orderId,model.name,status,model.startDate,model.endDate,model.campaignType,model.companion,json, model.disable)
            }
            case CampaignType.Network => {
                val json = WebUtils.toRawJson(model.asInstanceOf[NetworkCampaignModel])
                val status = if(model.status == null || model.status.equals("")) CampaignStatus.RUNNING else model.status
                new Campaign(model.id,model.orderId,model.name,status,model.startDate,model.endDate,model.campaignType,model.companion,json, model.disable)
            }
            case CampaignType.NetworkTVC => {
                val json = WebUtils.toRawJson(model.asInstanceOf[NetworkTVCCampaignModel])
                new Campaign(model.id,model.orderId,model.name,model.status,model.startDate,model.endDate,model.campaignType,model.companion,json, model.disable)
            }
            case CampaignType.PRNetwork => {
                val json = WebUtils.toRawJson(model.asInstanceOf[PRNetworkCampaignModel])
                new Campaign(model.id,model.orderId,model.name,model.status,model.startDate,model.endDate,model.campaignType,model.companion,json, model.disable)
            }
        }


    }

    override def load(id: Int): CampaignModel = {
        var model = super.load(id)
        if(model == null) return null.asInstanceOf[CampaignModel]
        model = model.campaignType match {
            case CampaignType.Booking => {
                val m = model.asInstanceOf[BookingCampaignModel]
                m.unlinkItemCount = env.newBookingService.getUnlinkedItem(id).size
                m.unlinkZoneCount = env.newBookingService.getUnlinkedZone(id).size
                m
            }
            case CampaignType.Network => {
                val m = model.asInstanceOf[NetworkCampaignModel]
                m
            }
            case CampaignType.NetworkTVC => {
                val m = model.asInstanceOf[NetworkTVCCampaignModel]
                m
            }
            case CampaignType.PRNetwork => {
                val m = model.asInstanceOf[PRNetworkCampaignModel]
                m
            }
        }
        model
    }

    override def search(query: String, pId: Int, isDisable: Boolean, take: Int = 10) = {
        if(pId > 0) env.campaignService.loadAll().filter(p => p.orderId == pId && p.disable == isDisable && p.name.toLowerCase.contains(query.toLowerCase)).take(take).map(toModel).toList
        else env.campaignService.loadAll().filter(p => p.disable == isDisable && p.name.toLowerCase.contains(query.toLowerCase)).take(take).map(toModel).toList
    }

    override def disable(id: Int): Unit = {
        super.disable(id)
        env.bannerService.listByReferenceId(id=id).data.foreach(b=>env.bannerService.disable(b.id))
    }

    override def enable(id: Int): Unit = {
        super.enable(id)
        env.bannerService.listDisable(0, Int.MaxValue).data.toList.filter(b=>b.campaignId == id).foreach(b=>env.bannerService.enable(b.id))
    }
    
}

class CampaignHandler(env: {
    val campaignService: IDataService[Campaign]
    val campaignModelService: IDataService[CampaignModel]
//    val bookingService: IBookingService
//    val websiteService: IDataService[Website]
//    val zoneModelService: IDataService[ZoneModel]
    val bannerModelService: IDataService[BannerModel]
    val orderService: IDataService[Order]
    val newBookingService: INewBookingService
}) extends RestHandler[CampaignModel,Campaign] ( ()=>null ,env.campaignModelService,env.campaignService){

    def roundTime(f: Long): Long = {
        val d = new Date(f)
        return new Date(d.getYear, d.getMonth, d.getDate).getTime
    }

    override def getItemFromModel(item: CampaignModel) : Any = env.campaignService.load(item.id)

    override def beforeSave(request: HttpServletRequest, instance: CampaignModel): Try[Unit] = {
        if (instance.name == null || instance.name.equals("") || instance.orderId == 0)
            return fail("Name is null or Invalid orderId")
        val current = roundTime(System.currentTimeMillis())
        if(roundTime(instance.startDate) < current || roundTime(instance.endDate) < roundTime(instance.startDate)) return fail("Invalid startDate and endDate")
        if (instance.status == "1") instance.status = "Running"
        val order = env.orderService.load(instance.orderId)
        if(order == null) return fail("Invalid order")
        if(order.status == OrderStatus.TERMINATED) return fail("Can't add campaign to terminated order")
        if(PermissionUtils.checkPermission(request, env.orderService.load(instance.orderId), Permission.ORDER_EDIT)) succeed()
        else fail("You don't have permission")
    }

    override def beforeUpdate(request: HttpServletRequest, instance: CampaignModel): Try[Unit] = {
        val old = env.campaignService.load(instance.id)
        if(old == null) return fail("Campaign is not exist")
        if (instance.name == null || instance.name.equals("")) return fail("Name is null")
        if (instance.orderId != old.orderId)
            return fail("Can't change orderId")
        if(old.status == CampaignStatus.TERMINATED) return fail("Can't edit terminated campaign")
        super.beforeUpdate(request, instance)
    }

    @Invoke(Parameters = "request")
    override def save(request: HttpServletRequest): Any = {
        val cType = request.getParameter("campaignType")

        val instance = cType match{
            case CampaignType.Booking => readFromRequest[BookingCampaignModel](request, new BookingCampaignModel(0,0,null,null,null,0,0,0,0,false))
            case CampaignType.Network => readFromRequest[NetworkCampaignModel](request, new NetworkCampaignModel(0,0,null,"New",CampaignType.Network,Array(0,0,0,0,0,0,0),0,0,0,TimeUnit.MINUTE,NetworkCampaignDisplayType.ONE_OR_MORE,false))
            case CampaignType.NetworkTVC => readFromRequest[NetworkTVCCampaignModel](request, new NetworkTVCCampaignModel(0,0,null,"New",CampaignType.NetworkTVC,Array(0,0,0,0,0,0,0),0,0,0,TimeUnit.MINUTE,NetworkCampaignDisplayType.ONE_OR_MORE))
            case CampaignType.PRNetwork =>readFromRequest[PRNetworkCampaignModel](request, new PRNetworkCampaignModel(0,0,"new",CampaignStatus.RUNNING,CampaignType.PRNetwork,0,0))
        }
        if(!CampaignStatus.ALL_STATUS.contains(instance.status)){
            instance.status = CampaignStatus.RUNNING
        }
        val s = beforeSave(request, instance)
        if (!s.isSuccess) return s
        instance.id = modelService.save(instance)
        afterSave(instance)
        Json(instance)
    }

//    @Invoke(Parameters = "request,campId")
//    def getBooked(request: HttpServletRequest, campId: Int) : Any = {
//        val campItem = env.campaignService.load(campId)
//        if(campItem != null) {
//            if(PermissionUtils.checkPermission(request, campItem, Permission.ORDER_VIEW_INFO)) {
//                SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request,item, Permission.ORDER_VIEW_INFO))
//                return Json(env.bookingService.getBookedZones(campId))
//            }
//        }
//        Json(null)
//    }


    @Invoke(Parameters = "request,campId")
    def getUnlinkedItem(request:HttpServletRequest, campId: Int) : Any = {
        val campItem = env.campaignService.load(campId)
        if(campItem != null) {
            if(PermissionUtils.checkPermission(request, campItem, Permission.ORDER_VIEW_INFO)) {
                SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request,item, Permission.ORDER_VIEW_INFO))
                val ids = env.newBookingService.getUnlinkedItem(campId).toList
                return Json(env.bannerModelService.listByIds(ids))
            }
        }
        Json(null)
    }

//    @Invoke(Parameters = "request,itemId")
//    def getLinkedZones(request: HttpServletRequest, itemId: Int) : Any = {
//        val item = env.bannerModelService.load(itemId)
//        if(item == null) return Json(null)
//        if(!PermissionUtils.checkPermission(request, item, Permission.ORDER_VIEW_INFO)) {
//            return Json(null)
//        }
//
//        SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request,item, Permission.ORDER_VIEW_INFO))
//        val bookeds = env.newBookingService.getBookedByCamp(item.campaignId, 0, Int.MaxValue, "id", "desc")
//        val ids = env.newBookingService.getLinkedByItem(itemId).toList
//        val zones = env.zoneModelService.listByIds(ids)
//        val r = new ArrayBuffer[BookedZone]()
//
//        for (z <- zones) {
//            val totalShare = bookeds.data.filter(b=>b.zoneId == z.id).map(b=>b.share).sum
//            val site = env.websiteService.load(z.siteId)
//            val booked = new BookedZone(z.id, z.name, site.name, z.width, z.height, totalShare, 0, z.disable)
//            r += booked
//        }
//
//        Json(r.toArray)
//    }
    @Invoke(Parameters = "request,id")
    def pause(request: HttpServletRequest, id :Int): Any =  {
        val item = env.campaignModelService.load(id)
        if(item != null){
            val s = beforeUpdate(request, item)
            if (!s.isSuccess) return s
            item.status = CampaignStatus.PAUSED
            env.campaignModelService.update(item)
            Text("{\"result\": \"success\"}")
        }else{
            Text("{\"result\":\"error\", \"message\": \"campaign not found!\"}")
        }
    }
    @Invoke(Parameters = "request,id")
    def resume(request:HttpServletRequest, id :Int): Any = {
        val item = env.campaignModelService.load(id)
        if(item != null){
            val order = env.orderService.load(item.orderId)
            if(order == null) return Text("{\"result\":\"error\", \"message\": \"Order not found!\"}")
            if(order.status == OrderStatus.PAUSED) return Text("{\"result\":\"error\", \"message\": \"Order is paused!\"}")
            val s = beforeUpdate(request, item)
            if (!s.isSuccess) return s
            item.status = CampaignStatus.RUNNING
            env.campaignModelService.update(item)
            Text("{\"result\": \"success\"}")
        }else{
            Text("{\"result\":\"error\", \"message\": \"campaign not found!\"}")
        }
    }
    @Invoke(Parameters = "request,id,status")
    def setStatus(request:HttpServletRequest, id :Int, status : String) : Any = {
        val item = env.campaignModelService.load(id)
        var success : Boolean  = false ;
        if(item != null){
            val s = beforeUpdate(request, item)
            if (!s.isSuccess) return s
            val statuses = Array(CampaignStatus.EXPIRED,CampaignStatus.FINISHED,CampaignStatus.OUT_OF_BUDGET,
            CampaignStatus.PAUSED,CampaignStatus.PENDING,CampaignStatus.RUNNING,CampaignStatus.SCHEDULED,CampaignStatus.TERMINATED)
            if(statuses.contains(status)){
                item.status = status ;
                env.campaignModelService.update(item)
                success = true ;
            }
        }
        if(success){
            Text("{\"result\": \"success\"}")
        }else {
            Text("{\"result\":\"error\", \"message\": \"campaign not found!\"}")
        }
    }

}
class CampaignServlet extends HandlerContainerServlet{
    def factory(): BaseHandler = new CampaignHandler(Environment)
}

trait ICampaign2Service extends IDataService[Campaign] {

}

class Campaign2Service(env:{
    val campaign2DataDriver : IDataDriver[Campaign]
    val actionLogServiceRef : Future[IActionLogService]
}) extends InMemoryDataService[Campaign](b=>b.orderId, env.campaign2DataDriver,env.actionLogServiceRef) with ICampaign2Service{
    spawn {
        while(true) {
            Thread.sleep(30 * 1000)
            try {
                //            println("------------Update campaign2Service---------------")
                for (item <- env.campaign2DataDriver.loadAll()) {
                    if (all.containsKey(item.id)){
                        val x = all.get(item.id)
                        all.remove(item.id)
                        removeIndex(x)
                    }

                    all.put(item.id, item)
                    index(item)
                }
                //            println("------------------End------------------------------")
            }
            catch {
                case NonFatal(e) => printException(e, System.out)
                case x: Throwable => printException(x, System.out)
            }
        }
    }
}

trait ICampaign2ModelService extends IDataDriver[CampaignModel] {

}

class Campaign2ModelService (env:{
    val campaign2Service: IDataService[Campaign]
    val newBookingService: INewBookingService
    val bannerService: IBannerService
}) extends AbstractDelegateDataService[Campaign,CampaignModel](env.campaign2Service) with ICampaign2ModelService{

    def toModel(item: Campaign): CampaignModel = {
        val banners = env.bannerService.listByReferenceId(id = item.id).data
        val bannerIds = banners.map(p=>p.id).toList.distinct
        val itemCount = env.bannerService.countByRef(item.id)
        val instance = item.campaignType match {
            case CampaignType.Booking => {
                val instance = WebUtils.fromJson(classOf[BookingCampaignModel], item.extra)
                if(instance != null){
                    instance.id = item.id
                    instance.status = item.status
                }
                instance
            }
            case CampaignType.Network =>{

                val instance = WebUtils.fromJson(classOf[NetworkCampaignModel],item.extra)
                if(instance != null){
                    instance.id = item.id
                    instance.status = item.status
                }
                instance
            }
            case CampaignType.NetworkTVC => {
                val instance = WebUtils.fromJson(classOf[NetworkTVCCampaignModel],item.extra)
                if(instance !=null){
                    instance.id = item.id
                    instance.status = item.status
                }
                instance
            }
            //pr network
            case CampaignType.PRNetwork => {
                val instance = WebUtils.fromJson(classOf[PRNetworkCampaignModel],item.extra)
                if(instance !=null){
                    instance.id = item.id
                    instance.status = item.status
                }
                instance
            }
        }
        instance.itemCount = itemCount
        instance
    }

    def fromModel(model: CampaignModel): Campaign = {
        model.campaignType match {
            case CampaignType.Booking => {
                val m = model.asInstanceOf[BookingCampaignModel]
                val json = WebUtils.toRawJson(m)
                val status = if(model.status.equals("")) CampaignStatus.RUNNING else model.status
                new Campaign(model.id,model.orderId,model.name,status,model.startDate,model.endDate,model.campaignType,m.companion,json)
            }
            case CampaignType.Network => {
                val json = WebUtils.toRawJson(model.asInstanceOf[NetworkCampaignModel])
                val status = if(model.status == null || model.status.equals("")) CampaignStatus.RUNNING else model.status
                new Campaign(model.id,model.orderId,model.name,status,model.startDate,model.endDate,model.campaignType,false,json)
            }
            case CampaignType.NetworkTVC => {
                val json = WebUtils.toRawJson(model.asInstanceOf[NetworkTVCCampaignModel])
                new Campaign(model.id,model.orderId,model.name,model.status,model.startDate,model.endDate,model.campaignType,false,json)
            }
            case CampaignType.PRNetwork => {
                val json = WebUtils.toRawJson(model.asInstanceOf[PRNetworkCampaignModel])
                new Campaign(model.id,model.orderId,model.name,model.status,model.startDate,model.endDate,model.campaignType,false,json)
            }
        }
    }

    override def load(id: Int): CampaignModel = {
        var model = super.load(id)
        if(model == null) return null.asInstanceOf[CampaignModel]
        model = model.campaignType match {
            case CampaignType.Booking => {
                val m = model.asInstanceOf[BookingCampaignModel]
                m.unlinkItemCount = env.newBookingService.getUnlinkedItem(id).size
                m.unlinkZoneCount = env.newBookingService.getUnlinkedZone(id).size
                m
            }
            case CampaignType.Network => {
                val m = model.asInstanceOf[NetworkCampaignModel]
                m
            }
            case CampaignType.NetworkTVC => {
                val m = model.asInstanceOf[NetworkTVCCampaignModel]
                m
            }
            case CampaignType.PRNetwork => {
                val m = model.asInstanceOf[PRNetworkCampaignModel]
                m
            }
        }
        model
    }
}
