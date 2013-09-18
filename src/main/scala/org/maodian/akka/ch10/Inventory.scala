package org.maodian.akka.ch10

import scala.actors.Actor
import akka.actor.FSM

class Inventory extends Actor with FSM[State, StateData] {
  startWith(WaitForRequests, StateData(0, Vector()))

  whenUnhandled {
    case Event(request: BookRequest, data: StateData) => {
      stay using (data.copy(pendingRequests = data.pendingRequests :+ request))
    }
    case Event(e, s) => {
      log.warning("received unhandled request {} in state {}/{}", e, stateName, s)
      stay
    }
  }

  when(WaitForRequests) {
    case Event(request: BookRequest, data: StateData) => {
      val newData = data.copy(pendingRequests = data.pendingRequests :+ request)
      if (newData.nrBooksInStore > 0) {
        goto(ProcessRequest) using (newData)
      } else {
        goto(WaitForPublisher) using (newData)
      }
    }
    case Event(PendingRequests, data: StateData) => {
      if (data.pendingRequests.isEmpty) {
        stay
      } else if (data.nrBooksInStore > 0) {
        goto(ProcessRequest)
      } else {
        goto(WaitForPublisher)
      }
    }
  }

  when(WaitForPublisher) {
    case Event(supply: BookSupply, data: StateData) =>
      goto(ProcessRequest) using (data.copy(nrBooksInStore = supply.nrBooks))
    case Event(BookSupplySoldOut, _) =>
      goto(ProcessSoldOut)
  }

  when(ProcessRequest) {
    case Event(Done, data: StateData) => {
      goto(WaitForRequests) using (
        data.copy(nrBooksInStore = data.nrBooksInStore - 1,
          pendingRequests = data.pendingRequests.tail))
    }
  }

  when(SoldOut) {
    case Event(request: BookRequest, data: StateData) => {
      goto(ProcessSoldOut) using (StateData(0, Vector(request)))
    }
  }

  when(ProcessSoldOut) {
    case Event(Done, data: StateData) => {
      goto(SoldOut) using StateData(0, Vector())
    }
  }
}

sealed trait State
case object WaitForRequests extends State
case object ProcessRequest extends State
case object WaitForPublisher extends State
case object SoldOut extends State
case object ProcessSoldOut extends State

case class StateData(nrBooksInStore: Int, pendingRequests: Seq[BookRequest])
case class BookRequest
case class PendingRequests
case class BookSupply(nrBooks: Int)
case object BookSupplySoldOut
case object Done