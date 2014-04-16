package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import akka.pattern.ask
import play.api.mvc.Controller
import play.libs.Akka
import play.api.data._
import play.api.data.Forms._
import scala.concurrent.Future
import play.api.mvc.Action
import play.api.mvc.WebSocket
import play.api.libs.iteratee.Iteratee
import play.api.libs.json.JsValue
import play.api.libs.iteratee.Concurrent
import actors.Supervisor
import akka.util.Timeout
import scala.concurrent.duration._
import actors.WebSocketActor
import play.api.libs.json.JsString

object Application extends Controller {
  implicit val _ = Timeout(5 seconds)
  def supervisor = Akka.system.actorSelection("user/supervisor")

  def index = Action { implicit request =>
    Ok(views.html.main())
  }

  def connect = WebSocket.async { implicit request =>
    //Concurrent.broadcast returns (Enumerator, Concurrent.Channel)
    val (out, channel) = Concurrent.broadcast[JsValue]

    (supervisor ? Supervisor.Join(channel)).map {
      case Supervisor.Connected(webSocketRef) =>
        val in = Iteratee.foreach[JsValue] { msg =>
          webSocketRef ! WebSocketActor.Received(msg)
        }.map { _ =>
          webSocketRef ! WebSocketActor.Disconnected
        }
        (in, out)
    }
  }
  
  def mainJS = Action {implicit request =>
    Ok(views.js.main())
  }
}
