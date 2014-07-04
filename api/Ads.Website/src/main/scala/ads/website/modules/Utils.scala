package ads.website.modules

import ads.web.{Invoke, HandlerContainerServlet}
import ads.web.mvc.{Json, Action, SmartDispatcherHandler, BaseHandler}
import javax.servlet.http.HttpServletRequest
import scala.util.parsing.json.JSON
import java.util
import java.util.Properties
import javax.mail._
import javax.mail.internet._
import javax.activation._
import org.im4java.core.{IMOperation, ConvertCmd}
import ads.common.model.Config


/**
 * Created by luannt2 on 12/31/13.
 */

class UtilsService {

  def getPasswordAuthentication(uname:String, psw:String):Authenticator=
  {
    new Authenticator(){
      override protected def getPasswordAuthentication():PasswordAuthentication = {
        return new PasswordAuthentication(uname, psw);
      }}
  }

  def sendMail(to :String, cc : util.ArrayList[String], bcc :util.ArrayList[String],
               subject :String, body: String,
               attachmentPaths : util.ArrayList[String]) : String =  {

    val prop = new Properties()
    prop.load(getClass().getClassLoader().getResourceAsStream("mail.conf"))
    val d_host = prop.get("host")
    val d_email = prop.get("uemail")

    /* for authentication */
//    val d_port = prop.get("port")
//    val d_password = prop.get("password")
//    val d_username = prop.get("uname")

    val props = System.getProperties
    props.put("mail.smtp.host", d_host)

    /* for authentication */
//    props.put("mail.smtp.socketFactory.port", d_port)
//    props.put("mail.smtp.socketFactory.class",
//      "javax.net.ssl.SSLSocketFactory")
//    props.put("mail.smtp.auth", "true")
//    props.put("mail.smtp.port", d_port)

//    val authen = getPasswordAuthentication(d_username + "", d_password + "")
//    val session = Session.getDefaultInstance(props, authen)

    // Get the default Session object.
    val session = Session.getDefaultInstance(props)
    session.setDebug(true)

    val msg = new MimeMessage(session)
    msg.setFrom(new InternetAddress(d_email + ""))
    // add To recipient
    msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to))
    // add CC recipients
    val ccIt = cc.iterator()
    while(ccIt.hasNext()) {
      msg.addRecipient(Message.RecipientType.CC, new InternetAddress(ccIt.next()))
    }
    // add BCC recipients
    val bccIt = bcc.iterator()
    while(bccIt.hasNext()) {
      msg.addRecipient(Message.RecipientType.BCC, new InternetAddress(bccIt.next()))
    }
    msg.setSubject(subject)
//    msg.setText(body)
//    msg.setContent(body,"text/html" ); // send as html

    // Create the message part
    val messageBodyPart:BodyPart = new MimeBodyPart();
    // Fill the message
    messageBodyPart.setContent(body,"text/html" ); // send as html

    // Create a multipart message
    val multipart:Multipart = new MimeMultipart();

    // Set text message part
    multipart.addBodyPart(messageBodyPart);

    // Part two is attachment
    val it = attachmentPaths.iterator()
    while (it.hasNext()) {
      val url = it.next()
      val attachmentPart = new MimeBodyPart()
      val source = new FileDataSource(url)
      attachmentPart.setDataHandler(new DataHandler(source))
      attachmentPart.setFileName(url)
      multipart.addBodyPart(attachmentPart)
    }

    msg.setContent(multipart)

    Transport.send(msg)

    return "Mail sent!!!"
  }
}
class UtilsHandler  extends SmartDispatcherHandler{

  val utilsService = new UtilsService()


  @Invoke(Parameters = "request")
  def test(request:HttpServletRequest):String = {
    return "Something for testing"
  }

  @Invoke(Parameters = "request,params")
  def sendMail(request:HttpServletRequest,params:String):String={

    // parsing email params from jsonString
    val json = JSON.parseFull(params)
    val cc = new util.ArrayList[String]()
    val bcc = new util.ArrayList[String]()
    val attachmentPaths = new util.ArrayList[String]()
    var to = ""
    var subject = ""
    var body = ""

    json match {
      case Some(root: Map[String,Any]) =>
        if (root.contains("cc")) {
          root("cc") match {
            case list: List[String] => val it = list.iterator
              while(it.hasNext) {
                cc.add(it.next())
              }
          }
        }
        if (root.contains("bcc")) {
          root("bcc") match {
            case list: List[String] => val it = list.iterator
              while(it.hasNext) {
                bcc.add(it.next())
              }
          }
        }
        if (root.contains("attachmentPaths")) {
          root("attachmentPaths") match {
            case list: List[String] => val it = list.iterator
              while(it.hasNext) {
                attachmentPaths.add(it.next())
              }
          }
        }
        if (root.contains("to")) {
          root("to") match {
            case v:String => to = v
          }
        }
        if (root.contains("subject")) {
          root("subject") match {
            case v:String => subject = v
          }
        }
        if (root.contains("body")) {
          root("body") match {
            case v:String => body = v
          }
        }
    }

    val res = utilsService.sendMail(to,cc,bcc,subject,body,attachmentPaths)

    return res
  }

}
class UtilsServlet extends HandlerContainerServlet{
  def factory(): BaseHandler = new UtilsHandler()
}

object ImageUtils {
    val cmd = new ConvertCmd
    cmd.setSearchPath(Config.imageMagickPath)

    def resize(path: String, fileName: String, width: Int, height: Int): Boolean={
        var pathDir = path
        if (path.endsWith("/"))
            pathDir = path.substring(0, path.length - 1)
        try {
            val op = new IMOperation
            op.addImage(s"${pathDir}/${fileName}");
            op.resize(width, height);
            val resizeFileName = fileName.replaceAll("\\..*", s"_${width}x${height}$$0")
            op.addImage(s"${pathDir}/${resizeFileName}")
            cmd.run(op)
            return true
        }
        catch {
            case e: Exception => {
                e.printStackTrace
                return false
            }
        }
    }

}

