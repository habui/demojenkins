package ads.common

import org.scalatest.FunSuite
import java.util.concurrent.atomic.{AtomicLong, AtomicReference, AtomicBoolean, AtomicInteger}
import scala.concurrent.ops._
import Syntaxs._
import java.util.Random
import java.util


class Epoch {
    val _count = new AtomicInteger()
    val _sealed = new AtomicBoolean()
    val _free = new AtomicInteger()

    override def toString() = "Epoch[" + _free.get() + "]"

    def seal() = _sealed.compareAndSet(false, true)

    def enter() =
        if (_sealed.get()) false
        else {
            _count.incrementAndGet()
            true
        }

    def free() =
        if (_sealed.get()) - 1
        else _free.incrementAndGet()

    def exit() = {
        val v = _count.decrementAndGet()
        if (v == 0 && _sealed.get()){
            println ("free epoch: " + this)
        }
    }
}


object Rcu {
    val _current = new AtomicReference[Epoch](new Epoch())

    val sum = new AtomicLong()

    def enter() = while (!_current.get().enter()){}

    def exit() = _current.get().exit()

    def free(){
        var r = 0
        do{
            r = _current.get().free()
            if (r > 1000) seal()
        } while (r < 0)
    }

    def seal() =
        if (_current.get().seal()){
            sum.addAndGet(_current.get()._free.get())
            _current.set(new Epoch())
        }

}

object Memory{
    val chunkSize = 65536

    class Heap{
        def allocate(size: Int) = null
    }

    val heapContainer = new ThreadLocal[Heap]()

    def allocate(size: Int) = getHeap.allocate(size)

    def getHeap: Heap = {
        var r = heapContainer.get()
        if (r == null){
            r = new Heap()
            heapContainer.set(r)
        }
        r
    }
}

class MemorySpecs extends FunSuite{
    test ("load"){
    }
}

class RcuSpecs extends FunSuite {

    test("load") {

        val threads = 50
        var sum = 0L
        var total = new AtomicInteger(threads)

        for (i <- 0 until threads){
            spawn{

                for (k <- 0 until 1000){
                    Rcu.enter()

                    for (l <- 0 until 1000) sum += l

                    Rcu.free()

                    Rcu.exit()
                }

                total.addAndGet(-1)
            }
        }

        while (total.get() > 0) Thread.sleep(1)

        Thread.sleep(100)

        println("sum = " + (Rcu.sum.get() + Rcu._current.get()._free.get()))

    }

}
