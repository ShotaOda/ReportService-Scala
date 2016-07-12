package controllers.Tweet

import javax.inject.Inject

import play.api.mvc.Controller
import services.AuthService
import services.Tweet.BaseService

/**
  * Created by Shota on 2016/07/12.
  */
class HomeController @Inject()(
                               val baseService: BaseService
                              ,val authService: AuthService
                              )
  extends Controller
  with Secured{

}
