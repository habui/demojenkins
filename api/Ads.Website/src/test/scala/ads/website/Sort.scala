package ads.website

import org.scalatest.FunSuite
import java.util.concurrent.{ConcurrentSkipListSet, ConcurrentSkipListMap}
import ads.common.Syntaxs._
import scala.collection.JavaConversions._
import java.util.Comparator
import ads.common.Reflect

class SortExperiments extends FunSuite{
    class Person(val id: Int, var age: Int)

    class PersonComparator extends Comparator[Person]{
        def compare(o1: Person, o2: Person): Int = o1.age - o2.age
    }

    class GenericComparator(get: (Any)=>Any) extends Comparator[Object]{
        def compare(o1: Object, o2: Object): Int = {
            val x = get(o1)
            val y = get(o2)

            x match{
                case x : Int => x - y.asInstanceOf[Int]
            }
        }
    }

    test("load"){
        val n = 100*1000
        val data = new ConcurrentSkipListMap[Int,Person]()
        for (i <- 0 until n) data.put(i, new Person(i, i/1000))

        val record = Reflect.getProperties(classOf[Person]).find(p=>p._1 == "age").get._2

        //

        measureTime{

            val set = new ConcurrentSkipListSet[Person](new GenericComparator(record.get))
            for (p <- data.values()) set.add(p)

            set.slice(n/2, 100).toList
        } |> println



        //val list = data.entrySet().toList.sortWith((x,y)=>x.getKey > y.getKey).toList
    }
}
