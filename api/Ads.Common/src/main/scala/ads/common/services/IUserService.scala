package ads.common.services
import ads.common.Syntaxs._
import ads.common.model.{Website, Order, UserRole, User}
import ads.common.database.{IDataDriver, InMemoryDataService, IDataService}
import java.util.concurrent.ConcurrentHashMap
import ads.common.IItem
import scala.concurrent.Future


trait IUserService extends IDataService[User]{
    def login(userName: String, password: String): Try[User]
    def findByName(userName: String): User
}

class UserService(env:{
    val userDataDriver:IDataDriver[User]
    val actionLogServiceRef : Future[IActionLogService]
})
    extends InMemoryDataService[User](u=>u.id, env.userDataDriver,env.actionLogServiceRef ) with IUserService{

    lazy val userIndex = new ConcurrentHashMap[String,User]()

    override def beforeSave(value: User): Unit = {
        if(value.password != null){
            value.password = value.password.hashCode().toString
        }else {
            value.password ="123456".hashCode.toString
        }

    }


    override def removeIndex(item: User): Unit = {
        super.removeIndex(item)
        userIndex.remove(item.name)
    }

    override def index(item: User): Unit ={
        super.index(item)
        userIndex.put(item.name, item)
    }

    def login(userName: String, pwd: String): Try[User] = {
        val password = pwd.hashCode.toString
        val user = userIndex.get(userName)
        if (user == null || user.password != password)
            return fail("wrong user name or password")

        return succeed(user)
    }

    def findByName(userName: String): User = userIndex.get(userName)

}