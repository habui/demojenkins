package ads.common.model

import ads.common.IItem

class Conversion(var id: Int,
                 var name: String,
                 var windows: Int,
                 var value: Double,
                 var updateDate: Long,
                 var trackingStatus: String,
                 var orderId: Int,
                 var currency : String = "VND",
                 var disable: Boolean = false
                )extends IItem

class ConversionType(var id: Int,
                 var name: String,
                 var disable: Boolean = false
                )extends IItem


