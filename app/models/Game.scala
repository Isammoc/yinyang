package models

case class Game(id: Long, creator: User, opponent: Option[User]) {
  val isWaiting = opponent.isEmpty
}
