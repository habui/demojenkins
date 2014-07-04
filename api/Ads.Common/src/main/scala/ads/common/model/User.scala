package ads.common.model

import ads.common.IItem

class User (var id: Int,var ownerId : Int, var name: String,var email :String, var password: String, var disable: Boolean = false) extends IItem

object Permission extends Enumeration {
    private val atom : Long = 1
    val WEBSITE_VIEW_INFO = atom
    val WEBSITE_REPORT  = atom<<1
    val WEBSITE_BOOKING = atom<<2
    val WEBSITE_APPROVE = atom<<3
    val WEBSITE_MANAGE_PERMISSION = atom << 4
    val WEBSITE_EDIT = atom << 5
    val WEBSITE_OWN = atom << 6
    val WEBSITE_PR_EDITOR = atom << 7
    val WEBSITE_CREATE = atom << 8

    val WEBSITE_ALL_PERMISSIONS = (atom << 21) - 1

    val ORDER_VIEW_INFO = (atom << 22)
    val ORDER_REPORT = (atom << 23)
    val ORDER_EDIT = (atom << 24)

    val ORDER_ALL_PERMISSIONS = (atom << 42) - 1 - WEBSITE_ALL_PERMISSIONS
    val ROOT_PERMISSION = (atom << 43) - 1
}
