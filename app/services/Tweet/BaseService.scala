package services.Tweet

import javax.inject.Inject

import models.ViewEntity.TweetItem
import org.joda.time._
import org.joda.time.format.DateTimeFormat
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.mvc.Controller
import slick.driver.JdbcProfile

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Shota on 2016/07/12.
  */
class BaseService @Inject()(
                             val dbConfigProvider: DatabaseConfigProvider
                           )
  extends Controller
    with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  def list(userId: Int): Future[Seq[TweetItem]] = {
    //TODO implements & paging
    // val query =
    val formatter = DateTimeFormat.forPattern("yyyymmdd")

    Future(
      (0 to 20).map { i =>
        TweetItem(
          "ubuntu"
          , s" No.$i \n test test test test"
          , formatter.parseDateTime("20151010")
        )
      })
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
