package org.maodian.akka.ch07

import akka.testkit.TestKit
import org.scalatest.matchers.MustMatchers
import org.maodian.akka.ch03.StopSystemAfterAll
import org.scalatest.WordSpec
import akka.actor.ActorSystem
import akka.testkit.TestProbe
import akka.actor.Props
import scala.concurrent.duration._
import java.util.Date

class PhotoMessageTest extends TestKit(ActorSystem("photo-message-test")) with WordSpec with MustMatchers with StopSystemAfterAll {
  "" must {
    "" in {
      val endProbe = TestProbe()
      val aggregateRef = system.actorOf(
        Props(new Aggregator(1 second, endProbe.ref)))
      val speedRef = system.actorOf(
        Props(new GetSpeed(aggregateRef)))
      val timeRef = system.actorOf(
        Props(new GetTime(aggregateRef)))
      val actorRef = system.actorOf(
        Props(new RecipientList(Seq(speedRef, timeRef))))
      val photoDate = new Date()
      val msg = PhotoMessage("id1", "p1")
      actorRef ! msg
      val combinedMsg = PhotoMessage(msg.id,
        msg.photo,
        Some(photoDate),
        Some(80))
      endProbe.expectMsg(combinedMsg)
    }
  }
}