package controllers

import javax.inject.Inject

import models.Tables._
import play.api.db.slick._
import play.api.mvc._
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Shota on 2016/06/13.
  */
class UserController  @Inject()(val dbConfigProvider: DatabaseConfigProvider) extends Controller with HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._

  /**
    * 一覧表示
    */
  def list = Action.async { implicit rs =>
    // IDの昇順にすべてのユーザ情報を取得
    db.run(User.sortBy(t => t.userId).result).map(users =>
      Ok(views.html.userList(users))
    )
  }
}
