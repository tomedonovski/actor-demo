package com.scalefocus.support

import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, Props, SupervisorStrategy}
import com.scalefocus.TicketMessages.{Ticket, TicketCompleted, TicketFailed}
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime

class SupportTeamActor(category: String, workerProps: Props, numberOfWorkers: Int) extends Actor with ActorLogging {
  private var nextWorker = 0
  private val maxRetries = 1
  private val retryInterval = 10.seconds
  private var retryCount = Map.empty[String, Int]

  // Create worker actors
  private val workers: Seq[ActorRef] = (1 to numberOfWorkers).map { i =>
    context.actorOf(workerProps, s"worker-$i")
  }

  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy(
    maxNrOfRetries = maxRetries,
    withinTimeRange = 1.minute
  ) {
    case exception : RuntimeException =>
      log.error(s"Restarting child due to RuntimeException with value: $exception")
      Restart
    case _: Exception => Escalate
  }

  override def receive: Receive = {
    case ticket @ Ticket(_, ticketCategory, _)
      if ticketCategory == category =>
      val worker = workers(nextWorker)
      nextWorker = (nextWorker + 1) % workers.size
      worker ! ticket

    case TicketCompleted(workerId, ticketId, result) =>
      retryCount -= ticketId
      log.info(s"$category Team: Worker $workerId completed ticket $ticketId with result: $result")

    case TicketFailed(workerId, ticketId, reason) =>
      log.info(s"$category Team: Worker $workerId failed ticket $ticketId due to $reason")
      val retryAttempts = retryCount.getOrElse(ticketId, 0)
      if (retryAttempts < maxRetries) {
        retryCount += (ticketId -> (retryAttempts + 1))
        val retryTicket = Ticket(ticketId, category, s"Retry for task that failed: $reason")
        log.info(s"Retrying ticket: $retryTicket")
//        context.system.scheduler.scheduleOnce(retryInterval, self, retryTicket)(context.dispatcher)
      } else {
        log.info(s"Exceeded max retries $retryCount for ticket $ticketId")
      }

    case other =>
      log.info(s"Handling exception: $other")
  }
}

object SupportTeamActor {
  def props(category: String, workerProps: Props, numberOfWorkers: Int): Props = Props(new SupportTeamActor(category, workerProps, numberOfWorkers))
}