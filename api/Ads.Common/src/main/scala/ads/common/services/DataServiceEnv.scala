package ads.common.services

    import ads.common.model._
    import ads.common.database._
    import ads.common.Syntaxs._
    import java.util.concurrent.ConcurrentHashMap
    import scala.collection.mutable.ArrayBuffer
    import scala.collection.JavaConversions._
    import ads.common.IItem
    import java.util
    import scala.util.control._
    import scala.concurrent._
    import ExecutionContext.Implicits.global
    import ads.common.services.serving.CachedLogService
    import org.apache.http.impl.conn.InMemoryDnsResolver

    trait DataServiceEnv extends DatabaseEnv{
        lazy val sessionService: ISessionService = new SessionService(this)
        lazy val userService = new UserService(this)
        lazy val zoneService = new ZoneService(this)
        lazy val zoneGroupService = new InMemoryDataService[ZoneGroup](z=>z.siteId,this.zoneGroupDataDriver,this.actionLogServiceRef)
        lazy val configTableService = new InMemoryDataService[ConfigTable](z=>z.id,this.configTableDataDriver,this.actionLogServiceRef)
        lazy val zoneToZoneGroupService = new ZoneToZoneGroupService(this)
        lazy val websiteService = new WebsiteService(this)
        lazy val zoneToBannerServiceRef : Future[IZoneToBannerService] = Future{this.zoneToBannerService}
        lazy val bannerService = new BannerService(this)
        lazy val orderService = new InMemoryDataService[Order](b=>b.ownerId, this.orderDataDriver,this.actionLogServiceRef)
        lazy val campaignService : ICampaignService = new CampaignService(this)
        lazy val bookService = new InMemoryDataService[BookRecord](b=>b.zoneId, this.bookDataDriver, this.actionLogServiceRef)
        lazy val zoneToBannerService = new ZoneToBannerService(this)
        lazy val userRoleService = new UserRoleService(this)
        lazy val roleService = new RoleService(this)
        lazy val categoryService = new InMemoryDataService[Category](c => c.id,this.categoryDataDriver, this.actionLogServiceRef)
        lazy val conversionTypeService = new ConversionTypeService(this)
        //lazy val cachedLogService = new InMemoryDataService[CachedLog](log=>log.id, new SqlDataService[CachedLog](()=>new CachedLog(0, 0, null,null, 0)), this.actionLogServiceRef)
        lazy val cachedLogService = new CachedLogService(Config.cachedLog.get())
        lazy val actionLogService = new ActionLogService(this)
        lazy val conversionService = new ConversionService(this)
        lazy val actionLogServiceRef : Future[IActionLogService] = Future{this.actionLogService}
        //article dao
        lazy val articleStatsService:IDataService[ArticleStats] = new  InMemoryDataService[ArticleStats]( stats=> stats.articleId,this.articleStatsDataDriver,this.actionLogServiceRef)
        lazy val articleLocationService:IDataService[ArticleLocation] = new  InMemoryDataService[ArticleLocation]( loc=> loc.articleId,this.articleLocationDataDriver,this.actionLogServiceRef)
        lazy val articleGenderService:IDataService[ArticleGender] = new  InMemoryDataService[ArticleGender](g=> g.articleId,this.articleGenderDataDriver,this.actionLogServiceRef)
        lazy val articleAgeService:IDataService[ArticleAge] = new  InMemoryDataService[ArticleAge](a=> a.articleId,this.articleAgeDataDriver,this.actionLogServiceRef)
        lazy val articleCommentService:IDataService[ArticleComment] = new  InMemoryDataService[ArticleComment](a=> a.articleId,this.articleCommentDataDriver,this.actionLogServiceRef)

        lazy val newBookService: IDataService[NewBookRecord] = new InMemoryDataService[NewBookRecord](b=>b.zoneId, this.newBookDataDriver, this.actionLogServiceRef)
        lazy val newBookServiceRef : Future[IDataService[NewBookRecord]] = Future{this.newBookService}

        //    lazy val banner2Service: IBanner2Service = new Banner2Service(this)
        //    lazy val zone2Service: IZone2Service = new Zone2Service(this)
        //    lazy val conversion2Service: IConversion2Service = new Conversion2Service(this)
    }

    class CachedLog(var id: Int, var time: Int, var group: String, var key: String, var value: Double, var disable: Boolean = false) extends IItem

trait IZoneToBannerService extends IDataService[ZoneToBanner]{
    def listByStatus(status : Int, zoneId: Int, from: Int, count : Int) : PagingResult[ZoneToBanner]
    def listByStatus(status : Int, from: Int, count: Int) : PagingResult[ZoneToBanner]
    def countByStatus(status : Int, zoneId: Int) : Int
    def approve(itemId : Int, zoneId : Int) : Int
    def reject(itemId : Int, zoneId : Int) : Int
    def disableLink(itemId: Int, zoneId: Int) : Unit
    def postpone(itemId: Int) : Unit
}


class ApproveItem(
                     var itemId: Int,
                     var zoneId: Int
                     )


class ZoneToBannerService (env: {
    val zoneToBannerDataDriver: IDataDriver[ZoneToBanner]
    val bannerService : IDataService[Banner]
    val websiteService : IDataService[Website]
    val zoneService : IDataService[Zone]
    val actionLogServiceRef : Future[IActionLogService]
})
    extends InMemoryDataService[ZoneToBanner](z=>z.zoneId, env.zoneToBannerDataDriver,env.actionLogServiceRef)
    with IZoneToBannerService{

    override def save(link: ZoneToBanner): Int = {
        if(!needApprove(link)) return 0
        val zoneToBannerItems = loadAll().toList.filter(p=>(p.itemId == link.itemId && p.zoneId == link.zoneId))
        if(zoneToBannerItems != null && zoneToBannerItems.length > 0) return zoneToBannerItems(0).id
        super.save(link)
    }

    override def update(link: ZoneToBanner) : Boolean = {
        if(!needApprove(link)) return true
        super.update(link)
    }

    def needApprove(link: ZoneToBanner) : Boolean = {
        val zone = env.zoneService.load(link.zoneId)
        if(zone == null) return false
        val site = env.websiteService.load(zone.siteId)
        if(site == null || site.reviewType.length == 0) return false
        val item = env.bannerService.load(link.itemId)
        if(item == null) return false
        site.reviewType.split(",").contains({
            item.kind match {
                case BannerKind.Media | BannerKind.Ecommerce | BannerKind.Html | BannerKind.Expandable | BannerKind.Catfish | BannerKind.Balloon | BannerKind.Popup => "booking"
                case BannerKind.NetworkMedia | BannerKind.NetworkHtml | BannerKind.NetworkOverlayBanner | BannerKind.NetworkExpandable | BannerKind.NetworkCatfish | BannerKind.NetworkBalloon | BannerKind.NetworkPopup => "network"
                case BannerKind.NetworkTVC | BannerKind.NetworkTVCBanner => "tvc"
                case _ => "___"
            }
        })
    }

    override def enable(id: Int) = {}

    def listByStatus(status: Int, zoneId: Int, from: Int, count: Int): PagingResult[ZoneToBanner] = new PagingResult[ZoneToBanner](listByReferenceId(from, count, zoneId).data.filter(p=>p.status == status), countByRef(zoneId))

    def countByStatus(status: Int, zoneId: Int): Int = listByReferenceId(0, Int.MaxValue, zoneId).data.filter(p=>p.status == status).length

    def approve(itemId: Int, zoneId: Int): Int = setStatus(itemId, zoneId, ZoneToBannerStatus.APPROVED, Array(ZoneToBannerStatus.PENDING))

    def reject(itemId: Int, zoneId: Int): Int = setStatus(itemId, zoneId, ZoneToBannerStatus.REJECTED, Array(ZoneToBannerStatus.PENDING, ZoneToBannerStatus.APPROVED))

    def postpone(itemId: Int, zoneId: Int): Int = setStatus(itemId, zoneId, ZoneToBannerStatus.PENDING, Array(ZoneToBannerStatus.APPROVED, ZoneToBannerStatus.REJECTED))

    def postpone(itemId: Int) = {
        loadAll().filter(p => p.itemId == itemId).foreach(p => {
            p.status = ZoneToBannerStatus.PENDING
            update(p)
        })
    }

    def setStatus(itemId: Int, zoneId: Int, status: Int, orgStatuses: Array[Int]) : Int = {
        val data = listByReferenceId(0, 1, zoneId).data.filter(l=>l.itemId == itemId)
        if(data == null || data.length == 0) return -1
        val orgStatusItems = data.filter(p=>orgStatuses.contains(p.status))
        if(orgStatusItems == null || orgStatusItems.length == 0) return -2
        val item = orgStatusItems(0)
        item.status = status
        update(item)
        return 0
    }

    def listByStatus(status: Int, from: Int, count: Int) = {
        val data = loadAll().filter(p=>p.status == status).toArray
        new PagingResult[ZoneToBanner](data.slice(from, count), data.length)
    }

    def disableLinksBySite(siteId: Int) = {
        env.zoneService.listByReferenceId(0, Int.MaxValue, siteId).data.map(z=>z.id).foreach(z=> {
            listByReferenceId(0, Int.MaxValue, z).data.foreach(l=>disable(l.id))
        })
    }

    def disableLink(itemId: Int, zoneId: Int): Unit = {
        val data = listByReferenceId(0, 1, zoneId).data
        if(data == null || data.length == 0) return
        data.foreach(l=>disable(l.id))
    }
}
