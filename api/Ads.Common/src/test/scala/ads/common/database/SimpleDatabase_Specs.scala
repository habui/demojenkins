package ads.common.database

import org.scalatest.FunSuite
import ads.common._

class Model (var name: String, var age: Int, var id: Int = 0, var disable: Boolean = false) extends IItem

class SimpleDatabase_Specs extends FunSuite{

    test("setup database"){
        val db = new SimpleDatabase[Model](null)(()=>new Model("noname00", 0))
        db.createTable()
    }

    test("save, load"){
        val db = new SimpleDatabase[Model](null)(()=>new Model("noname00", 0))
        val model = new Model("test", 1)

        val id = db.save(model)
        val read = db.load(id)

        assertResult(model.name)(read.name)
        assertResult(model.age)(read.age)
    }

    test("update"){
        val db = new SimpleDatabase[Model](null)(()=>new Model("", 0))
        val id = db.save(new Model("xxx", 10))
        val item = db.load(id)
        item.age += 1
        db.update(item)
        val updated = db.load(id)

        assertResult(item.age)(updated.age)
    }
}
