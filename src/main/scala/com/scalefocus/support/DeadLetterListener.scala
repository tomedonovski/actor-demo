package com.scalefocus.support

import akka.actor.{Actor, ActorLogging, DeadLetter, Props}

class DeadLetterListener extends Actor with ActorLogging {

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[DeadLetter])
  }

  override def postStop(): Unit = {
    context.system.eventStream.unsubscribe(self)
  }


  override def receive: Receive = {
    case deadLetter: DeadLetter =>
      log.info(s"Received dead letter: $deadLetter")
    // Implement additional logic to handle dead letters if needed
  }
}

object DeadLetterListener {
  def props(): Props = Props(new DeadLetterListener)
}

