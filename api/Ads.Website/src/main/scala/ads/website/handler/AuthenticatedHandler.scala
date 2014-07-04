package ads.website.handler

import ads.web.mvc.SmartDispatcherHandler
import ads.common.services.{Session, ISessionService}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import ads.common.Syntaxs._
import ads.web.WebUtils


class AuthenticatedHandler(env: {val sessionService: ISessionService}) extends SmartDispatcherHandler {
    override def filter(request: HttpServletRequest, response: HttpServletResponse): Boolean = {

        WebUtils.getSessionId(request) >=>
            env.sessionService.getSession >=>
            checkSession match {
            case Success(s) => super.filter(request, response)
            case Failure(_) => {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED)
                false
            }
        }
    }

    def checkSession(session: Session): Try[Unit] = succeed()
}