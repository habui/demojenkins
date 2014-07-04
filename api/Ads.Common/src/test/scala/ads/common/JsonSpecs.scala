package ads.common

import org.scalatest.FunSuite
import scala.collection.mutable.ArrayBuffer
import scala.collection.immutable.HashMap
import util.control.Breaks._

import Parser._
import ads.common.Syntaxs._
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.methods.HttpGet
import java.io.{InputStreamReader, BufferedReader}
import ads.common.model.Config
import java.util.concurrent.{ThreadLocalRandom, ConcurrentSkipListSet, ConcurrentMap}
import scala.Some
import ads.common.Parser.ParseResult
import java.util.UUID
import ads.common.sessions.{SiteZoneFreqStore, Session}
import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.ops._
import scala.Some
import ads.common.Parser.ParseResult
import scala.collection.parallel.mutable


object Parser {

    implicit class ParserExt[T](val self: ICharStream => ParseResult[T]) extends AnyVal{
        def map[K](f: T => K) = (stream: ICharStream) => {
            val r = self(stream)
            if (r.success) {
                try {
                    val data = f(r.value)
                    success(data)
                }
                catch {
                    case e: Throwable => fail(e.getMessage)
                }
            }
            else fail(r.message)
        }

        def + [K](f : ICharStream=>ParseResult[K]) = (stream: ICharStream)=>{
            val a = self(stream)
            if (a.success) {
                val b = f(stream)
                if (b.success) {
                    a.value match{
                        case list: List[K] => success(list :+ b.value)
                        case _ => success(List(a.value, b.value))
                    }
                }
                else fail(b.message)
            }
            else fail(a.message)
        }

        def repeat(seperator: ICharStream=>ParseResult[_]) = (stream: ICharStream)=>{
            val result = new ArrayBuffer[T]()
            var ok = true
            var first = true
            while (ok){
                if (!first){
                    val tmp = seperator(stream)
                    ok = tmp.success
                }
                else{
                    first = false
                }

                if (ok){
                    val r = self(stream)
                    if (r.success){
                        result += r.value
                    }
                    else{
                        ok = false
                    }
                }
            }
            success(result.toList)
        }

        def between(begin: ICharStream=>ParseResult[_], end: ICharStream=>ParseResult[_]) = (stream: ICharStream)=>{
            val b = begin(stream)
            if (b.success)
            {
                val r = self(stream)
                if (r.success){
                    val e = end(stream)
                    if (e.success){
                        success(r.value)
                    }
                    else fail(e.message)
                }
                else fail(r.message)
            }
            else fail(b.message)
        }
    }

    trait ICharStream {
        def current: Char
        def peek: Char
        def moveNext(): Boolean
        def getState: Int
        def setState (x: Int)
    }

    class CharStream(s: String) extends ICharStream {
        var index = -1
        var current: Char = 0.toChar

        def peek: Char =
            if (index < s.length - 1)
                s(index + 1)
            else 0.toChar

        def moveNext(): Boolean = {
            if (index >= s.length - 1) false
            index += 1
            current = s(index)
            true
        }

        def getState: Int = index

        def setState(x: Int): Unit = {
            index = x
            if (x < 0) current = 0.toChar
            else current = s(index)
        }
    }

    case class ParseResult[+T](val success: Boolean, val message: String = "", val value: T = null.asInstanceOf[T]) {
    }

    def success[T](v: T): ParseResult[T] = new ParseResult[T](true, value = v)

    def fail(msg: String): ParseResult[Nothing] = new ParseResult[Nothing](false, message = msg)

    def map[T, K](p: ICharStream => ParseResult[T], f: T => K) = (stream: ICharStream) => {
        val r = p(stream)
        if (r.success) {
            try {
                val data = f(r.value)
                success(data)
            }
            catch {
                case e: Throwable => fail(e.getMessage)
            }
        }
        else fail(r.message)
    }

    def char(c: Char) = (stream: ICharStream) => {
        if (stream.peek == c) {
            stream.moveNext()
            success(c)
        }
        else {
            fail("expected: " + c)
        }
    }

    def oneOrMany(f: Char => Boolean, info: String = ""): ICharStream => ParseResult[String] =
         stream => {
            val sb = new StringBuilder()
            while (f(stream.peek) && stream.moveNext()) {
                sb.append(stream.current)
            }
            val s = sb.toString()
            if (s.length < 1) fail("expect one or many " + info)
            else success(s)
        }

    def choice[T](list: List[(ICharStream)=>ParseResult[T]], msg: String = "???") = (stream: ICharStream)=>{

        var index = 0
        var result = null.asInstanceOf[ParseResult[T]]
        val state = stream.getState

        while (index < list.size && result == null){
            val p = list(index)
            val r = p(stream)

            if (r.success) result = r
            else
                stream.setState(state)

            index += 1
        }

        if (result != null) result
        else fail(msg)
    }
}

class ParseTests extends FunSuite {
    test("number") {
        //val number = map[String,Int](oneOrMany(c => c >= '0' && c <= '9'), s=>s.toInt)

        val number = oneOrMany(c => c >= '0' && c <= '9', "[0-9]").map(s=>s.toInt)

        number(new CharStream("123")) |> println

        val chars = oneOrMany(c => (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9'))

        val email = (chars + char('@') + chars + char('.') + chars).map(t=>t.reduce((x,y)=>x.toString+y.toString))

        email(new CharStream("abc@xyz.com")) |> println

        val list = number.repeat(char(','))

        val array = list.between(char('['), char(']')).map(list=>list.toArray)

        array(new CharStream("[1,2,3,4]")).value |> println

        list(new CharStream("1,2,3,4,5")) |> println

        val nameOrNumber = choice(List(email, number))
        nameOrNumber(new CharStream("123@xxx")) |> println


    }
}



object Json {

    trait IJsonSerializer {
        def save(data: Any): String

        def load(data: String, clazz: Class[_]): Any
    }

    trait IComplexSerializer extends IJsonSerializer {
        def canHandle(clazz: Class[_]): Boolean
    }

    class MapJsonSerializer extends IComplexSerializer {
        def canHandle(clazz: Class[_]): Boolean = clazz.equals(classOf[Map[_, _]])

        def save(data: Any): String = {
            val sb = new StringBuilder();
            sb.append("{");
            val map = data.asInstanceOf[Map[_, _]]
            var first = true
            map.foreach(
                (e: (_, _)) => {
                    if (!first) sb.append(',')
                    sb.append( """"""")
                    sb.append(e._1)
                    sb.append( """":""")
                    sb.append(toJson(e._2))
                    first = false
                }
            )

            sb.append("}")
            sb.toString()
        }

        def load(data: String, clazz: Class[_]): Any = null
    }

    class ListJsonSerializer extends IComplexSerializer {

        def canHandle(clazz: Class[_]): Boolean = {
            if (clazz.equals(classOf[ArrayBuffer[_]]) ||
                //clazz.equals(classOf[Array[_]]) ||
                clazz.equals(classOf[List[_]]))
                return true;
            return false;
        }

        def save(data: Any): String = {
            val sb = new StringBuilder()
            sb.append("[")
            var first = true
            if (data.isInstanceOf[ArrayBuffer[Any]]) {
                val arr = data.asInstanceOf[ArrayBuffer[Any]]
                for (item <- arr) {
                    if (!first) {
                        sb.append(",")
                    }
                    else first = false
                    sb.append(toJson(item))
                }
            } else if (data.isInstanceOf[List[Any]]) {
                val arr = data.asInstanceOf[List[Any]]
                for (item <- arr) {
                    if (!first) {
                        sb.append(",")
                    }
                    else first = false
                    sb.append(toJson(item))
                }
            }
            sb.append("]")
            sb.toString()
        }

        def load(data: String, clazz: Class[_]): Any = {
            if (clazz == classOf[List[Any]]) {
                val declaredField = clazz.getDeclaredField("list")
                val listType = declaredField.getGenericType
                println(listType)

            }
        }
    }

    class ArrayJsonSerializer extends IComplexSerializer {
        def canHandle(clazz: Class[_]): Boolean = clazz.isArray

        def save(data: Any): String = {
            val sb = new StringBuilder()
            sb.append('[')
            val arr = data.asInstanceOf[Array[_]]
            var i = 0;
            while (i < arr.length - 1) {
                sb.append(toJson(arr(i)))
                sb.append(',')
                i += 1
            }
            sb.append(toJson(arr(i)))
            sb.append(']')
            sb.toString()
        }

        def load(data: String, clazz: Class[_]): Any = {
            var result = Array[Any]("")
            val strs = data.split(Array[Char](',', '[', ']', '"')) //fail
            for (str <- strs)
                if (str != "")
                    result = result :+ str
            result
        }
    }

    class IntJsonSerializer extends IJsonSerializer {
        def save(data: Any): String =
            data.asInstanceOf[Int].toString

        def load(data: String, clazz: Class[_]): Any = data.trim.toInt
    }

    class DoubleJsonSerializer extends IJsonSerializer {
        def save(data: Any): String =
            data.asInstanceOf[Double].toString

        def load(data: String, clazz: Class[_]): Any = data.trim.toDouble
    }

    class FloatJsonSerializer extends IJsonSerializer {
        def save(data: Any): String =
            data.asInstanceOf[Float].toString

        def load(data: String, clazz: Class[_]): Any = data.trim.toFloat
    }


    class StringJsonHandler extends IJsonSerializer {
        def save(data: Any): String =
            '"' + data.asInstanceOf[String] + '"'

        def load(data: String, clazz: Class[_]): Any = data
    }

    def nativeHandlers = Map[Class[_], IJsonSerializer](
        classOf[Int] -> new IntJsonSerializer(),
        classOf[java.lang.Integer] -> new IntJsonSerializer(),
        classOf[String] -> new StringJsonHandler(),
        classOf[java.lang.String] -> new StringJsonHandler(),
        classOf[Double] -> new DoubleJsonSerializer(),
        classOf[java.lang.Double] -> new DoubleJsonSerializer(),
        classOf[Float] -> new FloatJsonSerializer(),
        classOf[java.lang.Float] -> new FloatJsonSerializer()
    )

    def toJsonFromComplex(obj: Any): String = {
        val properties = Reflect.getProperties(obj.getClass)
        val sb = new StringBuilder()
        sb.append("{")

        var first = true

        for (p <- properties) {
            if (!first) {
                sb.append(",\n")
            }
            else first = false

            val name = p._1
            val record = p._2
            val value = record.get(obj)

            val valueStr = nativeHandlers.get(record.clazz) match {
                case Some(handler) => handler.save(value)
                case None => {
                    complexHandlers.find(handler => handler.canHandle(record.clazz)) match {
                        case Some(handler) => handler.save(value)
                        case None => {
                            toJson(value)
                        }
                    }
                }
            }
            sb.append( """"""")
            sb.append(s"$name")
            sb.append( """"""")
            sb.append(s": $valueStr")
        }
        sb.append("}")
        sb.toString()
    }

    def complexHandlers = List(new ListJsonSerializer(), new ArrayJsonSerializer(), new MapJsonSerializer())

    def toJson(obj: Any): String = {
        nativeHandlers.get(obj.getClass) match {
            case Some(handler) => handler.save(obj)
            case None => toJsonFromComplex(obj)
        }
    }

    def fromJson[T](obj: String, clazz: Class[_]): Any = {
        if (clazz.isArray)
            (new ArrayJsonSerializer).load(obj, clazz)
        else {
            nativeHandlers.get(clazz) match {
                case Some(handler) => handler.load(obj, clazz)
                case None => fromJsonFromComplex(obj, clazz)
            }
        }
    }

    def fromJsonFromComplex[T](json: String, clazz: Class[_])(implicit mf: scala.reflect.Manifest[T]): Any = {
        var keyValue = new HashMap[String, Any]()
        val len = json.length
        var oldPosition = 0
        var newPosition = 0
        var countBrackets = 0
        var countQuotes = 0
        var countSquareBrackets = 0
        var isKey = true
        var key: String = ""
        var value: String = ""
        for (c <- json) {
            breakable {
                if (c == '{') {
                    countBrackets += 1
                    break
                }
                else if (c == '}' && newPosition != len - 1) {
                    countBrackets -= 1
                    break
                }
                if (countBrackets < 2) {
                    // not in sub Object
                    if (isKey) {
                        //get key
                        if (c == '"') {
                            if (countQuotes == 0) {
                                oldPosition = newPosition + 1
                                countQuotes += 1
                            } else {
                                key = json.substring(oldPosition, newPosition)
                                isKey = false
                                countQuotes -= 1
                            }
                        } else break
                    } else {
                        //get value
                        if (c == ':') {
                            oldPosition = newPosition + 1
                        } else if (c == '[') countSquareBrackets += 1
                        else if (c == ']') countSquareBrackets -= 1
                        if (countSquareBrackets == 0) {
                            // skip if value is list
                            if (c == '"') {
                                // if value is string
                                if (countQuotes == 0) {
                                    oldPosition = newPosition + 1
                                    countQuotes += 1
                                } else {
                                    countQuotes -= 1
                                    value = json.substring(oldPosition, newPosition)
                                    keyValue = keyValue.+((key, value))
                                    isKey = true
                                    break
                                }
                            }
                            if (c == ',' || newPosition == len - 1) {
                                // if value is not string
                                value = json.substring(oldPosition, newPosition)
                                keyValue = keyValue.+((key, value))
                                isKey = true
                            }
                        }
                    }
                }
            }
            newPosition += 1
        }
        println(keyValue)
        val properties = Reflect.getProperties(clazz)
        for (p <- properties) {
            val name = p._1
            val record = p._2
            val obj = nativeHandlers.get(record.clazz) match {
                case Some(handler) => handler.load(keyValue.getOrElse(name, null).asInstanceOf[String], record.clazz)
                case None => {
                    complexHandlers.find(handler => handler.canHandle(record.clazz)) match {
                        case Some(handler) => handler.load(keyValue.getOrElse(name, null).asInstanceOf[String], record.clazz)
                        case None => {
                            fromJson(keyValue.getOrElse(name, null).asInstanceOf[String], record.clazz)
                        }
                    }
                }
            }
            keyValue = keyValue.updated(name, obj)
        }
        val numberArg = clazz.getDeclaredFields().length
        val args: Array[AnyRef] = new Array(numberArg)
        args(0) = new ads.common.JsonSpecs()
        for (i <- 1 until numberArg) {
            args(i) = keyValue.getOrElse(clazz.getDeclaredFields()(i - 1).getName, null).asInstanceOf[AnyRef]
        }
        clazz.getConstructors()(0).newInstance(args: _*)
    }

}


class JsonSpecs extends FunSuite {

    class Student(var Id: Int, var name: String)

    class ListTest(var Id: Int, var list: List[Int])

    class Person(var name: String, var age: Int, var friends: ArrayBuffer[String], var array: Array[String], var list: List[Student], var map: Map[String, Int])

    class Parent(var name: String, var student: Student)


    test("test") {
        val student = new Student(0, "abc")
        println(student)
        val map : Map[Int, Student] = Map(1->student)
        val student2 = map(1)
        println(student2)
        val x = 1
    }

    test("freq") {
        val key = "abc12435678"
        Session.start()
        val atomic = new AtomicInteger(0)
        val fail = new AtomicInteger(0)
        val numOfThread = 10

        for(i <- 1 to numOfThread) {
            spawn {
                for(i <- 1 to 100) {
                    try {
                        var retry = -5
                        while(retry < 5) {
                            val session = Session.store.getOrAdd(key)
                            session.put(ThreadLocalRandom.current().nextInt(0,100), (System.currentTimeMillis()/1000).toInt)
                            if(Session.store.put(key, session)) retry = 10
                            else retry += 1
                        }
                        if(retry == 5) fail.incrementAndGet()
                    } catch {
                        case x:Throwable => x.printStackTrace()
                    }

                }
                atomic.incrementAndGet()
            }
        }

        while(atomic.get() <= numOfThread - 1) {
            Thread.sleep(1000)
        }
        println("Fail : " + fail.get())
        val session = Session.store.getOrAdd(key)
        val x = 1
    }

    test("freq_site") {
        val key = "abc12435678"
        SiteZoneFreqStore.start()
        val atomic = new AtomicInteger(0)
        val fail = new AtomicInteger(0)
        val numOfThread = 10

        for(i <- 1 to numOfThread) {
            spawn {
                for(i <- 1 to 100) {
                    try {
                        var retry = -5
                        while(retry < 5) {
                            val session = SiteZoneFreqStore.store.getOrAdd(key)
                            session.put(ThreadLocalRandom.current().nextInt(0,100), (System.currentTimeMillis()/1000).toInt)
                            if(SiteZoneFreqStore.store.put(key, session)) retry = 10
                            else retry += 1
                        }
                        if(retry == 5) fail.incrementAndGet()
                    } catch {
                        case x:Throwable => x.printStackTrace()
                    }

                }
                atomic.incrementAndGet()
            }
        }

        while(atomic.get() <= numOfThread - 1) {
            Thread.sleep(1000)
        }
        println("Fail : " + fail.get())
        val session = SiteZoneFreqStore.store.getOrAdd(key)
        val x = 1
    }
}
