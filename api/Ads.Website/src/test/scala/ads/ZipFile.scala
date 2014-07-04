package ads

import org.scalatest.FunSuite
import java.io._
import java.util.zip.{ZipFile, ZipOutputStream, ZipEntry, ZipInputStream}
import ads.common.Syntaxs._
import java.util
import scala.concurrent.ops._
import java.util.concurrent.ConcurrentLinkedQueue
import ads.website.modules.serving._
import scala.collection.mutable.ArrayBuffer

class ZipFile2 extends FunSuite {

    test("zip file") {
        val SOURCE_FOLDER = "D:/log/zip"
        val OUTPUT_ZIP_FILE = "D:/log/zip/zip/zip2.zip"
        val BYTE = 2048

        //create output folder if not exist
        val dir: File = new File(OUTPUT_ZIP_FILE)
        dir.getParentFile.mkdirs

        val dest: FileOutputStream = new FileOutputStream(OUTPUT_ZIP_FILE)
        val out: ZipOutputStream = new ZipOutputStream(new BufferedOutputStream(dest))
        try{
            val data = new Array[Byte](BYTE)
            val folder: File = new File(SOURCE_FOLDER)
            val files: Array[String] = folder.list.filter(f => f.startsWith("server"))
            for (file <- files) {
                println("File Added : " + file)
                val fi: FileInputStream = new FileInputStream(SOURCE_FOLDER + File.separator + file)
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
        } finally {
            out.close()
            dest.close()
        }
    }

    test("get zip file content") {
        measureTime{
            val ZIP_FILE = "D:/log/zip/zip/zip.zip"
            val zipFile = new ZipFile(ZIP_FILE);
            val zipEntries = zipFile.entries();
            while (zipEntries.hasMoreElements()) {
                zipEntries.nextElement().getName() |> println
            }
        } |> println

    }

    test("unzip") {
        val INPUT_ZIP_FILE = "D:/log/zip/zip/zip2.zip"
        val OUTPUT_FOLDER = "D:/log/zip/zip/"
        val buffer = new Array[Byte](1024)

        try {
            //create output directory is not exists
            val folder: File = new File(OUTPUT_FOLDER)
            if (!folder.exists) {
                folder.mkdir
            }
            //get the zip file content
            val zis: ZipInputStream = new ZipInputStream(new FileInputStream(INPUT_ZIP_FILE))
            //get the zipped file list entry
            var ze: ZipEntry = zis.getNextEntry
            while (ze != null) {
                val fileName: String = ze.getName
                val newFile: File = new File(OUTPUT_FOLDER + File.separator + fileName)
                System.out.println("file unzip : " + newFile.getAbsoluteFile)
                new File(newFile.getParent).mkdirs
                val fos: FileOutputStream = new FileOutputStream(newFile)
                var len: Int = 0
                while ((({len = zis.read(buffer); len})) > 0) {
                    fos.write(buffer, 0, len)
                }
                fos.close
                ze = zis.getNextEntry
            }
            zis.closeEntry();
            zis.close();
            println("Done");
        } catch {
            case ex: IOException => ex.printStackTrace
        }
    }


    test("unzip to memory") {
        val startTime = System.currentTimeMillis()
        val s = "vo cao tung\nly thuong kiet\nBa thang hai"
        val br = new BufferedReader(new StringReader(s))
        var line = br.readLine()
        while (line != null) {
            println(line)
            line = br.readLine()
        }
        val INPUT_ZIP_FILE = "D:/log/zip/zip/zip2.zip"
        val buffer = new Array[Byte](1024)

        try {
            //get the zip file content
            //val zis: ZipInputStream = new ZipInputStream(new FileInputStream(INPUT_ZIP_FILE))
            //var ze: ZipEntry = zis.getNextEntry
            val zf = new ZipFile(INPUT_ZIP_FILE)
            val entries = zf.entries
            while (entries.hasMoreElements) {
                val ze = entries.nextElement()
                if (ze.getSize > 0) {
                    val br = new BufferedReader(new InputStreamReader(zf.getInputStream(ze)))
                    //println(s"__________________${ze.getName}__________________")
                    var count: Long = 0
                    var line = br.readLine()
                    while (line != null) {
                        //println(s"$count $line")
                        count += 1
                        line = br.readLine()
                    }
                    println(count)
                    br.close
                }
            }
            zf.close
            println(s"Time: ${System.currentTimeMillis() - startTime}ms")
        } catch {
            case ex: IOException => ex.printStackTrace
        }
//        val result = new ByteArrayOutputStream()
//        val out = new ZipOutputStream(result)
//        //val data = new Array[Byte](BYTE)
//        out.putNextEntry(new ZipEntry(""))
//        out.write(s.getBytes())
//        println(result.toByteArray)
    }

    test("zip multi part"){
        def flush(source: String, out: ZipOutputStream){
            val BYTE = 2048
            val data = new Array[Byte](BYTE)
            val sbi = new StringBufferInputStream(source)
            val origin: BufferedInputStream = new BufferedInputStream(sbi, BYTE)
            var count: Int = origin.read(data, 0, BYTE)
            while (count != -1) {
                out.write(data, 0, count)
                count =  origin.read(data, 0, BYTE)
            }
        }

        val SOURCE_FOLDER = "D:/log/zip"
        val OUTPUT_ZIP_FILE = "D:/log/zip/zip/zip2.zip"
        val BLOGSIZE = 20

        //create output folder if not exist
        val dir: File = new File(OUTPUT_ZIP_FILE)
        dir.getParentFile.mkdirs

        val dest: FileOutputStream = new FileOutputStream(OUTPUT_ZIP_FILE)
        val out: ZipOutputStream = new ZipOutputStream(new BufferedOutputStream(dest))
        try{
            val folder: File = new File(SOURCE_FOLDER)
            val files: Array[String] = folder.list.filter(f => f.startsWith("server"))
            for (file <- files) {
                println("File Added : " + file)
                val fileReader = new FileReader(new File(SOURCE_FOLDER + File.separator + file))
                val br = new BufferedReader(fileReader)
                val blog = new StringBuilder
                var line = br.readLine()
                var currentLine = 1
                var old = System.currentTimeMillis()
                while (line != null) {
                    blog.append(line).append("\n")
                    if (currentLine % BLOGSIZE == 0) {
                        println(s"Blog ${currentLine / BLOGSIZE}: ${System.currentTimeMillis() - old}")
                        old = System.currentTimeMillis()
                        out.putNextEntry(new ZipEntry((currentLine / BLOGSIZE).toString))
                        flush(blog.toString, out)
                        println(s"Zip blog ${currentLine / BLOGSIZE}: ${System.currentTimeMillis() - old}")
                        old = System.currentTimeMillis()
                        blog.clear()
                    }
                    line = br.readLine()
                    currentLine += 1
                }
                if (!blog.isEmpty) {
                    out.putNextEntry(new ZipEntry((currentLine / BLOGSIZE + 1).toString))
                    flush(blog.toString, out)
                }
                fileReader.close
                br.close
            }
        } finally {
            out.close()
            dest.close()
        }
    }

    test("lock file") {
        def lockFile(body: => Any) =  {
            body
        }

        spawn {
            while(true) {
                lockFile({
                    println("Begin Read")
                    Thread.sleep(1000)
                    println("End Read")
                })
                Thread.sleep(10)
            }

        }

        spawn {
            while(true) {
                lockFile({
                    println("Begin Write")
                    Thread.sleep(1000)
                    println("End Write")
                })
                Thread.sleep(10)
            }
        }

        Thread.sleep(100*1000)
    }

    test("write 100,000 record") {
        def printLog(s: PrintWriter, record: LogRecord){
            s.println(s"${record.logKind},${record.task},${record.location},${record.time},${record.siteId},${record.zoneGroupIds.foldLeft[String]("")(_ + _ + "|")},${record.zoneId},${record.orderId},${record.campId},${record.itemId},${record.ip},${record.cookie},${record.kind},${record.value},${record.videoId},${record.adType}")
        }
        def openFile(): PrintWriter = {
            val s = "D:/log/zip/flush.txt"
            new File(s).getParentFile.mkdirs
            new PrintWriter(new BufferedWriter(new FileWriter(s, true)))
        }
        val list = new ArrayBuffer[LogRecord]()
        val record = new LogRecord("[N]", 3333, "Ho Chi Minh City", 100, Array(0,1), 100, 100, 100, 100, "100:100:100:100", "0d08ba35-6fc7-4ba0-828e-426ba8247202", "impression", 1400446975317L, 0, 0, 0, 0)
        for (i <- 0 to 100*1000) list += record
        val time = measureTime{
            val s = openFile()
            for (record <- list) {
                printLog(s, record)
            }
            s.close()
        }
        println(s"Time flush log: $time ms")
    }
}
