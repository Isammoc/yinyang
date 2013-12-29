package controllers

import play.api._
import play.api.mvc._
import play.api.Play.current
import com.typesafe.plugin.RedisPlugin
import org.sedis.Dress

object Application extends Controller {

  def index = Action {
    current.plugin[RedisPlugin].map ( _.sedisPool.withJedisClient { client =>
      import Dress._
      client.setnx("game:next.id", "0")
      val current = client.get("game:next.id")
      val waitingGames = List.range(1, current.toLong + 1)
      Ok(views.html.index(waitingGames))
    }).getOrElse(InternalServerError("Redis Play Plugin not here"))
  }

  def newGame = Action {
    current.plugin[RedisPlugin].map ( _.sedisPool.withJedisClient { client =>
      import Dress._
      client.setnx("game:next.id", "0")
      val nextId = client.incr("game:next.id")
      Redirect(routes.Application.game(nextId))
    }).getOrElse(InternalServerError("Redis Play Plugin not here"))
  }

  def game(id: Long) = Action {
    Ok(views.html.game(id))
  }
}
