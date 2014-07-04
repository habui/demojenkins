package ads.common.services

import ads.common.database.{IDataDriver, InMemoryDataService, IDataService}
import ads.common.model.{User, Role}
import scala.reflect.ClassTag
import scala.concurrent.Future

/**
 * Created by quangnbh on 10/24/13.
 */
trait IRoleService extends IDataService[Role] {
    def listByObject(obj : String) : java.util.ArrayList[Role]
}

class RoleService(env : {
    val roleDataDriver : IDataDriver[Role]
    val actionLogServiceRef : Future[IActionLogService]
}) extends InMemoryDataService[Role](r => r.id,env.roleDataDriver,env.actionLogServiceRef) with IRoleService {

    def listByObject(obj : String) : java.util.ArrayList[Role] = {
        val obName : String = obj.toLowerCase
        val iter : java.util.Iterator[Role] = all.values().iterator()
        val ret = new java.util.ArrayList[Role]()
        while(iter.hasNext){
            val e = iter.next()
            if(e.objName.toLowerCase.equals(obName)){
                ret.add(e)
            }
        }
        ret
    }
}
