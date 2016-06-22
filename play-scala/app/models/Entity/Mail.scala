package models.Entity

import java.io.InputStream
import javax.mail.internet.InternetAddress

import org.joda.time.DateTime

/**
  * Created by Shota on 2016/06/18.
  */
case class Mail(
                 from: InternetAddress
                 , tos: List[InternetAddress]
                 , ccs:List[InternetAddress]
                 , bccs:List[InternetAddress]
                 , subject: String
                 , plainBody: Option[String]
                 , htmlBody: Option[String]
                 , attachments: List[MailAttachment]
                 , sentDate: DateTime)

case class MailAttachment(cid: String, attachStream: InputStream, extention: String)
