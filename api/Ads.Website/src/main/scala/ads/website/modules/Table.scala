package ads.website.modules

import ads.common.database.{AbstractDelegateDataService, IDataService}
import ads.common.model._
import ads.web.mvc.{BaseHandler, Json, SmartDispatcherHandler}
import ads.web.{HandlerContainerServlet, Invoke}
import javax.servlet.http.HttpServletRequest
import scala.collection.mutable.ArrayBuffer
import ads.common.{IItem, SecurityUtils, SecurityContext}
import ads.website.{Environment, PermissionUtils}
import ads.website.modules.serving.InfoKind
import ads.website.handler.RestHandler

/**
 * Created by ducnm4 on 6/13/14.
 */
class ConfigTableModel ( var key : String, var value : String, var id:Int, var disable: Boolean = false) extends  IItem

class ConfigTableModelService (env: {
    val configTableService: IDataService[ConfigTable]
    //val zoneModelService: IDataService[ZoneModel]
    //val zoneService: IZoneService
}) extends AbstractDelegateDataService[ConfigTable, ConfigTableModel](env.configTableService) with IConfigTableModelService {
    override def fromModel(model: ConfigTableModel): ConfigTable = {
        new ConfigTable(model.key, model.value, 0, false)
    }

    override def toModel(item: ConfigTable): ConfigTableModel = {
        new ConfigTableModel(item.key, item.value, 0, false)
    }
}

object defaultConfigTableModel{
    def apply() = new ConfigTableModel("", "", 0)
}

trait IConfigTableModelService extends IDataService[ConfigTableModel] {
    //def getTable(tableId: Int): Array[TableModel]
}

class ConfigTableRestHandler(env: {
    val configTableModelService: IConfigTableModelService
    val configTableService: IDataService[ConfigTable]
}) extends RestHandler[ConfigTableModel, ConfigTable](() => defaultConfigTableModel(), env.configTableModelService, env.configTableService) {

}
class ConfigTableServlet extends HandlerContainerServlet {
    def factory(): BaseHandler = new ConfigTableRestHandler(Environment)
}