package org.maodian.akka

import spray.json.DefaultJsonProtocol
import akka.actor.Actor
import scala.collection.immutable.Vector
import akka.actor.PoisonPill

class TicketSeller extends Actor {
  import TicketProtocol._
  
  var tickets = Vector[Ticket]()
  
  def receive = {
    case Tickets(newTickets) => tickets ++= newTickets
    case GetEvents => sender ! tickets.size
    case BuyTicket => {
      if (tickets.isEmpty) {
        sender ! SoldOut
        self ! PoisonPill
      }
      tickets.headOption.foreach {ticket => 
        tickets = tickets.tail
        sender ! ticket
      }
    }
  }
}

object TicketProtocol {
  case class Event(event: String, nrOfTickets: Int)
  case object GetEvents
  case class Events(events: List[Event])
  case object EventCreated
  case class TicketRequest(event: String)
  case object SoldOut
  case class Ticket(event: String, nr: Int)
  case class Tickets(ticket: List[Ticket])
  case object BuyTicket

  //----------------------------------------------
  // JSON
  //----------------------------------------------

  object Event extends DefaultJsonProtocol {
    implicit val format = jsonFormat2(Event.apply)
  }

  object TicketRequest extends DefaultJsonProtocol {
    implicit val format = jsonFormat1(TicketRequest.apply)
  }

  object Ticket extends DefaultJsonProtocol {
    implicit val format = jsonFormat2(Ticket.apply)
  }
}