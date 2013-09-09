/**
 *
 */
package org.maodian.akka.ch07

import java.util.Date
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.actorRef2Scala
import scala.concurrent.duration.FiniteDuration
import scala.collection.mutable.ListBuffer

/**
 * @author mge
 *
 */
case class PhotoMessage(id: String,
  photo: String,
  createdTime: Option[Date] = None,
  speed: Option[Int] = None)

class GetTime(pipe: ActorRef) extends Actor {
  def receive = {
    case msg: PhotoMessage =>
      pipe ! msg.copy(createdTime = Some(new Date()))
  }
}

class GetSpeed(pipe: ActorRef) extends Actor {
  def receive = {
    case msg: PhotoMessage =>
      pipe ! msg.copy(speed = Some(80))
  }
}

class RecipientList(recipients: Seq[ActorRef]) extends Actor {
  def receive = {
    case msg: PhotoMessage => recipients.foreach(_ ! msg)
  }
}

case class TimeoutMessage(photoMsg: PhotoMessage)
class Aggregator(timeout: FiniteDuration, pipe: ActorRef) extends Actor {
  val messages = new ListBuffer[PhotoMessage]
  implicit val dispatcher = context.dispatcher

  override def preRestart(reason: Throwable, message: Option[Any]) = {
    super.preRestart(reason, message)
    messages.foreach(self ! _)
    messages.clear
  }

  def receive = {
    case msg: PhotoMessage =>
      messages.find(_.id == msg.id) match {
        case Some(alreadyRcvMsg) =>
          pipe ! alreadyRcvMsg.copy(
            createdTime = msg.createdTime orElse alreadyRcvMsg.createdTime,
            speed = msg.speed orElse alreadyRcvMsg.speed)
          messages -= alreadyRcvMsg
        case None =>
          messages += msg
          context.system.scheduler.scheduleOnce(timeout, self, TimeoutMessage(msg))
      }
    case timeoutMsg: TimeoutMessage =>
      messages.find(_.id == timeoutMsg.photoMsg.id) match {
        case Some(alreadyRcvMsg) =>
          pipe ! alreadyRcvMsg
          messages -= alreadyRcvMsg
        case None =>
      }
    case ex: Exception => throw ex // for debug
  }
}