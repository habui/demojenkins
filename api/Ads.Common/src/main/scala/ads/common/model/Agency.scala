package ads.common.model

import ads.common.IItem

/**
 * Created by vutt3 on 10/21/13.
 */
class Agency (id:Int,
              ownerId: Int,
              name:String,
              password:String,
              email:String,
              disable: Boolean = false) extends User(id:Int,ownerId:Int,name:String,email : String ,password:String, disable)