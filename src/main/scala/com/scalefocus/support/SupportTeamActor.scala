package com.scalefocus.support

import akka.actor.SupervisorStrategy.{Escalate, Restart, Stop}
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, Props, SupervisorStrategy, Terminated}
import com.scalefocus.TicketMessages.{Ticket, TicketCompleted, TicketFailed}
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime

class SupportTeamActor(category: String, workerProps: Props, numberOfWorkers: Int) extends Actor with ActorLogging {
  private var nextWorker = 0
  private val maxRetries = 0
  private val retryInterval = 10.seconds
  private var retryCount = Map.empty[String, Int]

  // Create worker actors
  private var workers: Seq[ActorRef] = (1 to numberOfWorkers).map { i =>
    val actorRef = context.actorOf(workerProps, s"worker-$i")
    context.watch(actorRef)
    actorRef
  }

  private def createWorker(index: Int): ActorRef = {
    val worker = context.actorOf(workerProps, s"worker-$index")
    context.watch(worker)
    worker
  }



  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy(
    maxNrOfRetries = maxRetries,
    withinTimeRange = 20.seconds
  ) {
    case exception : RuntimeException =>
      log.error(s"Restarting child due to RuntimeException with value: $exception")
      Restart
    case _: Exception => Stop
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

    case Terminated(worker) =>
      println(s"Worker ${worker.path} terminated, replacing it")
      val index = workers.indexOf(worker)
      val newWorker = createWorker(index + 1)
      workers = workers.updated(index, newWorker)

    case other =>
      log.info(s"Handling exception: $other")
  }
}

object SupportTeamActor {
  def props(category: String, workerProps: Props, numberOfWorkers: Int): Props = Props(new SupportTeamActor(category, workerProps, numberOfWorkers))
}