package services

import javax.inject.Inject

import models.Tables._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by ubun on 2016/06/27.
  */
class AuthService @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._

  def findByUsername(userName: String): Option[UserRow] = {
    val findUser = db.run(User.filter(_.userName === userName).result.headOption)
    Await.result(findUser, Duration.Inf)
  }

  def validate(userName: String, password: String): Boolean = {
    val query = User.filter(u => u.userName === userName && u.userPassword === password).result.headOption
    val validation = db.run(query).map(_.isDefined)
    Await.result(validation, Duration.Inf)
  }
}
