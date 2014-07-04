package ads.common.model

import ads.common.IItem

class CampaignModel(var id : Int, var orderId : Int, var name : String, var status : String, var campaignType : String, var startDate : Long, var endDate: Long, var companion : Boolean, var itemCount: Int = 0, var disable: Boolean = false) extends IItem

class BookingCampaignModel(id:Int, orderId: Int, name: String, status: String, campaignType : String, var unlinkZoneCount : Int = 0, var unlinkItemCount: Int = 0, startDate : Long, endDate : Long, companion : Boolean)
    extends CampaignModel(id, orderId, name,CampaignStatus.RUNNING, CampaignType.Booking, System.currentTimeMillis(), System.currentTimeMillis(), companion)
class NetworkCampaignModel(id : Int, orderId: Int, name: String, status : String, campaignType : String, var timeScheduled : Array[Int] = Array(0,0,0,0,0,0,0),var timeZone : Long, var freqCapping : Long,var freqCappingTime : Int,var freqCappingTimeUnit: String = TimeUnit.MINUTE, var displayType : Int = NetworkCampaignDisplayType.ONE_OR_MORE, companion: Boolean)
    extends CampaignModel(id, orderId, name, CampaignStatus.RUNNING, CampaignType.Network, System.currentTimeMillis(), System.currentTimeMillis(), companion)

class NetworkTVCCampaignModel(id : Int, orderId: Int, name: String, status : String, campaignType : String, var timeScheduled : Array[Int] = Array(0,0,0,0,0,0,0),var timeZone : Long, var freqCapping : Long,var freqCappingTime : Int,var freqCappingTimeUnit: String = TimeUnit.MINUTE, var displayType : Int = NetworkCampaignDisplayType.ONE_OR_MORE )
    extends CampaignModel(id, orderId, name, CampaignStatus.RUNNING, CampaignType.NetworkTVC, System.currentTimeMillis(), System.currentTimeMillis(), false)

class PRNetworkCampaignModel(id : Int, orderId: Int, name: String, status : String, campaignType : String, startDate : Long, endDate : Long)
    extends CampaignModel(id, orderId, name, CampaignStatus.RUNNING, CampaignType.PRNetwork, System.currentTimeMillis(), System.currentTimeMillis(), false)


object NetworkCampaignDisplayType {
    val ONLY_ONE = 1
    val ONE_OR_MORE = 2
    val AS_MANY_AS_POSSIBLE = 3
}
