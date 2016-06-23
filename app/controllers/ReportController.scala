package controllers

import java.sql.Timestamp
import java.text.SimpleDateFormat
import javax.sql.rowset.serial.SerialBlob
import javax.inject.Inject
import org.joda.time.Duration
import models.Tables._
import org.joda.time.DateTime
import play.api.db.slick._
import play.api.mvc._
import services.Mail.MailHandler
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by Shota on 2016/06/13.
  */
class ReportController @Inject()(val dbConfigProvider: DatabaseConfigProvider) extends Controller with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  //
  def list(date: String) = Action.async { implicit rs =>

    //validation
    val sdf = new SimpleDateFormat("yyyyMMdd")
    val targetDate = new DateTime(sdf.parse(date.toString))

    // background
    syncMail()

    // get date for show
    fetchMail(targetDate)

    db.run(User.sortBy(_.userId).result).map { users =>
      Ok(views.html.userList(users))
    }
  }

  def sync() = Action.async { implicit rs =>
    val origindate = new DateTime(2016, 4, 1, 0, 0)
    val end = new DateTime().dayOfMonth().roundFloorCopy()
    val d = new Duration(origindate, end)
    (0 to d.getStandardDays.toInt).foreach { i =>
      fetchInsertMail(origindate.plusDays(i))
    }

    db.run(User.sortBy(_.userId).result).map { users =>
      Ok(views.html.userList(users))
    }
  }

  def image(name: String) = Action {

    Ok.sendFile(new java.io.File(name)) // the name should contains the image extensions
  }

  private def fetchMail(date: DateTime): Future[Seq[(UserRow, ReportbodyRow, ReportbodyRow)]] = {
    val from = new Timestamp(date.getMillis)
    val to = new Timestamp(date.plusDays(1).getMillis)
    val query = for {
      report <- Report if report.reportSentdate >= from && report.reportSentdate < to
      user <- User if report.userId == user.userId
      plainbody <- Reportbody if report.reportId == plainbody.reportId && plainbody.reportbodyTypeCode == "PLAIN"
      htmlbody <- Reportbody if report.reportId == htmlbody.reportId && htmlbody.reportbodyTypeCode == "HTML"
    } yield (user, plainbody, htmlbody)
    println(query.insertStatement)
    db.run(query.result)
  }

  private def syncMail() = {
    db.run(Mailfetchlog.map(_.mailfetchDate).max.result).map { lastFetchDate =>

    }
  }

  private def fetchInsertMail(dateTime: DateTime) = {

    MailHandler.mailStream(dateTime) { implicit mail =>

      val action = for {
        // ユーザー探索
        user <- User.filter(_.userAddress === mail.from.getAddress).result.headOption

        // 日報レコード insert
        reportId <- if (user.isDefined) (Report returning Report.map(_.reportId)) += ReportRow(0, user.get.userId, Some(mail.subject), new Timestamp(mail.sentDate.getMillis))
        else {
          for {
            userId <- (User returning User.map(_.userId)) += UserRow(0, mail.from.getAddress.substring(0, mail.from.getAddress.indexOf('@')), "", mail.from.getAddress, "BOS")
            report <- (Report returning Report.map(_.reportId)) += ReportRow(0, userId, Some(mail.subject), new Timestamp(mail.sentDate.getMillis))
          } yield (report)
        }

        // アドレス情報 insert
        _ <- Reportheader ++= mail.tos.map { to =>
          ReportheaderRow(reportId, "TO", to.getAddress)
        }
        _ <- Reportheader ++= mail.ccs.map { cc =>
          ReportheaderRow(reportId, "CC", cc.getAddress)
        }
        _ <- Reportheader ++= mail.bccs.map { bcc =>
          ReportheaderRow(reportId, "BCC", bcc.getAddress)
        }

        // 日報本体 insert
        _ <-
        if (mail.plainBody.isDefined) Reportbody += ReportbodyRow(0, reportId, "PLAIN", mail.plainBody.get)
        else DBIO.successful(0)
        reportBodyId <-
        if (mail.htmlBody.isDefined) (Reportbody returning Reportbody.map(_.reportbodyId)) += ReportbodyRow(0, reportId, if (mail.attachments != Nil) "RELATED" else "HTML", mail.htmlBody.get)
        else DBIO.successful(0)

        // インライン添付ファイル insert
        _ <- if (mail.htmlBody.isDefined) {
          Reportasset ++= mail.attachments.map { attach =>
            ReportassetRow(
              0
              , attach.cid
              , reportBodyId
              , Some(new SerialBlob(Stream.continually(attach.attachStream.read).takeWhile(_ != -1).map(_.toByte).toArray))
              , Some(attach.extention)
            )
          }
        } else DBIO.successful(0)
      } yield ()

      db.run(action.transactionally).onFailure { case e => println(e) }
    }
  }
}
