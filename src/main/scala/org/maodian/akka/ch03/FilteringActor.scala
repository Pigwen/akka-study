package org.maodian.akka.ch03

import akka.actor.Actor
import akka.actor.ActorRef

class FilteringActor(nextActor: ActorRef, bufferSize: Int) extends Actor {
  import FilteringActorProtocol._
  var lastMessages = Vector[Event]()
  
  def receive = {
    case event: Event =>
      if (!lastMessages.contains(event)) {
        lastMessages = lastMessages :+ event
        nextActor ! event
      }
      if (lastMessages.size > bufferSize) {
        lastMessages = lastMessages.tail
      }
  }
}

object FilteringActorProtocol {
  case class Event(id: Long)
}