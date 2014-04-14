package models

import redis.clients.jedis.Jedis

case class Game(id: Long, creator: User, opponent: Option[User]) {
  val isWaiting = opponent.isEmpty

  def save(implicit jedis: Jedis): Unit = {
    opponent match {
      case None =>
        jedis.sadd("game:waitings", id.toString)
        jedis.srem("game:pendings", id.toString)
        jedis.set("game:" + id + ":creator", creator.id.toString)
        jedis.del("game:" + id + ":opponent")
      case Some(opponent) =>
        jedis.srem("game:waitings", id.toString)
        jedis.sadd("game:pendings", id.toString)
        jedis.set("game:" + id + ":creator", creator.id.toString)
        jedis.set("game:" + id + ":opponent", opponent.id.toString)
    }
  }

  def join(user: User)(implicit jedis: Jedis): Option[Game] = {
    opponent match {
      case None if user != Game.this.creator =>
        val answer = Game.this.copy(opponent = Some(user))
        answer.save
        Some(answer)
      case _ => None
    }
  }
}
