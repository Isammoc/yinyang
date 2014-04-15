package actors.user

import akka.actor.Actor
import models.User
import models.AnonymousUser
import akka.actor.Props

class ConnectedUserActor(id: Long) extends Actor {
  import ConnectedUserActor._
  var user: User = new AnonymousUser(id)

  def receive: Receive = {
    case Get => sender ! user
  }
}

object ConnectedUserActor {
  case object Get
  def props(id: Long) = Props(new ConnectedUserActor(id))
}