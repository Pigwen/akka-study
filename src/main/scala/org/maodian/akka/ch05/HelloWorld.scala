package org.maodian.akka.ch05

import akka.actor.Actor
import akka.actor.ActorLogging

class HelloWorld extends Actor with ActorLogging {
  def receive = {
    case msg: String =>
      val hello = s"Hello ${msg}"
      sender ! hello
      log.info("Sent response {}", hello)
  }
}