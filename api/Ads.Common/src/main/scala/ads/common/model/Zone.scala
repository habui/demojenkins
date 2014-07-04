package ads.common.model

import ads.common.IItem

object TimeUnit {
    val MINUTE = "minute"
    val HOUR = "hour"
    val DAY = "day"
}

object EZoneRenderKind {
    val BANNER_SKIN = "banner_skin"
}

class Zone(
              var id: Int,
              var siteId: Int,
              var name: String,
              var runningMode: List[String],
              var categories: Array[Int],
              var bookingPrice: Int = 500,
              var minCPC: Int,
              var minCPM: Int,
              var width: Int,
              var height: Int,
              var kind :String = ZoneKind.Banner,
              var renderKind: String,
              var extra: String,
              var frequencyCapping: Int = 0,
              var frequencyCappingTime: Int = 0,
              var disable: Boolean = false
              ) extends IItem

class ZoneModel(
                   var id : Int,
                   var siteId :Int,
                   var name : String,
                   var categories : Array[Int],
                   var width : Int,
                   var height: Int,
                   var groups : Array[Int],
                   var kind : String,
                   var renderKind: String = "",
                   var frequencyCapping: Int = 0,
                   var frequencyCappingTime: Int = 0,
                   var disable: Boolean = false
                   ) extends  IItem


class BannerZoneModel(
                         id: Int,
                         siteId: Int,
                         name: String,
                         var runningMode: List[String],
                         categories: Array[Int],
                         var bookingPrice: Int = 500,
                         var minCPC: Int,
                         var minCPM: Int,
                         var links: Int,
                         var usage: Float,
                         var allowedExpand : Boolean,
                         width: Int,
                         height: Int,
                         groups: Array[Int])
    extends ZoneModel(id,siteId,name,categories,width,height,groups,ZoneKind.Banner)


class VideoZoneTimeSegment(var start : Int, var spend : Int)

class VideoZoneModel(id : Int, siteId : Int, name : String, categories : Array[Int], width : Int,
                     height: Int, groups : Array[Int],
                     var allowedType : Array[String],
                     //for banner type
                     var startTime : Int,
                     var timeSegments : Array[VideoZoneTimeSegment],
                     var runningMode :Array[String],
                     var positions : Array[String],
                     var maxAdsDuration : Int,
                     var maxAdPodDuration: Int,
                     var minCPC : Int,
                     var minCPM : Int,
                     //for tvc type
                     var tvcPositions : Array[String],

                     var preSkiptime : Int,
                     var preMaxDuration : Int,
                     var preMaxPodDuration : Int,
                     var preMinCPM :Int,
                     var preMinCPMType : Int = RateUnit.CREATIVE_VIEW,

                     var midSkipTime : Int,
                     var midMaxDuration : Int,
                     var midMaxPodDuration : Int,
                     var midMinCPM : Int,
                     var midMinCPMType : Int = RateUnit.CREATIVE_VIEW,
                     var midStartTime : Int,
                     var midMaxAdpod : Int,
                     var midTimeScheduled : Int,
                     var midTimeScheduledUnit : String = TimeUnit.MINUTE,

                     var postSkipTime : Int,
                     var postMaxDuration : Int,
                     var postMaxPodDuration : Int,
                     var postMinCPM : Int,
                     var postMinCPMType : Int = RateUnit.CREATIVE_VIEW
                        )
    extends ZoneModel(id,siteId,name,categories,width,height,groups,ZoneKind.Video)

class PRZoneModel(
                     id:Int,
                     siteId:Int,
                     name:String,
                     var price:Long,
                     var slotTotal:Int,
                     var availableSlots:Int
                     ) extends ZoneModel(id,siteId,name,null,0,0,null,ZoneKind.PRNetwork)
