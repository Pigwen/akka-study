package org.maodian.akka.ch07

import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import org.maodian.akka.ch03.StopSystemAfterAll
import akka.testkit.TestProbe
import akka.actor.Props
import scala.concurrent.duration._

class PhotoTest extends TestKit(ActorSystem("photo-test")) with WordSpec with MustMatchers with StopSystemAfterAll {
  "a photo filter chain" must {
    "filter photo" in {
      val testProbe = TestProbe()
      val licenceFilter = system.actorOf(Props(new LicenceFilter(testProbe.ref)))
      val speedFilter = system.actorOf(Props(new SpeedFilter(50, licenceFilter)))

      val p1 = Photo("abc", 100)
      speedFilter ! p1
      testProbe.expectMsg(p1)

      val p2 = Photo("abc", 40)
      speedFilter ! p2
      testProbe.expectNoMsg(1.seconds)

      val p3 = Photo("", 100)
      speedFilter ! p3
      testProbe.expectNoMsg(1.seconds)
    }
  }
}