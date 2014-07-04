package ads.common.model

import ads.common.IItem
import java.util
import ads.common.model.ActionType.ActionType
import scala.collection.mutable.ArrayBuffer

//class AdsRequest(
//                    val site: Int,
//                    val zoneId: Int,
//                    val url: String,
//                    val ip: String,
//                    val cookie: String,
//                    val viewed: List[Int],
//                    val items: Int = 1,
//                    val listValues:Map[String,List[String]] = null)

class Track(
               val site: String,
               val zoneId: String,
               val url: String,
               val ip: String,
               val cookie: String,
               val kind: String = "Impression")

class AdsResponse(val banner: Array[Banner])

class VideoAdsResponse(val data: Map[String, Array[Array[BannerModel]]])

class VideoCompanionInfo(val tvcId: String, val videoId: String, val companions: List[java.util.Map[String, String]], val videoZoneId: String)

class ZingTVVideoAdsResponse(val data: Map[String, Array[(BannerModel, Int, Int, Map[Int, BannerModel])]])

class ZoneGroup(var id: Int, var siteId: Int, var name: String, var disable: Boolean = false) extends IItem

class ZoneToZoneGroup(var id: Int, var zoneId: Int, var zoneGroup: Int, var disable: Boolean = false) extends IItem

object ZoneKind {
    val Banner = "banner"
    val Video = "video"
    val PRNetwork="pr"
}




class ZoneToBanner(var id: Int, var itemId: Int, var zoneId: Int, var status : Int, var disable: Boolean = false) extends IItem

object ZoneToBannerStatus extends Enumeration {
    val PENDING = 0
    val APPROVED = 1
    val REJECTED = 2
}

class Website(var id: Int, var ownerId: Int, var name: String, var description: String, var reviewType: String, var frequencyCapping: Int = 0, var frequencyCappingTime: Int = 0, var disable: Boolean = false) extends IItem

class Banner (var id: Int, var campaignId: Int, var name: String, var targetUrl: String, var thirdPartyImpressionUrl: List[String], var width: Int, var height: Int, var kind: String, var extra: String, var from: Long, var to: Long, var checkFreqSiteOrZone: Boolean = false, var disable: Boolean = false) extends IItem

object OverlayBannerSize {
    def apply(sizes: String) = {
        val arr = sizes.split("x")
        new OverlayBannerSize(arr(0).toInt, arr(1).toInt)
    }
}

class OverlayBannerSize(val width: Int, val height: Int)

object OrderStatus {
    val PAUSED = "Paused"
    val RUNNING = "Running"
    val TERMINATED = "Terminated"
}
class Order(var id: Int, var ownerId: Int, var name: String, var description: String,var status: String = OrderStatus.RUNNING, var disable: Boolean = false) extends IItem

object CampaignType {
   val Booking = "booking"
   val Network = "network"
   val NetworkTVC = "networkTVC"
    val PRNetwork = "networkPR"
}

object CampaignStatus {
    val PAUSED = "Paused"
    val RUNNING = "Running"
    val TERMINATED = "Terminated"
    val PENDING = "Pending"
    val SCHEDULED = "Scheduled"
    val OUT_OF_BUDGET = "Out_of_budget"
    val EXPIRED = "Expired"
    val FINISHED = "Finished"
    val ALL_STATUS = Array(PAUSED,RUNNING,TERMINATED,PENDING,SCHEDULED,OUT_OF_BUDGET,EXPIRED,FINISHED)
}
class Campaign(var id: Int, var orderId: Int, var name: String, var status: String,var startDate : Long, var endDate : Long, var campaignType: String, var companion : Boolean, var extra : String, var disable: Boolean = false) extends IItem

class BookRecord(var id: Int, var campaignId: Int, var zoneId: Int, var share: Float, var from: Long, var to: Long, var disable: Boolean = false) extends IItem

class Role (var id : Int,
            var name :String ,
            var description : String,
            var permissions : Long,
            var objName : String,
            var disable: Boolean = false) extends  IItem

class UserRole(var id : Int,
               var userId : Int,
               var objName : String,
               var itemId : Int,
               var roles : Array[Int],
               var disable: Boolean = false) extends IItem


object Publisher {
    val ZINGNEWS = Config.mapPublishers("zingnews")
}

class BannerModel(
 var id: Int,
 var campaignId: Int,
 var name: String,
 var targetUrl: String,
 var thirdPartyImpressionUrl: List[String],
 var width: Int,
 var height: Int,
 var kind: String,
 var currentUsage: Float = 0,
 var from: Long = 0,
 var to: Long = 0,
 var extendData : util.HashMap[String, String] = new util.HashMap[String, String](),
 var checkFreqSiteOrZone: Boolean = false,
 var disable: Boolean = false
 )
    extends IItem

object BannerKind{
    val Media = "media"
    val Ecommerce = "Ecommerce"
    val Html  = "html"
    val NetworkMedia = "networkMedia"
    val NetworkHtml = "networkHtml"
    val NetworkTVC = "networkTVC"
    val NetworkOverlayBanner = "networkOverlayBanner"
    val NetworkTVCBanner = "networkTVCBanner"
    val Expandable = "expandable"
    val NetworkExpandable = "networkExpandable"
    val Tracking = "tracking"
    val Catfish = "catfish"
    val NetworkCatfish = "networkCatfish"
    val Balloon = "balloon"
    val NetworkBalloon = "networkBalloon"
    val Popup = "popup"
    val NetworkPopup = "networkPopup"
    val Article = "article"
    val NetworkPauseAd = "networkPauseAd"
    val PrBanner = "prBanner"
    val NetworkPrBanner = "networkPrBanner"
}

object ZingTVVideoKind {
    val Skip = "S"
    val Standard = "D"
    val Trueview = "T"
    val Overlay = "O"
}

object PrCategoryKind {
    val World = 0
    val Cuture = 1
    val Society = 2
    val Economy = 3
    val Science = 4
    val Sports = 5
    val Entertainment = 6
    val Law = 7
    val Education = 8
    val Health = 9
    val CarsMotobikes = 10
    val RealEstate = 11
}

class CatfishBannerModel(id: Int, campaignId: Int, name: String, targetUrl: String,
                       thirdPartyImpressionUrl: List[String], width: Int, height: Int,
                       bannerFile: String, bannerFileFallback: String, linkedZones: Int)
    extends MediaBannerModel(id, campaignId, name, targetUrl, thirdPartyImpressionUrl, width, height, bannerFile, bannerFileFallback, linkedZones)

class MediaBannerModel(id: Int, campaignId: Int, name: String, targetUrl: String,
                       thirdPartyImpressionUrl: List[String], width: Int, height: Int,
                       var bannerFile: String, var bannerFileFallback: String, var linkedZones: Int, var popupBanner: Boolean = false, var actionCloseBtn: String = "")
    extends BannerModel(id, campaignId, name, targetUrl, thirdPartyImpressionUrl, width, height, BannerKind.Media)

class PopupBannerModel(id: Int, campaignId: Int, name: String, targetUrl: String,
                       thirdPartyImpressionUrl: List[String], width: Int, height: Int,
                       bannerFile: String, bannerFileFallback: String, linkedZones: Int, popupBanner: Boolean = false, actionCloseBtn: String = "")
    extends MediaBannerModel(id, campaignId, name, targetUrl, thirdPartyImpressionUrl, width, height, bannerFile, bannerFileFallback, linkedZones, popupBanner, actionCloseBtn)

class EcommerceBannerModel(id: Int, campaignId: Int, name: String, targetUrl: String, thirdPartyImpressionUrl: List[String], var status: String = "running", var click: Int, var startDate: Long, var endDate: Long,
                           var properties: Map[String,String])
    extends BannerModel(id, campaignId, name, targetUrl, thirdPartyImpressionUrl, 190, 663, BannerKind.Ecommerce)

class HtmlBannerModel(id:Int, campaignId : Int, name : String, targetUrl : String, thirdPartyImpressionUrl : List[String],var embeddedHtml : String, var linkedZones : Int = 0, var inline: Boolean = false, var autoLayout: Boolean = false)
    extends BannerModel(id,campaignId,name,targetUrl,thirdPartyImpressionUrl,200,300,BannerKind.Html)

class PrBannerModel(id:Int, campaignId : Int, name : String, targetUrl : String, thirdPartyImpressionUrl : List[String], var templateType : Int, var categoryTypes : Array[Int], var links : Array[Int] = null)
    extends BannerModel(id,campaignId,name,targetUrl,thirdPartyImpressionUrl,200,300,BannerKind.NetworkPrBanner)

class TrackingBanner(id: Int, campaignId: Int, name: String, targetUrl: String, var bannerFile: String, var retargeting: Int, var zoneId: Int = 0)
    extends BannerModel(id, campaignId, name, targetUrl, List.empty, 0, 0, "tracking")

object ExpandDirection {
    val TOP_PUSH_DOWN = 1
    val RIGHT_TO_LEFT = 2
}
object ExpandStyle {
    val MOUSE_OVER = 1
    val AUTO = 2
}

object ExpandDisplayStyle {
    val OVERLAY_LAYOUT = 1
    val PUSH_DOWN_LAYOUT = 2
}
object ExpandTVCExtension {
    val NONE = 1
    val VIDEO_INTEGRATED = 2
}

class ExpandableBannerModel(
    id : Int,
    campaignId : Int,
    name : String,
    targetUrl : String,
    thirdPartyImpressionUrl : List[String],
    var linkedZones : Int,
    var expandFile : String,
    var backupFile : String,
    var standardFile : String,
    var expandHeight : Int,
    var expandWidth : Int,
     height : Int,
     width : Int,
    var expandDirection : Int  = ExpandDirection.TOP_PUSH_DOWN,
    var expandStyle : Int = ExpandStyle.MOUSE_OVER,
    var displayStyle : Int = ExpandDisplayStyle.OVERLAY_LAYOUT,
    var iTVCExtension : Int = ExpandTVCExtension.NONE,
    var tvcFile : String,
    var thirdPartyClickUrl : String
) extends BannerModel(id,campaignId,name,targetUrl,thirdPartyImpressionUrl, height, width,BannerKind.Expandable)

class BalloonBannerModel(
    id : Int,
    campaignId : Int,
    name : String,
    targetUrl : String,
    thirdPartyImpressionUrl : List[String],
    var linkedZones : Int,
    var expandFile : String,
    var backupFile : String,
    var standardFile : String,
    var expandHeight : Int,
    var expandWidth : Int,
    height : Int,
    width : Int,
    var barHeight: Int,
    var barWidth : Int,
    var iTVCExtension : Int = ExpandTVCExtension.NONE,
    var tvcFile:String,
    var thirdPartyClickUrl : String,
    var introText : String
    ) extends BannerModel(id,campaignId,name,targetUrl,thirdPartyImpressionUrl, height, width,BannerKind.Balloon)


object LimitUnit {
    val IMPRESSION = "impression"
    val CLICK = "click"
    val BUDGET = "budget"
}
object TargetPlatform {
    val WEB = "web"
    val MOBILE = "mobile"
}

object RateUnit {
    val CPM  = 1
    val CPC = 2
    val CREATIVE_VIEW = 3
    val PER_1_4_VIDEO = 4
    val PER_1_2_VIDEO = 5
    val PER_3_4_VIDEO = 6
    val FULL_VIDEO = 7
}

object VariableTargetingBound extends Enumeration {
    val Equal = 0
    val NotEqual = 1
}

class VariableTargeting(var key: String, var value: String, var bound: Int = 1)
class GeoTargeting(var value: String, var in : Boolean = true)

trait INetwork{
    var targetContent: Array[Int]
    var targetZones: Array[Int]  // null or undefined => mean target all
    var status: String                             // [] or [..] => target specific ?
    var limit: Long
    var timeSpan: Long
    var limitUnit: String
    var dailyLimit: Long
    var lifetimeLimit: Long
    var startDate: Long
    var endDate: Long
    var rate : Long
    var rateUnit : Int
    var variables: Array[VariableTargeting] = new Array[VariableTargeting](0)
    var geographicTargetings : Array[GeoTargeting] = new Array[GeoTargeting](0)
    var freqCapping : Int
    var freqCappingTime : Long
    var highPriority : Boolean
    var companionTargetingValues: Array[String] = new Array[String](0)
}

// Minh duc
class NetworkPrBannerModel(id: Int,
                           campaignId: Int,
                           name: String,
                           targetUrl: String,
                           thirdPartyImpressionUrl : List[String],
                           templateType : Int,
                           categoryTypes : Array[Int],
                           var targetContent: Array[Int],       // link to website
                           var targetZones: Array[Int],         // link to zones
                           var status: String,
                           var limit: Long,
                           var timeSpan: Long,                  // reset time
                           var limitUnit: String,
                           var dailyLimit: Long,
                           var lifetimeLimit: Long,
                           var startDate: Long,
                           var endDate: Long,
                           var rate : Long,
                           var rateUnit : Int,
                           var freqCapping : Int,
                           var freqCappingTime : Long,
                           var highPriority : Boolean,
                           links : Array[Int] = null
                           )extends PrBannerModel(id, campaignId, name, targetUrl, thirdPartyImpressionUrl, templateType, categoryTypes, links)
                        with INetwork

class NetworkMediaBannerModel(id: Int,
                              campaignId: Int,
                              name: String,
                              targetUrl: String,
                              thirdPartyImpressionUrl: List[String],
                              width: Int,
                              height: Int,
                              var wrapper : Boolean,
                              var extendURL : String,
                              bannerFile: String,
                              bannerFileFallback: String,
                              var rate : Long,
                              var rateUnit : Int = RateUnit.CPM,
                              var limit: Long,
                              var timeSpan: Long,
                              var limitUnit : String,
                              var dailyLimit : Long,
                              var dailyLimitUnit : String,
                              var lifetimeLimit : Long,
                              var lifetimeLimitUnit : String,
                              var targetPlatform : String,
                              var targetContent : Array[Int],
                              var targetZones : Array[Int],
                              var duration : Long,
                              var startDate : Long,
                              var endDate : Long,
                              var freqCapping : Int,
                              var freqCappingTime : Long,
                              var status: String = "Running",
                              var highPriority: Boolean = false)
extends MediaBannerModel(id, campaignId, name, targetUrl, thirdPartyImpressionUrl, width, height, bannerFile, bannerFileFallback, 0)
with INetwork

class NetworkPopupBannerModel(id: Int,
                              campaignId: Int,
                              name: String,
                              targetUrl: String,
                              thirdPartyImpressionUrl: List[String],
                              width: Int,
                              height: Int,
                              var wrapper : Boolean,
                              var extendURL : String,
                              bannerFile: String,
                              bannerFileFallback: String,
                              var rate : Long,
                              var rateUnit : Int = RateUnit.CPM,
                              var limit: Long,
                              var timeSpan: Long,
                              var limitUnit : String,
                              var dailyLimit : Long,
                              var dailyLimitUnit : String,
                              var lifetimeLimit : Long,
                              var lifetimeLimitUnit : String,
                              var targetPlatform : String,
                              var targetContent : Array[Int],
                              var targetZones : Array[Int],
                              var duration : Long,
                              var startDate : Long,
                              var endDate : Long,
                              var freqCapping : Int,
                              var freqCappingTime : Long,
                              var status: String = "Running",
                              var highPriority: Boolean = false)
    extends PopupBannerModel(id, campaignId, name, targetUrl, thirdPartyImpressionUrl, width, height, bannerFile, bannerFileFallback, 0)
    with INetwork

class NetworkCatfishBannerModel(id: Int,
                              campaignId: Int,
                              name: String,
                              targetUrl: String,
                              thirdPartyImpressionUrl: List[String],
                              width: Int,
                              height: Int,
                              var wrapper : Boolean,
                              var extendURL : String,
                              bannerFile: String,
                              bannerFileFallback: String,
                              var rate : Long,
                              var rateUnit : Int = RateUnit.CPM,
                              var limit: Long,
                              var timeSpan: Long,
                              var limitUnit : String,
                              var dailyLimit : Long,
                              var dailyLimitUnit : String,
                              var lifetimeLimit : Long,
                              var lifetimeLimitUnit : String,
                              var targetPlatform : String,
                              var targetContent : Array[Int],
                              var targetZones : Array[Int],
                              var duration : Long,
                              var startDate : Long,
                              var endDate : Long,
                              var freqCapping : Int,
                              var freqCappingTime : Long,
                              var status: String = "Running",
                              var highPriority: Boolean = false)
    extends CatfishBannerModel(id, campaignId, name, targetUrl, thirdPartyImpressionUrl, width, height, bannerFile, bannerFileFallback, 0)
    with INetwork

//class NetworkMediaBannerModel(id: Int,
//                              campaignId: Int,
//                              name :String,
//                              targetUrl: String,
//                              thirdPartyImpressionUrl: List[String],
//                              width : Int,
//                              height: Int,
//                            var wrapper : Boolean,
//                            var extendURL : String,
//                             var bannerFile : String,
//                             var bannerFileFallback : String,
//                             var rate : Long,
//                             var rateUnit : Int = RateUnit.CPM,
//                             var dailyLimit : Long,
//                             var dailyLimitUnit : String,
//                             var lifetimeLimit : Long,
//                             var lifetimeLimitUnit : String,
//                             var targetPlatform : String,
//                             var targetContent : Array[Int],
//                             var targetZones : Array[Int],
//                             //var targetZonesOption : String,
//                             var to : Long,
//                             var targetCountry : String,
//                             var status: String = "Running")
//    extends BannerModel(id, campaignId, name, targetUrl, thirdPartyImpressionUrl, width, height, BannerKind.NetworkMedia)
//    with INetwork


class NetworkHtmlBannerModel(id:Int,
                             campaignId : Int,
                             name : String,
                             targetUrl : String,
                             thirdPartyImpressionUrl : List[String],
                             width : Int,
                             height: Int,
                             embeddedHtml : String,
                             var rate : Long,
                             var rateUnit : Int = RateUnit.CPM,
                             var limit: Long,
                             var timeSpan: Long,
                             var limitUnit : String,
                             var dailyLimit : Long,
                             var dailyLimitUnit : String,
                             var lifetimeLimit : Long,
                             var lifetimeLimitUnit : String,
                             var targetPlatform : String,
                             var targetContent : Array[Int],
                             var targetZones : Array[Int],
                             var duration : Long,
                             var targetCountry : String,
                             var startDate : Long,
                             var endDate : Long,
                             var freqCapping : Int,
                             var freqCappingTime : Long,
                             var status: String = "Running",
                             var highPriority: Boolean = false)
    extends HtmlBannerModel(id:Int, campaignId, name, targetUrl, thirdPartyImpressionUrl, embeddedHtml, 0, false)
    with INetwork

//class NetworkHtmlBannerModel(id:Int,
//                             campaignId : Int,
//                             name : String,
//                             targetUrl : String,
//                             thirdPartyImpressionUrl : List[String],
//                             width : Int,
//                             height: Int,
//                             var embeddedHtml : String,
//                             var rate : Long,
//                             var rateUnit : Int = RateUnit.CPM,
//                             var dailyLimit : Long,
//                             var dailyLimitUnit : String,
//                             var lifetimeLimit : Long,
//                             var lifetimeLimitUnit : String,
//                             var targetPlatform : String,
//                             var targetContent : Array[Int],
//                             var targetZones : Array[Int],
//                             //var targetZonesOption : String,
//                             var to : Long,
//                             var targetCountry : String,
//                             var status: String = "Running")
//  extends BannerModel(id,campaignId, name, targetUrl, thirdPartyImpressionUrl,width, height, BannerKind.NetworkHtml)
//    with INetwork

class NetworkExpandableBannerModel(
    id: Int,
    campaignId: Int,
    name: String,
    targetUrl: String,
    thirdPartyImpressionUrl: List[String],
    linkedZones: Int,
    expandFile: String,
    backupFile: String,
    standardFile: String,
    expandHeight: Int,
    expandWidth: Int,
    height: Int,
    width: Int,
    expandDirection: Int = ExpandDirection.TOP_PUSH_DOWN,
    expandStyle: Int = ExpandStyle.MOUSE_OVER,
    displayStyle: Int = ExpandDisplayStyle.OVERLAY_LAYOUT,
    iTVCExtension: Int = ExpandTVCExtension.NONE,
    tvcFile: String,
    thirdPartyClickUrl: String,
    var targetContent: Array[Int],
    var targetZones: Array[Int], // null or undefined => mean target all
    var status: String, // [] or [..] => target specific ?
    var limit: Long,
    var timeSpan: Long,
    var limitUnit: String,
    var dailyLimit: Long,
    var lifetimeLimit: Long,
    var startDate: Long,
    var endDate: Long,
    var freqCapping : Int,
    var freqCappingTime : Long,
    var rate: Long,
    var rateUnit: Int,
    var highPriority: Boolean = false
    ) extends ExpandableBannerModel(id,campaignId,name,targetUrl,thirdPartyImpressionUrl,linkedZones,
    expandFile,backupFile,standardFile,expandHeight,expandWidth,height,width,expandDirection,expandStyle
    ,displayStyle,iTVCExtension,tvcFile,thirdPartyClickUrl) with INetwork

class NetworkBalloonBannerModel(
    id: Int,
    campaignId: Int,
    name: String,
    targetUrl: String,
    thirdPartyImpressionUrl: List[String],
    linkedZones: Int,
    expandFile: String,
    backupFile: String,
    standardFile: String,
    expandHeight: Int,
    expandWidth: Int,
    height: Int,
    width: Int,
    barHeight: Int,
    barWidth : Int,
    iTVCExtension: Int = ExpandTVCExtension.NONE,
    tvcFile:String,
    thirdPartyClickUrl: String,
    introText : String,
    var targetContent: Array[Int],
    var targetZones: Array[Int], // null or undefined => mean target all
    var status: String, // [] or [..] => target specific ?
    var limit: Long,
    var timeSpan: Long,
    var limitUnit: String,
    var dailyLimit: Long,
    var lifetimeLimit: Long,
    var startDate: Long,
    var endDate: Long,
    var freqCapping : Int,
    var freqCappingTime : Long,
    var rate: Long,
    var rateUnit: Int,
    var highPriority: Boolean = false
    ) extends BalloonBannerModel(id,campaignId,name,targetUrl,thirdPartyImpressionUrl,linkedZones,
    expandFile,backupFile,standardFile,expandHeight,expandWidth,height,width,barWidth,barHeight,iTVCExtension,tvcFile,thirdPartyClickUrl,introText) with INetwork


object VideoZonePosition {
    val PRE = "pre"
    val MID = "mid"
    val POST = "post"
}

class TVCItemPosition(var zoneId : Int, var position : String)

class NetworkTVCThirdPartyTracking {
    var impression = ""
    var click = ""
    var complete = ""
}
class NetworkTVCBannerModel(id: Int, campaignId: Int, name :String, targetUrl: String,
                              thirdPartyImpressionUrl: List[String], width : Int, height: Int,
                              var tvcFile : String,
                              var rate : Long,
                              var rateUnit : Int = RateUnit.CREATIVE_VIEW,
                              var limit: Long,
                              var timeSpan: Long,
                              var limitUnit : String,
                              var dailyLimit : Long,
                              var dailyLimitUnit : String,
                              var lifetimeLimit : Long,
                              var lifetimeLimitUnit : String,
                              var targetPlatform : String,
                              var targetContent : Array[Int],
                              var targetZones : Array[Int],
                              var positions: Array[TVCItemPosition],
                              var duration : Long,
                              var wrapper : Boolean,
                              var extendURL : String,
                              var targetCountry : String,
                              var startDate : Long,
                              var endDate : Long,
                              var freqCapping : Int,
                              var freqCappingTime : Long,
                              var status: String = "Running",
                              var skip : Boolean = true,
                              var skipAfter : Int = 6,
                              var highPriority: Boolean = false,
                              var thirdParty: NetworkTVCThirdPartyTracking = null
                               )
    extends BannerModel(id, campaignId, name, targetUrl, thirdPartyImpressionUrl, width, height, BannerKind.NetworkTVC)
    with INetwork
class NetworkOverlayBannerModel(
                                    id: Int,
                                    campaignId: Int,
                                    name :String,
                                    targetUrl: String,
                                    thirdPartyImpressionUrl: List[String],
                                    width : Int,
                                    height: Int,
                                    bannerFile: String,
                                    bannerFileFallback: String,
                                    var rate : Long,
                                    var rateUnit : Int = RateUnit.CREATIVE_VIEW,
                                    var limit: Long,
                                    var timeSpan: Long,
                                    var limitUnit : String,
                                    var dailyLimit : Long,
                                    var dailyLimitUnit : String,
                                    var lifetimeLimit : Long,
                                    var lifetimeLimitUnit : String,
                                    var targetPlatform : String,
                                    var targetContent : Array[Int],
                                    var targetZones : Array[Int],
                                    var positions: Array[TVCItemPosition],
                                    var duration : Long,
                                    var wrapper : Boolean,
                                    var extendURL : String,
                                    var targetCountry : String,
                                    var startDate : Long,
                                    var endDate : Long,
                                    var freqCapping : Int,
                                    var freqCappingTime : Long,
                                    var status: String = "Running",
                                    var highPriority: Boolean = false)
    extends MediaBannerModel(id, campaignId, name, targetUrl, thirdPartyImpressionUrl, width, height, bannerFile, bannerFileFallback, 0)
    with INetwork

class NetworkPauseAdBannerModel(
                                   id: Int,
                                   campaignId: Int,
                                   name :String,
                                   targetUrl: String,
                                   thirdPartyImpressionUrl: List[String],
                                   width : Int,
                                   height: Int,
                                   bannerFile: String,
                                   bannerFileFallback: String,
                                   var rate : Long,
                                   var rateUnit : Int = RateUnit.CREATIVE_VIEW,
                                   var limit: Long,
                                   var timeSpan: Long,
                                   var limitUnit : String,
                                   var dailyLimit : Long,
                                   var dailyLimitUnit : String,
                                   var lifetimeLimit : Long,
                                   var lifetimeLimitUnit : String,
                                   var targetPlatform : String,
                                   var targetContent : Array[Int],
                                   var targetZones : Array[Int],
//                                   var positions: Array[TVCItemPosition],
                                   var extendURL : String,
                                   var targetCountry : String,
                                   var startDate : Long,
                                   var endDate : Long,
                                   var freqCapping : Int,
                                   var freqCappingTime : Long,
                                   var status: String = "Running",
                                   var highPriority: Boolean = false)
    extends MediaBannerModel(id, campaignId, name, targetUrl, thirdPartyImpressionUrl, width, height, bannerFile, bannerFileFallback, 0)
    with INetwork

class ApproveAdsModel(
                        var websiteId: Int,
                        var website: String,
                        var owner: String,
                        var zoneIds: Array[Int],
                        var zones: Array[String],
                        var item: BannerModel
                         )

object ActionType extends Enumeration{
    type ActionType = Value
    val CREATE = "create"
    val EDIT = "edit"
    val DELETE = "delete"
    val DISABLE = "disable"
    val ENABLE = "enable"
    val PAUSE = "pause"
    val RESUME = "resume"
    val SHARE  = "share"
    val UNSHARE = "unshare"
    val LINK = "link"
    val UNLINK = "unlink"
    val TERMINATE = "terminate"
    val BOOK = "book"
    val APPROVE = "approve"
    val REJECT = "reject"
}

class ActionLog(
    var id :Int,
    var userId : Int,
    var action : String,
    var objectType : String,
    var objectId : Int,
    var objectName : String,
    var time : Long,
    var oldVal : String,
    var newVal : String,
    var disable: Boolean = false
) extends  IItem

class ActionLogModel(
    var id :Int,
    var action  : String,
    var userId : Int,
    var userName : String,
    var objectType : String,
    var objectId : Int,
    var objectName : String,
    var time : Long,
    var detail : String,
    var disable: Boolean = false
) extends  IItem

class ShareActionLogModel(id : Int,
  action : String = ActionType.SHARE,
  userId : Int,
  userName : String,
  objectType: String,
  objectId : Int,
  objectName : String,
  time : Long,
  toUserId : Int,
  roles : Array[Int],
  detail : String)
extends ActionLogModel(id,action,userId,userName,objectType,objectId,objectName,time,detail)

class CreateActionLogModel(id: Int,
    action: String = ActionType.CREATE,
    userId : Int, userName : String,
    objectType : String,
    objectId : Int,
    objectName: String,
    time : Long,
    detail : String)
extends ActionLogModel(id,action,userId,userName,objectType, objectId, objectName,time,detail)

class ArticleModel(
  id:Int,
  name:String,
  var summary:String,
  var contentType:String,
  var content:String,
  var status:Int,
  var photos:List[String],
  var videos:List[String],
  var publishedDate:Long,
  var author:String,
  var source:String,
  var tags:String,
  var isReview:Boolean,
  var createDate:Long,
  var updateDate:Long,
  var reviewDate:Long,
  var editorDate:Long,
  var zoneId:Int,
  campaignId:Int,
  var comments:List[ArticleComment],
  var notes:List[ArticleComment],
  var websiteId:Int,
  var creator:Int,
  var updater:Int,
  var reviewer:Int,
  var editor:Int,
  var pubArticleId:Int,
  var retargeting:Int,
  var retargetingEnable: Boolean,
  var category: Int,
  var publishedUrl: String,
  var companionTargeting: String
  ) extends BannerModel(id,campaignId,name,"",null,0,0,BannerKind.Article)
