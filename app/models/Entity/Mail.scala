package models.Entity

import java.io.InputStream
import javax.mail.internet.InternetAddress

import akka.io.IO.Extension
import org.joda.time.DateTime

/**
  * Created by Shota on 2016/06/18.
  */
case class Mail(
                 from: InternetAddress
                 , tos         : Seq[InternetAddress]
                 , ccs         : Seq[InternetAddress]
                 , bccs        : Seq[InternetAddress]
                 , subject     : String
                 , sentDate    : DateTime
                 , plainBody   : Option[String]
                 , htmlBody    : Option[String]
                 , attachments : Seq[MailAttachment]
                 , error       : Option[String])

case class MailAttachment(cid: String, attach: Array[Byte], extension : String)
