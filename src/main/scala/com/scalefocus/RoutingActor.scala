package com.scalefocus

import akka.actor.{Actor, ActorRef}

class RoutingActor(graphActor: ActorRef) extends Actor {

  def receive: Receive = {
    case RouteMessage(senderId, recipientId, message) =>
      graphActor ! AddNode(senderId)
  }
}
