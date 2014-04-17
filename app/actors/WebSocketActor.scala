package actors

import akka.actor.Actor
import akka.actor.Props
import play.api.libs.iteratee.Concurrent
import scala.util.Random
import play.api.libs.json._
import play.api.libs.functional.syntax._
import models.User
import play.api.data.validation.ValidationError
import models.GameInformation

class WebSocketActor(channel: Concurrent.Channel[JsValue]) extends Actor {
  import WebSocketActor._

  val id: Long = Random.nextInt
  var username = s"Anne Onyme (${id})"

  val listeners = context.actorOf(ListenerSupport.props(self))

  self ! Send(JsObject(Seq("type" -> JsString("self"), "content" -> Json.toJson(User(id, username)))))

  def receive: Receive = {
    case Received(value) =>
      Json.fromJson[Command](value)(readsCommand).map { self ! _ }
    case Send(value) =>
      channel push value
    case ChangeUsernameCommand(newUsername) =>
      username = newUsername
      self ! Send(JsObject(Seq("type" -> JsString("self"), "content" -> Json.toJson(User(id, username)))))
      listeners ! User(id, username)

    case gi: GameInformation =>
      self ! Send(JsObject(Seq("type" -> JsString("game"), "content" -> Json.toJson(gi))))
    case Disconnected =>
      println("disconnected")
      context stop self
    case Get =>
      sender ! User(id, username)

    case event: ListenerSupport.ListenEvent =>
      listeners forward event

    case msg: User =>
      self ! Send(JsObject(Seq("type" -> JsString("username"), "content" -> Json.toJson(msg))))
  }
}

object WebSocketActor {
  case class Received(value: JsValue)
  case class Send(value: JsValue)
  case object Disconnected
  case object Get

  abstract class Command
  case class ChangeUsernameCommand(username: String) extends Command

  def equalReads[T](v: T)(implicit r: Reads[T]): Reads[T] = Reads.filter(ValidationError("validate.error.unexpected.value", v))(_ == v)
  implicit val changeUsernameCommandReads: Reads[ChangeUsernameCommand] = (__ \ "content").read[String].map(v => ChangeUsernameCommand(v))

  implicit val readsCommand: Reads[Command] = ((__ \ "type").read[String](equalReads("username")) andKeep changeUsernameCommandReads).map(v => v)

  def props(channel: Concurrent.Channel[JsValue]) = Props(new WebSocketActor(channel))
}

