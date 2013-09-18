package org.maodian.akka.ch09

import akka.testkit.TestKit
import org.scalatest.matchers.MustMatchers
import org.maodian.akka.ch03.StopSystemAfterAll
import org.scalatest.WordSpec
import akka.actor.ActorSystem
import akka.testkit.TestProbe
import scala.concurrent.duration._

class OrderMessageBusTest extends TestKit(ActorSystem("order-event-bus-test")) with WordSpec with MustMatchers with StopSystemAfterAll {
  "OrderEventBus" must {
    "publish event to subscriber according number of books" in {
      val bus = new OrderMessageBus
      val singleBooks = TestProbe()
      bus.subscribe(singleBooks.ref, false)
      val multiBooks = TestProbe()
      bus.subscribe(multiBooks.ref, true)
      val msg = new bus.Order("me", "Akka in Action", 1)
      bus.publish(msg)
      singleBooks.expectMsg(msg)
      multiBooks.expectNoMsg(3.seconds)
      val msg2 = new bus.Order("me", "Akka in Action", 3)
      bus.publish(msg2)
      singleBooks.expectNoMsg(3.seconds)
      multiBooks.expectMsg(msg2)
    }
  }
}