package org.maodian.akka.ch05

import akka.kernel.Bootable
import akka.actor.ActorSystem
import akka.actor.Props
import scala.concurrent.duration._

class BootHello extends Bootable {
  val system = ActorSystem("hellokernel")
  
  def startup = {
    val actor = system.actorOf(Props[HelloWorld])
    val config = system.settings.config
    val timer = config.getInt("helloworld.timer")
    system.actorOf(Props(new HelloWorldCaller(timer.millis, actor)))
  }
  
  def shutdown = {
    system.shutdown
  }
}