package controllers

import javax.inject.{Inject, Singleton}

import controllers.composite.Secured
import play.api.mvc.Controller
import services.AuthService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(
 val authService: AuthService
)
extends Controller
 with Secured{

  def index = withUser { user => implicit rs =>
    Future(Ok(views.html.index()))
  }
}
