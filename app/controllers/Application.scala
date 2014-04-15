package controllers

import play.api.mvc._

object Application extends Controller {

  def index = Authenticated { implicit request =>
    Ok(views.html.index())
  }


}
