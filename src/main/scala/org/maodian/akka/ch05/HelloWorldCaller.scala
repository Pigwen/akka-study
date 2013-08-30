package org.maodian.akka.ch05

import scala.concurrent.duration.FiniteDuration

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.actorRef2Scala

class HelloWorldCaller(time: FiniteDuration, actor: ActorRef) extends Actor with ActorLogging {
  case class TimerTick(msg: String)
  implicit val ec = context.dispatcher

  override def preStart = {
    context.system.scheduler.schedule(time, time, self, new TimerTick("everybody"));
  }

  def receive = {
    case TimerTick(msg) => actor ! msg
    case msg: String => log.info("received {}", msg)
  }
}