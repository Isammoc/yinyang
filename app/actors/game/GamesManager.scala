package actors.game

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Terminated
import models.Game
import akka.actor.Props
import akka.actor.actorRef2Scala

class GamesManager extends Actor {
  import GamesManager._
  var currentId: Long = 0
  def nextId = {
    val current = currentId
    currentId += 1
    current
  }

  var waitingGames = Map.empty[Long, ActorRef]
  def receive: Receive = {
    case New(creatorRef) =>
      val newId = nextId
      val child = context.actorOf(GameActor.props(newId, creatorRef))
      waitingGames += (newId -> child)
      child forward GameActor.Get
    case ListWaitingGames =>
      val requester = sender
      context.actorOf(Props(new WaitingsGameList(requester, waitingGames))) forward ListWaitingGames
  }
}

class WaitingsGameList(requester: ActorRef, waitingGames: Map[Long, ActorRef]) extends Actor {
  var answer = List.empty[Game]
  var pending = waitingGames
  pending.values.foreach { gameRef =>
    context.watch(gameRef)
    gameRef ! GameActor.Get
  }
  check

  def receive: Receive = init

  def init: Receive = {
    case Terminated(ref) =>
      pending.find { case (_, gameRef) => gameRef == ref }.foreach { case (id, _) => pending -= id }
      check
    case game: Game =>
      pending -= game.id
      answer = game :: answer
      check
  }

  def check: Unit = {
    if (pending.isEmpty) {
      requester ! answer.sortBy(_.id)
      context stop self
    }
  }
}

object GamesManager {
  case class New(creatorRef: ActorRef)
  case object ListWaitingGames

  def props = Props[GamesManager]
}