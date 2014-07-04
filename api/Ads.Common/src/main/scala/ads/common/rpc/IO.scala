package ads.common.rpc

import java.nio.ByteBuffer
import java.io.{OutputStream, InputStream}
import com.dyuproject.protostuff.{LinkedBuffer, ProtobufIOUtil}
import com.dyuproject.protostuff.runtime.RuntimeSchema


class Params {
    var data: Array[Object] = null
}

object ProtobufUtils {
    val schema = RuntimeSchema.getSchema(classOf[Params])

    def decode(input: ByteBufferBackedInputStream): Array[Object] = {
        val param = new Params()
        ProtobufIOUtil.mergeFrom(input, param, schema)
        return param.data
    }

    def encode(output: ByteBufferBackedOutputStream, buffer: LinkedBuffer, args: Array[Object]) {
        output.clear()
        buffer.clear()
        val param = new Params() {
            data = args
        }
        ProtobufIOUtil.writeTo(output, param, schema, buffer)
    }
}

class BytesInputStream(var buf: Array[Byte], val offset: Int = 0, val max: Int) extends InputStream{
    var count = 0

    override def read(b: Array[Byte], off: Int, len: Int): Int = {
        val min = Math.min(len, max - count)
        if (min > 0){
            System.arraycopy(buf, offset + count, b, off, min)
            count += min
        }
        return min
    }

    def read(): Int = {
        if (count >= max) return -1
        count += 1
        buf(offset + count - 1).toInt
    }
}

class BytesOutputStream(val buf: Array[Byte], val offset: Int = 0) extends OutputStream{

    var count = 0

    def write(b: Int) {
        count += 1
        buf(offset + count - 1)=b.toByte
    }

    override def write(b: Array[Byte], off: Int, len: Int) {
        System.arraycopy(b, off, buf, offset + count, len)
        count += len
    }

    def reset() = count=0
}

class ByteBufferBackedInputStream(var buffer: ByteBuffer) extends InputStream {

    override def read(): Int = {
        if (!buffer.hasRemaining) return -1
        buffer.get() & 0xff
    }

    override def read(b: Array[Byte], off: Int, len: Int): Int = {
        if (!buffer.hasRemaining) return -1

        val l = math.min(len, buffer.remaining())
        buffer.get(b, off, l)
        return l
    }

    def clear() = buffer.clear()
}

object ByteBufferUtils {
    def expand(buffer: ByteBuffer, capacity: Int): ByteBuffer = {
        val newBuffer = ByteBuffer.allocate(capacity)
        val position = buffer.position()
        buffer.rewind()
        newBuffer.put(buffer)
        newBuffer.position(position)
        return newBuffer
    }
}

class ByteBufferBackedOutputStream() extends OutputStream {
    var buffer = ByteBuffer.allocate(1 * 1024)

    def write(b: Int) {
        expandIfNeeded(1)
        buffer.put(b.toByte)
    }

    override def write(b: Array[Byte], off: Int, len: Int) {
        expandIfNeeded(len)
        buffer.put(b, off, len)
    }

    def expandIfNeeded(len: Int) {
        val need = buffer.position() + len
        if (need < buffer.capacity()) return

        var capacity = buffer.capacity()
        while (capacity < need) capacity *= 2

        buffer = ByteBufferUtils.expand(buffer, capacity)
    }

    def length = buffer.position()

    def getBuffer(): ByteBuffer = {
        buffer.flip()
        buffer
    }

    def clear() = buffer.clear()
}
