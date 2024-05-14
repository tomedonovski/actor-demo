package com.scalefocus.support

import akka.actor.{Actor, ActorLogging, Props}
import com.scalefocus.TicketMessages.{Ticket, TicketCompleted, TicketFailed}

class SupportWorkerActor extends Actor with ActorLogging {
  val workerId: String = self.path.name

  override def receive: Receive = {
    case Ticket(ticketId, category, details) =>
        if (details.contains("fail")) {
          throw new RuntimeException("Simulated failure")
        } else {
          val result = s"Resolved ticket in $category: $details"
          log.info(s"$workerId completed ticket $ticketId")
          sender() ! TicketCompleted(workerId, ticketId, result)
        }

    case other =>
      println(s"Received other: $other")
  }

  def props(): Props = Props[SupportWorkerActor]
}
