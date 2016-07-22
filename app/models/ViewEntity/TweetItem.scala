package models.ViewEntity

import org.joda.time.DateTime
import play.api.libs.json._

/**
  * Created by Shota on 2016/07/12.
  */
case class TweetItem (
                       user     : String
                     , comment  : String
                     , date     : DateTime
                    ) {
  def toJson: JsObject = {
    Json.obj(
       "user" -> this.user
      ,"comment" -> this.comment
      ,"date" -> this.date.toString("yyyyMMdd")
    )
  }
}