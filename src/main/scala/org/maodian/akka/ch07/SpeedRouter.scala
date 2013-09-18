package org.maodian.akka.ch07

import akka.actor.Props
import akka.actor.SupervisorStrategy
import akka.dispatch.Dispatchers
import akka.routing.Destination
import akka.routing.RouteeProvider
import akka.routing.RouterConfig

class SpeedRouter(minSpeed: Int, normalFlow: Props, cleanUp: Props) extends RouterConfig {
  def routerDispatcher = Dispatchers.DefaultDispatcherId

  def supervisorStrategy = SupervisorStrategy.defaultStrategy

  def createRoute(routeeProvider: RouteeProvider) = {
    val normalFlowActor = routeeProvider.context.actorOf(normalFlow, "normal-flow")
    val cleanUpActor = routeeProvider.context.actorOf(cleanUp, "clean-up")
    routeeProvider.registerRoutees(Vector(normalFlowActor, cleanUpActor))

    {
      case (sender, message) =>
        message match {
          case photo: Photo =>
            println(photo.speed)
            if (photo.speed < minSpeed) {
              Vector(Destination(sender, cleanUpActor))
            } else {
              Vector(Destination(sender, normalFlowActor))
            }
        }
    }
  }
}