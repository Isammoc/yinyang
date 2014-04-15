package models

abstract class User {
  val id: Long
  val isGuest = false
}

case class AnonymousUser(id:Long) extends User

case class GuestUser(id: Long, nickname: String) extends User {
  override val isGuest = true
}
