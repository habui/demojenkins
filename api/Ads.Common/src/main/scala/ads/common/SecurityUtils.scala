package ads.common

import java.util.concurrent.{ConcurrentLinkedQueue, ThreadLocalRandom}
import ads.common.Syntaxs._
import scala.util.control.NonFatal
import ads.common.rpc.BytesOutputStream

object SecurityUtils {
    val window = 15 * 60 // seconds

    def rand(r: Int) = ThreadLocalRandom.current().nextInt(r)

    private val primaryKey = 1442968193
    private val subKey = 655360001
    private val mask = 304250263527209L
    private val padd = subKey >> 8
    private val maxSize = 1024

    def encode(v: Int): String = {
        val s = (v.toLong + padd) * primaryKey
        val x = s / subKey
        val y = s - x * subKey.toInt
        val f = (y << 32) | x
        (f ^ mask).toString
    }

    def decode(v: String): Int = {
        val f = v.toLong ^ mask
        val x = (f >> 32) & 0xffffffff
        val y = f & 0xffffffffL
        val s = y * subKey + x
        (s / primaryKey - padd).toInt
    }

    def currentTime(): Int = (System.currentTimeMillis() / 1000).toInt

    def generateTimeToken(): String = {
        val seed = rand(9000) + 1000
        seed + "" + encode(currentTime() + seed)
    }

    def decodeTimeToken(token: String): Try[Int] = {
        Try {
            val seed = token.substring(0, 4).toInt
            val ticks = decode(token.substring(4)) - seed
            ticks
        }
    }

    val queue = new ConcurrentLinkedQueue[Array[Byte]]()

    def getBuf() = queue.poll() ?? new Array[Byte](maxSize)

    def releaseBuf(buf: Array[Byte]) = queue.add(buf)


    //    private val algorithm = "DES"
    //    private val factory = SecretKeyFactory.getInstance(algorithm)
    //    private val spec = new DESKeySpec("304250263527209655360001".getBytes())
    //    private val key = factory.generateSecret(spec)
    //
    //    def encodeData(input: Array[Byte], offset: Int, count: Int, seed: Byte, output: Array[Byte], outputOffset: Int): Int = {
    //        val cipher = Cipher.getInstance(algorithm)
    //        cipher.init(Cipher.ENCRYPT_MODE, key)
    //        cipher.doFinal(input, offset, count, output, outputOffset)
    //    }
    //
    //    def decodeData(input: Array[Byte], offset: Int, count: Int, output: Array[Byte], outputOffset: Int): Int = {
    //        val cipher = Cipher.getInstance(algorithm)
    //        cipher.init(Cipher.DECRYPT_MODE, key)
    //        cipher.doFinal(input, offset, count, output, outputOffset)
    //    }

    def encodeData(input: Array[Byte], offset: Int, count: Int, seed: Byte, output: Array[Byte], outputOffset: Int): Int = {
        var write = 0
        val key = ((seed.toLong + padd) * primaryKey) ^ mask
        var checkSum:Byte = 13

        output(outputOffset + write) = seed
        write += 1

        for (i <- 0 until count) {
            val x = (key << ((i % 7) * 8)).toByte ^ input(i) ^ checkSum
            output(outputOffset + write) = x.toByte
            write += 1
            checkSum = (checkSum*13 + input(i) + x).toByte
        }
        output(write) = checkSum

        write + 1
    }

    def decodeData(input: Array[Byte], offset: Int, count: Int, output: Array[Byte], outputOffset: Int): Int = {
        val seed = input(offset)
        val key = ((seed.toLong + padd) * primaryKey) ^ mask
        var checkSum = 13
        val finalSum = input(offset + count - 1)

        for (i <- 1 until count - 1) {
            val x = (key << (((i-1) % 7) * 8)).toByte ^ input(offset + i) ^ checkSum
            checkSum = (checkSum * 13 + x + input(offset + i)).toByte
            output(outputOffset + i - 1) = x.toByte
        }

        if (checkSum != finalSum) return 0

        count - 1
    }

    def decodeData(input: String): Try[Array[String]] = {
        if (input.length >= maxSize) return fail("too long!")

        //val buf = Base64.decodeBase64(input)
        val buf = getBuf()

        val bufLength = Base64.decode(buf, 0, input)

        val output = getBuf()
        var charBuf = charQueue.poll()
        if (charBuf == null) charBuf = new Array[Char](1024)
        try {
            decodeData(buf, 0, bufLength, output, 0)

            val length = output(0)
            var pos = 1
            val result = new Array[String](length)

            for (i <- 0 until length){
                var l = output(pos).toInt
                if (l < 0) l += 256

                for (j <- 0 until l) charBuf(j) = output(pos + 1 + j).toChar

                result(i) = new String(charBuf, 0, l)
                pos += l + 1
            }

            succeed(result)
        }
        catch {
            case NonFatal(e) => fail(e)
        }

        finally {
            releaseBuf(output)
            releaseBuf(buf)
            charQueue.add(charBuf)
            //releaseBuf(tmp)
        }
    }

    class FastWriter(val output: BytesOutputStream){
        def buf = output.buf

        def write(s: Int){
            output.write(s)
        }

        def write(s: String){
            for (i <- 0 until s.length){
                output.write(s.charAt(i).toInt)
            }
        }

        def reset()= output.reset()
    }

    val charQueue = new ConcurrentLinkedQueue[Array[Char]]()

    def encodeData(data: Array[String]): String = {
        val seed = rand(256).toByte

        val output = new BytesOutputStream(getBuf())
        var charBuf = charQueue.poll()
        if (charBuf == null) charBuf = new Array[Char](1024)
        val writer = new FastWriter(output)
        val result = getBuf()
        try {
            writer.write(data.length)
            for (s <- data) {
                writer.write(s.length)
                writer.write(s)
            }
            val output = writer.output
            val count = encodeData(output.buf, 0, output.count, seed, result, 0)

            Base64.encode(charBuf, result, 0, count)
        }
        finally {
            releaseBuf(output.buf)
            charQueue.add(charBuf)
            releaseBuf(result)
        }
    }

    def encodeParams(map: Map[String,String]): String= encodeData(map.flatMap(e=>List(e._1, e._2)).toArray)

    def md5Hash(text: String) : String = java.security.MessageDigest.getInstance("MD5").digest(text.getBytes()).map(0xFF & _).map { "%02x".format(_) }.foldLeft(""){_ + _}

    def sha256Hash(text: String) : String = java.security.MessageDigest.getInstance("SHA-256").digest(text.getBytes()).map(0xFF & _).map(_ + 0x100).map(Integer.toString(_, 16).substring(1)).foldLeft(""){_ + _}
}
