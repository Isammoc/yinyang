import play.api.GlobalSettings
import play.api.Application
import play.libs.Akka
import actors.GamesManager

object Global extends GlobalSettings {
  override def onStart(app: Application) = {
    val actorRef = Akka.system().actorOf(GamesManager.props, name = "gamesManager")
    super.onStart(app)
  }
}