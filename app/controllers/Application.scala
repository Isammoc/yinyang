package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import akka.pattern.ask
import play.api.mvc.Controller
import play.libs.Akka
import play.api.data._
import play.api.data.Forms._
import scala.concurrent.Future
import play.api.mvc.Action

object Application extends Controller {

  def actionsRef = Akka.system.actorSelection("user/actions")

  def index = Action { implicit request =>
    Ok(views.html.main())
  }
}
