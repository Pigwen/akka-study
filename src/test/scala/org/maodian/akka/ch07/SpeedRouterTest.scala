package org.maodian.akka.ch07

import akka.testkit.TestKit
import org.scalatest.matchers.MustMatchers
import org.maodian.akka.ch03.StopSystemAfterAll
import org.scalatest.WordSpec
import akka.actor.ActorSystem
import akka.testkit.ExtractRoute
import akka.actor.Props
import akka.testkit.TestProbe
import akka.actor.Actor
import akka.actor.ActorRef
import scala.concurrent.duration._

class RedirectActor(pipe: ActorRef) extends Actor {
  println("RedirectActor instance created")
  def receive = {
    case msg: AnyRef => {
      pipe ! msg
    }
  }
}

class SpeedRouterTest extends TestKit(ActorSystem("speed-router-test")) with WordSpec with MustMatchers with StopSystemAfterAll {
  "a speed router" must {
    "route task according to speed" in {
      val normalFlow = TestProbe()
      val cleanUp = TestProbe()
      val router = system.actorOf(Props.empty.withRouter(new SpeedRouter(50, Props(new RedirectActor(normalFlow.ref)), Props(new RedirectActor(cleanUp.ref)))))
      val msg = new Photo("123xyz", speed = 60)
      router ! msg

      normalFlow.expectMsg(msg)
      cleanUp.expectNoMsg(1.seconds)

      val msg2 = new Photo("234", 45)
      router ! msg

      cleanUp.expectMsg(msg)
      normalFlow.expectNoMsg(1.seconds)
    }
  }
}