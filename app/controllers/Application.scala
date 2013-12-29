package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {
  var currentId = 0
  def nextId = {
    val ans = currentId
    currentId += 1
    ans
  }

  def index = Action {
    Ok(views.html.index())
  }

  def newGame = Action {
    Redirect(routes.Application.game(nextId))
  }

  def game(id: Int) = Action {
    Ok(views.html.game(id))
  }
}