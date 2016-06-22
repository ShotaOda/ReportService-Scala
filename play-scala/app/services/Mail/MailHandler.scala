package services.Mail

import java.io.UnsupportedEncodingException
import javax.mail._
import javax.mail.internet.InternetAddress
import javax.mail.search.{AndTerm, ComparisonTerm, SearchTerm, SentDateTerm}

import com.typesafe.config.{Config, ConfigFactory}
import models.Entity.{Mail, MailAttachment}
import org.joda.time.DateTime

/**
  * Created by shota.oda on 2016/06/16.
  */

object MailHandler {

  def testMailStream() = {
    implicit val handler: Mail => Unit = { m =>

    }
    import org.joda.time.Duration
    val origindate = new DateTime(2016, 5, 11, 0, 0)
    val end = new DateTime().dayOfMonth().roundFloorCopy()
    val d = new Duration(origindate, end)
    (0 to d.getStandardDays.toInt).foreach { i =>
      println(origindate.plusDays(i))
      mailStream(origindate.plusDays(i))
    }
  }

  case class SimpleMultiPart(
                               plainBody   : Option[String]
                              ,htmlBody    : Option[String]
                              ,error       : Option[String]
                              ,attachments : Seq[MailAttachment]
                            ) {

    def +(part: SimpleMultiPart): SimpleMultiPart = {
      SimpleMultiPart.apply(
        plainBody = this.plainBody.isEmpty && part.plainBody.isEmpty match {
          case true => None
          case false => Some(Seq(this.plainBody, part.plainBody).flatMap(s => s) mkString "\n")
        }
        , htmlBody = this.htmlBody.isEmpty && part.htmlBody.isEmpty match {
          case true => None
          case false => Some(Seq(this.htmlBody, part.htmlBody).flatMap(s => s) mkString "\n")
        }
        , error = this.error.isEmpty && part.error.isEmpty match {
          case true => None
          case false => Some(Seq(this.error, part.error).flatMap(s => s) mkString "\n")
        }
        , attachments = Seq(attachments, part.attachments).flatMap(s => s).toList
      )
    }
  }

  def mailStream(dateTime: DateTime)(implicit handler: (Mail) => Unit) = {

    /* =========================================================
        じゅんび
    ========================================================= */
    val fromTerm = new SentDateTerm(ComparisonTerm.GE, dateTime.toDate)
    val toTerm   = new SentDateTerm(ComparisonTerm.LT, dateTime.plusDays(1).toDate)
    val mc = MailConfig.default()
    mc.open()
    println(s"=== RESULT at $dateTime ==============")
    println(mc.search(new AndTerm(Array(fromTerm, toTerm))).length)

    /* =========================================================
         たんさく
    ========================================================= */
    mc.search(new AndTerm(Array(fromTerm, toTerm))).foreach { mes =>

      /* =========================================================
         へっだー
      ========================================================= */
      val from = mes.getFrom()(0).asInstanceOf[InternetAddress]
      var tos: List[InternetAddress] = Nil
      if (mes.getRecipients(Message.RecipientType.TO) != null) {
        tos = mes.getRecipients(Message.RecipientType.TO).map {
          _.asInstanceOf[InternetAddress]
        }.toList
      }
      var ccs: List[InternetAddress] = Nil
      if (mes.getRecipients(Message.RecipientType.CC) != null) {
        ccs = mes.getRecipients(Message.RecipientType.CC).map {
          _.asInstanceOf[InternetAddress]
        }.toList
      }
      var bccs: List[InternetAddress] = Nil
      if (mes.getRecipients(Message.RecipientType.BCC) != null) {
        bccs = mes.getRecipients(Message.RecipientType.BCC).map {
          _.asInstanceOf[InternetAddress]
        }.toList
      }
      val sentDateTime = new DateTime(mes.getSentDate)


      val subject = mes.getSubject
      var plainbody: Option[String] = None
      var htmlbody: Option[String] = None
      var errorMessage: Option[String] = None
      var attachments: List[MailAttachment] = Nil
      var multipart: Option[SimpleMultiPart] = None

      /* =========================================================
         ほんぶん
      ========================================================= */
      val CONTENTTYPE = mes.getContentType.toUpperCase
      if (CONTENTTYPE startsWith "TEXT/PLAIN") {
        try{
          plainbody = Some(mes.getContent.toString)
        } catch {
          case e: UnsupportedEncodingException =>
            errorMessage = Some(Seq(errorMessage, Some(s"対応していないフォーマットのため読み込めませんでした\nフォーマット: ${getFormat(e)}")).flatMap { x => x }.mkString("\n"))
          case ee: Throwable =>
            errorMessage = Some(Seq(errorMessage, Some(s"読込中に例外が発生しました。\n$ee")).flatMap { x => x }.mkString("\n"))
        }
      } else if (CONTENTTYPE startsWith "TEXT/HTML") {
          htmlbody = Some(mes.getContent.toString)

      } else if (CONTENTTYPE startsWith "MULTIPART") {
        val parsed = parseMultiPart(mes.getContent.asInstanceOf[Multipart])
        multipart match {
          case Some(p) => multipart = Some(p + parsed)
          case None => multipart = Some(parsed)
        }
      }
      if (errorMessage.isDefined) {
        plainbody = Some(Seq(plainbody, errorMessage).flatMap { v => v }.mkString("\n"))
      }
      print("・") // かくにん
      multipart.foreach { v =>
        if (v.plainBody.isEmpty && v.htmlBody.isEmpty) {
          println(from)
          println("☆起こってはいけない")
        } else if (v.attachments != Nil && v.htmlBody.isEmpty) {
          println(from)
          println(v.attachments)
        } else if (v.error.isDefined) {
          println(s"error message ${from.getAddress}")
          println(v.error.get)
        }
      }

      handler(Mail(from, tos, ccs, bccs, subject, plainbody, htmlbody, attachments, sentDateTime))
    }
//    folder.close(true)
//    store.close()
    mc.close()
  }

  private def getExtension(contentType: String): String = {
    contentType.substring(contentType.indexOf('/') + 1, contentType.indexOf(';')).toLowerCase()
  }

  private def getFormat(e: UnsupportedEncodingException): String = {
    val message = e.getMessage
    message.substring(message.indexOf(':') + 1, message.length)
  }

  private def parseMultiPart(multipart: Multipart): SimpleMultiPart = {
    var plain: Option[String] = None
    var html: Option[String] = None
    var error: Option[String] = None
    var attachments: Seq[MailAttachment] = Nil
    var childMultipart: Option[SimpleMultiPart] = None

    (0 until multipart.getCount).map(i => multipart.getBodyPart(i)).foreach { part =>
      val partType = part.getContentType.toUpperCase
      if (partType startsWith "TEXT/PLAIN") {
        try{
          plain = Some(part.getContent.toString)
        } catch {
          case e: UnsupportedEncodingException =>
            error = Some(Seq(error, Some(s"対応していないフォーマットのため読み込めませんでした\nフォーマット: ${getFormat(e)}")).flatMap { x => x }.mkString("\n"))
          case ee: Throwable =>
            error = Some(Seq(error, Some(s"読込中に例外が発生しました。\n$ee")).flatMap { x => x }.mkString("\n"))
        }
      }
      else if (partType startsWith "TEXT/HTML") html = Some(part.getContent.toString)
      else if (partType startsWith "MULTIPART/") childMultipart = Some(parseMultiPart(part.getContent.asInstanceOf[Multipart]))
      else if (partType startsWith "IMAGE") {
        if (part.getDisposition == Part.INLINE.toUpperCase) {
          attachments :+= MailAttachment.apply(
            cid = part.getHeader("Content-ID")(0).replace("<", "").replace(">", "")
            , attachStream = part.getInputStream
            , extention = getExtension(part.getContentType)
          )
        } else {
          println("find 添付ファイル Error")
          error = Some(Seq(error, Some(s"※元のメールには、添付ファイルが付与されています。")).flatMap { x => x }.mkString("\n"))
        }
      } else {
        println(s"find Error $partType")
        error = Some(Seq(error, Some(s"対応していないフォーマットが存在したため一部読み込みがスキップされました\nフォーマット: $partType")).flatMap { x => x }.mkString("\n"))
      }
    }
    val result = SimpleMultiPart(
      plainBody = plain
      , htmlBody = html
      , error = error
      , attachments = attachments
    )
    childMultipart match {
      case Some(p) => result + p
      case None => result
    }
  }
}
