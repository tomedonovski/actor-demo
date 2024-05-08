package com.scalefocus

import akka.actor.{AbstractActor, Actor, Terminated}

class RouterActor extends Actor {

  def receive: Receive = {
    case msg @ SendMessage(_, recipient, _) =>
      context.actorSelection(s"/user/$recipient") ! msg
    case msg @ Message(sender, recipient, _) =>
      println(s"[$sender] of message for recipient: $recipient")
    case Terminated(userActor) =>
      println(s"Router: Actor $userActor terminated")
    case other =>
      context.actorSelection(s"/user/$other") ! other
      println(s"All messages: $other")
  }
}
