package ads.common.model

import ads.common.IItem

/**
 * Created by stroumphs on 1/6/14.
 */
class NewBookRecord(var id: Int, var zoneId: Int, var itemId: Int, var from: Long, var to: Long, var share: Float, var status: Int, var disable: Boolean = false) extends IItem

class NewBookRecordModel(var id: Int, var zoneId: Int, var zoneName: String, var itemId: Int, var itemName: String, var siteId: Int, var siteName: String, var from: Long, var to: Long, var share: Float, var status: Int, var itemWidth: Int, var itemHeight: Int, var disable: Boolean = false) extends IItem

