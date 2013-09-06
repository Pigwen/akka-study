package org.maodian.akka

import scala.concurrent.duration.DurationInt

import org.maodian.akka.TicketProtocol.Event
import org.maodian.akka.TicketProtocol.EventCreated
import org.maodian.akka.TicketProtocol.Events
import org.maodian.akka.TicketProtocol.GetEvents
import org.maodian.akka.TicketProtocol.SoldOut
import org.maodian.akka.TicketProtocol.Ticket
import org.maodian.akka.TicketProtocol.TicketRequest

import akka.actor.Actor
import akka.actor.PoisonPill
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout
import spray.http.StatusCodes
import spray.httpx.SprayJsonSupport.sprayJsonMarshaller
import spray.httpx.SprayJsonSupport.sprayJsonUnmarshaller
import spray.routing.Directive.pimpApply
import spray.routing.HttpService
import spray.routing.RequestContext
import spray.routing.Route

class RestInterface extends Actor
  with RestApi with BoxOfficeCreator {
  def actorRefFactory = context
  def receive = runRoute(routes)
}

class RemoteRestInterface extends RestInterface with ConfiguredRemoteBoxOfficeDeployment {
  
}

trait RestApi extends HttpService { actor: Actor with BoxOfficeCreator =>
  implicit val timeout = Timeout(5.seconds)
  implicit val ec = actorRefFactory.dispatcher
  val boxOffice = creatBoxOffice

  def routes: Route =
    path("events") {
      put {
        entity(as[Event]) { event =>
          ctx =>
            val responder = createResponder(ctx)
            boxOffice.ask(event).pipeTo(responder)
        }
      } ~
        get { ctx =>
          val responder = createResponder(ctx)
          boxOffice.ask(GetEvents).pipeTo(responder)
        }
    } ~
      path("ticket" / Segment) { eventName => ctx =>
        val ticketRequest = TicketRequest(eventName)
        val responder = createResponder(ctx)
        boxOffice.ask(ticketRequest).pipeTo(responder)
      }

  def createResponder(ctx: RequestContext) = context.actorOf(Props(new Responder(ctx)))
}

class Responder(ctx: RequestContext) extends Actor {
  def receive = {
    case EventCreated =>
      ctx.complete(StatusCodes.OK)
      self ! PoisonPill
    case Events(events) =>
      ctx.complete(StatusCodes.OK, events)
      self ! PoisonPill
    case ticket: Ticket =>
      ctx.complete(StatusCodes.OK, ticket)
      self ! PoisonPill
    case SoldOut =>
      ctx.complete(StatusCodes.NotFound)
      self ! PoisonPill
  }
}