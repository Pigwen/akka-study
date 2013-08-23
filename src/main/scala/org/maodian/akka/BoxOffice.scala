package org.maodian.akka

import akka.actor.Actor
import org.maodian.akka.TicketProtocol._
import akka.actor.Props
import akka.actor.ActorRef
import akka.actor.ActorLogging
import akka.pattern.ask
import scala.concurrent.duration._
import akka.util.Timeout
import scala.concurrent.Future

class BoxOffice extends Actor with CreateTicketSellers with ActorLogging {
  implicit val timeout = Timeout(5.seconds)
  implicit val ec = context.dispatcher

  def receive = {
    case Event(name, nrOfTickets) =>
      log.info(s"creating new event ${name} with ${nrOfTickets} tickets")
      if (context.child(name).isEmpty) {
        val ticketSeller = createTicketSeller(name)
        val tickets = Tickets((1 to nrOfTickets).map(Ticket(name, _)).toList)
        ticketSeller ! tickets
      }
      sender ! EventCreated
    case GetEvents =>
      log.info(s"getting all events")
      def askForEvent(actor: ActorRef) = {
        val futureInt = actor.ask(GetEvents).mapTo[Int]
        futureInt.map(Event(actor.actorRef.path.name, _))
      }
      val futures = context.children.map(askForEvent(_))
      val cachedSender = sender
      Future.sequence(futures).map(events => cachedSender ! Events(events.toList))
    case TicketRequest(eventName) =>
      log.info(s"Getting a ticket for the ${eventName} event.")
      context.child(eventName) match {
        case Some(child) => child.forward(BuyTicket)
        case None => sender ! SoldOut
      }
  }
}

trait CreateTicketSellers { self: Actor =>
  def createTicketSeller(name: String) = context.actorOf(Props[TicketSeller], name)
}