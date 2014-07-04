package ads.web

import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import ads.web.mvc.BaseHandler
import scala.util.control.NonFatal

abstract class BaseServlet extends HttpServlet {
    override def doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        process(req, resp)
    }

    override def doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        process(req, resp)
    }

    def process(request: HttpServletRequest, response: HttpServletResponse)
}

abstract class HandlerContainerServlet extends BaseServlet{
    def factory():BaseHandler

    lazy val handler = factory()

    def process(request: HttpServletRequest, response: HttpServletResponse) {
        response.addHeader("Access-Control-Allow-Origin","*")
        response.setContentType("text/html; charset=UTF-8")
        response.addHeader("Access-Control-Expose-Headers", "X-sessionId")

        try
        {
            handler.process(request, response)
        }
        catch{
            case NonFatal(e)=>{
                if (e.getCause != null){
                    e.getCause.printStackTrace(response.getWriter)
                }
                else e.printStackTrace(response.getWriter)
                val redirectUrl = request.getParameter("redirectUrl")
                if (redirectUrl != null && !redirectUrl.equals(""))
                    response.sendRedirect(redirectUrl)
                println(s"Request: ${request.getRequestURL}?${request.getQueryString}")
                throw e
            }
        }
    }

    override def doOptions(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.addHeader("Access-Control-Allow-Origin","*")
        resp.addHeader("Access-Control-Allow-Methods","POST, GET, OPTIONS")
        resp.addHeader("Access-Control-Allow-Headers", "X-Requested-With,X-sessionId")
        resp.addHeader("Access-Control-Expose-Headers", "X-sessionId")
        resp.setStatus(HttpServletResponse.SC_OK)
    }
}
