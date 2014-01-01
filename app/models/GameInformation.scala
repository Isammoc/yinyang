package models

import redis.clients.jedis.Jedis

case class GameInformation(id: Long, creator: User, opponent: Option[User]) {
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

  def join(user: User)(implicit jedis: Jedis): Option[GameInformation] = {
    opponent match {
      case None if user != this.creator =>
        val answer = this.copy(opponent = Some(user))
        answer.save
        Some(answer)
      case _ => None
    }
  }
}

object GameInformation {
  def fromId(id: Long)(implicit jedis: Jedis): Option[GameInformation] = {
    val sedis = org.sedis.Dress.up(jedis)
    sedis.get("game:" + id + ":creator").map{creatorId =>
      val opponent = sedis.get("game:" + id + ":opponent").map(_.toLong).map(User.fromId)
      new GameInformation(id, User.fromId(creatorId.toLong), opponent)}
  }

  def create(user: User)(implicit jedis: Jedis): GameInformation = {
    jedis.setnx("game:next.id", "0")
    val id = jedis.incr("game:next.id")
    val game = new GameInformation(id, user, None)
    game.save
    game
  }

  def getWaitings(implicit jedis: Jedis): Set[GameInformation] = {
    val sedis = org.sedis.Dress.up(jedis)
    sedis.smembers("game:waitings").map (_.toLong).flatMap (gameId => fromId(gameId) match {
      case None => Set.empty[GameInformation]
      case Some(x) => Set(x)
    })
  }
}
