package ads.website.modules

import ads.web.{WebUtils, HandlerContainerServlet, Invoke}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import ads.common.Syntaxs._
import ads.common.model.User
import ads.website.handler.AuthenticatedHandler
import ads.web.mvc.BaseHandler
import ads.common.services.ISessionService
import ads.website.Environment

class SessionHandler(env: {val sessionService: ISessionService}) extends AuthenticatedHandler(env) {

    @Invoke(Parameters = "response,userName,password", bypassFilter = true)
    def login(response: HttpServletResponse, userName: String, password: String) = {
        env.sessionService.login(userName, password) match {
            case Success(sessionId) => {
                WebUtils.setSessionId(response, sessionId)
                new Success("ok")
            }
            case Failure(x) => new Failure(x)
        }
    }

    @Invoke(Parameters = "request,response")
    def logout(request: HttpServletRequest, response: HttpServletResponse) = {
        val cookie = WebUtils.getSessionId(request).getOrElse("")
        env.sessionService.logout(cookie)
        WebUtils.clearSessionId(response)
        new Success("ok")
    }

    @Invoke(Parameters = "request,response")
    def getCurrentUser(request: HttpServletRequest, response: HttpServletResponse): Try[User] =
        (WebUtils.getSessionId(request) >=>
            env.sessionService.getSession).
            tryGet(session => new User(session.user.id, session.user.ownerId, session.user.name, session.user.email, ""))
}

class SessionServlet extends HandlerContainerServlet{
    def factory(): BaseHandler = new SessionHandler(Environment)
}