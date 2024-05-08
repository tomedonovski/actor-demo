package com.scalefocus

import akka.actor.{Actor, Terminated}

class GroupActor(name: String) extends Actor {

  def receive: Receive = {
    case SendMessage(sender, recipient, content) =>
      println(s"[$name] GroupSendMessage sent to group: $content and recipient: $recipient")
      context.children.foreach(_ ! Message(sender, name, content))
    case _ =>
      println("Router: Unhandled message for UserActor")
  }
}