package org.maodian.akka.ch07

import akka.actor.Actor
import akka.actor.ActorRef

case class PerformanceRoutingMessage(photo: String, licence: Option[String], processedBy: Option[String])

class GetLicence(name: String, pipe: ActorRef) extends Actor {
  def receive = {
    case m: PerformanceRoutingMessage => 
      pipe ! m.copy(licence = Some("licence"), processedBy = Some(name))
  }
}