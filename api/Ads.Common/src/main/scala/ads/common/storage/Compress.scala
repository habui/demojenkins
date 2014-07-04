package ads.common.storage

import java.io.{BufferedOutputStream, BufferedInputStream, ByteArrayInputStream, FileOutputStream}
import java.util.zip.{ZipEntry, ZipOutputStream}
import ads.common.Syntaxs._

object Compress {
    def compressStringToFile(content: String, path: String, fileName: String) {
        val BYTE = 1024
        var dest: FileOutputStream = null
        var out: ZipOutputStream = null
        try {
            dest = new FileOutputStream(path + "/" + fileName + ".zip")
            val fi = new ByteArrayInputStream(content.getBytes)
            val origin: BufferedInputStream = new BufferedInputStream(fi, BYTE)
            out = new ZipOutputStream(new BufferedOutputStream(dest))
            out.putNextEntry(new ZipEntry(fileName))
            val data = new Array[Byte](BYTE)
            var count: Int = origin.read(data, 0, BYTE)
            while (count != -1) {
                out.write(data, 0, count)
                count =  origin.read(data, 0, BYTE)
            }
            origin.close
            fi.close
        } catch {
            case ex: Exception => {
                println(s"Error while compress file: $fileName!!!")
                printException(ex, System.out)
            }
        } finally {
            if (out != null) out.close()
            if (dest != null) dest.close()
        }
    }
}
