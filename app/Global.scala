import play.api.GlobalSettings
import play.api.Application
import play.libs.Akka
import actors.Supervisor

object Global extends GlobalSettings {
  override def onStart(app: Application) = {
    Akka.system().actorOf(Supervisor.props, name = "supervisor")
    super.onStart(app)
  }
}