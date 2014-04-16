package models

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class GameInformation(white: Option[User], black: Option[User], spectatorsCount: Int)

object GameInformation {
  import User._
  implicit val gameInformation: Writes[GameInformation] = (
      ( __ \ "white" ).writeNullable[User] and
      ( __ \ "black" ).writeNullable[User] and
      ( __ \ "spectators").write[Int]
    )(unlift(GameInformation.unapply))
}