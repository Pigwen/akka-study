package org.maodian.akka.ch07

import org.maodian.akka.ch03.StopSystemAfterAll
import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import akka.actor.ActorSystem
import akka.testkit.TestKit
import akka.testkit.TestProbe
import akka.actor.Props
import akka.routing.RoundRobinRouter
import scala.concurrent.duration._
import akka.actor.ActorRef
import akka.routing.SmallestMailboxRouter

class PerformanceTest extends TestKit(ActorSystem("photo-message-test")) with WordSpec with MustMatchers with StopSystemAfterAll {
  "Round Robin router" must {
    "dispatch task equally" in {
      val pipe = TestProbe()
      val router = system.actorOf(Props(new GetLicence("11", pipe.ref)).withRouter(RoundRobinRouter(nrOfInstances = 5)))
      val message = PerformanceRoutingMessage("abc", None, None)
      for (i <- 0 until 10) {
        router ! message
      }

      val processedMessages = pipe.receiveN(10, 5 seconds).collect {
        case m: PerformanceRoutingMessage => m
      }
      processedMessages.size must be(10)
      val grouped = processedMessages.groupBy(_.processedBy)
      grouped.values.foreach(listProcessedByOneActor =>
        listProcessedByOneActor must have size (10))
    }
  }
  
  "SmallestMailboxRouter" must {
    "dispatch task according to the receiver's mailbox" in {
      val endProbe = TestProbe()
      val actor1 = system.actorOf(Props(new GetLicence("250", endProbe.ref)), "250")
      val actor2 = system.actorOf(Props(new GetLicence("500", endProbe.ref)), "500")
      val routees = Vector[ActorRef](actor1, actor2)
      val router = system.actorOf(Props(new GetLicence("500", endProbe.ref)).withRouter(SmallestMailboxRouter(routees = routees)))
      val message = PerformanceRoutingMessage("abc", None, None)
      for (i <- 0 until 10) {
        router ! message
      }

      val processedMessages = endProbe.receiveN(10, 5 seconds).collect {
        case m: PerformanceRoutingMessage => 
          println(m.processedBy)
          m
      }
      processedMessages.size must be(10)
      val grouped = processedMessages.groupBy(_.processedBy)
      val msgProcessedByActor1 = grouped.get(Some("250")).getOrElse(Seq())
      val msgProcessedByActor2 = grouped.get(Some("500")).getOrElse(Seq())
      msgProcessedByActor1.size must be > (msgProcessedByActor2.size)
    }
  }
}