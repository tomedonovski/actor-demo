package com.scalefocus.supervision

import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor.{Actor, ActorLogging, OneForOneStrategy, Props, SupervisorStrategy}
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime

class Supervisor extends Actor with ActorLogging {

  override val supervisorStrategy: SupervisorStrategy = OneForOneStrategy(maxNrOfRetries = 1, withinTimeRange = 1.minute) {
    case _: RuntimeException =>
      log.error("Restarting child due to RuntimeException")
      Restart
    case _: IllegalArgumentException =>
      log.error("Stopping child due to IllegalArgumentException")
      Stop
    case _: Exception =>
      log.error("Escalating due to unknown exception")
      SupervisorStrategy.Escalate
  }

  override def preStart(): Unit = {
    // Create a worker actor as a child
    val worker = context.actorOf(Worker.props(), "worker")
    context.watch(worker)
  }

  def receive: Receive = {
    case msg: String =>
      context.child("worker").foreach(_ ! msg)
  }
}

object Supervisor {
  def props(): Props = Props(new Supervisor)
}


