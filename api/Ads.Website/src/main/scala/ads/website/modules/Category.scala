package ads.website.modules

import ads.website.handler.RestHandler
import ads.common.model.Category
import ads.common.database.IDataService
import ads.web.{Invoke, HandlerContainerServlet, BaseServlet}
import ads.web.mvc.{Json, BaseHandler}
import ads.website.Environment
import ads.common.Syntaxs.{succeed, fail, Try}
import javax.servlet.http.HttpServletRequest

/**
 * Created by quangnbh on 11/5/13.
 */
class CategoryRestHandler (env : {
    val categoryService  : IDataService[Category]
}) extends RestHandler(() => new Category(0,null,null), env.categoryService, env.categoryService) {


    override def beforeSave(request: HttpServletRequest, instance: Category): Try[Unit] = {
        if (instance.name == null || instance.name.equals(""))
            return fail("Name is null")
        super.beforeSave(request, instance)
    }

    override def beforeUpdate(request: HttpServletRequest, instance: Category): Try[Unit] = {
        if (instance.name == null || instance.name.equals(""))
            return fail("Name is null")
        super.beforeUpdate(request, instance)
    }

    @Invoke()
    def listAll() = {
        val list = modelService.loadAll().toArray
        Json(list)
    }
}

class CategoryServlet extends HandlerContainerServlet {
    def factory() : BaseHandler = new CategoryRestHandler(Environment)
}