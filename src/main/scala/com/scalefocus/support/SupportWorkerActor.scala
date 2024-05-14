package com.scalefocus.support

import akka.actor.{Actor, ActorLogging, Props, Terminated}
import com.scalefocus.TicketMessages.{Ticket, TicketCompleted, TicketFailed}

class SupportWorkerActor extends Actor with ActorLogging {
  val workerId: String = self.path.name

  override def postStop(): Unit = {
    println(s"Stopping ${self.path}")
  }

  override def receive: Receive = {
    case Ticket(ticketId, category, details) =>
        if (details.contains("fail")) {
          throw new RuntimeException(details)
        } else {
          val result = s"Resolved ticket in $category: $details"
          log.info(s"$workerId completed ticket $ticketId")
          sender() ! TicketCompleted(workerId, ticketId, result)
        }

    case Terminated(worker) =>
      println(s"Terminated worker: $worker")

    case other =>
      println(s"Received other: $other")
  }

  def props(): Props = Props[SupportWorkerActor]
}
