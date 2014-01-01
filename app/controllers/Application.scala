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

object Application extends Controller {

  def index = Authenticated { implicit request =>
    request.jedis.setnx("game:next.id", "0")
    val current = request.jedis.get("game:next.id")
    val waitingGames = List.range(1, current.toLong + 1)
    Ok(views.html.index(waitingGames))
  }

  def newGame = Authenticated { implicit request =>
    request.jedis.setnx("game:next.id", "0")
    val nextId = request.jedis.incr("game:next.id")
    Redirect(routes.Application.game(nextId))
  }

  def game(id: Long) = Authenticated { implicit request =>
    Ok(views.html.game(id))
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
