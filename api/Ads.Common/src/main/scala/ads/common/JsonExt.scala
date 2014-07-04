package ads.common

import com.google.gson._
import java.lang.reflect.{ParameterizedType, Type}
import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._
import ads.common.Syntaxs._

class ArrayBufferAdapter
    extends JsonSerializer[ArrayBuffer[_]]
    with JsonDeserializer[ArrayBuffer[_]] {

    def serialize(src: ArrayBuffer[_], typeOfSrc: Type, context: JsonSerializationContext): JsonElement = {
        val arr = new JsonArray()
        for (item <- src) {
            arr.add(context.serialize(item))
        }
        arr
    }

    def deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ArrayBuffer[_] = {
        val arr = json.asInstanceOf[JsonArray]
        val result = new ArrayBuffer[Any]()
        val elType = typeOfT.asInstanceOf[ParameterizedType].getActualTypeArguments()(0)

        for (el <- arr) {
            result += context.deserialize(el, elType)
        }
        result
    }
}

class MapAdapter
    extends JsonSerializer[Map[_, _]]
    with JsonDeserializer[Map[_, _]] {

    def serialize(src: Map[_, _], typeOfSrc: Type, context: JsonSerializationContext): JsonElement = {
        val map = new JsonObject()
        for ((k, v) <- src) {
            val key = k.toString
            val value = context.serialize(v)

            map.add(key, value)
        }
        map
    }

    def deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Map[_, _] = {
        var map = Map[Any, Any]()
        val keyType = typeOfT.asInstanceOf[ParameterizedType].getActualTypeArguments()(1)

        for (entry <- json.asInstanceOf[JsonObject].entrySet()) {
            val k = entry.getKey.toString
            val v = entry.getValue
            val value = JsonExt.deserialize(context, keyType, v)

            map = map + ((k, value))
        }
        map
    }
}

class ListAdapter
    extends JsonSerializer[List[_]]
    with JsonDeserializer[List[_]] {

    def serialize(src: List[_], typeOfSrc: Type, context: JsonSerializationContext): JsonElement = {
        val array = new JsonArray()
        for (item <- src) {
            context.serialize(item) |> array.add
        }
        array
    }

    def deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): List[_] = {
        val buf = new ArrayBuffer[Any]()
        val elType = typeOfT.asInstanceOf[ParameterizedType].getActualTypeArguments()(0)

        for (el <- json.asInstanceOf[JsonArray]) {
            val value = JsonExt.deserialize(context, elType, el)
            buf += value
        }

        buf.toList
    }
}


object JsonExt{
    def deserialize(context: JsonDeserializationContext, elementType: Type, v: JsonElement): Any =
        elementType match {
            case x if x == classOf[Int] => v.getAsInt
            case x if x == classOf[Long] => v.getAsLong
            case x if x == classOf[String] => v.getAsString
            case _ => context.deserialize(v, elementType)
        }


    val b = new GsonBuilder()
    b.registerTypeAdapter(classOf[ArrayBuffer[_]], new ArrayBufferAdapter())
    b.registerTypeAdapter(classOf[Map[_, _]], new MapAdapter())
    b.registerTypeAdapter(classOf[List[_]], new ListAdapter())
    b.registerTypeAdapter(List().getClass, new ListAdapter())
    b.registerTypeAdapter(List(1,2,3).getClass, new ListAdapter())

    val gson = b.create()
}
