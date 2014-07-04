package ads.website.handler

import ads.web.mvc.{Text, Json, SmartDispatcherHandler}
import scala.reflect.ClassTag
import ads.common.{SecurityContext, IItem}
import ads.common.database.{PagingResult, IDataService}
import ads.web.Invoke
import javax.servlet.http.HttpServletRequest
import ads.common.Syntaxs._

import ads.common.model._
import ads.website.{PermissionUtils, Environment}
import ads.website.modules._

abstract class RestHandler[T <: IItem : ClassTag, K <: IItem] (factory: ()=>T, val modelService: IDataService[T], val service: IDataService[K]) extends SmartDispatcherHandler {

    lazy val log = Environment.actionLogService

    def getItemFromModel(item: T) : Any = item

    def afterSave(instance: T){
        val cl = evidence$1.runtimeClass
        //log.log(0,ActionType.CREATE,cl,null,instance)
    }
    def afterUpdate(old: T, instance: T){
        val cl = evidence$1.runtimeClass
        //log.log(0,ActionType.EDIT,cl,old,instance)
    }

    def beforeSave(request: HttpServletRequest, instance: T): Try[Unit] = {
        if(PermissionUtils.checkPermission(request, instance, Permission.WEBSITE_CREATE)) succeed()
        else fail("You don't have permission")
    }
    def beforeUpdate(request: HttpServletRequest, instance: T): Try[Unit] = {
        if(PermissionUtils.checkPermission(request, getItemFromModel(instance), Permission.WEBSITE_EDIT)) succeed()
        else fail("You don't have permission")
    }

    def beforeRemove(request:HttpServletRequest, id : Int) : Try[Unit] = {
        val cl = evidence$1.runtimeClass
        val item  = modelService.load(id)
        //log.log(0,ActionType.DELETE,cl,item,null)
        if(PermissionUtils.checkPermission(request, getItemFromModel(item), Permission.WEBSITE_EDIT)) succeed()
        else fail("You don't have permission")
    }
    def afterRemove(id : Int) {}

    @Invoke(Parameters = "request")
    def save(request: HttpServletRequest): Any = {
        val instance = readFromRequest[T](request, factory())
        val s = beforeSave(request: HttpServletRequest, instance)
        if (!s.isSuccess) return s

        instance.id = modelService.save(instance)
        afterSave(instance)
        Json(instance)
    }

    @Invoke(Parameters = "request,id")
    def update(request: HttpServletRequest, id: Int): Any = {
        val old = modelService.load(id)
        if (old != null) {
            val instance = readFromRequest[T](request, old)
            val s = beforeUpdate(request, instance)
            if (!s.isSuccess) return s
            modelService.update(instance)
            afterUpdate(old, instance)

            Text("{\"result\": \"success\"}")
        }
        else {
            Text("{\"result\":\"error\", \"message\": \"not found!\"}")
        }
    }

    @Invoke(Parameters = "request,id")
    def load(request: HttpServletRequest,id: Int) = {
        SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request,item, Permission.WEBSITE_VIEW_INFO))
        val instance = modelService.load(id)
        Json(instance)
    }

    @Invoke(Parameters = "request,id")
    def remove(request: HttpServletRequest ,id: Int) : Any = {
        val s = beforeRemove(request: HttpServletRequest, id)
        if (!s.isSuccess) return s
    	modelService.remove(id)
        afterRemove(id)
        success()
    }

    @Invoke(Parameters = "request,ids")
    def listByIds(request:HttpServletRequest, ids: Array[Int]) = {
        SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request,item, Permission.WEBSITE_VIEW_INFO))
        val result = modelService.listByIds(ids.toList)
        Json(result)
    }

    @Invoke(Parameters = "request,from,count")
    def list(request: HttpServletRequest, from: Int, count: Int) = {
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
        Json(modelService.list(from, count, sortBy, direction))
    }

    @Invoke(Parameters = "request,from,count")
    def listSimple(request: HttpServletRequest, from: Int, count: Int) = {
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
        Json(service.list(from, count, sortBy, direction))
    }

    @Invoke(Parameters = "request,from,count,ref")
    def listDisable(request: HttpServletRequest, from: Int, count: Int, ref: Int): Any = {
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
        var data = modelService.listDisable(0, Int.MaxValue, sortBy, direction)
        if(ref == 0 || data.total == 0) return Json(data)

        if(data.data(0).isInstanceOf[BannerModel]) {
            val temp = data.data.filter(i=>i.asInstanceOf[BannerModel].campaignId == ref)
            data = new PagingResult[T](temp, temp.size)
        } else if(data.data(0).isInstanceOf[CampaignModel]) {
            val temp = data.data.filter(i=>i.asInstanceOf[CampaignModel].orderId == ref)
            data = new PagingResult[T](temp, temp.size)
        } else if(data.data(0).isInstanceOf[ZoneGroupModel]) {
            val temp = data.data.filter(i=>i.asInstanceOf[ZoneGroupModel].siteId == ref)
            data = new PagingResult[T](temp, temp.size)
        } else if(data.data(0).isInstanceOf[ZoneModel]) {
            val temp = data.data.filter(i=>i.asInstanceOf[ZoneModel].siteId == ref)
            data = new PagingResult[T](temp, temp.size)
        }

        val size = Math.min(count, data.data.size - from)
        val arr = data.data.slice(from, from + size).toArray

        new PagingResult(arr, data.data.size)

        Json(data)
    }

    @Invoke(Parameters="request,query,pId")
    def search(request: HttpServletRequest, query: String, pId: Int) = {
        SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request,item, Permission.WEBSITE_VIEW_INFO))
        val isDisable = request.getParameter("isDisable")
        if(isDisable == "true") {
            Json(modelService.search(query, pId, true))
        } else {
            Json(modelService.search(query, pId))
        }
    }

    @Invoke(Parameters = "request,from,count,id")
    def listByReferenceId(request: HttpServletRequest, from: Int, count: Int, id: Int) = {
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

        Json(modelService.listByReferenceId(from, count, id, sortBy, direction))
    }

    @Invoke(Parameters = "request,id")
    def disable(request:HttpServletRequest, id: Int): Any = {
        val instance = modelService.load(id)
        if(instance == null) return fail("Item not found!")
        val s = beforeUpdate(request, instance)
        if (!s.isSuccess) return s
        modelService.disable(id)
        success()
    }

    @Invoke(Parameters = "request,id")
    def enable(request: HttpServletRequest, id: Int) : Any = {
        val instance = modelService.load(id)
        if(instance == null) return fail("Item not found!")
        val s = beforeUpdate(request, instance)
        if (!s.isSuccess) return s
        modelService.enable(id)
        success()
    }
}
