package ads.common.collections

import scala.collection.mutable.ArrayBuffer
import java.util
import scala.collection.mutable.HashMap
import scala.collection.mutable


class RadixReportTrie{

    class Node (val key: Int, var value: Option[Double], var children: HashMap[Int,Node])

    val _root = new mutable.HashMap[String,Node]()

    def find(key: String, path: Array[Int]): Option[Double] = {
        _root.get(key) match{
            case Some(node)=>{
                var r = node
                for (i <- 0 until path.length){
                    val c = path(i)
                    if (r.children != null && r.children.contains(c)){
                        r = r.children(c)
                        if (i + 1 == path.length) return r.value
                    }
                    else return None
                }
                return None
            }
            case None => None
        }
    }

    def insert(key: String, path: Array[Int], value: Double){

        if (!_root.contains(key)) _root.put(key, new Node(0, None, new mutable.HashMap[Int,Node]()))

        var r = _root.get(key).get
        for (i <- 0 until path.length){
            val c = path(i)
            if (r.children == null) r.children = new mutable.HashMap[Int,Node]()
            if (!r.children.contains(c)) r.children.put(c, new Node(c, None, null))
            r = r.children.get(c).get
        }
        r.value = Some(value)
    }

    def sumAll(key: String, path: Array[Int]): Option[Double] = {
        _root.get(key) match{
            case Some(node)=>{
                var r = node
                for (i <- 0 until path.length){
                    val c = path(i)
                    if (r.children != null && r.children.contains(c)){
                        r = r.children.get(c).get
                        if (i + 1 == path.length){
                            var result = 0d
                            val stack = new util.Stack[Node]()
                            stack.push(r)

                            while (!stack.empty()){
                                r = stack.pop()
                                if (r.children != null)
                                    for (child <- r.children.values) {
                                        if (child.children != null) stack.push(child)
                                        else result += child.value.getOrElse(0d)
                                    }
                                else
                                    result += r.value.getOrElse(0d)
                            }

                            return Some(result)
                        }
                    }
                    else return None
                }
            }
            case None => None
        }
        None
    }

    def getChildOf(key: String, path: Array[Int]): Option[mutable.HashMap[Int,Node]]={
        _root.get(key) match{
            case Some(node)=>{
                var r = node
                for (i <- 0 until path.length){
                    val c = path(i)
                    if (r.children != null && r.children.contains(c)){
                        r = r.children(i)
                        if (i + 1 == path.length){
                            if (r.children != null) return Some(r.children)
                            else return None
                        }
                    }
                    else return None
                }
                None
            }
            case None => None
        }
    }

    def findAll(key: String, path: Array[Int]): Option[ArrayBuffer[Node]] = {
        _root.get(key) match{
            case Some(node)=>{
                var r = node
                for (i <- 0 until path.length){
                    val c = path(i)
                    if (r.children != null && r.children.contains(c)){
                        r = r.children.get(c).get
                        if (i + 1 == path.length){
                            val result = new ArrayBuffer[Node]()
                            val stack = new util.Stack[Node]()
                            stack.push(r)

                            while (!stack.empty()){
                                r = stack.pop()
                                if (r.children != null)
                                    for (child <- r.children.values) stack.push(child)
                                else
                                    result += r
                            }

                            return Some(result)
                        }
                    }
                    else return None
                }
            }
            case None => None
        }
        None
    }

}


class RadixDictionary[T] {
    val small = 8

    class Node (val key: Char, var value: Option[T], var children: HashMap[Char,Node])

    val _root = new Node(' ', None, null)

    def find(key: String): Option[T] = {

        var r = this._root

        for (i <- 0 until key.length){
            val c = key.charAt(i)
            if (r.children != null && r.children.contains(c)){
                r = r.children.get(c).get
                if (i + 1 == key.length) return r.value
            }
            else return None
        }
        None
    }

    def findAll(start: String): Option[ArrayBuffer[T]] = {
        var r = _root
        for (i <- 0 until start.length){
            val c = start.charAt(i)
            if (r.children != null && r.children.contains(c)){
                r = r.children.get(c).get
                if (i + 1 == start.length){
                    val result = new ArrayBuffer[T]()
                    val stack = new util.Stack[Node]()
                    stack.push(r)

                    while (!stack.empty()){
                        r = stack.pop()
                        if (r.value.isDefined) result += r.value.get

                        if (r.children != null)
                            for (child <- r.children.values) stack.push(child)
                    }

                    return Some(result)
                }
            }
            else return None
        }
        None
    }

    def insert(key: String, value: T){
        var r = _root
        for (i <- 0 until key.length){
            val c = key.charAt(i)

            if (r.children == null) r.children = new HashMap[Char,Node]()
            if (!r.children.contains(c)){
                r.children.put(c, new Node(c, None, null))
            }
            r = r.children.get(c).get
        }
        r.value = Some(value)
    }
}
