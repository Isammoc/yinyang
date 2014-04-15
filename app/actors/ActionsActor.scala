package actors

import akka.actor.Actor
import models.User
import actors.game.GamesManager
import actors.user.UsersManager
import akka.actor.ActorRef
import akka.actor.Props

class ActionsActor extends Actor {
  import ActionsActor._

  val gamesManagerRef = context.actorOf(GamesManager.props, "gamesManager")
  val usersManagerRef = context.actorOf(UsersManager.props, "usersManager")

  def receive: Receive = {
    case NewGame(creator) =>
      context.actorOf(Props(new NewGameActor(gamesManagerRef, usersManagerRef))) forward NewGame(creator)
    case ChangeNickname(user, nickname) =>
      usersManagerRef forward UsersManager.ChangeNickname(user, nickname)
    case NewUser =>
      usersManagerRef forward UsersManager.New
    case GetOrConnectUser(id) =>
      usersManagerRef forward UsersManager.GetOrConnectUser(id)
    case UsersStat =>
      usersManagerRef forward UsersManager.UsersStat
  }
}

class NewGameActor(gamesManagerRef: ActorRef, usersManagerRef: ActorRef) extends Actor {
  var requester: ActorRef = null

  def receive: Receive = init

  def init: Receive = {
    case ActionsActor.NewGame(creator) =>
      requester = sender
      usersManagerRef ! UsersManager.GetOrConnectUserActor(creator.id)
      context become waitingForUserRef
  }

  def waitingForUserRef: Receive = {
    case creatorRef: ActorRef =>
      gamesManagerRef.tell(GamesManager.New(creatorRef), requester)
      context stop self
  }
}

object ActionsActor {
  case class NewGame(creator: User)
  case class ChangeNickname(user: User, nickname: String)
  case object NewUser
  case class GetOrConnectUser(id: Long)
  case object UsersStat

  def props = Props[ActionsActor]
}