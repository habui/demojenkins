package ads.common

import org.scalatest.FunSuite
/**
 * Created by quangnbh on 10/17/13.
 */
class Base64Specs extends FunSuite{
  test("encode data") {
    val chars : Array[Char] = new Array[Char](100);
    val str : String = "scalatesting"
    val buf : Array[Byte] = str.getBytes
    //    buf.foreach(b => {
    //      print(b + ":")
    //    })
    val offset = 2 ;
    val count = 9 ;

    val encoded  = Base64.encode(chars,buf,offset,count)
    //   println()
    //   buf.foreach(b => {
    //     print(b + ":")
    //   })
    println(encoded)
    assert(encoded.length > 0)

    val decoded = Base64.decode(buf,offset,encoded)
    //   println()
    //   buf.foreach(b => {
    //     print(b + ":")
    //   })
    //   println(buf)
    println(decoded)
    assert(decoded == count);
    for(i <- offset to decoded){
      var  dc : Char = buf(i-offset).toChar;
      var  sc : Char = str.charAt(i);
      println(dc + ":" + sc)
      assert(dc == sc)
    }
  }
//  test("unicode data") {
//      val chars : Array[Char] = new Array[Char](100);
//      val str : String = "scala â ă testing"
//      val buf : Array[Byte] = str.getBytes
//      //    buf.foreach(b => {
//      //      print(b + ":")
//      //    })
//      val offset = 1 ;
//      val count = 9 ;
//
//      val encoded  = Base64.encode(chars,buf,offset,count)
//      //   println()
//      //   buf.foreach(b => {
//      //     print(b + ":")
//      //   })
//      println(encoded)
//      assert(encoded.length > 0)
//
//      val decoded = Base64.decode(buf,offset,encoded)
//      //   println()
//      //   buf.foreach(b => {
//      //     print(b + ":")
//      //   })
//      //   println(buf)
//      println(decoded)
//      assert(decoded == count);
//      for(i <- offset to decoded){
//        var  dc : Char = buf(i-offset).toChar;
//        var  sc : Char = str.charAt(i);
//        println(dc + ":" + sc)
//        assert(dc == sc)
//      }
//  }
//  test("one char encode/decode") {
//
//    val encodeTable = Array('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M','N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z','a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm','n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z','0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_')
//
//    val chars : Array[Char] = new Array[Char](100);
//    val str : String = "scala â ă testing"
//    val buf : Array[Byte] = str.getBytes
//    //    buf.foreach(b => {
//    //      print(b + ":")
//    //    })
//    val offset = 0 ;
//    val count = 1 ;
//    for(i <- 0 until encodeTable.length-1 ) {
//      val oneChar: Array[Byte] = Array(encodeTable(i).toByte,'0'.toByte)
//      val encoded  = Base64.encode(chars,oneChar,offset,count)
//      println("encode at " + i + ":" + encoded)
//      val decoded = Base64.decode(oneChar,offset,encoded)
//      println("decode : " + decoded)
//      println(encoded + ":::" + oneChar(decoded-offset))
//      assert(oneChar(decoded) == encoded.toByte)
//    }
//
//  }
}
