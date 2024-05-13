package com.scalefocus.transaction

import akka.actor.{Actor, Props, Status}
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime

import java.util.Date

class TransactionProcessor extends Actor {
  import context.dispatcher

  override def receive: Receive = {

        // Acknowledge to start the stream
    case TransactionProcessor.Init =>
      println(s"Stream initialized")
      sender ! TransactionProcessor.Ack

      // Simulate some process delay
    case transaction: Transaction =>
      val now = new Date()
      val replyTo = sender()
      println(s"Current sender: $sender()")
      println(s"Processing transaction: $transaction, time: $now")
      context.system.scheduler.scheduleOnce(5.seconds) {
        println("Sending ack at time: " + new Date() +  s" current sender: ${sender()}")
        replyTo ! TransactionProcessor.Ack
      }

    case Status.Success(_) =>
      println(s"Stream processing completed successfully")
      context.stop(self)

    case Status.Failure(ex) =>
      println(s"Stream processing failed: ${ex.getMessage}")
      context.stop(self)

    case other =>
      println(s"Received unknown message: $other")
      context.stop(self)
  }
}

final case class Transaction(id: Int, amount: Double, description: String)

object TransactionProcessor {
  def props: Props = Props(new TransactionProcessor)
  case object Ack
  case object Init
}
