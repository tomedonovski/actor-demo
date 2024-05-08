package com.scalefocus

import akka.actor.Actor

class UserActor(name: String) extends Actor {

  def receive: Receive = {
    case SendMessage(sender, recipient, content) =>
      println(s"[$name] SendMessage sent from $sender to $recipient: $content")
      context.actorSelection(s"/user/router") ! Message(sender, recipient, content)
    case Message(sender, recipient, content) =>
      println(s"[$name] Message sent from $sender to $recipient: $content")
    case _ =>
      println("Router: Unhandled message for UserActor")
  }
}