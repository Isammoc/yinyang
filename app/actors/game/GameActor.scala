package actors.game

import akka.actor.ActorRef
import akka.actor.Actor
import models.Game
import akka.actor.Props
import akka.actor.Terminated
import actors.user.ConnectedUserActor
import akka.actor.actorRef2Scala

class GameActor(id: Long, creatorRef: ActorRef) extends Actor {
  import GameActor._

  context.watch(creatorRef)

  def receive: Receive = waiting

  def waiting: Receive = {
    case Get =>
      val s = sender
      context.actorOf(Props(new WaitingGameActor(s, id, creatorRef))) forward Get
    case Terminated(ref) if ref == creatorRef =>
      context stop self
  }
}

class WaitingGameActor(requester: ActorRef, id: Long, creatorRef: ActorRef) extends Actor {
  creatorRef ! ConnectedUserActor.Get

  def receive: Receive = {
    case creator: models.User =>
      requester ! Game(id, creator, None)
  }
}

object GameActor {
  case class Get

  def props(id: Long, creatorRef: ActorRef) = Props(new GameActor(id, creatorRef))
}