package models

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class User(id: Long, username: String)

object User {
  implicit val _:Writes[User] = (
      ( __ \ "id" ).write[Long] and
      ( __ \ "username" ).write[String]
  )(unlift(User.unapply))
}