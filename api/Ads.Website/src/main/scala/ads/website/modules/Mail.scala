package ads.website.modules

import java.util.Properties
import ads.common.model.Config
import javax.mail._
import javax.mail.internet.{MimeMultipart, MimeBodyPart, InternetAddress, MimeMessage}
import scala.concurrent.ops._
import javax.activation.{DataHandler, FileDataSource, DataSource}

trait IMailService {
    def send(sender: String, receiver: String, subject: String, body: String, attachmentPath: String = null)
}

class  MailService extends  IMailService{
    override def send(sender: String, receiver: String, subject: String, body: String, attachmentPath: String = null){
        val props: Properties = new Properties()
        props.put("mail.smtp.host", Config.smtpHost.getValue);
        props.put("mail.smtp.port", Config.smtpPort.getValue)
        val isAuth = Config.isAuth.getValue
        props.put("mail.smtp.auth", isAuth.toString);
        props.put("mail.smtp.starttls.enable", isAuth.toString);

        var auth: Authenticator = null;
        if (isAuth)
            auth = new Authenticator (){
                override def getPasswordAuthentication():PasswordAuthentication = {
                    new PasswordAuthentication(Config.smtpUser.getValue, Config.smtpPassword.getValue)
                }
            }
        val session: Session = Session.getDefaultInstance(props, auth);
        val message = new MimeMessage(session)
        message.setFrom(new InternetAddress(sender))
        message.setRecipients(Message.RecipientType.TO, receiver)
        message.setSubject(subject)
        var messageBodyPart = new MimeBodyPart()
        val multiPart = new MimeMultipart()
        messageBodyPart.setContent(body, "text/html")
        multiPart.addBodyPart(messageBodyPart)

        if (attachmentPath != null){
            messageBodyPart = new MimeBodyPart()
            val data = new FileDataSource(attachmentPath)
            messageBodyPart.setDataHandler(new DataHandler(data))
            messageBodyPart.setFileName(data.getName)
            multiPart.addBodyPart(messageBodyPart)
        }

        message.setContent(multiPart)
        spawn{
            try{
                Transport.send(message);
            } catch {
                case e:MessagingException => throw new RuntimeException(e)
            }
        }
        System.out.println("Send mail done: " + receiver)

    }
}
