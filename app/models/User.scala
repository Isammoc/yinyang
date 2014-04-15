package models

abstract class User {
  val id: Long
}

case class AnonymousUser(id:Long) extends User

