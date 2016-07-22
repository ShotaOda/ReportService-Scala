package controllers.composite

import controllers.routes
import models.Tables._
import play.api.mvc._
import services.AuthService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by ubun on 2016/06/26.
  */
trait Secured {

  //Inject対象
  val authService: AuthService

  private  def username(request: RequestHeader) = request.session.get(Security.username)

  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.AuthController.login)

  def withAuth(f: => String => Request[AnyContent] => Future[Result]) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action.async(request => f(user)(request))
    }
  }

  def withUser(f: UserRow => Request[AnyContent] => Future[Result]) = withAuth { username => implicit request =>
    authService.findByUsername(username).map { user =>
      f(user)(request)
    }.getOrElse(Future(onUnauthorized(request)))
  }
}
