package ads.common.model

import ads.common.IItem


object CommentKind {
    val NOTE = "note"
    val COMMENT = "comment"
}

class ArticleComment (
    var id:Int,
    var articleId:Int,
    var comment:String,
    var kind:String,
    var ownerId:Int,
    var date:Long,
    var disable: Boolean = false
) extends IItem

object ArticleContentType {
    val ATTACH_FILE = "Attach File"
    val INPUT = "Input"
}

object ArticleStatus extends Enumeration{
    val PENDING = 1
    val COMPOSING = 2
    val REJECT_BY_REVIEWER = 3
    val WAITING_APPROVAL_BY_SALE = 4
    val WAITING_FOR_SYNC_TO_CMS = 5
    val WAITING_FOR_APPROVAL_BY_PUBLISHER = 6
    val WAITING_FOR_PUBLISHING = 7
    val PUBLISHED = 8
    val EXPIRED = 9
    val COMPOSING_AFTER_REJECTED_BY_PUBLISHER = 10


//    val PENDING = 1
//    val REJECT_BY_REVIEWER = 2
//    val REJECT_BY_PUBLISHER = 3
//    val DELETED = 4
//    val COMPOSING = 5
//    val WAITING_APPROVAL_BY_PUBLISHER = 6
//    val WAITING_FOR_PUBLISHING = 7
//    val PUBLISHED = 8
//    val EDITED_WAITING_FOR_APPROVAL = 9
//    val EDIT = 10
}
//class Article(var id:Int,
//              var title:String,
//              var summary:String,
//              var contentType:String,
//              var content:String,
//              var status:Int,
//              var campaignId:Int,
//              var photos:List[String],
//              var videos:List[String],
//              var publishedDate:Long,
//              var author:String,
//              var source:String,
//              var tags:String,
//              var isReview:Boolean,
//              var zones:Array[Int],
//              var websiteId:Int,
//              var pubArticleId:Int,
//              var createBy:Int,
//              var createDate:Long,
//              var updateBy:Int,
//              var updateDate:Long,
//              var reviewBy:Int,
//              var reviewDate:Long,
//              var editorBy:Int,
//              var editorDate:Long
//) extends IItem {
//    def getZones: Array[Int] = {
//        if(zones == null ) Array[Int]()
//        else zones
//    }
//}


class ArticleStats(
    var id:Int,
    var articleId:Int,
    var views:Int,
    var shares:Int,
    var shareThroughView:Int,
    var date:Long,
    var disable: Boolean = false
) extends IItem

class ArticleLocation (
    var id:Int,
    var articleId:Int,
    var location:String,
    var views:Int,
    var date:Long,
    var disable: Boolean = false
 ) extends IItem

class ArticleGender (
  var id:Int,
  var articleId:Int,
  var gender:String,
  var views:Int,
  var date:Long,
  var disable: Boolean = false
  )extends IItem


class ArticleAge (
    var id:Int,
    var articleId:Int,
    var age:String,
    var views:Int,
    var date:Long,
    var disable: Boolean = false
 )extends IItem