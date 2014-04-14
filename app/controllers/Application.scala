package controllers

import com.typesafe.plugin.RedisPlugin
import models.User
import org.sedis.Dress
import play.api._
import play.api.Play.current
import play.api.mvc._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import redis.clients.jedis.Jedis
import models.Game
import play.libs.Akka
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

object Application extends Controller {
  implicit val _ = Timeout(3 seconds)
  def gamesManagerRef = Akka.system.actorSelection("user/gamesManager")

  def index = Action { implicit request =>
     Ok(views.html.index())
  }
}
