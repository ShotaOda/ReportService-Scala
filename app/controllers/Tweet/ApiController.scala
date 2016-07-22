package controllers.Tweet

import javax.inject.Inject

import controllers.composite.Secured
import play.api.libs.json.{JsArray, Json}
import play.api.mvc.Controller
import services.{AuthService, Tweet}

import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by Shota on 2016/07/12.
  */
class ApiController @Inject()(
 val baseService: Tweet.BaseService
,val authService: AuthService
)
extends Controller
  with Secured{

  def list = withUser { user => implicit rs =>
    baseService.list(10).map { comments =>
      val json = Json.obj(
        "lists" -> JsArray(
          comments.map(_.toJson)
        ))
      Ok(json)
    }
  }


}
