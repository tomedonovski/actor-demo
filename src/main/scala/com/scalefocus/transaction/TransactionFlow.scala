package com.scalefocus.transaction

import akka.NotUsed
import akka.actor.Status.{Failure, Success}
import akka.actor.{ActorSystem, Cancellable, Status}
import akka.stream.{ClosedShape, FlowShape}
import akka.stream.scaladsl._

import scala.concurrent.duration._
import scala.util.Random


class TransactionFlow {

  def run(): Unit = {
    implicit val system: ActorSystem = ActorSystem("AdvancedFlowSystemClassic")

    def randomTransaction(): Transaction = {
      val id = Random.nextInt(1000)
      val amount = 100 + Random.nextDouble() * 900
      val descriptions = List("Payment", "Refund", "Withdrawal", "Deposit")
      val description = descriptions(Random.nextInt(descriptions.size))
      Transaction(id, amount, description)
    }

    val transactionSource: Source[Transaction, Cancellable] = Source.tick(
      initialDelay = 0.seconds,
      interval = 1.seconds,
      tick = ()
    ).map(_ => randomTransaction())

    val highValueTag: Flow[Transaction, Transaction, NotUsed] = Flow[Transaction].filter(_.amount > 500).wireTap(t => println(s"High: $t")).map(t => t)
    val lowValueTag: Flow[Transaction, Transaction, NotUsed] = Flow[Transaction].filter(_.amount <= 500).wireTap(t => println(s"Low: $t")).map(t => t)

    val transactionProcessor = system.actorOf(TransactionProcessor.props, "transactionProcessor")

    val sink = Sink.actorRefWithBackpressure(
      ref = transactionProcessor,
      ackMessage = TransactionProcessor.Ack,
      onInitMessage = TransactionProcessor.Init,
      onCompleteMessage = Status.Success("Successfully completed stream"),
      onFailureMessage = Status.Failure(_)
    )

    val compositeFlow: Flow[Transaction, Transaction, NotUsed] = Flow.fromGraph(GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      // Add a Broadcast stage with 2 outputs
      val broadcast = builder.add(Broadcast[Transaction](2) )

      // Add a Merge stage with 2 inputs for Strings
      val merge = builder.add(Merge[Transaction](2) )

      broadcast.out(0) ~> highValueTag ~> merge.in(0)
      broadcast.out(1) ~> lowValueTag ~> merge.in(1)

      FlowShape(broadcast.in, merge.out)
    })

    val runnableGraph: RunnableGraph[Cancellable] = transactionSource
      .via(compositeFlow)
      .toMat(sink)(Keep.left)

    runnableGraph.run()
  }
}

object Main extends App {
  val flow = new TransactionFlow
  flow.run()
}
