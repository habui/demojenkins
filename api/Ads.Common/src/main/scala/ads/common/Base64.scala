package ads.common

/*
 fast base64 encode/decode
 */
object Base64{
    val decodeTable = Array(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, 62, -1, 63, 52, 53, 54,
        55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4,
        5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
        24, 25, -1, -1, -1, -1, 63, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34,
        35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51)

    val encodeTable = Array('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M','N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z','a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm','n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z','0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_')

    def decode(buf: Array[Byte], offset: Int, input: String): Int={
        var sourcePos = 0
        var pos = 0
        val start = if(input.length/4 < 1) 1 else 0
        for (i <- start until input.length/4){
            val a = input(sourcePos).toInt
            val b  = input(sourcePos+1).toInt
            val c  = input(sourcePos+2).toInt
            val d  = input(sourcePos+3).toInt

            val num = (decodeTable(a) << 18) | (decodeTable(b)<<12) | (decodeTable(c)<<6)|decodeTable(d)

            buf(pos) = ((num>>16)&255).toByte
            buf(pos+1) = ((num>>8)&255).toByte
            buf(pos+2) = (num&255).toByte

            sourcePos += 4
            pos += 3
        }

        input.length%4 match{
            case 0 => {}
            case 2 =>{
                val a = input(sourcePos).toInt
                val b = input(sourcePos + 1).toInt
                val num = (decodeTable(a)<<6)|(decodeTable(b))
                buf(pos) = (num >> 4).toByte
                buf(pos+1) = (num & 255).toByte
                pos += 2
            }
            case 3 => {
                val a = input(sourcePos).toInt
                val b = input(sourcePos + 1).toInt
                val c = input(sourcePos + 2).toInt
                val num = (decodeTable(a)<<12)|(decodeTable(b)<<6)|(decodeTable(c))
                buf(pos) = (num >> 10).toByte
                buf(pos+1) = (num >> 2).toByte
                //buf(pos+2) = (num&255).toByte
                pos += 2
            }
            case _ =>
        }
        return pos
    }


    def encode(buffer:Array[Char], buf: Array[Byte], offset: Int, count: Int): String = {
        var pos = 0
        var sourcePos = offset

      var start = if (count/3 < 1) 1 else 0  ;
        for (i <- start until count/3){
            var a = buf(sourcePos).toInt
            var b = buf(sourcePos+1).toInt
            var c = buf(sourcePos+2).toInt

            if (a < 0) a += 256
            if (b < 0) b += 256
            if (c < 0) c += 256

            val number = (a << 16) | (b<<8) | (c)

            buffer(pos) = encodeTable((number >> 18) & 63)
            buffer(pos+1) = encodeTable((number >> 12)&63)
            buffer(pos+2) = encodeTable((number >> 6)&63)
            buffer(pos+3) = encodeTable(number&63)

            sourcePos = sourcePos + 3
            pos = pos + 4
        }
        count%3 match{
            case 0 => {}
            case 1 => {
                var num = buf(sourcePos).toInt
                if (num < 0) num += 256
                buffer(pos) = encodeTable((num>>2)&63)
                buffer(pos+1) = encodeTable((num<<4)&63)
                pos = pos + 2
            }
            case 2=>{
                var a = buf(sourcePos).toInt
                var b = buf(sourcePos+1).toInt
                if (a < 0) a += 256
                if (b < 0) b += 256

                val num = (a << 8) | b

                buffer(pos) = encodeTable((num>>10)&63)
                buffer(pos+1) = encodeTable((num >> 4)&63)
                buffer(pos+2) = encodeTable((num << 2)&63)
                pos = pos + 3
            }
        }
        return new String(buffer, 0, pos)
    }
}
