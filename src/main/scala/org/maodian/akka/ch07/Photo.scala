package org.maodian.akka.ch07

import akka.actor.ActorRef
import akka.actor.Actor

case class Photo(licence: String, speed: Int)

class SpeedFilter(minSpeed: Int, pipe: ActorRef) extends Actor {
  def receive = {
    case p @ Photo(_, speed) if (speed >= minSpeed) => pipe ! p
  }
}

class LicenceFilter(pipe: ActorRef) extends Actor {
  def receive = {
    case p @ Photo(licence, _) if (!licence.isEmpty()) => pipe ! p
  }
}