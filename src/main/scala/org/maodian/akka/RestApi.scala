package org.maodian.akka

import akka.actor._
import spray.routing._
import spray.httpx.SprayJsonSupport._
import akka.actor.Props
import org.maodian.akka.TicketProtocol._
import spray.http.StatusCode
import spray.http.StatusCodes
import akka.actor.PoisonPill
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout
import scala.concurrent.duration._

class RestInterface extends Actor
  with RestApi {
  def actorRefFactory = context
  def receive = runRoute(routes)
}

trait RestApi extends HttpService { actor: Actor =>
  implicit val timeout = Timeout(5.seconds)
  implicit val ec = actorRefFactory.dispatcher
  val boxOffice = context.actorOf(Props[BoxOffice])

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