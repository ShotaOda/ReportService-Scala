package services.Tweet

import javax.inject.Inject

import org.joda.time._

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.mvc.Controller
import slick.driver.JdbcProfile

/**
  * Created by Shota on 2016/07/12.
  */
class BaseService @Inject()(
                             val dbConfigProvider: DatabaseConfigProvider
                           )
  extends Controller
  with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  def listJson(userId: Int) = {
    //TODO implements & paging
  }

  def addComment(userId: Int, comment: String, dt: DateTime) = {
    //TODO implements
  }

  def deleteComment(commentId: Int) = {

  }

  def addFavorite(commentId: Int) = {

  }

  def deleteFavorite(commentId: Int) = {

  }
















}
