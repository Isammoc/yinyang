package models

import play.api.libs.json._
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._

case class BoardMap(tokens: List[Token]) {
  require(tokens.size == 16, "BoardMap needs 16 tokens (empty or not)")
}

object BoardMap {
  implicit val boardMapWrite: Writes[BoardMap] = Writes {(board: BoardMap) => JsArray(board.tokens.map(Json toJson _ ))}
}

abstract class Token {
  def empty = EmptyToken
  val isEmpty = true
}

object Token {
  implicit val tokenWrite: Writes[Token] = new Writes[Token] {
    def writes(token: Token): JsValue = JsString(token match {
      case EmptyToken => "."
      case Black => "b"
      case White => "w"
    })
  }
  def equalReads[T](v: T)(implicit r: Reads[T]): Reads[T] = Reads.filter(ValidationError("validate.error.unexpected.value", v))(_ == v)

  implicit val tokenReads: Reads[Token] = (
      ( __ ).read[String](equalReads(".")).map(_ => EmptyToken:Token) or
      ( __ ).read[String](equalReads("w")).map(_ => White:Token) or
      ( __ ).read[String](equalReads("b")).map(_ => Black:Token)
  )
}

case object EmptyToken extends Token

case object Black extends Token {
  override val isEmpty = false
}

case object White extends Token {
  override val isEmpty = false
}

