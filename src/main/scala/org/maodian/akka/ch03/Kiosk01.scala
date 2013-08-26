package org.maodian.akka.ch03

import akka.actor.Actor
import akka.actor.ActorRef

class Kiosk01(next: ActorRef) extends Actor {
  import Kiosk01Protocol._
  
  def receive = {
    case game @ Game(_, tickets) => next ! game.copy(tickets = tickets.tail)
  }
}

object Kiosk01Protocol {
  case class Ticket(seat: Int)
  case class Game(name: String, tickets: Seq[Ticket])
}