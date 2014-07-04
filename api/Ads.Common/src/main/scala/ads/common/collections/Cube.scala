package ads.common.collections

import scala.collection.mutable.{ArrayBuffer, HashMap}
import scala.collection.mutable


class Cube[T]{
    val dimensions = new HashMap[String,HashMap[Int,ArrayBuffer[Int]]]()
    val items = new ArrayBuffer[T]()

    def add(item: T, properties: Array[(String,Int)], complexProperties: Array[(String,Array[Int])] = null){
        val id = items.length
        items += item
        for ((k,v) <- properties){
            if (!dimensions.contains(k)) dimensions.put(k,new HashMap[Int,ArrayBuffer[Int]]())
            val d = dimensions(k)
            if (!d.contains(v)) d.put(v, new ArrayBuffer[Int]())
            d(v) += id
        }

        if (complexProperties != null){
            for ((k,arr) <- complexProperties){
                if (!dimensions.contains(k)) dimensions.put(k, new mutable.HashMap[Int,ArrayBuffer[Int]]())
                val d = dimensions(k)
                for (v <- arr){
                    if (!d.contains(v)) d.put(v, new ArrayBuffer[Int]())
                    d(v) += id
                }
            }
        }
    }

    private def foldId[S](filters: Array[(String,Int)], start: S, f: (S,Int)=>S): S = {
        var r = start

        var list = new ArrayBuffer[ArrayBuffer[Int]]()
        for ((k,v) <- filters){
            if (dimensions.contains(k)) {
                val map = dimensions(k)
                if (map.contains(v))
                    list += map(v)
                else return r
            } else return r
        }

        val joined =
            if (list.length == 0) null
            else if (list.length == 1) list(0)
            else{
                var a = new ArrayBuffer[Int]()
                var b = new ArrayBuffer[Int]()

                join(list(0), list(1), a)

                for (i <- 2 until list.length){
                    join(list(i), a, b)
                    val tmp = a
                    a = b
                    b = tmp
                }
                a
            }
        if (joined != null)
            for (x <- joined) r = f(r, x)
        r
    }

    def fold[S](filters: Array[(String,Int)], start: S, f: (S,T)=>S): S = foldId[S](filters, start, (s,id)=>f(s,items(id)))

    def join(x: ArrayBuffer[Int], y: ArrayBuffer[Int], output: ArrayBuffer[Int])={
        output.clear()

        var i = 0
        var j = 0

        while (i < x.length && j < y.length){
            val cmp = x(i) - y(j)

            if (cmp == 0){
                output += x(i)
                i+=1
                j+=1
            }
            else if (cmp < 0){
                while (i < x.length && x(i) < y(j)) i+=1
            }
            else {
                while (j < y.length && y(j) < x(i)) j+=1
            }
        }

        output
    }

    def contains(set: mutable.HashSet[Int], list: ArrayBuffer[Int]): Boolean = {
        for (x <- list){
            if (set.contains(x)) return true;
        }
        return false;
    }

    def getValues(filters: Array[(String,Int)], dimension: String) : ArrayBuffer[Int] = {
        val set = foldId[mutable.HashSet[Int]](filters, new mutable.HashSet[Int](), (set,id) => set += id)
        val r = new ArrayBuffer[Int]()
        if (dimensions.contains(dimension)) {
            val map = dimensions(dimension)
            for ((k,v) <- map) {
                if (contains(set,v)) r += k
            }
        }
        r
    }

    def getValues(dimension: String): ArrayBuffer[Int] = {
        val r = new ArrayBuffer[Int]()
        if (dimensions.contains(dimension)){
            val map = dimensions(dimension)
            for ((k,v) <- map) r += k
        }
        r
    }
}
