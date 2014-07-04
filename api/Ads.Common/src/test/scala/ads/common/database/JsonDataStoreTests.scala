package ads.common.database

import org.scalatest.FunSuite
import ads.common.model.Website


class JsonDataStoreTests extends FunSuite{
    test("load"){
        val store = new JsonDataStore[Website]()
        val id = store.save(new Website(1, 1, "Name", "desc", "việt"))

        val list = store.loadAll().toList
        val web = list(0)

        assertResult(id)(web.id)
        assertResult(1)(web.ownerId)
        assertResult("Name")(web.name)
        assertResult("desc")(web.description)
        assertResult("việt")(web.reviewType)

        web.name = "Updated!"

        store.update(web)

        val list2 =store.loadAll().toList
        val web2 = list2(0)

        assertResult(1)(list2.length)
        assertResult(id)(web2.id)
        assertResult(1)(web2.ownerId)
        assertResult("Updated!")(web2.name)
        assertResult("desc")(web2.description)
        assertResult("việt")(web2.reviewType)

        store.save(new Website(1, 1, "Name2", "desc", "xxx"))
        val list3 =store.loadAll().toList
        assertResult(2)(list3.length)
    }
}
