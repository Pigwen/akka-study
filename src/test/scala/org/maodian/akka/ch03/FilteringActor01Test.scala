package org.maodian.akka.ch03

import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import akka.actor.Props
import FilteringActorProtocol._

class FilteringActor01Test extends TestKit(ActorSystem("filtering-actor-test"))
  with WordSpec with MustMatchers with StopSystemAfterAll {

  "A Filtering Actor" must {
    "filter out particular messages" in {
      val filter = system.actorOf(Props(new FilteringActor(testActor, 5)), "filter-1")
      filter ! Event(1)
      filter ! Event(2)
      filter ! Event(1)
      filter ! Event(3)
      filter ! Event(1)
      filter ! Event(4)
      filter ! Event(5)
      filter ! Event(5)
      filter ! Event(6)

      val eventIds = receiveWhile() {
        case Event(id) if id <= 5 => id
      }
      
      eventIds must be (Seq(1, 2, 3, 4, 5))
      expectMsg(Event(6))
    }
    
    "filter out particular messages using expectNoMsg" in {
      val filter = system.actorOf(Props(new FilteringActor(testActor, 5)))
      filter ! Event(1)
      filter ! Event(2)
      expectMsg(Event(1))
      expectMsg(Event(2))
      expectNoMsg
      filter ! Event(3)
      expectMsg(Event(3))
      expectNoMsg
    }
  }
}