import scala.annotation.implicitNotFound
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import actors.user.UsersManager
import akka.pattern.ask
import akka.util.Timeout
import play.api.mvc.ActionBuilder
import play.api.mvc.Request
import play.api.mvc.SimpleResult
import play.api.mvc.WrappedRequest
import play.libs.Akka
import models.User
import models.SimpleStatistics

package object controllers {
  implicit val _ = Timeout(3 seconds)

  trait ViewContext {
    val currentUser: User
    val stats: SimpleStatistics
  }

  def usersManagerRef = Akka.system.actorSelection("user/usersManager")

  class AuthenticatedRequest[A](val currentUser: User, val stats: SimpleStatistics, request: Request[A]) extends WrappedRequest[A](request) with ViewContext

  object Authenticated extends ActionBuilder[AuthenticatedRequest] {
    def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[SimpleResult]) = {
      for {
        currentUser <- request.session.get("userId")
          .fold(usersManagerRef ? UsersManager.New)(userId => usersManagerRef ? UsersManager.GetOrConnectUser(userId.toLong))
          .mapTo[User]
        usersStats <- (usersManagerRef ? UsersManager.UsersStat).mapTo[UsersManager.UsersStat]
        stats = SimpleStatistics(usersStats.connectedCount)
        simpleResult <- block(new AuthenticatedRequest(currentUser, stats, request))
          .map(_.withSession(request.session + ("userId" -> currentUser.id.toString)))
      } yield simpleResult
    }
  }
}