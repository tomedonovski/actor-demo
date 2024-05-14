package com.scalefocus.supervision

import akka.actor.{Actor, ActorLogging, Props}

class Worker extends Actor with ActorLogging {

  override def receive: Receive = {
    case "fail" =>
      log.info("Worker received 'fail', throwing RuntimeException")
      throw new RuntimeException("Simulated failure")
    case "fatal" =>
      log.info("Worker received 'fatal', throwing IllegalArgumentException")
      throw new IllegalArgumentException("Simulated fatal failure")
    case msg: String =>
      log.info(s"Worker received message: $msg")
  }
}

object Worker {
  def props(): Props = Props(new Worker)
}
