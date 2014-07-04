package ads.website.handler

import ads.web.mvc._
import ads.common.Syntaxs._
import ads.web.{Invoke, HandlerContainerServlet}
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import java.io.File
import scala.collection.JavaConversions._
import java.util.{GregorianCalendar, UUID, Calendar}
import ads.common.services.serving.Configs
import ads.common.model.Config
import eu.medsea.mimeutil.MimeUtil
import java.util
import java.nio.file.{Paths, Path, Files}
import scala.sys.process._
import com.xuggle.xuggler.IContainer
import ads.common.FtpUtils

class MiscHandler extends SmartDispatcherHandler {
    MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");

    @Invoke()
    def getCategories = Json(Array("Music", "Entertainment", "Health", "Sport", "Beauty and Spa", "News"))

    @Invoke()
    def getCountries = Json(Array(
        "Vietnam","United Kingdom", "United State", "Korea", "Japan", "Turkey", "Singapore",
        "Phillipin","Russia", "Germany", "Uzebekistan","Ethiopia"
    ))

    @Invoke(Parameters = "request")
    def upload(request: HttpServletRequest): Action = {

        val isWindow = System.getProperty("os.name").toLowerCase.indexOf("win") >= 0

        if (ServletFileUpload.isMultipartContent(request)) {
            val tempPath = isWindow ? "C:\\temp" | "/usr/local/nginx/html/tmp"
            val uploadPath = isWindow ? tempPath | Config.uploadDir.getValue
            val file = new File(tempPath)
            val factory = new DiskFileItemFactory(8 * 1024 * 1024, file)

            val upload = new ServletFileUpload(factory)
            val items = upload.parseRequest(request)

            for (item <- items) {
                if (!item.isFormField) {
                    val fileName: String = UUID.randomUUID().toString

                    val tmpFile = new File(uploadPath + "/" + item.getName)
                    item.write(tmpFile)

                    val mimeType:util.Collection[_] = MimeUtil.getMimeTypes(tmpFile)
//                    val mimeType = Files.probeContentType(Paths.get(tmpFile.getAbsolutePath))
                    val name = mimeType.toString match {
                        case "image/jpeg" | "image/pjpeg" => fileName + ".jpg"
                        case "image/png" => fileName + ".png"
                        case "image/gif" => fileName + ".gif"
                        case "application/x-shockwave-flash" => fileName + ".swf"
                        case "video/x-flv" => fileName + ".flv"
                        case "video/mp4" => fileName + ".mp4"
                        case "video/x-ms-wmv" => fileName + ".wmv"
                        case "application/msword" => fileName + ".doc"
                        case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" => fileName + ".docx"
                        case "application/pdf" => fileName + ".pdf"
                        case "text/plain" => fileName + ".txt"

                        case _ => null
                    }

                    var uploadDomain = Config.uploadDomain.getValue
                    if (uploadDomain.endsWith("/"))
                        uploadDomain = uploadDomain.substring(0, uploadDomain.length - 1)

                    if(name != null) {
                        //item.write(new File(uploadPath + "/" + name))
                        tmpFile.renameTo(new File(uploadPath + "/" + name));
                        for (f <- items) f.delete()

                        if (mimeType.toString.compareToIgnoreCase("video/x-flv") == 0 ||
                          mimeType.toString.compareToIgnoreCase("video/mp4") == 0 ||
                          mimeType.toString.compareToIgnoreCase("video/x-ms-wmv") == 0) { // get video duration
                            val duration: Int = Math.ceil(getVideoDuration(uploadPath + "/" + name).toDouble/1000000).toInt // round to second

                            val date = new GregorianCalendar
                            val location = date.get(Calendar.YEAR) + "/" + (date.get(Calendar.MONTH) + 1).toString
                            var uploadResult = false

                            if (Config.streamAdtimaServerInUse) {
                                if (Config.streamAdtimaServerByProxy)
                                    uploadResult = FtpUtils.uploadByProxy(Config.streamAdtimaServerProxyHost, Config.streamAdtimaServerProxyPort, Config.streamAdtimaServerHost, Config.streamAdtimaServerPort, Config.streamAdtimaServerUsername, Config.streamAdtimaServerPassword, new File(uploadPath + "/" + name), location)
                                else
                                    uploadResult = FtpUtils.upload(Config.streamAdtimaServerHost, Config.streamAdtimaServerPort, Config.streamAdtimaServerUsername, Config.streamAdtimaServerPassword, new File(uploadPath + "/" + name), location)
                            }

                            if (uploadResult) {
                                val urlPath = s"http://${Config.streamAdtimaServerDomain}/${location}/${name}"
                                return Text("{\"result\":\"success\", \"msg\": \"" + urlPath + "\", \"type\":\"" + mimeType.toString + "\", \"duration\":" + duration + "}")
                            }
                            return Text("{\"result\":\"success\", \"msg\": \"" + uploadDomain + "/" + name + "\", \"type\":\"" + mimeType.toString + "\", \"duration\":" + duration + "}")
                        }

                        return Text("{\"result\":\"success\", \"msg\": \"" + uploadDomain + "/" + name + "\", \"type\":\"" + mimeType.toString + "\"}")
                    }
                    // remove tmpFile
                    tmpFile.delete()
                }
            }
        }

        return Text("{\"result\":\"error\", \"msg\": \"Item type is not supported!\"}")
    }

    def getVideoDuration(videoFile: String): Long = {
      // Create a Xuggler container object
      var duration: Long = 0;
      val container:IContainer = IContainer.make();

      // Open up the container
      if (container.open(videoFile, IContainer.Type.READ, null) < 0) {
        return 0;
      }
      duration = container.getDuration();
      container.close();
      return duration;

    }
}

class MiscServlet extends HandlerContainerServlet {
    def factory(): BaseHandler = new MiscHandler()
}

class CrossDomainHandler extends BaseHandler {
    override def process(request: HttpServletRequest, response: HttpServletResponse) {
        val res :String = "<?xml version=\"1.0\"?><cross-domain-policy><allow-access-from domain=\"*\"/></cross-domain-policy>"
        response.getWriter.println(res)
    }
}

class CrossDomainServlet extends HandlerContainerServlet {
    def factory(): BaseHandler = new CrossDomainHandler()
}
