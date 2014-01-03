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
import models.GameInformation
import play.libs.Akka
import actors.GamesManager
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

object Application extends Controller {
  implicit val _ = Timeout(3 seconds)
  def gamesManagerRef = Akka.system.actorSelection("user/gamesManager")

  def index = Authenticated.async { implicit request =>
    (gamesManagerRef ? GamesManager.ListWaitingGames).map {
      case GamesManager.MultipleOperationOk(games) =>
        Ok(views.html.index(GameInformation.getWaitings(request.jedis)))
    }
  }

  def newGame = Authenticated.async { implicit request =>
    (gamesManagerRef ? GamesManager.Create(request.user)).map {
      case GamesManager.SimpleOperationOk(game) =>
        Redirect(routes.Application.game(game.id))
    }
  }

  def game(id: Long) = Authenticated.async { implicit request =>
    (gamesManagerRef ? GamesManager.Find(id)).map {
      case GamesManager.SimpleOperationOk(game) =>
        Ok(views.html.game(game))
      case GamesManager.OperationFailed =>
        Redirect(routes.Application.index)
    }
  }

  def join(id: Long) = Authenticated.async { implicit request =>
    (gamesManagerRef ? GamesManager.Join(id, request.user)).map {
      case GamesManager.SimpleOperationOk(game) =>
        Redirect(routes.Application.game(game.id))
      case GamesManager.OperationFailed =>
        Redirect(routes.Application.index)
    }
  }

  class JedisRequest[A](val jedis: Jedis, request: Request[A]) extends WrappedRequest[A](request)

  object JedisAction extends ActionBuilder[JedisRequest] {
    def invokeBlock[A](request: Request[A], block: (JedisRequest[A]) => Future[SimpleResult]) = {
      current.plugin[RedisPlugin].map(_.sedisPool.withJedisClient { client =>
        block(new JedisRequest(client, request))
      }).getOrElse(Future.successful(InternalServerError("Redis Play Plugin not here")))
    }
  }

  class AuthenticatedRequest[A](val user: User, request: JedisRequest[A]) extends JedisRequest[A](request.jedis, request)

  object Authenticated extends ActionBuilder[AuthenticatedRequest] {
    def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[SimpleResult]) =
      JedisAction.invokeBlock(request, { (request: JedisRequest[A]) =>
        val userId: Long = request.session.get("userId").map(_.toLong).getOrElse {
          request.jedis.setnx("user:next.id", "0")
          request.jedis.incr("user:next.id")
        }
        block(new AuthenticatedRequest(User(userId), request))
            .map( _.withSession ( request.session + ("userId" -> userId.toString) ))
      })
  }
}
