package org.maodian.akka.ch09

import akka.event.EventBus
import akka.event.ActorEventBus
import akka.event.LookupClassification
import scala.actors.ActorRef

class OrderMessageBus extends EventBus with LookupClassification with ActorEventBus {
  type Classifier = Boolean
  type Event = Order
  
  def mapSize = 2
  
  protected def classify(event: Event): Boolean = {
    return event.num > 1
  }
  
  protected def publish(event: Event, subscriber: Subscriber) = {
    subscriber ! event
  }
  
  case class Order(receiver: String, title: String, num: Int)
}