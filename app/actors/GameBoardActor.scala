package actors

import akka.actor.Actor
import models.Game
import akka.actor.Props

class GameBoardActor extends ObservableActor {
  import GameBoardActor._

  var game = Game()

  def receive: Receive = listen orElse {
    case Get =>
      sender ! game
  }
}

object GameBoardActor {
  case object Get
  
  def props = Props[GameBoardActor]
} 