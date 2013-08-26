package org.maodian.akka.ch03

import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import akka.actor.Props

class SendingActor01Test extends TestKit(ActorSystem("sending-actor-test")) 
  with WordSpec with MustMatchers with StopSystemAfterAll {

  "A Sending Actor" must {
    "send a message to an actor when it has finished" in {
      import Kiosk01Protocol._
      val sendingActor = system.actorOf(Props(new Kiosk01(testActor)))
      val game = Game("Lakers vs Bull", Vector(Ticket(1), Ticket(2), Ticket(3)))
      sendingActor ! game 
      expectMsgPF() {
        case Game(_, tickets) => tickets.size must be (game.tickets.size - 1)
      }
    }
  }
}