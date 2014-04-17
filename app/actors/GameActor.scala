package actors

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.Terminated
import models.User
import models.GameInformation
import models.GameInformation
import scala.collection.immutable.Queue

class GameActor extends Actor {
  import GameActor._

  var whiteRef = Option.empty[ActorRef]
  var blackRef = Option.empty[ActorRef]

  var spectators = Queue.empty[ActorRef]

  def all = {
    val withWhite = whiteRef.fold(spectators)(_ +: spectators)
    blackRef.fold(withWhite)(_ +: withWhite)
  }

  def receive = {
    case Join =>
      val older = all
      if (blackRef.isEmpty) {
        blackRef = Some(sender)
      } else if (whiteRef.isEmpty) {
        whiteRef = Some(sender)
      } else {
        spectators +:= sender
      }
      context.watch(sender)
      older.foreach { old =>
        old.tell(ListenerSupport.Listen, sender)
        sender.tell(ListenerSupport.Listen, old)
      }
      sender ! ListenerSupport.Listen
      all.foreach(ref => self.tell(GetInformation, ref))

    case GetInformation =>
      val requester = sender
      context actorOf Props(new GetInformation(requester, whiteRef, blackRef, spectators.size))

    case Terminated(oldRef) =>
      if (whiteRef == Some(oldRef)) {
        whiteRef = None
      } else if (blackRef == Some(oldRef)) {
        blackRef = None
      } else {
        spectators = spectators.filterNot(_ == oldRef)
      }
  }
}

class GetInformation(requester: ActorRef, whiteRef: Option[ActorRef], blackRef: Option[ActorRef], spectators: Int) extends Actor {
  var white = Option.empty[User]
  var black = Option.empty[User]

  whiteRef.fold(blackRef)(Some(_)).map(_ ! WebSocketActor.Get)

  def receive = if (whiteRef.isDefined) forWhite else forBlack

  def forWhite: Receive = {
    case user: User =>
      white = Some(user)
      if (blackRef.isDefined) {
        blackRef.get ! WebSocketActor.Get
        context become forBlack
      }
      check
  }

  def forBlack: Receive = {
    case user: User =>
      black = Some(user)
      check
  }

  def check = {
    if (white.isDefined == whiteRef.isDefined && black.isDefined == blackRef.isDefined) {
      requester ! GameInformation(white, black, spectators)
      context stop self
    }
  }
}

object GameActor {
  case object GetInformation
  case object Join

  case class Game(whiteRef: Option[ActorRef], blackRef: Option[ActorRef])

  def props = Props[GameActor]
}