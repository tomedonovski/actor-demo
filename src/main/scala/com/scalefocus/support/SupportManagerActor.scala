package com.scalefocus.support

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.scalefocus.TicketMessages.{Ticket, TicketFailed}

class SupportManagerActor(teams:  scala.collection.mutable.Map[String, ActorRef]) extends Actor with ActorLogging {

  private val workerId = self.path.name

  override def receive: Receive = {
    case ticket @ Ticket(ticketId, category, details) =>
      teams.get(category) match {
        case Some(team) => team ! ticket
        case None => log.info(s"No team available for category: $category")
      }
  }
}

object SupportManagerActor {
  def props(teams: scala.collection.mutable.Map[String, ActorRef]): Props = Props(new SupportManagerActor(teams))
}
