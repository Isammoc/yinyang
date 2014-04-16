package actors

import akka.actor.Actor
import akka.actor.ActorRef
import play.api.libs.json._
import akka.actor.Props

class Supervisor extends Actor {
  import Supervisor._

  val gameRef = context.actorOf(GameActor.props)
  var members = Map.empty[Int, ActorRef]

  def receive: Receive = {
    case Join(channel) =>
      println("Join")
      val child = context.actorOf(WebSocketActor.props(channel))
      gameRef.tell(GameActor.Join, child)
      sender ! Connected(child)
  }
}

object Supervisor {
  case class Join(channel: play.api.libs.iteratee.Concurrent.Channel[JsValue])
  case class Connected(webSocketActor: ActorRef)

  def props = Props[Supervisor]
}