package models

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Rule(start: RuleBoard, end: Option[RuleBoard])

case class RuleBoard(hidden: Boolean = true, tokens: List[Token]) {
  require(tokens.size == 4, "RuleBoard needs 4 tokens")
}

object Rule {
  implicit val ruleBoardWrites: Writes[RuleBoard] = Writes {(ruleBoard: RuleBoard) => JsArray(ruleBoard.tokens.map(Json toJson _ ))}
  implicit val ruleBoardReads: Reads[RuleBoard] = __.read[List[Token]].map(RuleBoard(true, _))

  implicit val ruleWrites: Writes[Rule] = (
    (__ \ "start").write[RuleBoard] and
    (__ \ "end").writeNullable[RuleBoard])(unlift(Rule.unapply))
  implicit val ruleReads: Reads[Rule] = (
      (__ \ "start").read[RuleBoard] and
      (__ \ "end").readNullable[RuleBoard]
      )(Rule.apply _)
}