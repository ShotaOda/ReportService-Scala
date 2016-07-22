package controllers

import javax.inject.Inject

import controllers.composite.Secured
import models.Tables._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.mvc.Controller
import services.AuthService
import slick.driver.JdbcProfile
import play.api.i18n.{I18nSupport, MessagesApi}

/**
  * Created by Shota on 2016/06/26.
  */
object AuthController{

}

class AuthController @Inject()(  val dbConfigProvider: DatabaseConfigProvider
                               , override val authService: AuthService
                               , val messagesApi: MessagesApi)
  extends Controller
    with Secured
    with HasDatabaseConfigProvider[JdbcProfile]
    with I18nSupport{

  val loginForm = Form(
    tuple(
        "userName"    -> nonEmptyText
      , "password" -> nonEmptyText
    ) verifying(
        "ユーザー名または、パスワードが間違っています。"
      , form => authService.validate(form._1,form._2))
  )

  def authenticate = Action { implicit request =>
    request.session.get(Security.username).map { user =>
      Redirect(routes.UserController.list()).withNewSession.flashing()
    }.getOrElse {
      loginForm.bindFromRequest.fold(
         f => BadRequest(views.html.login(f)),
        success => Redirect(routes.UserController.list()).withSession(Security.username -> success._1)
      )
    }
  }

  def login = Action { implicit  request =>
    Ok(views.html.login(loginForm))
  }

  def logout = Action { implicit request =>
    Redirect(routes.AuthController.login).withNewSession.flashing(
      "success" -> "You are now logged out."
    )
  }
}
