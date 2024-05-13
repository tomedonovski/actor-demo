package com.scalefocus.support

import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor.{Actor, ActorRef, OneForOneStrategy, Props, SupervisorStrategy}
import com.scalefocus.TicketMessages.{Ticket, TicketCompleted, TicketFailed}

class SupportTeamActor(category: String, workers: Seq[ActorRef]) extends Actor {
  private var nextWorker = 0
  private val maxRetries = 3
  private val retryInterval = scala.concurrent.duration.Duration("10 seconds")
  private var retryCount = Map.empty[String, Int]

  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy(maxNrOfRetries = maxRetries) {
    case _: Exception => Restart
    case _ => Stop
  }

  override def receive: Receive = {
    case ticket @ Ticket(_, ticketCategory, _)
      if ticketCategory == category =>
      val worker = workers(nextWorker)
      nextWorker = (nextWorker + 1) % workers.size
      worker ! ticket

    case TicketCompleted(workerId, ticketId, result) =>
      retryCount -= ticketId
      println(s"$category Team: Worker $workerId completed ticket $ticketId with result: $result")

    case TicketFailed(workerId, ticketId, reason) =>
      println(s"$category Team: Worker $workerId failed ticket $ticketId due to $reason")
      val retryAttempts = retryCount.getOrElse(ticketId, 0)
      if (retryAttempts < maxRetries) {
        retryCount += (ticketId -> (retryAttempts + 1))
        self ! Ticket(ticketId, category, s"Retry for task that failed: $reason")
      } else {
        println(s"Exceeded max retries for ticket $ticketId")
      }
  }

}

object SupportTeamActor {
  def props(category: String, workers: Seq[ActorRef]): Props = Props(new SupportTeamActor(category, workers))
}
