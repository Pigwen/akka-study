package org.maodian.akka

import akka.actor.ActorSystem
import akka.actor.Props
import com.typesafe.config.ConfigFactory

object BackendMain extends App {
  val config = ConfigFactory.load("backend")
  val system = ActorSystem("backend", config)
}