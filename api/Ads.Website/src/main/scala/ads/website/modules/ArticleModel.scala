package ads.website.modules

import ads.common.{SecurityUtils, UserContext, IItem}
import ads.common.database.{IDataService, AbstractDelegateDataService, PagingResult}
import ads.common.model._
import ads.common.services.{IUserService, IUserRoleService}
import ads.web._
import ads.website.{PermissionUtils, Environment}
import ads.website.handler.RestHandler
import ads.web.mvc.{Text, Json, BaseHandler}
import javax.servlet.http.HttpServletRequest
import ads.common.Syntaxs.{succeed, Try, fail}
import scala.collection.mutable.ArrayBuffer
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient
import java.util
import org.apache.http.{HttpHost, NameValuePair}
import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.util.EntityUtils
import org.apache.http.conn.params.ConnRoutePNames
import ads.website.modules.serving.TrackKind
import java.util.Properties
import javax.mail.internet.InternetAddress
import java.io.File

/**
 *
 * Created by cuongtv2 on 12/19/13.
 */
class ArticleCommentModel (
  var id:Int,
  var articleId:Int,
  var comment:String,
  var kind:String,
  var owner:User,
  var date:Long,
  var disable: Boolean = false
  ) extends IItem

class ArticleCommentModelService(env : {
val articleCommentService: IDataService[ArticleComment]
val userService: IDataService[User]
}) extends AbstractDelegateDataService[ArticleComment,ArticleCommentModel](env.articleCommentService) {
    def toModel(item: ArticleComment): ArticleCommentModel = {
        val user = env.userService.load(item.ownerId)
        val userId = {
            if(user == null) 0
            else user.id
        }
        new ArticleCommentModel(item.id, item.articleId, item.comment,item.kind,user,item.date)
    }

    def fromModel(model: ArticleCommentModel): ArticleComment = new ArticleComment(model.id,model.articleId,model.comment,model.kind,model.owner.id,model.date, model.disable)
}




object defaultArticleModel {
    def apply() = new ArticleModel(0,"","","","",ArticleStatus.PENDING,null,null,0,"","","",false,0,0,0,0,0,0,null,null,0,0,0,0,0,0,0,false,0,"","")
}


//controller processing your request
class ArticleRestHandler(env:{
    val bannerModelService:IBannerModelService
    val bannerService:IDataService[Banner]
    val campaignService:IDataService[Campaign]
    val websiteModelService:IWebsiteModelService
    val zoneService:IDataService[Zone]
    val articleCommentModelService:IDataService[ArticleCommentModel]
    val userRoleService: IUserRoleService
    val userService: IUserService
}) extends RestHandler[BannerModel, Banner](() => null, env.bannerModelService, env.bannerService){

    @Invoke(Parameters = "request")
    override def save(request: HttpServletRequest): Any = {
        val instance = readInstance(request)
        val s = beforeSave(request, instance)
        if (!s.isSuccess) return s
        instance.id = modelService.save(instance)
        afterSave(instance)
        notify(instance)
        Json(instance)
    }

    override def afterSave(instance: BannerModel) {
        if(instance.asInstanceOf[ArticleModel].retargetingEnable) {
            instance.asInstanceOf[ArticleModel].retargeting = instance.id
            modelService.update(instance)
        }
        super.afterSave(instance)
    }

    @Invoke(Parameters = "request,id")
    override def update(request: HttpServletRequest, id: Int): Any = {
        val instance = readInstance(request)
        val s = beforeUpdate(request, instance)
        if (!s.isSuccess) return s
        val old = service.load(id)
        if(instance.asInstanceOf[ArticleModel].retargetingEnable) {
            instance.asInstanceOf[ArticleModel].retargeting = instance.id
        } else {
            instance.asInstanceOf[ArticleModel].retargeting = 0
        }
        val ret = modelService.update(instance)
        if(old.asInstanceOf[ArticleModel].retargetingEnable != instance.asInstanceOf[ArticleModel].retargetingEnable){
            //TODO: update tracking link
        }
        notify(instance)
        Text("{\"result\": \"success\"}")
    }

    def readInstance(request: HttpServletRequest): BannerModel = {
        val kind = request.getParameter("kind")
        kind match{
            case BannerKind.Article => readFromRequest[ArticleModel](request, new ArticleModel(0,"","","","",0,null,null,0,"","","",false,0,0,0,0,0,0,null,null,0,0,0,0,0,0,0,false, 0, "",""))
        }
    }

    //update article status
    @Invoke(Parameters="request,articleId,status")
    def updateArticleStatus(request:HttpServletRequest,articleId:Int,status:Int) = {
        val instance = modelService.load(articleId)
        if(instance != null && instance.isInstanceOf[ArticleModel]) {
            instance.asInstanceOf[ArticleModel].status = status
            modelService.update(instance)
            afterUpdate(instance,instance)
            Text("{\"result\": \"success\"}")
        } else fail("Item not found")
    }

    @Invoke(Parameters="request,id")
    def getTrackingLink(request: HttpServletRequest, id: Int) : Any = {
        val instance = env.bannerModelService.load(id)
        if (instance == null || !instance.isInstanceOf[ArticleModel]) return fail("Invalid item")
        val item = instance.asInstanceOf[ArticleModel]
        val trackingLink = AdsUtils.getTrackPRUrl("display", Map("track" -> TrackKind.Impression, "zoneId" -> SecurityUtils.encode(item.zoneId), "itemId" -> SecurityUtils.encode(item.id),"retargeting" -> SecurityUtils.encode(item.retargeting)))
        Text(trackingLink)
    }

    @Invoke(Parameters="request,articleId")
    def syncToCMS(request: HttpServletRequest, articleId: Int): Any = {
        val instance = env.bannerModelService.load(articleId)
        if (instance == null || !instance.isInstanceOf[ArticleModel]) return fail("Invalid item")
        val item = instance.asInstanceOf[ArticleModel]
        if (item.status == ArticleStatus.WAITING_FOR_SYNC_TO_CMS) {
            item.websiteId match {
                case Publisher.ZINGNEWS => {
                    val token = SecurityUtils.sha256Hash(((item.name + item.summary + item.source + item.tags + item.creator + item.createDate + Config.secretKeyZNews.getValue)))

                    val post = new HttpPost(Config.urlCreateArticleZNews.getValue)

                    val client = new DefaultHttpClient
                    val proxy = new HttpHost(Config.proxyHost.get(), Config.proxyPort.get(), "http")
                    post.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy)
                    val nameValuePairs = new util.ArrayList[NameValuePair]()
                    nameValuePairs.add(new BasicNameValuePair("title", item.name))
                    nameValuePairs.add(new BasicNameValuePair("summary", item.summary))
                    nameValuePairs.add(new BasicNameValuePair("source", item.source))
                    nameValuePairs.add(new BasicNameValuePair("author", item.author))
                    nameValuePairs.add(new BasicNameValuePair("note", item.tags))
                    nameValuePairs.add(new BasicNameValuePair("clientid", item.creator.toString))
                    nameValuePairs.add(new BasicNameValuePair("timestamp", item.createDate.toString))
                    nameValuePairs.add(new BasicNameValuePair("mac", token))
                    val trackingLink = AdsUtils.getTrackPRUrl("display", Map("track" -> TrackKind.Impression, "zoneId" -> SecurityUtils.encode(item.zoneId), "itemId" -> SecurityUtils.encode(item.id),"retargeting" -> SecurityUtils.encode(item.retargeting)))
                    nameValuePairs.add(new BasicNameValuePair("tracking", trackingLink))
                    post.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"))
                    // send the post request
                    try{
                        val response = client.execute(post)
//                    println("--- HEADERS ---")
                    if (response.getStatusLine().getStatusCode() == 200) {
                        val source = scala.io.Source.fromInputStream(response.getEntity().getContent()).getLines().mkString("\n")
//                        println(source)
                        val map = WebUtils.fromJson(classOf[java.util.HashMap[String, Object]], source.substring(source.lastIndexOf("{")))
                        if (map.containsKey("articleid")) {
                            item.pubArticleId = map.get("articleid").toString.toFloat.toInt
                            item.status = ArticleStatus.WAITING_FOR_APPROVAL_BY_PUBLISHER
                            item.companionTargeting = "zn"+item.pubArticleId
                            env.bannerModelService.update(item)
                            Text("{\"result\": \""+ item.pubArticleId +"\"}")
                        } else {
                            fail("Have exception in sync to cms !!!")
                        }

                        } else {
                            fail("Have exception in sync to cms")
                        }

                    }catch{
                        case e:Exception=> e.printStackTrace()
                    }
                }
                case _ => fail("Unsupported website")
            }
        } else fail("Wrong status")
    }

    @Invoke(Parameters="request,zoneId,status,from,count")
    def loadArticlesByZoneId(request:HttpServletRequest,zoneId:Int,status:Array[Int],from:Int,count:Int) = Json(env.bannerModelService.loadArticlesByZoneId(zoneId,status,from,count))

    @Invoke(Parameters="request,campaignId,from,count,status")
    def loadArticlesByCampaignId(request:HttpServletRequest,campaignId:Int,from:Int,count:Int,status:Array[Int]) = Json(env.bannerModelService.loadArticlesByCampaignId(campaignId,status,from,count))

    @Invoke(Parameters="request,orderId,from,count")
    def getArticlesInOrder(request:HttpServletRequest,orderId:Int,status:Array[Int],from:Int,count:Int) = {
        val campaignIds = env.campaignService.listByReferenceId(0, Int.MaxValue, orderId).data.toList.map(c=>c.id)
        val data = new ArrayBuffer[ArticleModel]()
        campaignIds.foreach(c=>{
            data ++= env.bannerModelService.loadArticlesByCampaignId(c, status, 0, Int.MaxValue).data
        })
        Json(new PagingResult[ArticleModel](data.slice(from, from+count).toArray,data.size))

    }
    @Invoke(Parameters="request")
    def listPRZonesOfPublisher(request:HttpServletRequest) = Json(env.websiteModelService.loadZonesByGroupWebsite(ZoneKind.PRNetwork))

    override def beforeSave(request: HttpServletRequest, instance: BannerModel): Try[Unit] = {
        val user = UserContext.get()
        if(user == null) return fail("Invalid session")
        if(!instance.isInstanceOf[ArticleModel]) return fail("Invalid item")
        instance.asInstanceOf[ArticleModel].creator = user.id
        instance.asInstanceOf[ArticleModel].createDate = System.currentTimeMillis()
        instance.asInstanceOf[ArticleModel].status = ArticleStatus.PENDING
        instance.asInstanceOf[ArticleModel].updateDate = 0
        succeed()
    }

    override def beforeUpdate(request: HttpServletRequest, instance: BannerModel) : Try[Unit] = {
        val user = UserContext.get()
        if(user == null) return fail("Invalid session")
        val old = env.bannerModelService.load(instance.id)
        if(old == null || !old.isInstanceOf[ArticleModel]) return fail("Item not found")

        old.asInstanceOf[ArticleModel].status match {
            case ArticleStatus.PENDING => {
                if(!PermissionUtils.checkPermission(request, instance, Permission.WEBSITE_APPROVE)) return fail("You don't have permission!")
                if(instance.asInstanceOf[ArticleModel].status != ArticleStatus.WAITING_FOR_SYNC_TO_CMS && instance.asInstanceOf[ArticleModel].status != ArticleStatus.REJECT_BY_REVIEWER) return fail("Wrong status!")
                instance.asInstanceOf[ArticleModel].reviewDate = System.currentTimeMillis()
                instance.asInstanceOf[ArticleModel].reviewer = user.id
            }
/*            case ArticleStatus.COMPOSING | ArticleStatus.COMPOSING_AFTER_REJECTED_BY_PUBLISHER => {
                if(!PermissionUtils.checkPermission(request, instance, Permission.WEBSITE_PR_EDITOR)) return fail("You don't have permission!")
                instance.asInstanceOf[ArticleModel].status = ArticleStatus.WAITING_APPROVAL_BY_SALE
                instance.asInstanceOf[ArticleModel].editorDate = System.currentTimeMillis()
                instance.asInstanceOf[ArticleModel].editor = user.id
            }*/
            case ArticleStatus.REJECT_BY_REVIEWER => {
                if(!PermissionUtils.checkPermission(request, instance, Permission.WEBSITE_BOOKING)) return fail("You don't have permission!")
                if(instance.asInstanceOf[ArticleModel].status != ArticleStatus.PENDING) {
                    instance.asInstanceOf[ArticleModel].status = old.asInstanceOf[ArticleModel].status
                    instance.disable = true
                }else {
                    instance.asInstanceOf[ArticleModel].updateDate = System.currentTimeMillis()
                    instance.asInstanceOf[ArticleModel].updater = user.id
                }
            }
            case ArticleStatus.WAITING_APPROVAL_BY_SALE => {
                if(!PermissionUtils.checkPermission(request, instance, Permission.WEBSITE_BOOKING)) return fail("You don't have permission!")
                if(instance.asInstanceOf[ArticleModel].status != ArticleStatus.WAITING_FOR_SYNC_TO_CMS && instance.asInstanceOf[ArticleModel].status != ArticleStatus.PENDING) return fail("Wrong status")
            }
        }

        if(old.asInstanceOf[ArticleModel].pubArticleId != 0) instance.asInstanceOf[ArticleModel].pubArticleId = old.asInstanceOf[ArticleModel].pubArticleId
        instance.asInstanceOf[ArticleModel].publishedUrl = old.asInstanceOf[ArticleModel].publishedUrl
        instance.asInstanceOf[ArticleModel].companionTargeting = old.asInstanceOf[ArticleModel].companionTargeting
        succeed()
    }

    @Invoke(Parameters="request,articleId,comment")
    def addComment(request:HttpServletRequest,articleId:Int,comment:String) : Any = {
        val user = UserContext.get()
        if(user == null) return fail("Invalid user")
        val instance = new ArticleCommentModel(0,articleId,comment,CommentKind.COMMENT,user,System.currentTimeMillis())
        env.articleCommentModelService.save(instance)
    }
    @Invoke(Parameters="articleId,from,count")
    def listComments(articleId:Int,from:Int,count:Int):Any = {
        val all = env.articleCommentModelService.listByReferenceId(0, Int.MaxValue, articleId).data
        Json(new PagingResult[ArticleCommentModel](all.slice(from, from+count).toArray,all.size))
    }
    @Invoke(Parameters="request,orderId,status,from,count")
    def loadArticlesInOrderByOwner(request:HttpServletRequest,orderId:Int,status:Array[Int],from:Int,count:Int) = {
        val campaignIds = env.campaignService.listByReferenceId(0, Int.MaxValue, orderId).data.toList.map(c=>c.id)
        val data = new ArrayBuffer[ArticleModel] ()
        val userId = {
            val user = UserContext.get()
            if(user == null) 0
            else user.id
        }

        campaignIds.foreach(c=>{
            data ++= env.bannerModelService.loadArticlesByCampaignId(c, status, 0 , Int.MaxValue).data.filter(b=>b.creator == userId)
        })
        Json(new PagingResult[ArticleModel](data.slice(from, from+count).toArray,data.size))
    }

    @Invoke(Parameters="request,status,from,count")
    def loadArticlesByPublisher(request:HttpServletRequest,status:Array[Int],from:Int,count:Int) = {
        //load website id that assigned this user
        val ownerId = {
            val user = UserContext.get()
            if(user == null) 0
            else user.id
        }

        val siteIds = env.websiteModelService.listByReferenceId(0,Int.MaxValue,ownerId).data.map(f=>f.id)
        val all = env.bannerModelService.list(a=> a.kind == BannerKind.Article && ((status.length == 0 || status.contains(a.asInstanceOf[ArticleModel].status)) && siteIds.contains(a.asInstanceOf[ArticleModel].websiteId))).map(b=>b.asInstanceOf[ArticleModel])
        Json(new PagingResult[ArticleModel](all.slice(from, from+count).toArray,all.size))
    }

    @Invoke(Parameters="request,query,orderId,status,from,count")
    def searchArticlesInOrder(request:HttpServletRequest,query:String,orderId:Int,status:Int,from:Int,count:Int):Any = {
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
        val result = env.bannerModelService.listByFilterInOrder(orderId,status,from,count,sortBy,direction,query)
        Json(result)

    }

    @Invoke(Parameters="request,query,campaignId,status,from,count")
    def searchArticlesInCampaign(request:HttpServletRequest,query:String,campaignId:Int,status:Int,from:Int,count:Int) = {
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

        val result = env.bannerModelService.listByFilterInCampaign(campaignId,status,from,count,sortBy,direction,query)
        Json(result)
    }
    @Invoke(Parameters="request,query,zoneId,status,from,count")
    def searchArticlesInZone(request:HttpServletRequest,query:String,zoneId:Int,status:Int,from:Int,count:Int) = {
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
        val result = env.bannerModelService.listByFilterInZone(zoneId,status,from,count,sortBy,direction,query)
        Json(result)
    }

    def notify(instance: BannerModel){
        val article = instance.asInstanceOf[ArticleModel]
        if (article.status != ArticleStatus.WAITING_APPROVAL_BY_SALE && article.status != ArticleStatus.PENDING
            && article.status != ArticleStatus.WAITING_FOR_SYNC_TO_CMS)
            return

        val sender = Config.smtpUser.getValue
        var receiver = "";
        var subject = Config.mailSubject.getValue
        var body = ""
        val engine = new TemplateEngine(){
            workingDirectory = new File(Config.pathTemplate.getValue)
        }
        var link = ""
        var action = "approving"
        article.status match {
            case ArticleStatus.WAITING_APPROVAL_BY_SALE => {
                val creator = env.userService.load(article.creator)
                val campaign = env.campaignService.load(article.campaignId);
                if (campaign == null) return
                val orderId = campaign.orderId
                link = Config.articleSaleURL.getValue
                link = link.replace("orderId", orderId.toString)
                link = link.replace("campaignId", article.campaignId.toString)
                link = link.replace("articleId", article.id.toString)
                body = engine.layout("templates/mailpr.mustache",
                    Map(
                        "nameto" -> creator.name,
                        "action"-> action,
                        "link"-> link
                    ))
                receiver = creator.email;
            }
            case ArticleStatus.PENDING | ArticleStatus.WAITING_FOR_SYNC_TO_CMS =>{
                val website = env.websiteModelService.load(article.websiteId)
                if (website == null) return ;
                val publisher = env.userService.load(website.ownerId);
                if (article.status == ArticleStatus.WAITING_FOR_SYNC_TO_CMS) action = "synchronizing"
                link = Config.articlePublisherURL.getValue
                link = link.replace("siteId", article.websiteId.toString)
                link = link.replace("zoneId", article.zoneId.toString)
                link = link.replace("articleId", article.id.toString)
                body = engine.layout("templates/mailpr.mustache",
                    Map(
                        "nameto" -> publisher.name,
                        "action"-> action,
                        "link"-> link
                    ))
                receiver = publisher.email;
            }
        }
        subject = subject.replace("ACTION", action)
        subject = subject.replace("ITEMNAME", article.name)
        new MailService().send(sender, receiver, subject, body);
    }

    @Invoke(Parameters = "request,id,api")
    def publish(request: HttpServletRequest, id: Int, api: String): Any = {
        if(api != "asdfghjkl-POIUYTREWQ") return fail()
        val old = service.load(id)
        old.asInstanceOf[ArticleModel].publishedUrl = "http://zing.vn"
        old.asInstanceOf[ArticleModel].status = ArticleStatus.PUBLISHED
        val ret = service.update(old)
        Text("{\"result\": \"success\"}")
    }
}

//accept http request
class ArticleServlet extends HandlerContainerServlet {
    def factory():BaseHandler = new ArticleRestHandler(Environment)
}







