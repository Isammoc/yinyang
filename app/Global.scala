import play.api.GlobalSettings
import play.api.Application
import play.libs.Akka
import actors.user.UsersManager
import actors.game.GamesManager
import actors.ActionsActor

object Global extends GlobalSettings {
  override def onStart(app: Application) = {
    Akka.system().actorOf(ActionsActor.props, name = "actions")
    super.onStart(app)
  }
}