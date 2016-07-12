package services

import javax.inject.Inject

import models.Tables._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by dabuntu on 2016/06/27.
  */
class AuthService @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._

  def findByUsername(userName: String): Option[UserRow] = {
    val findUser = db.run(User.filter(_.userName === userName).result.headOption)
    Await.result(findUser, Duration.Inf)
  }

  def validate(userName: String, password: String): Boolean = {
    val validation = db.run(User.filter(u => u.userName === userName && u.userPassword === password).result.headOption).map { user =>
      user match {
        case Some(u) => true
        case None    => false
      }

    }
    Await.result(validation, Duration.Inf)
  }
}
