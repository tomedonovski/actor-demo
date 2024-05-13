package com.scalefocus.support

import akka.actor.{Actor, Props}
import com.scalefocus.TicketMessages.{Ticket, TicketCompleted, TicketFailed}

class SupportWorkerActor extends Actor {
  val workerId: String = self.path.name

  override def receive: Receive = {
    case Ticket(ticketId, category, details) =>
      try {
        if (details.contains("fail")) {
          sender ! TicketFailed(workerId, ticketId, details)
        } else {
          val result = s"Resolved ticket in $category: $details"
          println(s"$workerId completed ticket $ticketId")
          sender() ! TicketCompleted(workerId, ticketId, result)
        }
      } catch {
        case e: Exception =>
          sender() ! TicketFailed(workerId, ticketId, e.getMessage)
      }
  }

  def props(): Props = Props[SupportWorkerActor]
}
