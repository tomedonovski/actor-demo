package com.scalefocus.basic

import akka.actor.{Actor, ActorSystem, Props}

case class BasicMessage(message: String)

class BasicActor extends Actor {

  override def receive: Receive =  {

    case BasicMessage(msg) =>
      println(s"Received message: $msg")

    case unknownMessage =>
      println(s"Received unknown message: $unknownMessage")
  }
}

object BasicActor {
  val props: Props = Props(new BasicActor)
}

object Main extends App {
  val actorSystem = ActorSystem.create("BasicActorSystem")
  val basicActorRef = actorSystem.actorOf(BasicActor.props, "basicActor")

  basicActorRef ! BasicMessage("New Message")

  val basicActorSelect = actorSystem.actorSelection("akka://BasicActorSystem/user/basicActor")
  basicActorSelect ! BasicMessage("Sent through actor selection")

  println(s"Actor path: ${basicActorRef.path}")
}


