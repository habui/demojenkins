package ads.common

import java.util
import java.util.concurrent.ConcurrentHashMap
import ads.common.Syntaxs._
import scala.collection.JavaConversions._

class PropertyRecord(val get: (Any) => Any, val set: (Object, Object) => Unit, val clazz: Class[_])

object Reflect {

    val cached = new ConcurrentHashMap[Class[_], Map[String, PropertyRecord]]()

    def toMap(obj: Any) = {
        val records = obj.getClass |> getProperties

        records.
            entrySet().
            map((r) => {
            val name = r.getKey
            val record = r.getValue
            val value = record.get(obj)
            value match{
                case null => (name, "")
                case _ => (name, value.toString)
            }
        }
        ).toMap
    }

    def getProperties(clazz: Class[_]) = cached.getOrAdd(clazz, create)

    def create(clazz: Class[_]) = {
        var map = Map[String, PropertyRecord]()
        val setMap = clazz.getMethods.filter(m => m.getName.endsWith("_$eq")).map(m => (m.getName, m)).toMap

        val all = clazz.getMethods.map(m => (m.getName, m)).toMap

        for (nameX <- setMap.keySet) {
            val name = nameX.substring(0, nameX.length - 4)

            val getMethod = all.get(name).get
            val setMethod = setMap.get(nameX).get

            map += ((name, new PropertyRecord((obj) => getMethod.invoke(obj), (obj, value) => setMethod.invoke(obj, value), getMethod.getReturnType)))
        }

        map
    }
    implicit def reflector(ref: AnyRef) = new {
        def getV(name: String): Any = ref.getClass.getMethods.find(_.getName == name) match {
            case Some(m) => m.invoke(ref)
            case None => null
        }
        def setV(name: String, value: Any): Unit = ref.getClass.getMethods.find(_.getName == name + "_$eq").get.invoke(ref, value.asInstanceOf[AnyRef])
    }
}
