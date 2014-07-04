package ads.common.rpc

import org.apache.thrift.transport.{TServerSocket, TSocket}
import org.apache.thrift.protocol.TBinaryProtocol
import com.dyuproject.protostuff.LinkedBuffer
import java.util.concurrent.{ConcurrentLinkedQueue, ConcurrentHashMap}
import java.nio.ByteBuffer
import java.lang.reflect.{InvocationHandler, Proxy, Method}
import org.apache.thrift.server.TThreadPoolServer
import org.apache.thrift.server.TThreadPoolServer.Args

object Rpc {
    val handler = new ThriftServerHandler()

    def register(name: String, service: Any) = handler.register(name, service)

    def startServer(port: Int) {
        val processor = new IRpcService.Processor(handler)
        val transport = new TServerSocket(port)

        val server = new TThreadPoolServer(new Args(transport).processor(processor))
        server.serve()
    }
}

object ProxyGen {
    def createProxy[T](name: String, callback: (String, String, Array[Object]) => Object)(implicit manifest: Manifest[T]): T = {
        val clazz = manifest.runtimeClass
        return Proxy.newProxyInstance(
            clazz.getClassLoader,
            Array(clazz),
            new InvocationHandler() {
                def invoke(proxy: Object, method: Method, args: Array[Object]): Object = {
                    try
                    {
                        if (method.getName == "toString") "$proxy"
                        else callback(name, method.getName, args)
                    }
                    catch{
                        case x : Throwable => {
                            x.printStackTrace()
                            throw x
                        }
                    }
                }
            }).asInstanceOf[T]
    }
}

class ThriftClient(host: String, port: Int) {
    val transport = new TSocket(host, port)
    transport.open()

    val protocol = new TBinaryProtocol(transport)
    val client = new IRpcService.Client(protocol)
    val buffer = LinkedBuffer.allocate(16 * 1024)
    val output = new ByteBufferBackedOutputStream()

    def process(target: String, method: String, params: Array[Object]): Object = {
        ProtobufUtils.encode(output, buffer, params)
        val result = client.process(target, method, output.getBuffer())
        val arr = ProtobufUtils.decode(new ByteBufferBackedInputStream(result))
        return arr(0)
    }

    def close() = transport.close()
}

class ThriftServerHandler extends IRpcService.Iface {
    val map = new ConcurrentHashMap[String, Any]()
    val methods = new ConcurrentHashMap[String, Method]()
    val bufferQueue = new ConcurrentLinkedQueue[LinkedBuffer]()

    def register(name: String, instance: Any) {
        map.put(name, instance)

        for (m <- instance.getClass.getMethods) {
            methods.put(name + "." + m.getName, m)
        }
    }

    def invoke(service: Any, m: Method, p: Array[Object]): AnyRef = {
        p.length match {
            case 0 => m.invoke(service)
            case 1 => m.invoke(service, p(0))
            case 2 => m.invoke(service, p(0), p(1))
            case 3 => m.invoke(service, p(0), p(1), p(2))
            case 4 => m.invoke(service, p(0), p(1), p(2), p(3))
            case 5 => m.invoke(service, p(0), p(1), p(2), p(3), p(4))
            case 6 => m.invoke(service, p(0), p(1), p(2), p(3), p(4), p(5))
            case 7 => m.invoke(service, p(0), p(1), p(2), p(3), p(4), p(5), p(6))
            case 8 => m.invoke(service, p(0), p(1), p(2), p(3), p(4), p(5), p(6), p(7))
            case _ => throw new NotImplementedError(s"Too many arguments: ${
                service.getClass.getName
            }${m.getName()}")
        }
    }

    def process(target: String, method: String, data: ByteBuffer): ByteBuffer = {

        val output = new ByteBufferBackedOutputStream()
        var buffer = bufferQueue.poll()
        if (buffer == null) buffer = LinkedBuffer.allocate(1024)

        val input = new ByteBufferBackedInputStream(data)
        val params = ProtobufUtils.decode(input)

        val service = map.get(target)
        val m = methods.get(target + "." + method)

        var result: AnyRef = null

        if (m != null) {
            result = invoke(service, m, params)
        }
        else println(s"Command $service.$method not found!")

        ProtobufUtils.encode(output, buffer, Array(result))

        bufferQueue.add(buffer)

        return output.getBuffer()
    }
}