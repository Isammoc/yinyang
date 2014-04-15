package controllers

import scala.concurrent.ExecutionContext.Implicits.global

import actors.user.UsersManager
import akka.pattern.ask
import play.api.mvc.Controller
import play.libs.Akka
import play.api.data._
import play.api.data.Forms._

object Application extends Controller {

  def usersManagerRef = Akka.system.actorSelection("user/usersManager")

  def index = Authenticated { implicit request =>
    Ok(views.html.index())
  }

  def changeNickname = Authenticated.async { implicit request =>
    val userForm = Form(single("nickname" -> nonEmptyText))
    (usersManagerRef ? UsersManager.ChangeNickname(request.currentUser, userForm.bindFromRequest.get)).map(_ => Redirect(routes.Application.index))
  }
}
