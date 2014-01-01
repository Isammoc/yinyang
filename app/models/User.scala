package models

import redis.clients.jedis.Jedis

case class User(id: Long)
object User {
  def fromId(id: Long)(implicit jedis: Jedis): User = new User(id)
}