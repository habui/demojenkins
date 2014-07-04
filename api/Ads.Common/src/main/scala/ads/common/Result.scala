package ads.common

/**
 * Created by quangnbh on 12/3/13.
 */

trait CommonCode {
    val SUCCESS = 0
    val FAILED = 1
    val COULD_NOT_LOAD_DATA = 100
    val DATABASE_ERROR = 200

}

class ResultMessage(var code : Int, var data : Any, var description : String)
