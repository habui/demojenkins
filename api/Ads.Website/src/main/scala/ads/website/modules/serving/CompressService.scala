package ads.website.modules.serving

import java.util.Date
import ads.common.model.Config
import java.io._
import java.util.zip.{ZipInputStream, ZipFile, ZipOutputStream, ZipEntry}
import scala.collection.mutable
import scala.concurrent.ops._

trait ICompressService {
    def getContentAndDecompress(from: Long, to: Long)
    def compress(from: Long, to: Long): Long
    def getCurrentCompressTime(from: Long): Long
    def fileExists(time: Date): Boolean
    def getFileSize(time: Date): Long
    def deleteLog(from: Long, to: Long)
    var isCurrentCompress: Boolean
    def getPath(time: Date): String
    //listFile is list all content in zip file with value is status of file (true: decompress complete)
    var listFile: mutable.HashMap[String,Boolean]
}

class CompressService(path: String) extends ICompressService{
    val BYTE = 1024
    var isCurrentCompress = false
    var listFile: mutable.HashMap[String,Boolean] = null

    def getPath(time: Date): String = Config.compressFolder.getValue + f"/${time.getYear + 1900}/${time.getMonth}%02d/${time.getDate}%02d/" + LogFile.getFileName(time) + ".zip"

    def zipFileExists(time: Long): Boolean = {
        val file = new File(getPath(new Date(time)))
        file.exists()
    }
    //get all content of all zip file and decompress
    def getContentAndDecompress(from: Long, to: Long) {
        listFile = new mutable.HashMap[String,Boolean]()
        var current = from
        while (current < to) {
            if (zipFileExists(current)) {
                val zipFile = new ZipFile(getPath(new Date(current)))
                val zipEntries = zipFile.entries
                while (zipEntries.hasMoreElements)
                    listFile += ((zipEntries.nextElement.getName, false))
            }
            current += 3600*1000
        }
        spawn{
            decompress(from, to)
        }
    }

    def decompress(from: Long, to: Long) {
        var current = from
        current = from
        val buffer = new Array[Byte](BYTE)
        while (current < to) {
            val currentDate = new Date(current)
            val originFile = new File(LogFile.getPathFile(currentDate))
            //neu co file zip va (file goc khong ton tai hoac neu ton tai ma size file goc khong bang noi dung file nen) thi unzip
            if (zipFileExists(current) && (!originFile.exists || originFile.length != getFileSize(currentDate))) {
                var fileName: String = ""
                var newFile: File = null
                try {
                    val zis: ZipInputStream = new ZipInputStream(new FileInputStream(getPath(new Date(current))))
                    //get the zipped file list entry
                    var ze: ZipEntry = zis.getNextEntry
                    while (ze != null) {
                        fileName = ze.getName
                        newFile = new File(LogFile.getPathFile(currentDate))
                        newFile.getParentFile.mkdirs
                        System.out.println("Unzip : " + newFile.getAbsoluteFile)
                        val fos: FileOutputStream = new FileOutputStream(newFile)
                        var len: Int = 0
                        while (({len = zis.read(buffer); len}) > 0) {
                            fos.write(buffer, 0, len)
                        }
                        fos.close
                        listFile(fileName) = true
                        ze = zis.getNextEntry
                    }
                    zis.closeEntry
                    zis.close
                } catch {
                    case ex: IOException => {
                        println(s"Error while unzip file: $fileName")
                        if (newFile != null && newFile.delete())
                            println(s"Delete file: $fileName")
                        ex.printStackTrace
                    }
                }
            }
            current += 3600*1000
        }
    }

    def compress(from: Long, to: Long): Long = {
        isCurrentCompress = true
        var current = from
        var isComplete = true
        while (current < to && isComplete) {
            var dest: FileOutputStream = null
            var out: ZipOutputStream = null
            val time = new Date(current)
            try{
                val logFile = new File(LogFile.getPathFile(time))
                if (logFile.exists && (!zipFileExists(current) || logFile.length > getFileSize(time))) {
                    val file = LogFile.getFileName(time)
                    val pathCompressFile = getPath(time)
                    new File(pathCompressFile).getParentFile.mkdirs
                    dest = new FileOutputStream(pathCompressFile)
                    out = new ZipOutputStream(new BufferedOutputStream(dest))
                    val data = new Array[Byte](BYTE)
                    println("Compress file: " + file)
                    val fi: FileInputStream = new FileInputStream(LogFile.getPathFile(time))
                    val origin: BufferedInputStream = new BufferedInputStream(fi, BYTE)
                    out.putNextEntry(new ZipEntry(file))
                    var count: Int = origin.read(data, 0, BYTE)
                    while (count != -1) {
                        out.write(data, 0, count)
                        count =  origin.read(data, 0, BYTE)
                    }
                    origin.close
                    fi.close
                }
                current += 3600*1000
            } catch {
                case ex: Exception => {
                    isComplete = false
                    println("Error while compress file!!!")
                    println(ex.getStackTraceString)
                    println("Delete file: " + getPath(time))
                    new File(getPath(time)).delete
                    current -= 3600*1000
                }
            } finally {
                if (out != null) out.close()
                if (dest != null) dest.close()
            }
        }
        isCurrentCompress = false
        current
    }

    def getCurrentCompressTime(from: Long): Long = {
        var current = from
        val now = System.currentTimeMillis
        var next = from
        while (next < now) {
            val nextDate = new Date(next)
            val logFile = new File(LogFile.getPathFile(nextDate))
            if (zipFileExists(next) && logFile.length == getFileSize(nextDate))
                current = next
            else if (logFile.exists)
                return next - 3600*1000
            next += 3600*1000
        }
        current
    }

    def fileExists(time: Date): Boolean = {
        if (zipFileExists(time.getTime)) {
            val zipFile = new ZipFile(getPath(time))
            val zipEntries = zipFile.entries
            while (zipEntries.hasMoreElements) {
                if (LogFile.getFileName(time).equals(zipEntries.nextElement.getName))
                    return true
            }
        }
        false
    }

    def getFileSize(time: Date): Long = {
        if (zipFileExists(time.getTime)) {
            try {
                val zipFile = new ZipFile(getPath(time))
                val zipEntries = zipFile.entries
                while (zipEntries.hasMoreElements) {
                    val element = zipEntries.nextElement
                    if (LogFile.getFileName(time).equals(element.getName))
                        return element.getSize
                }
            } catch { //if error while opening zip file
                case _ => return 0
            }

        }
        return 0
    }

    def deleteLog(from: Long, to: Long): Unit = {
        var current = from
        var file: File = null
        while (current < to) {
            val currentDate = new Date(current)
            file = new File(LogFile.getPathFile(currentDate))
            if (file.exists()) {
                if (zipFileExists(current) && file.length == getFileSize(currentDate)) {
                    try {
                        println(s"Delete file: ${file.getName}")
                        file.delete()
                    } catch {
                        case ex: Exception => println(s"Error while delete ${file.getPath}\n${ex.getStackTraceString}")
                    }
                } else {
                    while (isCurrentCompress) {} //waiting if server is compressing
                    compress(current, current + 1)
                    current -= 3600*1000
                }
            }
            current += 3600*1000
        }
    }
}
