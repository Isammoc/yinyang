package actors.user

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props

class UsersManager extends Actor {
  import UsersManager._

  var connectedUsers: Map[Long, ActorRef] = Map.empty
  var currentId = 0
  def nextId = {
    val current = currentId
    currentId += 1
    current
  }

  def receive: Receive = {
    case GetOrConnectUser(id) =>
      connectedUsers.getOrElse(id, createChild(id)) forward ConnectedUserActor.Get
    case GetOrConnectUserActor(id) =>
      sender ! connectedUsers.getOrElse(id, createChild(id))
    case New =>
      val newId: Long = nextId
      createChild(newId) forward ConnectedUserActor.Get
    case UsersStat =>
      sender ! UsersStat(connectedUsers.size)
    case ConnectedUserActor.Disconnected(id) =>
      connectedUsers -= id
  }

  def createChild(id: Long) = {
    val child = context.actorOf(ConnectedUserActor.props(id))
    connectedUsers += (id -> child)
    child
  }
}

object UsersManager {
  case class GetOrConnectUser(id: Long)
  case class GetOrConnectUserActor(id: Long)
  case class UserActor(id: Long, ref: ActorRef)
  case object New

  case object UsersStat
  case class UsersStat(connectedCount: Int)

  def props = Props[UsersManager]
}