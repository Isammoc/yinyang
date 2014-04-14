import play.api.GlobalSettings
import play.api.Application
import play.libs.Akka

object Global extends GlobalSettings {
  override def onStart(app: Application) = {
    super.onStart(app)
  }
}