package actors

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Terminated
import akka.actor.Props

class ListenerSupport(observableRef: ActorRef) extends Actor {
  import ListenerSupport._

  var listeners = Set.empty[ActorRef]

  def receive = {
    case Listen =>
      listeners += sender
      context.watch(sender)
    case Unlisten =>
      listeners -= sender
      context.unwatch(sender)
    case Terminated(oldRef) =>
      listeners -= oldRef
    case event =>
      listeners.foreach(_.tell(event,observableRef))
  }
}

trait ObservableActor extends Actor {
  import ListenerSupport._
  val listeners = context.actorOf(ListenerSupport.props(self))
  def listen: Receive = {
    case event: ListenerSupport.ListenEvent =>
      listeners forward event
  }
}

object ListenerSupport {
  abstract class ListenEvent
  case object Listen extends ListenEvent
  case object Unlisten extends ListenEvent

  def props(observableRef: ActorRef) = Props(new ListenerSupport(observableRef))
}