package controllers


import javax.inject.Inject

import controllers.composite.Secured
import models.Tables._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.mvc.Controller
import services.AuthService
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Shota on 2016/06/28.
  */
class JsonApiController @Inject()(val dbConfigProvider: DatabaseConfigProvider
                                  , override val authService: AuthService
                                 )
  extends Controller
    with HasDatabaseConfigProvider[JdbcProfile]
    with Secured {

  import driver.api._

  def reportBody(reportId: Int) = withAuth { username => implicit rs =>
    db.run(Reportbody.filter(_.reportId === reportId).result).map { reports =>
      val json = Json.obj(
        "reports" -> JsArray(reports.map{ report =>
          Json.obj(
            "id"        -> report.reportId,
            "type"      -> report.reportbodyTypeCode,
            "body"      -> report.reportbody.replace("cid:",s"inline?bid=${report.reportbodyId}&cid=")
          )
        })
      )
      Ok(json)
    }
  }


}