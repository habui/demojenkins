package ads.web.mvc

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import ads.web.WebUtils

abstract class Action{
    def apply(request: HttpServletRequest, response: HttpServletResponse)
}

class View (val data: Object) extends Action {
    def apply(request: HttpServletRequest, response: HttpServletResponse){
        response.getWriter.print(data)
    }
}

class Fail(message: String, code: Int = 500) extends Action{
    def apply(request: HttpServletRequest, response: HttpServletResponse): Unit = {
        response.setStatus(code)
        response.getWriter.print(message)
    }
}

class Json (data: Any) extends View(WebUtils.toJson(data))


object Json{
    def apply(data: Object): Action = new Json(data)
}
object RawJson {
    def apply(data : Object) : Action = new View(WebUtils.toRawJson(data))
}

object Text{
    def apply(text: Object): Action = new View(text)
}
