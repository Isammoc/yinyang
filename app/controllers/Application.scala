package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import actors.user.UsersManager
import akka.pattern.ask
import play.api.mvc.Controller
import play.libs.Akka
import play.api.data._
import play.api.data.Forms._
import actors.game.GamesManager
import actors.ActionsActor
import scala.concurrent.Future
import models.Game

object Application extends Controller {

  def actionsRef = Akka.system.actorSelection("user/actions")

  def index = Authenticated.async { implicit request =>
    (actionsRef ? ActionsActor.ListWaitingGames).mapTo[List[Game]].map(games => 
    Ok(views.html.index(games))
    )
  }

  def newGame = Authenticated.async { implicit request =>
    (actionsRef ? ActionsActor.NewGame(request.currentUser)).map ( _ => Redirect(routes.Application.index))
  }

  def changeNickname = Authenticated.async { implicit request =>
    val userForm = Form(single("nickname" -> nonEmptyText))
    (actionsRef ? ActionsActor.ChangeNickname(request.currentUser, userForm.bindFromRequest.get)).map(_ => Redirect(routes.Application.index))
  }
}
