package ads.website.modules

import ads.common.{Reflect, SecurityUtils, SecurityContext}
import ads.common.database.{GenericComparator, PagingResult, IDataService, AbstractDelegateDataService}
import ads.common.model._
import ads.web.{AdsUtils, WebUtils, Invoke, HandlerContainerServlet}
import ads.website.handler.RestHandler
import javax.servlet.http.HttpServletRequest
import ads.web.mvc.{Text, BaseHandler, Json}
import ads.common.Syntaxs._
import ads.common.services.{IZoneToBannerService, IBanner2Service, IZoneService, IBannerService}
import ads.website.{PermissionUtils, Environment}
import java.util.Date
import scala.collection.mutable
import java.util
import scala.Some
import scala.concurrent.ops._
import scala.Some
import scala.util.control.NonFatal
import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.{HttpHost, HttpResponse, NameValuePair}
import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.util.EntityUtils
import java.io.{BufferedReader, InputStreamReader}
import com.google.gson.internal.LinkedTreeMap
import org.apache.http.conn.params.ConnRoutePNames

import ads.website.modules.serving.TrackKind
import org.jsoup.Jsoup

trait IBannerModelService extends IDataService[BannerModel] {
    def searchItems(query: String, pId: Int, searchBy: String) : List[BannerModel]
    def getByTargetedZone(zid : Int, from : Int, count : Int) : PagingResult[BannerModel] ;
    def getByLinkedZone(zid : Int, from : Int, count : Int) : PagingResult[BannerModel]
    def getItemsByZoneId(zid : Int, from : Int, count: Int) : PagingResult[BannerModel]
    def getItemsBy(zid : Int, campaignId : Int, from : Int, count : Int) : PagingResult[BannerModel]
    def loadArticlesByZoneId(zoneId:Int,status:Array[Int],from:Int,count:Int):PagingResult[ArticleModel]
    def loadArticlesByCampaignId(campaignId:Int,status:Array[Int],from:Int,count:Int):PagingResult[ArticleModel]
    def listByFilterInOrder(orderId:Int,status:Int,from: Int, count: Int, sortBy: String, direction: String, filter: String) : PagingResult[ArticleModel]
    def listByFilterInCampaign(campaignId:Int,status:Int,from: Int, count: Int, sortBy: String, direction: String, filter: String) : PagingResult[ArticleModel]
    def listByFilterInZone(zoneId:Int,status:Int,from: Int, count: Int, sortBy: String, direction: String, filter: String) : PagingResult[ArticleModel]
}

class BannerModelService(env: {
    val bannerService: IBannerService
    val zoneService  : IZoneService
    val newBookingService: INewBookingService
    val zoneToBannerService: IZoneToBannerService
    val newBookService: IDataService[NewBookRecord]
    val campaignService : IDataService[Campaign]
})
    extends AbstractDelegateDataService[Banner, BannerModel](env.bannerService)
    with IBannerModelService{

    //TODO: update status of article
    spawn {
        while(Config.isMaster) {
            Thread.sleep(Config.timeUpdateStatusPr.getValue * 60 * 1000)
            println("Begin update PR article status")

            for (instance <- env.bannerService.loadAll()) {
                if(instance.kind == BannerKind.Article) {
                    val item = toModel(instance).asInstanceOf[ArticleModel]
                    if(item.websiteId == Publisher.ZINGNEWS) {
                        if(item.pubArticleId > 0) {
                            try {
                                val token = SecurityUtils.sha256Hash(s"${item.pubArticleId}${item.createDate}${Config.secretKeyZNews.getValue}")

                                val post = new HttpPost(Config.urlReportArticleZNews.getValue)

                                val client = new DefaultHttpClient
                                val proxy = new HttpHost(Config.proxyHost.get(), Config.proxyPort.get(), "http")
                                post.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy)

                                val nameValuePairs = new util.ArrayList[NameValuePair]()
                                nameValuePairs.add(new BasicNameValuePair("articleid", item.pubArticleId.toString))
                                nameValuePairs.add(new BasicNameValuePair("timestamp", item.createDate.toString))
                                nameValuePairs.add(new BasicNameValuePair("mac", token))
                                post.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"))

                                // send the post request
                                val response = client.execute(post)
                                if(response.getStatusLine().getStatusCode() == 200) {
                                    val source = scala.io.Source.fromInputStream(response.getEntity().getContent()).getLines().mkString("\n")
                                    val map = WebUtils.fromJson(classOf[util.ArrayList[LinkedTreeMap[String,Object]]], source)
                                    if(map.size() > 0 && map.get(0).containsKey("status_code")) {
                                        val status = map.get(0).get("status_code").toString.toFloat.toInt
                                        val oldStatus = item.status

                                        status match {
                                            //SubmittedItemReturned
                                            case 2 => {
                                                item.disable = true
                                                item.status = ArticleStatus.COMPOSING_AFTER_REJECTED_BY_PUBLISHER
                                            }
                                            case 3 | 4 => {
                                                item.status = ArticleStatus.WAITING_FOR_PUBLISHING
                                            }
                                            case 5 => {
                                                item.status = ArticleStatus.PUBLISHED
                                                item.publishedDate = System.currentTimeMillis()
                                                //TODO : get published url
                                                item.publishedUrl = "http://news.zing.vn" + map.get(0).get("link").toString
                                            }
                                            case 6 => {
                                                item.status = ArticleStatus.EXPIRED
                                            }
//                                            case 3|4|5|6=>{
//                                                item.status = ArticleStatus.PUBLISHED
//                                                item.publishedDate = System.currentTimeMillis()
//                                                //TODO : get published url
//                                                item.publishedUrl = "http://news.zing.vn" + map.get(0).get("link").toString
//                                            }
                                            case _ => 0
                                        }
                                        if(item.status > 0 && oldStatus != item.status)
                                            update(item)
                                    }
                                }
                            }
                            catch {
                                case NonFatal(e) => printException(e, System.out)
                                case x: Throwable => printException(x, System.out)
                            }

                        }
                    }
                }
            }


            println("End update PR article status")
        }
    }

    def roundTime(f: Long): Long = {
        if(f == 0) return 0
        val d = new Date(f)
        return new Date(d.getYear, d.getMonth, d.getDate).getTime
    }

    def validateTimeBanner(instance: BannerModel) {
        instance.from = roundTime(instance.from)
        instance.to = roundTime(instance.to)
        if(instance.from > instance.to) {
            val temp = instance.from
            instance.from = instance.to
            instance.to = temp
        }
        instance match {
            case p : INetwork => {
                val model = p.asInstanceOf[INetwork]
                model.startDate = roundTime(model.startDate)
                model.endDate = roundTime(model.endDate)
            }
            case _ => {}
        }
    }

    override def beforeUpdate(instance: BannerModel) = validateTimeBanner(instance)

    override def beforeSave(instance: BannerModel) = validateTimeBanner(instance)

    override def update(instance: BannerModel) : Boolean = {
        val old = load(instance.id)
        if(old == null) return true
        val oldZones = old match {
            case p: INetwork => getTargetZones(old.asInstanceOf[INetwork])
            case _ => new Array[Int](0)
        }
        super.update(instance)
        val newZones = instance match {
            case p: INetwork => getTargetZones(instance.asInstanceOf[INetwork])
            case _ => new Array[Int](0)
        }

        //update -> reapprove item

//        oldZones.filter(z=>(!newZones.contains(z))).foreach(z=>env.zoneToBannerService.disableLink(instance.id, z))
//        if(old.kind == instance.kind) {
//            old.kind match {
//                case BannerKind.Media | BannerKind.NetworkMedia | BannerKind.NetworkOverlayBanner => {
//                    if(instance.asInstanceOf[MediaBannerModel].bannerFile != old.asInstanceOf[MediaBannerModel].bannerFile ||
//                        instance.asInstanceOf[MediaBannerModel].bannerFileFallback != old.asInstanceOf[MediaBannerModel].bannerFileFallback) {
//                        env.zoneToBannerService.postpone(instance.id)
//                    }
//                }
//                case BannerKind.Expandable | BannerKind.NetworkExpandable => {
//                    if(instance.asInstanceOf[ExpandableBannerModel].tvcFile != old.asInstanceOf[ExpandableBannerModel].tvcFile ||
//                        instance.asInstanceOf[ExpandableBannerModel].standardFile != old.asInstanceOf[ExpandableBannerModel].standardFile ||
//                        instance.asInstanceOf[ExpandableBannerModel].backupFile != old.asInstanceOf[ExpandableBannerModel].backupFile ||
//                        instance.asInstanceOf[ExpandableBannerModel].expandFile != old.asInstanceOf[ExpandableBannerModel].expandFile) {
//                        env.zoneToBannerService.postpone(instance.id)
//                    }
//                }
//                case BannerKind.Html | BannerKind.NetworkHtml => {
//                    if(instance.asInstanceOf[HtmlBannerModel].embeddedHtml != old.asInstanceOf[HtmlBannerModel].embeddedHtml) {
//                        env.zoneToBannerService.postpone(instance.id)
//                    }
//                }
//                case BannerKind.NetworkTVC => {
//                    if(instance.asInstanceOf[NetworkTVCBannerModel].tvcFile != old.asInstanceOf[NetworkTVCBannerModel].tvcFile) {
//                        env.zoneToBannerService.postpone(instance.id)
//                    }
//                }
//                case _=> {}
//            }
//        }
//        newZones.filter(z=>(!oldZones.contains(z))).foreach(z=>env.zoneToBannerService.save(new ZoneToBanner(0, instance.id, z, ZoneToBannerStatus.PENDING)))

        oldZones.filter(z=>(!newZones.contains(z))).foreach(z=>env.zoneToBannerService.disableLink(instance.id, z))
        newZones.filter(z=>(!oldZones.contains(z))).foreach(z=>env.zoneToBannerService.save(new ZoneToBanner(0, instance.id, z, ZoneToBannerStatus.PENDING)))
        true
    }

    override def save(instance: BannerModel) = {
        val id = super.save(instance)
        instance match {
            case p: INetwork => getTargetZones(instance.asInstanceOf[INetwork]).foreach(z=>
                env.zoneToBannerService.save(new ZoneToBanner(0, id, z, ZoneToBannerStatus.PENDING)))
            case _ => {}
        }
        id
    }

    def getTargetZones(model: INetwork) : Array[Int] = {
        (model.targetZones, model.targetContent) match {
            case (null, null) => Array()
            case (null, _) => model.targetContent.flatMap(site=>env.zoneService.listByReferenceId(0, Int.MaxValue, site).data).filter(z => z.width >= model.asInstanceOf[BannerModel].width && z.height >= model.asInstanceOf[BannerModel].height).map(z => z.id).distinct
            case (_, _) => model.targetZones
        }
    }

    def toModel(item: Banner): BannerModel = {
        lazy val linkedZones =  env.newBookingService.getLinkedByItem(item.id)
        item.kind match {
            case BannerKind.Media => {
                val model = WebUtils.fromJson(classOf[MediaBannerModel], item.extra)
                model.id = item.id
                model.linkedZones = if(linkedZones != null) linkedZones.size else 0
                model
            }
            case BannerKind.Ecommerce=>{
                val model = WebUtils.fromJson(classOf[EcommerceBannerModel], item.extra)
                model.id = item.id
                model
            }
            case BannerKind.Expandable => {
                val model : ExpandableBannerModel  = WebUtils.fromJson(classOf[ExpandableBannerModel],item.extra)
                if(model != null) {
                    model.id = item.id
                    model.linkedZones = if(linkedZones != null) linkedZones.size else 0
                }
                model
            }
            case BannerKind.Html=>{
                val model = WebUtils.fromJson(classOf[HtmlBannerModel],item.extra)
                model.id = item.id
                model.linkedZones = if(linkedZones != null) linkedZones.size else 0
                model
            }
            case BannerKind.Balloon => {
                val model = WebUtils.fromJson(classOf[BalloonBannerModel],item.extra)
                model.id = item.id
                model.linkedZones = if(linkedZones != null) linkedZones.size else 0
                model
            }
            case BannerKind.Catfish => {
                val model = WebUtils.fromJson(classOf[CatfishBannerModel],item.extra)
                model.id = item.id
                model.linkedZones = if(linkedZones != null) linkedZones.size else 0
                model
            }
            case BannerKind.Popup => {
                val model = WebUtils.fromJson(classOf[PopupBannerModel],item.extra)
                model.id = item.id
                model.linkedZones = if(linkedZones != null) linkedZones.size else 0
                model
            }
            case BannerKind.NetworkHtml => {
                val model = WebUtils.fromJson(classOf[NetworkHtmlBannerModel],item.extra)
                model.id = item.id
                model
            }
            case BannerKind.NetworkMedia => {
                val model = WebUtils.fromJson(classOf[NetworkMediaBannerModel],item.extra)
                model.id = item.id
                model
            }
            case BannerKind.NetworkTVC => {
                val model = WebUtils.fromJson(classOf[NetworkTVCBannerModel],item.extra)
                model.id = item.id
                model
            }
            case BannerKind.NetworkTVCBanner => {
                val model = WebUtils.fromJson(classOf[NetworkOverlayBannerModel],item.extra)
                if(model.id != item.id || model.kind != BannerKind.NetworkOverlayBanner) {
                    model.id = item.id
                    model.kind = BannerKind.NetworkOverlayBanner
                }
                model
            }

            case BannerKind.Tracking => {
                val model = WebUtils.fromJson(classOf[TrackingBanner], item.extra)
                model.id = item.id
                model
            }
            case BannerKind.NetworkOverlayBanner => {
                val model = WebUtils.fromJson(classOf[NetworkOverlayBannerModel],item.extra)
                model.id = item.id
                model
            }
            case BannerKind.NetworkPauseAd => {
                val model = WebUtils.fromJson(classOf[NetworkPauseAdBannerModel],item.extra)
                if(model.id != item.id || model.kind != BannerKind.NetworkPauseAd) {
                    model.id = item.id
                    model.kind = BannerKind.NetworkPauseAd
                }
                model
            }
            case BannerKind.NetworkExpandable => {
                val model = WebUtils.fromJson(classOf[NetworkExpandableBannerModel], item.extra)
                model.id = item.id
                model
            }
            case BannerKind.NetworkBalloon => {
                val model = WebUtils.fromJson(classOf[NetworkBalloonBannerModel], item.extra)
                model.id = item.id
                model
            }
            case BannerKind.NetworkCatfish => {
                val model = WebUtils.fromJson(classOf[NetworkCatfishBannerModel], item.extra)
                model.id = item.id
                model
            }
            case BannerKind.NetworkPopup => {
                val model = WebUtils.fromJson(classOf[NetworkPopupBannerModel], item.extra)
                model.id = item.id
                model
            }
            case BannerKind.Article => {
                val model = WebUtils.fromJson(classOf[ArticleModel], item.extra)
                model.id = item.id
                model
            }

            case BannerKind.NetworkPrBanner => {
                val model = WebUtils.fromJson(classOf[NetworkPrBannerModel], item.extra)
                model.id = item.id
                model
            }

            case BannerKind.PrBanner => {
                val model = WebUtils.fromJson(classOf[PrBannerModel], item.extra)
                model.id = item.id
                model
            }
        }
    }

    def fromModel(model: BannerModel): Banner = {
        val json = model.kind match {
            case BannerKind.Media =>
                WebUtils.toRawJson(model.asInstanceOf[MediaBannerModel])
            case BannerKind.Ecommerce =>
                WebUtils.toRawJson(model.asInstanceOf[EcommerceBannerModel])
            case BannerKind.Expandable =>
                WebUtils.toRawJson(model.asInstanceOf[ExpandableBannerModel])
            case BannerKind.Html =>
                WebUtils.toRawJson(model.asInstanceOf[HtmlBannerModel])
            case BannerKind.Balloon =>
                WebUtils.toRawJson(model.asInstanceOf[BalloonBannerModel])
            case BannerKind.Catfish =>
                WebUtils.toRawJson(model.asInstanceOf[CatfishBannerModel])
            case BannerKind.Popup =>
                WebUtils.toRawJson(model.asInstanceOf[PopupBannerModel])
            case BannerKind.NetworkHtml =>
                WebUtils.toRawJson(model.asInstanceOf[NetworkHtmlBannerModel])
            case BannerKind.NetworkMedia =>
                WebUtils.toRawJson(model.asInstanceOf[NetworkMediaBannerModel])
            case BannerKind.NetworkTVC =>
                WebUtils.toRawJson(model.asInstanceOf[NetworkTVCBannerModel])
            case BannerKind.NetworkTVCBanner =>
                WebUtils.toRawJson(model.asInstanceOf[NetworkOverlayBannerModel])
            case BannerKind.Tracking =>
                WebUtils.toRawJson((model.asInstanceOf[TrackingBanner]))
            case BannerKind.NetworkOverlayBanner =>
                WebUtils.toRawJson(model.asInstanceOf[NetworkOverlayBannerModel])
            case BannerKind.NetworkPauseAd =>
                WebUtils.toRawJson(model.asInstanceOf[NetworkPauseAdBannerModel])
            case BannerKind.NetworkExpandable =>
                WebUtils.toRawJson(model.asInstanceOf[NetworkExpandableBannerModel])
            case BannerKind.NetworkBalloon =>
                WebUtils.toRawJson(model.asInstanceOf[NetworkBalloonBannerModel])
            case BannerKind.NetworkCatfish =>
                WebUtils.toRawJson(model.asInstanceOf[NetworkCatfishBannerModel])
            case BannerKind.NetworkPopup =>
                WebUtils.toRawJson(model.asInstanceOf[NetworkPopupBannerModel])
            case BannerKind.Article =>
                WebUtils.toRawJson(model.asInstanceOf[ArticleModel])
            case BannerKind.NetworkPrBanner =>
                WebUtils.toRawJson(model.asInstanceOf[NetworkPrBannerModel])
            case BannerKind.PrBanner =>
                WebUtils.toRawJson(model.asInstanceOf[PrBannerModel])

        }
        new Banner(model.id, model.campaignId, model.name, model.targetUrl, model.thirdPartyImpressionUrl, model.width, model.height, model.kind, json, model.from, model.to, model.checkFreqSiteOrZone, model.disable)
    }

    def searchItems(query: String, pId: Int, searchBy: String): List[BannerModel] = env.bannerService.searchItems(query, pId, searchBy).map(b=>internalToModel(b))

    def getByTargetedZone(zid : Int, from : Int, count : Int) : PagingResult[BannerModel]  = {
        val networkBanners = loadAll().filter( b => b.isInstanceOf[INetwork]).toArray
        val items =  networkBanners.filter( b => getTargetZones(b.asInstanceOf[INetwork]).contains(zid))
        items.foreach(b=>b.currentUsage = -1)
        new PagingResult[BannerModel](items.slice(from,from + count), items.size)
    }

    def getByLinkedZone(zid : Int, from : Int, count : Int) : PagingResult[BannerModel] = {
        val now = System.currentTimeMillis()
        val books = env.newBookingService.getItemsByZoneId(zid).data.filter(item=> item.from <= now && item.to >= now).groupBy(item=>item.itemId).map(data => (data._1, data._2.map(item=>item.share).sum))
        val banners = listByIds(books.map(b => b._1).toList.sortWith(_>_))
        banners.foreach(b=>{
            b.currentUsage = books.find(d => d._1 == b.id) match {
                case Some(p) => p._2
                case _ => 0
            }
        })
        if (banners == null || banners.size <= from) {
            new PagingResult(Array[BannerModel](), 0)
        }else {
            val size = Math.min(count, banners.size - from)
            val pagingResult = new PagingResult(banners.slice(from,from+size).toArray, banners.size)
            pagingResult
        }
    }

    def getItemsByZoneId(zid : Int, from : Int, count: Int) : PagingResult[BannerModel] = {
        val zone = env.zoneService.load(zid)

        if(zone != null) {
            val allBanner = env.bannerService.list(f => f.kind.startsWith("network") && f.kind != "networkPR", transform => internalToModel(transform).asInstanceOf[INetwork]).filter(b => getTargetZones(b).contains(zid))
            val now = System.currentTimeMillis()
            val validNetworkItems = allBanner.filter(b=>{
                val instance = b
                if(instance.startDate <= now && (instance.endDate == 0 || instance.endDate >= now)) {
                    val campaign = env.campaignService.load(b.asInstanceOf[BannerModel].campaignId)
                    if(campaign != null) {
                        if(campaign.startDate <= now && campaign.endDate >= now && campaign.status == CampaignStatus.RUNNING) true
                        else false
                    } else false
                }
                else false
            })
            val data = validNetworkItems.map(_.asInstanceOf[BannerModel]) ++ getByLinkedZone(zid, 0, Int.MaxValue).data.toList

            new PagingResult[BannerModel](data.toArray.slice(from, count), data.length)


//            var networkItems = getByTargetedZone(zid, 0, Int.MaxValue)
//            val now = System.currentTimeMillis()
//            val validNetworkItems = networkItems.data.filter(b=>{
//                val instance = b.asInstanceOf[INetwork]
//                if(instance.startDate <= now && (instance.endDate == 0 || instance.endDate >= now)) {
//                    val campaign = env.campaignService.load(b.campaignId)
//                    if(campaign != null) {
//                        if(campaign.startDate <= now && campaign.endDate >= now && campaign.status == CampaignStatus.RUNNING) true
//                        else false
//                    } else false
//                }
//                else false
//            })
//            val data = validNetworkItems ++ getByLinkedZone(zid, 0, Int.MaxValue).data.toList
//
//            new PagingResult[BannerModel](data.toArray.slice(from, count), data.length)
        } else {
            new PagingResult[BannerModel](Array(),0)
        }
    }

    def getItemsBy(zid : Int, campaignId : Int, from : Int, count : Int) : PagingResult[BannerModel] = {
        val banners = getItemsByZoneId(zid,0,Int.MaxValue).data.filter(p => p.campaignId == campaignId)
        new PagingResult[BannerModel](banners.slice(from,count+from),banners.size)
    }

    def loadArticlesByCampaignId(campaignId:Int,status:Array[Int],from:Int,count:Int):PagingResult[ArticleModel] = {
        val articles = env.bannerService.loadAll.filter(b=>b.kind == BannerKind.Article).map(b=>toModel(b).asInstanceOf[ArticleModel]).filter(a=>a.campaignId==campaignId && (status.length == 0 || status.contains(a.status)))
        val selectedArticles = articles.slice(from,count+from)
        new PagingResult(selectedArticles.toArray,articles.size)
    }
    //filter

    def listByFilterInOrder(orderId:Int,status:Int,from: Int, count: Int, sortBy: String, direction: String, filter: String) : PagingResult[ArticleModel] = {
        val campaignIds = env.campaignService.listByReferenceId(0, Int.MaxValue, orderId).data.map(c=>c.id).toList
        var all = campaignIds.map(c=>env.bannerService.listByReferenceId(0, Int.MaxValue, c).data.toList.filter(a=>a.kind == BannerKind.Article)).
            foldLeft(List[Banner]()){_ ++ _}.map(c=>toModel(c).asInstanceOf[ArticleModel])

        if (!filter.isEmpty) {
            all = all.filter(a=>a.name.toLowerCase.contains(filter.toLowerCase))
        }
        if(status > 0) {
            all = all.filter(a=>a.status == status)
        }

        val c = new GenericComparator(Reflect.getProperties(classOf[ArticleModel]).get(sortBy).get.get)
        all = all.sortWith((x,y)=>c.compare(x,y)>0).toList
        if (direction == "asc")
            all = all.reverse
        val articles = all.slice(from, from+count)
        new PagingResult[ArticleModel](articles.toArray, all.size)
    }

    //filter
    def listByFilterInCampaign(campaignId:Int,status:Int,from: Int, count: Int, sortBy: String, direction: String, filter: String) : PagingResult[ArticleModel] = {
        var all = env.bannerService.listByReferenceId(0,Int.MaxValue,campaignId).data.toList.filter(b=>b.kind == BannerKind.Article).map(b=>toModel(b).asInstanceOf[ArticleModel])
        if (!filter.isEmpty) {
            all = all.filter(a=>a.name.toLowerCase.contains(filter.toLowerCase))
        }
        if(status > 0) {
            all = all.filter(a=>a.status == status)
        }

        val c = new GenericComparator(Reflect.getProperties(classOf[ArticleModel]).get(sortBy).get.get)
        all = all.sortWith((x,y)=>c.compare(x,y)>0).toList
        if (direction == "asc")
            all = all.reverse
        val articles = all.slice(from, from+count)
        new PagingResult[ArticleModel](articles.toArray, all.size)
    }

    def listByFilterInZone(zoneId:Int,status:Int,from: Int, count: Int, sortBy: String, direction: String, filter: String) : PagingResult[ArticleModel] = {
        var all = env.bannerService.loadAll().toList.filter(b=>b.kind == BannerKind.Article).map(b=>toModel(b).asInstanceOf[ArticleModel]).filter(b=>b.zoneId == zoneId)
        if (!filter.isEmpty) {
            all = all.filter(a=>a.name.toLowerCase.contains(filter.toLowerCase))
        }
        if(status > 0) {
            all = all.filter(a=>a.status == status)
        }

        val c = new GenericComparator(Reflect.getProperties(classOf[ArticleModel]).get(sortBy).get.get)
        all = all.sortWith((x,y)=>c.compare(x,y)>0).toList
        if (direction == "asc")
            all = all.reverse
        val articles = all.slice(from, from+count)
        new PagingResult[ArticleModel](articles.toArray, all.size)
    }

    //load all articles by zone id
    def loadArticlesByZoneId(zoneId:Int,status:Array[Int],from:Int,count:Int):PagingResult[ArticleModel] = {
        val articles = env.bannerService.loadAll().toList.filter(b=>b.kind == BannerKind.Article).map(b=>toModel(b).asInstanceOf[ArticleModel]).filter(a=> (status.length == 0 || status.contains(a.status)) && a.zoneId == zoneId)
        val selectedZoneArticles = articles.slice(from,count+from)
        new PagingResult[ArticleModel](selectedZoneArticles.toArray, articles.size)
    }
}

class BannerHandler(env: {
    val bannerModelService: IBannerModelService
    val campaignService: IDataService[Campaign]
    val orderService: IDataService[Order]
    val bannerService: IDataService[Banner]
    val zoneService: IDataService[Zone]
})
    extends RestHandler[BannerModel, Banner](() => null, env.bannerModelService, env.bannerService){

    override def getItemFromModel(item: BannerModel) : Any = env.bannerService.load(item.id)

    def beforeLink(request:HttpServletRequest, zones: Array[Int]) : Try[Unit] = {
        zones.foreach(zid=>{
            val zone = env.zoneService.load(zid)
            if(zone == null) return fail("Zone not found")
            if(!PermissionUtils.checkPermission(request, zone, Permission.WEBSITE_EDIT)) return fail("You don't have permission")
        })
        succeed()
    }

    def getSeconds(time_string: String):Int  = {
        val times = time_string.split(":")
        if (times.length != 3)
            return 0

        val hour = times(0).toInt
        val minute = times(1).toInt
        val second = times(2).toInt
        hour * 3600 + minute * 60 + second
    }

    // overlay tvc banner
    def resizeOverlayBanner(fileUrl: String, width: Int, height: Int): Unit = {
        if (!fileUrl.contains(Config.uploadDomain.getValue.replaceAll("http://", "").replaceAll("/", "")))
            return

        val fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1)
        if (fileName.isEmpty)
            return
        spawn {


            var overlayThumbSizes = List[OverlayBannerSize]()
            if (width > 300)
                overlayThumbSizes = overlayThumbSizes :+ new OverlayBannerSize(300, 60)
            if (width > 450)
                overlayThumbSizes = overlayThumbSizes :+ new OverlayBannerSize(450, 50)
            if (width > 468)
                overlayThumbSizes = overlayThumbSizes :+ new OverlayBannerSize(468, 60)
            if (width > 480)
                overlayThumbSizes = overlayThumbSizes :+ new OverlayBannerSize(480, 70)

            for (size <- overlayThumbSizes) {
                ImageUtils.resize(Config.uploadDir.getValue, fileName, size.width, size.height)
            }
        }
    }

    def getWrapperTvcDuration(tvc: NetworkTVCBannerModel): Long = {
        if (!tvc.extendURL.isEmpty && tvc.duration == 0) {
            try {
                val doc = Jsoup.connect(tvc.extendURL).get
                if (doc != null) {
                    val element = doc.select("Duration").first
                    if (element != null)
                        return getSeconds(element.text)
                }
                return 0
            }
            catch  {
                case e: Exception => e.printStackTrace
            }
        }
        tvc.duration
    }

    override def beforeSave(request: HttpServletRequest, instance: BannerModel): Try[Unit] = {
        if(instance.kind == BannerKind.NetworkHtml && instance.asInstanceOf[NetworkHtmlBannerModel].limitUnit.toLowerCase.equals("click")) return fail("Can't set click limit for network html banner")
        val campItem = env.campaignService.load(instance.campaignId)
        if(campItem == null) return fail("Invalid campaignId")
        val campaign = env.campaignService.load(instance.campaignId)
        if(campaign == null || campaign.status == CampaignStatus.TERMINATED) return fail("Can't edit terminated campaign")
        if(PermissionUtils.checkPermission(request, env.orderService.load(campItem.orderId), Permission.ORDER_EDIT)) {

            if (instance.kind == BannerKind.NetworkTVC) {
                instance.asInstanceOf[NetworkTVCBannerModel].duration = getWrapperTvcDuration(instance.asInstanceOf[NetworkTVCBannerModel])
            }
            succeed()
        }
        else fail("You don't have permission")
    }

    override def afterSave(instance: BannerModel): Unit = {
        if (instance.kind.compareTo(BannerKind.NetworkOverlayBanner) == 0 && Config.isResizeOverlayBanner) {
            // generate small size for overlay banner (in case embeded zingtv)
            val overlayBanner = instance.asInstanceOf[NetworkOverlayBannerModel]

            var imageFile = overlayBanner.bannerFile
            if (overlayBanner.bannerFile.endsWith(".swf")) {
                imageFile = overlayBanner.bannerFileFallback
            }

            if (imageFile.contains(Config.uploadDomain.getValue.replaceAll("http://", "").replaceAll("/", "")))
                resizeOverlayBanner(imageFile, overlayBanner.width, overlayBanner.height)
        }
        super.afterSave(instance)
    }

    override def beforeUpdate(request:HttpServletRequest, instance: BannerModel): Try[Unit] = {
        if(instance.kind == BannerKind.NetworkHtml && instance.asInstanceOf[NetworkHtmlBannerModel].limitUnit.toLowerCase.equals("click")) return fail("Can't set click limit for network html banner")
        val old = env.bannerService.load(instance.id)
        if(old == null) return fail("Item not found")
        if (instance.name == null || instance.name.equals("")) return fail("Name is null")
        if (instance.campaignId != old.campaignId) return fail("Can't change campaignId")
        val campaign = env.campaignService.load(instance.campaignId)
        if(campaign == null || campaign.status == CampaignStatus.TERMINATED) return fail("Can't edit terminated campaign")

        if (instance.kind == BannerKind.NetworkTVC) { // update duration for TVC
            instance.asInstanceOf[NetworkTVCBannerModel].duration = getWrapperTvcDuration(instance.asInstanceOf[NetworkTVCBannerModel])
        }
        succeed()
    }

    override def afterUpdate(old: BannerModel, instance: BannerModel): Unit = {
        spawn {
            if (instance.kind.compareTo(BannerKind.NetworkOverlayBanner) == 0 && Config.isResizeOverlayBanner) {
                val newItem = instance.asInstanceOf[NetworkOverlayBannerModel]
                val oldItem = old.asInstanceOf[NetworkOverlayBannerModel]


                var newValue = newItem.bannerFile
                if (newItem.bannerFile.endsWith(".swf")) {
                    newValue = newItem.bannerFileFallback
                }

                var oldValue = oldItem.bannerFile
                if (oldItem.bannerFile.endsWith(".swf")) {
                    oldValue = oldItem.bannerFileFallback
                }

                if (newValue.compareTo(oldValue) == 0 ||
                    !newValue.contains(Config.uploadDomain.getValue.replaceAll("http://", "").replaceAll("/", "")))
                    return

                resizeOverlayBanner(newValue, newItem.width, newItem.height)
            }

        }
    }

    @Invoke(Parameters = "request")
    override def save(request: HttpServletRequest): Any = {
        val instance = readInstance(request)
        val s = beforeSave(request, instance)
        if (!s.isSuccess) return s
        instance.id = modelService.save(instance)
        afterSave(instance)
        Json(instance)
    }

    @Invoke(Parameters = "request,id")
    override def update(request: HttpServletRequest, id: Int): Any = {
        val instance = readInstance(request)
        val s = beforeUpdate(request, instance)
        if (!s.isSuccess) return s
        val old = modelService.load(instance.id)
        val ret = modelService.update(instance)
        afterUpdate(old, instance)
        Text("{\"result\": \"success\"}")
    }

    def readInstance(request: HttpServletRequest): BannerModel = {
        val kind = request.getParameter("kind")
        kind match{
            case BannerKind.Media => readFromRequest[MediaBannerModel](request, new MediaBannerModel(0, 0, null, null, null, 0, 0, null, null, 0))
            case BannerKind.Html => {
                val embed = request.getParameter("embeddedHtml")
                val instance = readFromRequest[HtmlBannerModel](request, new HtmlBannerModel(0,0,"","",List(""),""))
                instance.embeddedHtml = embed
                instance
            }
            case BannerKind.Ecommerce => readFromRequest[EcommerceBannerModel](request, new EcommerceBannerModel(0,0,null,null,null,null,0,0,0,null))
            case BannerKind.Expandable =>
                readFromRequest[ExpandableBannerModel](request, new ExpandableBannerModel(0,0,null,null,null,0,null,null,null,0,0,0,0,0,0,0,0,"" ,""))
            case BannerKind.Balloon => readFromRequest[BalloonBannerModel](request, new BalloonBannerModel(0,0,"","",null,0,"","","",0,0,0,0,0,0,ExpandTVCExtension.NONE,"","",""))
            case BannerKind.Catfish => readFromRequest[CatfishBannerModel](request, new CatfishBannerModel(0,0,"","",null,0,0,"","",0))
            case BannerKind.Popup => readFromRequest[PopupBannerModel](request, new PopupBannerModel(0, 0, null, null, null, 0, 0, null, null, 0))
            case BannerKind.NetworkMedia => readFromRequest[NetworkMediaBannerModel](request, new NetworkMediaBannerModel(0,0,"","",List(""),0,0,false,"","","",0,RateUnit.CPC,0,0,"",0,"",0,"","",null,null,0,0,0,0,0,""))
            case BannerKind.NetworkHtml => readFromRequest[NetworkHtmlBannerModel](request, new NetworkHtmlBannerModel(0,0,"","",List(""),0,0,"",0,RateUnit.CPC,0,0,"",0,"",0,"","",null,null,0,"",0,0,0,0))
            case BannerKind.NetworkTVC => {
                val ins = readFromRequest[NetworkTVCBannerModel](request, new NetworkTVCBannerModel(0,0,"","",List(""),0,0,"",0,RateUnit.CPC,0,0,"",0,"",0,"","",null,null, null,0,false,"","",0,0,0,0, null))
                var json = request.getParameter("positions")
                if(json != null){
                    json = "["+json+"]";
                    val ob : Array[TVCItemPosition] = WebUtils.fromJson(classOf[Array[TVCItemPosition]],json)
                    ins.positions = ob
                    ins.targetZones = ob.map(t=>t.zoneId).toArray
                }
                json = request.getParameter("thirdParty")
                if (json != null) {
                    var thirdParty = WebUtils.fromJson(classOf[NetworkTVCThirdPartyTracking], json)
                    thirdParty.impression = java.net.URLDecoder.decode(thirdParty.impression, "UTF-8")
                    thirdParty.click = java.net.URLDecoder.decode(thirdParty.click, "UTF-8")
                    thirdParty.complete = java.net.URLDecoder.decode(thirdParty.complete, "UTF-8")
                    ins.thirdParty = thirdParty
                }

                ins
            }
            case BannerKind.Tracking => readFromRequest[TrackingBanner](request, new TrackingBanner(0,0,"","","",0))
            case BannerKind.NetworkPauseAd => readFromRequest[NetworkPauseAdBannerModel](request, new NetworkPauseAdBannerModel(0,0,"","",null,0,0,"","",0,RateUnit.CPM,0,0,LimitUnit.IMPRESSION,0,"",0,"","",null,null,"","",0,0,0,0))
            case BannerKind.NetworkOverlayBanner => {
                val ins = readFromRequest[NetworkOverlayBannerModel](request, new NetworkOverlayBannerModel(0,0,"","",List(""),0,0,"","",0,RateUnit.CPC,0,0,"",0,"",0,"","",null,null,null,0,false,"","",0,0,0,0))
                var json = request.getParameter("positions")
                if(json != null){
                    json = "["+json+"]"
                    val ob : Array[TVCItemPosition] = WebUtils.fromJson(classOf[Array[TVCItemPosition]],json)
                    ins.positions = ob
                    ins.targetZones = ob.map(t=>t.zoneId).toArray
                }
                ins
            }
            case BannerKind.NetworkExpandable =>
                readFromRequest[NetworkExpandableBannerModel](request, new NetworkExpandableBannerModel(0,0,"","",null,0,"","","",0,0,0,0,0,0,0,0,"","",null,null,"",0,0,"",0,0,0,0,0,0,0,0))
            case BannerKind.NetworkBalloon =>
                readFromRequest[NetworkBalloonBannerModel](request, new NetworkBalloonBannerModel(0,0,"","",null,0,"","","",0,0,0,0,0,0,ExpandTVCExtension.NONE,"","","",null,null,"",0,0,"",0,0,0,0,0,0,0,RateUnit.CPM))
            case BannerKind.NetworkCatfish =>
                readFromRequest[NetworkCatfishBannerModel](request, new NetworkCatfishBannerModel(0,0,"","",null,0,0,false,"","","",0,RateUnit.CPM,0,0,"",0,"",0,"","",null,null,0,0,0,0,0))
            case BannerKind.NetworkPopup =>
                readFromRequest[NetworkPopupBannerModel](request, new NetworkPopupBannerModel(0,0,"","",List(""),0,0,false,"","","",0,RateUnit.CPC,0,0,"",0,"",0,"","",null,null,0,0,0,0,0,""))
            case BannerKind.NetworkPrBanner =>
                readFromRequest[NetworkPrBannerModel](request, new NetworkPrBannerModel(0,0,"","",List(""),0,null,null,null,"",0,0,"",0,0,0,0,0,RateUnit.CPC,0,0,true))
            case BannerKind.PrBanner =>
                readFromRequest[PrBannerModel](request, new PrBannerModel(0,0,"","",null,0,null))
        }
    }

    @Invoke(Parameters = "request,zid,from,count")
    def getByTargetedZones(request: HttpServletRequest, zid : Int, from : Int, count : Int) = {
        SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request,item, Permission.WEBSITE_VIEW_INFO))
        Json(env.bannerModelService.getByTargetedZone(zid,from,count))
    }

    @Invoke(Parameters = "request,id,from,count")
    def getItemsByZoneId(request: HttpServletRequest, id: Int, from: Int, count: Int) = {
        if(PermissionUtils.checkPermission(request,env.zoneService.load(id), Permission.WEBSITE_VIEW_INFO))
            Json(env.bannerModelService.getItemsByZoneId(id, from, count))
        else Json(new PagingResult[BannerModel](new Array[BannerModel](0),0))
    }

    @Invoke(Parameters = "request,id,from,count")
    def getItemsByOrderId(request: HttpServletRequest, id: Int, from: Int, count: Int) = {
        SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request,item, Permission.ORDER_VIEW_INFO))
        val banners = env.campaignService.listByReferenceId(id = id).data.flatMap(c=>env.bannerModelService.listByReferenceId(id = c.id).data).toList.distinct.sortWith(_.id > _.id)
        if (banners == null || banners.size <= from) {
            Json(new PagingResult(new Array[Int](0), 0))
        } else {
            val size = Math.min(count, banners.size - from)
            val pagingResult = new PagingResult(banners.slice(from,from+size).toArray, banners.size)
            Json(pagingResult)
        }
    }

    @Invoke(Parameters = "request,query,pId,searchBy")
    def searchItems(request: HttpServletRequest, query: String, pId: Int, searchBy: String) = {
        SecurityContext.set((item:Any)=>PermissionUtils.checkPermission(request, item, Permission.ORDER_VIEW_INFO))
        Json(env.bannerModelService.searchItems(query, pId, searchBy))
    }

    @Invoke(Parameters = "id")
    def getTrackingLinks(id: Int) = {
        val banner = env.bannerModelService.load(id).asInstanceOf[TrackingBanner]
        val bannerFile = banner.bannerFile
        var default = Map("zoneId" -> SecurityUtils.encode(banner.zoneId), "token" -> "0", "ip" -> "", "itemId" -> SecurityUtils.encode(banner.id))
        if(banner.retargeting > 0) default += ("retargeting" -> SecurityUtils.encode(banner.retargeting))
        val impression = AdsUtils.getTrackUrl("display",
            default + ("track" -> TrackKind.Impression))
        val impressionPixel = AdsUtils.getTrackUrl("display",
            default + ("track" -> TrackKind.Impression) + ("pixel" -> "true"))
        val click = AdsUtils.getTrackUrl("click",
            default + ("track" -> TrackKind.Click), banner.targetUrl)
        val clickPixel = AdsUtils.getTrackUrl("click",
            default + ("track" -> TrackKind.Click) + ("pixel" -> "true"))
        Text("{\"data\":{\"bannerFile\":\""+ bannerFile+"\",\"impression\":\""+impression+"\",\"impressionPixel\":\""+impressionPixel+"\",\"click\":\""+click+"\",\"clickPixel\":\""+clickPixel+"\"}}")
    }


}

class BannerServlet extends HandlerContainerServlet {
    def factory(): BaseHandler = new BannerHandler(Environment)
}
