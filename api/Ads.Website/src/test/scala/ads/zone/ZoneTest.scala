package ads.zone

import org.scalatest.FunSuite
import ads.website.Environment
import ads.common.model.PRZoneModel

/**
 * Created by cuongtv2 on 1/2/14.
 */
class ZoneTest extends FunSuite{


    test("create przone") {
        val przone = new PRZoneModel(1,1,"PR zone",0,10,10)
        Environment.zoneModelService.save(przone)
    }

}
