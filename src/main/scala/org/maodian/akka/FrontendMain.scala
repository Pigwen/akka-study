package org.maodian.akka

import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.actor.Props
import akka.io.IO
import spray.can.Http

object FrontendMain extends App {
  val config = ConfigFactory.load("frontend")
  implicit val system = ActorSystem("frontend", config)
  val host = config.getString("http.host")
  val port = config.getInt("http.port")
  var restInterface = system.actorOf(Props[RemoteRestInterface], "http-service")
  IO(Http) ! Http.Bind(restInterface, host, port)
}