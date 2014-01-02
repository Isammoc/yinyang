package actors

import akka.actor.Actor
import models.User
import models.GameInformation
import play.api.Play.current
import com.typesafe.plugin.RedisPlugin
import akka.actor.Props

class GamesManager extends Actor {
  import GamesManager._

  val sedisPool = current.plugin[RedisPlugin].get.sedisPool

  def receive: Receive = {
    case Create(user) =>
      sedisPool.withJedisClient { implicit client =>
        sender ! SimpleOperationOk(GameInformation.create(user))
      }
    case ListWaitingGames => {
      sedisPool.withJedisClient { implicit client =>
        sender ! MultipleOperationOk(GameInformation.getWaitings)
      }
    }
    case Join(id: Long, user: User) => 
      sedisPool.withJedisClient { implicit client =>
        GameInformation.fromId(id)
          .flatMap(_.join(user)) match {
          case None => sender ! OperationFailed
          case Some(game) => sender ! SimpleOperationOk(game)
        }
      }
    case Find(id: Long) =>
      sedisPool.withJedisClient { implicit client =>
        GameInformation.fromId(id) match {
          case None => sender ! OperationFailed
          case Some(game) => sender ! SimpleOperationOk(game)
        }
      }
  }
}

object GamesManager {
  // Requests
  case class Create(creator: User)
  case class Join(id: Long, opponent: User)
  case class Find(id: Long)
  case class ListWaitingGames()

  // Answers
  abstract class OperationAck()
  case class SimpleOperationOk(game: GameInformation) extends OperationAck
  case class OperationFailed() extends OperationAck
  case class MultipleOperationOk(games: Iterable[GameInformation]) extends OperationAck

  val props = Props[GamesManager]
}