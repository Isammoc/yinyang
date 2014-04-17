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

object GameState extends Enumeration {
  type GameState = Value
  val Init, BlackToPlay, WhiteToPlay, BlackWin, WhiteWin = Value
}

case class Game(
    state: GameState.Value = GameState.Init,
    board: BoardMap = BoardMap(List(Black, White, White, White, Black, White, White, White, Black, Black, Black, White, Black, Black, Black, White)),
    whiteRules: List[Option[Rule]] = List(None, None, None, None),
    blackRules: List[Option[Rule]] = List(None, None, None, None)
) {
  require(whiteRules.size == 4)
  require(blackRules.size == 4)
}

object Game {
  implicit val writeState: Writes[GameState.Value] = Writes {s => JsString(s.toString)}
  implicit val writeGame: Writes[Game] = Json.writes[Game]
}