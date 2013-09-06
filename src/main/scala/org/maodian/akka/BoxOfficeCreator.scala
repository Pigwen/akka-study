package org.maodian.akka

import com.typesafe.config.ConfigFactory
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.ActorLogging
import scala.concurrent.duration._
import akka.actor.Identify
import akka.actor.ActorIdentity
import akka.actor.Terminated
import akka.actor.ReceiveTimeout
import akka.actor.Terminated

trait BoxOfficeCreator { this: Actor =>
  def creatBoxOffice: ActorRef = context.actorOf(Props[BoxOffice])
}

trait ConfiguredRemoteBoxOfficeDeployment extends BoxOfficeCreator { this: Actor =>
  override def creatBoxOffice: ActorRef = context.actorOf(Props[RemoteOfficeBoxFowarder], "forwarder")
}

object RemoteBoxOfficeCreator {
  val config = ConfigFactory.load("frontend").getConfig("backend")
  val protocol = config.getString("protocol")
  val host = config.getString("host")
  val port = config.getInt("port")
  val systemName = config.getString("system")
  val actor = config.getString("actor")
}

trait RemoteBoxOfficeCreator extends BoxOfficeCreator { this: Actor =>
  import RemoteBoxOfficeCreator._
  def path = s"${protocol}://${systemName}@${host}:${port}/${actor}"

  override def creatBoxOffice = context.actorOf(Props(classOf[RemoteLookup], path), "lookupBoxOffice")
}

class RemoteOfficeBoxFowarder extends Actor with ActorLogging {
  context.setReceiveTimeout(3.seconds)
  deployAndWatch

  def deployAndWatch = {
    val actorRef = context.actorOf(Props[BoxOffice], "boxOffice")
    context.watch(actorRef)
    log.info("switching to maybe active state")
    context.become(maybeActive(actorRef))
    context.setReceiveTimeout(Duration.Undefined)
  }

  def deploying: Receive = {
    case ReceiveTimeout =>
      deployAndWatch
    case msg: Any =>
      log.error(s"Ignoring message $msg, not ready yet.")
  }

  def maybeActive(actor: ActorRef): Receive = {
    case Terminated(actorRef) =>
      log.info(s"Actor $actorRef terminated.")
      log.info("switching to deploying state")
      context.become(deploying)
      context.setReceiveTimeout(3.seconds)
      deployAndWatch
    case msg: Any =>
      actor.forward(msg)
  }

  def receive = deploying
}

class RemoteLookup(path: String) extends Actor with ActorLogging {
  context.setReceiveTimeout(3.seconds)
  sendIdentityRequest

  def sendIdentityRequest: Unit = {
    val selection = context.actorSelection(path)
    selection ! Identify(path)
  }

  def identify: Receive = {
    case ActorIdentity(_, Some(actor)) =>
      context.setReceiveTimeout(Duration.Undefined)
      log.info("switch to active state")
      context.become(active(actor))
      context.watch(actor)
    case ActorIdentity(_, None) =>
      log.error(s"Remote actor with path $path is not available.")
    case ReceiveTimeout =>
      sendIdentityRequest
    case msg: Any =>
      log.error(s"Ignoring message $msg, not ready yet.")
  }

  def active(actor: ActorRef): Receive = {
    case Terminated(actorRef) =>
      log.info(s"Actor $actorRef terminated")
      context.become(identify)
      log.info("switch to identify state")
      context.setReceiveTimeout(3.seconds)
      sendIdentityRequest
    case msg: Any => actor.forward(msg)
  }

  def receive = identify
}