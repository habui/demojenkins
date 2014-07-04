package ads.common.services

import ads.common.database.{IDataDriver, InMemoryDataService, IDataService}
import ads.common.model.{Banner, ConversionType, Conversion}
import scala.concurrent.Future
import ads.common.Reflect

trait IConversionService extends IDataService[Conversion] {

}

trait IConversionTypeService extends IDataService[ConversionType] {
    def searchByName(search: String) : ConversionType
}

class ConversionService(env: {
    val conversionDataDriver: IDataDriver[Conversion]
    val actionLogServiceRef : Future[IActionLogService]
}) extends InMemoryDataService[Conversion](c=>c.orderId, env.conversionDataDriver, env.actionLogServiceRef) with IConversionService {

}

class ConversionTypeService(env: {
    val conversionTypeDataDriver: IDataDriver[ConversionType]
    val actionLogServiceRef : Future[IActionLogService]
}) extends InMemoryDataService[ConversionType](c=>c.id, env.conversionTypeDataDriver, env.actionLogServiceRef) with IConversionTypeService {
    def searchByName(search: String): ConversionType = {
        val listItems = loadAll().toList.filter(p=>{
            val name = Reflect.getProperties(p.getClass).get("name").get.get(p)
            if(name != null) {
                name.toString.toLowerCase.equals(search.toLowerCase)
            } else {
                false
            }})
        if (!listItems.isEmpty) listItems.head
        else null
    }
}

trait IConversion2Service extends IDataService[Conversion] {

}

class Conversion2Service(env: {
    val conversionDataDriver: IDataDriver[Conversion]
    val actionLogServiceRef : Future[IActionLogService]
}) extends InMemoryDataService[Conversion](c=>c.orderId, env.conversionDataDriver, env.actionLogServiceRef) with IConversion2Service {

}
