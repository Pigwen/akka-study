package org.maodian.akka

import akka.actor.ActorSystem
import akka.actor.Props
import spray.can.Http
import akka.io.IO
import com.typesafe.config.ConfigFactory

object Main extends App {
  implicit val system = ActorSystem("go-ticket")
  val config = ConfigFactory.load()
  val host = config.getString("http.host")
  val port = config.getInt("http.port")
  var restInterface = system.actorOf(Props[RestInterface], "http-service")
  IO(Http) ! Http.Bind(restInterface, host, port)
}