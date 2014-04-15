import play.api.GlobalSettings
import play.api.Application
import play.libs.Akka
import actors.user.UsersManager

object Global extends GlobalSettings {
  override def onStart(app: Application) = {
    val actorRef = Akka.system().actorOf(UsersManager.props, name = "usersManager")
    super.onStart(app)
  }
}