package services.Mail

import java.io.UnsupportedEncodingException
import javax.mail.{search, _}
import javax.mail.internet.InternetAddress
import javax.mail.search.{AndTerm, ComparisonTerm, SentDateTerm}

import models.Entity.{Mail, MailAttachment}
import org.joda.time.DateTime

/**
  * Created by shota.oda on 2016/06/16.
  */

object MailHandler {

  def syncDefaultPerWeek(from:DateTime, to:DateTime) = {
    /* =========================================================
        じゅんび
    ========================================================= */
    val fromTerm = new SentDateTerm(ComparisonTerm.GE, from.toDate)
    val toTerm   = new SentDateTerm(ComparisonTerm.LT, to.toDate)
    val mc = MailConfig.default()
    mc.open()

    /* =========================================================
         たんさく
    ========================================================= */
    val mails = mc.search(new AndTerm(Array(fromTerm,toTerm)))
      .map { mes => parseMessage(mes) }
    mc.close()
    mails
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

  def mailStream(from: DateTime) = {

    /* =========================================================
        じゅんび
    ========================================================= */
    val fromTerm = new SentDateTerm(ComparisonTerm.GE, from.toDate)
    val mc = MailConfig.default()
    mc.open()

    /* =========================================================
         たんさく
    ========================================================= */
    val mails = mc.search(fromTerm)
//      .filter  { mes =>
//        mes.getFrom.contains(m => m.asInstanceOf[InternetAddress].getAddress.contains("div-hr"))
//      }
      .map { mes => parseMessage(mes) }
    mc.close()
    mails
  }

  private def getExtension(contentType: String): String = {
    contentType.substring(contentType.indexOf('/') + 1, contentType.indexOf(';')).toLowerCase()
  }

  private def getFormat(e: UnsupportedEncodingException): String = {
    val message = e.getMessage
    message.substring(message.indexOf(':') + 1, message.length)
  }

  private def trimHtml(htmlStr: String): String = {
    htmlStr.trim.replaceAll("|\n|\r\n|\r", "")
  }

  private def rewriteImageSrc(htmlStr: String): String = {
    htmlStr.replace("cid:", "image/")
  }

  private def parseMessage(mes: Message): Mail = {
    /* =========================================================
         へっだー
      ========================================================= */
    val from = mes.getFrom()(0).asInstanceOf[InternetAddress]
    println(from)
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
    //println(sentDateTime)
    val subject = if(mes.getSubject == null) "件名なし" else mes.getSubject

    /* =========================================================
       ほんぶん
    ========================================================= */
    var plainbody: Option[String] = None
    var htmlbody: Option[String] = None
    var errorMessage: Option[String] = None
    var attachments: Seq[MailAttachment] = Nil
    var multipart: Option[SimpleMultiPart] = None

    /* =========================================================
       かいせき
    ========================================================= */
    val CONTENTTYPE = mes.getContentType.toUpperCase
    if (CONTENTTYPE startsWith "TEXT/PLAIN") {
      try{
        plainbody = Some(mes.getContent.toString)
      } catch {
        case e: UnsupportedEncodingException =>
          errorMessage = Some(s"対応していないフォーマットのため読み込めませんでした\nフォーマット: ${getFormat(e)}")
        case ee: Throwable =>
          errorMessage = Some(s"読込中に例外が発生しました。\n$ee")
      }
    } else if (CONTENTTYPE startsWith "TEXT/HTML") {
      htmlbody = Some(mes.getContent.toString)

    } else if (CONTENTTYPE startsWith "MULTIPART") {
      multipart = Some(parseMultiPart(mes.getContent.asInstanceOf[Multipart]))
      val m = multipart.get
      plainbody = m.plainBody.map(x => x)
      htmlbody = m.htmlBody.map(x => x)
      if (m.attachments.nonEmpty) attachments = m.attachments
      m.error.foreach(x => errorMessage = Some(x))
    } else {
      errorMessage = Some(s"対応していないフォーマットのため読み込めませんでした\nフォーマット: $CONTENTTYPE")
    }

    //print("・")
    multipart.foreach { v =>
      if (v.plainBody.isEmpty && v.htmlBody.isEmpty) {
        println(from)
        println("[err]ボディがNone")
      } else if (v.attachments != Nil && v.htmlBody.isEmpty) {
        println(from)
        println(s"[err]INLINEアタッチメントありなのに、本文無し\n${v.attachments}")
      } else if (v.error.isDefined) {
        println(from)
        println(s"[warn]なにかしら\n${v.error.get}")
      }
    }

    Mail(
      from = from
      , tos = tos
      , ccs = ccs
      , bccs = bccs
      , subject = subject
      , sentDate = sentDateTime
      , plainBody = plainbody
      , htmlBody = htmlbody.map(s => trimHtml(s))
      , attachments = attachments
      , error = errorMessage)
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
          val in = part.getInputStream
          attachments :+= MailAttachment(
            cid = part.getHeader("Content-ID")(0).replace("<", "").replace(">", "")
            , attach = Stream.continually(in.read).takeWhile(_ != -1).map{ b =>
              print(b.toByte)
              b.toByte
            }.toArray
            , extension = getExtension(part.getContentType)
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
