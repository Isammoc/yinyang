package models

import redis.clients.jedis.Jedis

abstract class User {
  val id: Long
  val isRegistred: Boolean
}

case class AnonymousUser(id:Long) extends User {
  val isRegistred = false
}

case class RegistredUser(id:Long, login: String) extends User {
  val isRegistred = true
}
