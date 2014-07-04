package ads.common.services

import ads.common.Syntaxs._
import ads.common.model.User
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.{AtomicLong, AtomicInteger}
import java.util.{UUID, Random}
import ads.common.{SecurityUtils, Base64}

class Session (val user: User)

trait ISessionService {
    def login (userName: String, password: String):Try[String]
    def logout(sessionId: String)
    def getSession(sessionId: String): Try[Session]
    def getCurrentSession() : Try[Session]
}

object SessionInfo {
    var sessionId :String = ""
}

class SessionService (env: {val userService: IUserService}) extends ISessionService{
    val map = new ConcurrentHashMap[String,Session]()
    val userMap = new ConcurrentHashMap[Int,String]()
 
    def login(userName: String, password: String): Try[String] =
    {
        env.userService.login(userName,password) match{
            case Success(user) => {
                val sessionId = generateSessionId()
                val oldId = userMap.get(user.id)
                if (oldId != null) map.remove(oldId)
                userMap.put(user.id, sessionId)
                map.put(sessionId, new Session(user))
                return succeed(sessionId)
            }
            case Failure(s)=>new Failure(s)
        }
    }

    def generateSessionId(): String = {
        val sessionId = UUID.randomUUID().toString
        return sessionId
    }

    def logout(sessionId: String): Unit = map.remove(sessionId)

    def getSession(sessionId: String): Try[Session] = {
        val session = map.get(sessionId)
        if (session == null) fail("not found session id!")
        else succeed(session)
    }
    def getCurrentSession() : Try[Session] = {
        if(SessionInfo.sessionId != null){
            getSession(SessionInfo.sessionId)
        }else {
            fail("not found session id!")
        }
    }
}
