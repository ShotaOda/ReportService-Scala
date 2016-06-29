package controllers

import java.io.File
import java.sql.Timestamp
import java.text.{ParseException, SimpleDateFormat}
import javafx.scene.image.WritableImage
import javax.inject.Inject
import javax.sql.rowset.serial.SerialBlob

import models.Tables._
import models.ViewEntity.ReportRowItem
import org.joda.time.DateTime
import play.api.db.slick._
import play.api.libs.iteratee.Enumerator
import play.api.mvc._
import services.AuthService
import services.Mail.MailHandler
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}


/**
  * Created by Shota on 2016/06/13.
  */
class ReportController @Inject()( val dbConfigProvider: DatabaseConfigProvider
                                 ,override val authService: AuthService
                                )
  extends Controller
    with HasDatabaseConfigProvider[JdbcProfile]
    with Secured{

  import driver.api._

  /* =========================================================
    コントローラー
  ========================================================= */

  def list(date: Option[String]) = withAuth { username => implicit rs =>

    //validation
    val sdf = new SimpleDateFormat("yyyyMMdd")
    val targetDate: DateTime = try {
      date match {
        case Some(d) => new DateTime(sdf.parse(d))
        case None    => new DateTime()
      }
    } catch {
      case e: ParseException => new DateTime()
    }

    // background
    //syncMail()

    fetchReportItem(targetDate).map { item =>
      Ok(views.html.reportList(item))
    }
  }



  def sync() = Action.async { implicit rs =>
    //import org.joda.time.Duration
    //val start = new DateTime(2016,6,17,0,0)
    //val end = new DateTime()
    //val d = new Duration(start, end)
    //(0 to d.getStandardDays.toInt / 7 + 1).foreach {i =>
    //  println(s"====${start.plusDays(i * 7)} 〜 ${start.plusDays((i + 1) * 7)}=====")
    //  fetchInsertPerWeek(start.plusDays(i * 7), start.plusDays((i + 1) * 7))
    //}

    //fetchInsertMail(new DateTime(selectLastFetch))
    fetchReportItem(new DateTime().dayOfMonth.roundFloorCopy).map { item =>
      Ok(views.html.reportList(item))
    }
  }

  def inlineImage(bid: Int, cid: String) = withAuth { username => implicit rs =>
    println("inline image call")
    val assetAction = Reportasset.filter {asset => asset.reportbodyId === bid && asset.reportassetCid === cid}.result.head
    db.run(assetAction).map { asset =>
      asset.reportasset match {
        case Some(b) =>
          val bs = asset.reportasset.get.getBinaryStream
          Ok(Stream.continually(bs.read).takeWhile(_ != -1).map(_.toByte)toArray).as(s"image/${asset.reportassetExtention}")
        case None => NotFound("")
      }
    }
  }

  /* =========================================================
    プライベート
  ========================================================= */

  private def fetchReportItem(date: DateTime): Future[Seq[ReportRowItem]] = {
    val from = new Timestamp(date.getMillis)
    val to = new Timestamp(date.plusDays(1).getMillis)
    val query = for {
      report <- Report.filter(r => r.reportSentdate >= from && r.reportSentdate < to)
      user <- User if report.userId === user.userId
    } yield (report, user)
    db.run(query.sortBy(_._1.reportSentdate.desc).result).map { resultSeq =>
      resultSeq.map { r =>
        val date = new DateTime(r._1.reportSentdate)
        ReportRowItem(
            name = r._2.userName
          , title = r._1.reportTitle.get
          , commentCount = 0
          , sentDate = date.toString("MM/dd HH:mm")
          , reportId = r._1.reportId
        )
      }
    }
  }

  private def syncMail() = {
    db.run(Mailfetchlog.map(_.mailfetchDate).max.result).map { lastFetchDate =>
    }
  }

  private def fetchInsertPerWeek(from: DateTime, to: DateTime) = {
    val actions = MailHandler.syncDefaultPerWeek(from, to).map { mail =>
      for {
      // ユーザー探索 select 1
        user <- User.filter(_.userAddress === mail.from.getAddress).result.headOption

        // 日報レコード insert
        reportId <-
        if (user.isDefined) (Report returning Report.map(_.reportId)) += ReportRow(0, user.get.userId, Some(mail.subject), new Timestamp(mail.sentDate.getMillis))
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
              , Some(new SerialBlob(attach.attach))
              , Some(attach.extension)
            )
          }
        } else DBIO.successful(0)
      } yield ()
    }
    Await.ready(db.run(DBIO.sequence(actions)), Duration.Inf)
  }

  // 同期処理
  private def fetchInsertMail(dateTime: DateTime) = {

    val actions = MailHandler.mailStream(dateTime).map { mail =>
      for {
      // ユーザー探索 select 1
        user <- User.filter(_.userAddress === mail.from.getAddress).result.headOption

        // 日報レコード insert
        reportId <-
        if (user.isDefined) (Report returning Report.map(_.reportId)) += ReportRow(0, user.get.userId, Some(mail.subject), new Timestamp(mail.sentDate.getMillis))
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
              , Some(new SerialBlob(attach.attach))
              , Some(attach.extension)
            )
          }
        } else DBIO.successful(0)
      } yield ()
    }
    // TODO アクションの配列作成して、ループ外でまとめて実行する
    Await.ready(db.run(DBIO.sequence(actions)), Duration.Inf)

    //
    //    val result = db.run(action.transactionally)
    //    result.onFailure { case e => println(e) }
    //    Await.ready(result, Duration.Inf)
  }

  private def selectLastFetch: Timestamp = {
    val action = Mailfetchlog.map(_.mailfetchDate).max.result
    Await.result(db.run(action).map {
      _.get
    }, Duration.Inf)
  }
}
