package ads.common

import com.netflix.hystrix.HystrixCommand
import rx.lang.scala.Observable
import scala.util.control.NonFatal
import java.util
import java.util.concurrent.{ConcurrentLinkedQueue, ConcurrentHashMap}
import java.io.PrintStream
import java.util.Date
import scala.collection.mutable.ArrayBuffer

trait IDisposable {
    def dispose()
}

package object Syntaxs {

    abstract class Try[+T] {
        def isSuccess: Boolean = true

        def >=>[A](f: T => Try[A]): Try[A] = {
            this match {
                case Success(s) => f(s)
                case Failure(e) => new Failure(e)
            }
        }

        def |+>[A](f: => A): Try[(T, A)] = {
            this match {
                case Success(s) => Try((s, f))
                case Failure(e) => new Failure(e)
            }
        }

        def ||>[A](f: => A): Try[A] = {
            this match {
                case Success(s) => Try(f)
                case Failure(e) => new Failure(e)
            }
        }

        def +[A](f: Try[A]): Try[(T, A)] = {
            this match {
                case Success(st) =>
                    f match {
                        case Success(sa) => new Success((st, sa))
                        case Failure(e) => new Failure(e)
                    }
                case Failure(e) => new Failure(e)
            }
        }

        def isTrue[T >: Boolean](): Try[Boolean] = new Success(true)

        def getOrElse[G >: T](v: G): T =
            this match {
                case Success(s) => s
                case Failure(e) => v.asInstanceOf[T]
            }

        def tryGet[A](f: T => A): Try[A] =
            this match {
                case Success(s) => succeed(f(s))
                case Failure(e) => new Failure(e)
            }
    }


    case class Success[TSuccess](value: TSuccess) extends Try[TSuccess]

    case class Failure(cause: Any) extends Try[Nothing] {
        override def isSuccess: Boolean = false
    }

    object Try {

        def apply[A](r: => A): Try[A] =
            try new Success(r)
            catch {
                case NonFatal(e) => new Failure(e)
            }

        def apply[A, B](r: (A) => B): A => Try[B] = (arg: A) => Try(r(arg))


        def apply[A, B, C](a: Try[A], b: Try[B], c: Try[C]): Try[(A, B, C)] = {
            a match {
                case Failure(fa) => new Failure(fa)
                case Success(sa) =>
                    b match {
                        case Failure(fb) => new Failure(fb)
                        case Success(sb) =>
                            c match {
                                case Failure(fc) => new Failure(fc)
                                case Success(sc) => new Success((sa, sb, sc))
                            }
                    }
            }
        }

        def notNull[A](r: => A): Try[A] = {
            val tmp = r
            if (tmp == null) new Failure(null)
            else new Success(tmp)
        }
    }

    final class Cond[A](flag: Boolean, first: A) {
        def |(second: A) = if (flag) first else second
    }

    implicit class PipelineContainer[F](val self: F) extends AnyVal {
        @inline final def |>[G](f: F => G) = f(self)

        final def ??(d: => F)(implicit ev: Null <:< F): F =
            if (self == null) d else self

        final def ?[T](first: T)(implicit ev: F <:< Boolean): Cond[T] = new Cond[T](self, first)

        final def ignore(): Unit = {}
    }

    implicit class ConcurrentHashMapExt[K, V](val map: ConcurrentHashMap[K, V]) extends AnyVal {

        def tryGetValue(key: K): Option[V] = {
            val value = map.get(key)
            if (value == null) return None
            return Some(value)
        }

        def getOrAdd(key: K, f: K => V): V = {
            var record = map.get(key)
            if (record == null) {
                record = f(key)
                val tmp = map.putIfAbsent(key, record)
                if (tmp != null) record = tmp
            }
            record
        }
    }

    implicit class ArrayBufferExt[T <: IItem](val ab : ArrayBuffer[T]) extends AnyVal{
        def updateOrAdd(item : T) : ArrayBuffer[T] = {
          var ab1 = ab.filter(t => t.id != item.id)
            ab1 += item
            ab1
        }
    }

    implicit class ConcurrentQueueExt[T](val queue: ConcurrentLinkedQueue[T]) extends AnyVal {

        def enqueue(item: T) = queue.offer(item)

        def dequeue(): Option[T] = {
            if (!queue.isEmpty) {
                val data = queue.poll()
                if (data != null) return Some(data)
            }
            None
        }
    }


    implicit class DateContainer[F](val value: Date) extends AnyVal {
        def addHour(hour: Int) = new Date(value.getTime + hour * 3600L * 1000)

        def <(another: Date): Boolean = value.getTime < another.getTime

        def <=(another: Date): Boolean = value.getTime <= another.getTime

        def +(ticks: Long) = new Date(value.getTime + ticks)
    }

    implicit class DateConverter[F](val x: Int) extends AnyVal {
        def day = x * 24 * 1000 * 3600

        def hour = x * 1000L * 3600

        def min = x * 1000L * 60

        def sec = x * 1000L
    }

    implicit class ObservableContainer[F](val value: HystrixCommand[F]) extends AnyVal {
        def observeIt(): Observable[F] = Observable(value.observe())
    }

    implicit class ArrayContainer[T](val value: Array[T]) extends AnyVal {
        def toArrayList(): util.ArrayList[T] = {
            val list = new util.ArrayList[T](value.length)
            for (item <- value) list.add(item)
            return list
        }
    }

    implicit class ScalaHashMapExt[K,V](val m: scala.collection.mutable.HashMap[K,V]) extends AnyVal{
        def getOrAdd(key: K, f: K=>V):V = {
            m.get(key) match {
                case Some(value) => value
                case None =>{
                    val value = f(key)
                    m.put(key, value)
                    value
                }
            }
        }
    }

    implicit class HashMapExt[K,V](val map: util.HashMap[K,V]) extends AnyVal{
        def putOrUpdate(key: K, default: V, update: V=>V)= {
            if (!map.containsKey(key)){
                map.put(key, default)
            }
            else{
                val value = map.get(key)
                val updated = update(value)
                map.put(key, updated)
            }
        }
    }

    implicit class RailwayContainer[T](val self: T) extends AnyVal {

        def bind[A, B](implicit evidence: T <:< (A => Try[B])): (Try[A] => Try[B]) =
            (arg: Try[A]) => {
                arg match {
                    case Success(s) => self(s)
                    case Failure(f) => new Failure(f)
                }
            }

        def >>[A, B, C](g: B => C)(implicit evidence: T <:< (A => B)) = (arg: A) => g(self(arg))

        def >=>[A, B, C](g: B => Try[C])(implicit evidence: T <:< (A => Try[B])) =
            (arg: A) => {
                self(arg) match {
                    case Success(s) => g(s)
                    case Failure(f) => new Failure(f)
                }
            }

        def <+>[A, B, C, D](f: C => Try[D])(implicit evidence: T <:< (A => Try[B])) =
            (x: A, y: C) => {
                val fr = f(y)
                val gr = self(x)

                fr match {
                    case Success(sf) =>
                        gr match {
                            case Success(sg) => succeed((sf, sg))
                            case Failure(fg) => new Failure(fg)
                        }
                    case Failure(ff) => new Failure(ff)
                }
            }

        def switch() = new Success(self)

        def checkNull[A, B]()(implicit evidence: T <:< (A => B)) = (arg: A) => {
            val t = self(arg)
            if (t != null) succeed(t)
            else fail("null")
        }

        def checkEmpty()(implicit evidence: T <:< String): Try[String] = {
            if (self == null || self.isEmpty) return fail("null")
            return succeed(self)
        }
    }

    object switch {
        def apply[A, R](f: A => R): (A) => Try[R] = (arg: A) => Success(f(arg))
    }

    object tee {
        def apply[A](f: A => Unit): A => A = (arg: A) => {
            f(arg)
            arg
        }
    }

    object tryCatch {

        def apply[A, B](r: A => B) = (arg: A) =>
            try new Success(r(arg))
            catch {
                case NonFatal(e) => new Failure(e)
            }
    }

    object either {
        def apply[A, B](sf: A => B, ff: Any => Any): Try[A] => Try[B] =
            (arg: Try[A]) => {
                arg match {
                    case Success(s) => succeed(sf(s))
                    case Failure(f) => fail(ff(f))
                }
            }
    }

    object checkEmpty {
        def apply(x: String): Try[String] = {
            if (x == null || x.isEmpty) return fail("null")
            return succeed(x)
        }
    }

    object succeed {
        def apply[A](x: A) = new Success(x)
    }

    object fail {
        def apply[A](x: A) = new Failure(x)
    }

    def measureTimeNano(body: => Any): Long = {
        val begin = System.nanoTime()
        body
        val end = System.nanoTime()
        end - begin
    }

    def measureTime(body: => Any): Long = {
        val begin = System.currentTimeMillis()
        body
        val end = System.currentTimeMillis()
        end - begin
    }

    def printException(e: Throwable, p: PrintStream) {
        var ex = e
        p.println("------------------------------------------------------")
        while (ex != null) {
            p.println()
            ex.printStackTrace(p)
            ex = ex.getCause
        }
    }

    def printException(e: Throwable, f: String=>Unit) {
        var ex = e
        f("------------------------------------------------------")
        while (ex != null) {
            f(ex.getMessage)
            f(ex.getStackTraceString)
            ex = ex.getCause
        }
    }

    def minDate(x: Date, y: Date): Date = (x.getTime > y.getTime) ? x | y

    def using(e: Any)(body: => Any) {
        try {
            body
        }
        finally {
            e match {
                case d: IDisposable =>
                    d.dispose()
                case _ =>
            }
        }
    }

    def loopUntilZeroException[A >: Null,B](f: B => A , retry : Int, default : A, timeSpan: Int) = (s: B) => {
        var done = false
        var result: A = null
        for(i <- 0 to retry if !done) {
//        while (!done && retry > 0){
            try{
                result = f(s)
                done = true
            }
            catch{
                case (x: Throwable) =>{
                    printException(x, s => println(s))
                    if(retry > 0) {
                        Thread.sleep(timeSpan)
                    }
                }
            }
        }
        if(!done) result = default
        result
    }

    def formatMoney(x: Long): String  = {
        var s = ""
        var n = x
        var count = 0
        while (n > 0){
            val mod = n % 10
            if (count % 3 == 0 & count > 0){
                s = "." + s
            }
            s = mod + s
            n = n / 10
            count += 1
        }
        s
    }
}
