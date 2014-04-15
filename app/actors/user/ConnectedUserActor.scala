package actors.user

import akka.actor.Actor
import models.User
import models.AnonymousUser
import akka.actor.Props
import scala.concurrent.duration._
import akka.actor.ReceiveTimeout
import models.GuestUser

class ConnectedUserActor(id: Long) extends Actor {
  import ConnectedUserActor._
  var user: User = new AnonymousUser(id)
  context.setReceiveTimeout(60 seconds)

  def receive: Receive = {
    case Get => sender ! user
    case ReceiveTimeout =>
      context.parent ! Disconnected(user.id)
      context stop self
    case NicknameUser(nickname) =>
      user = GuestUser(id, nickname)
      sender ! user
  }
}

object ConnectedUserActor {
  case object Get
  case class NicknameUser(nickname: String)

  case class Disconnected(id: Long)
  def props(id: Long) = Props(new ConnectedUserActor(id))
}