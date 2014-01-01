package controllers

import com.typesafe.plugin.RedisPlugin
import org.sedis.Dress
import play.api._
import play.api.Play.current
import play.api.mvc._
import scala.concurrent.Future
import redis.clients.jedis.Jedis

object Application extends Controller {

  def index = JedisAction { request =>
    request.jedis.setnx("game:next.id", "0")
    val current = request.jedis.get("game:next.id")
    val waitingGames = List.range(1, current.toLong + 1)
    Ok(views.html.index(waitingGames))
  }

  def newGame = JedisAction { request =>
    request.jedis.setnx("game:next.id", "0")
    val nextId = request.jedis.incr("game:next.id")
    Redirect(routes.Application.game(nextId))
  }

  def game(id: Long) = Action {
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
}
