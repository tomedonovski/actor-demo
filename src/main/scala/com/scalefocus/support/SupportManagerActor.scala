package com.scalefocus.support

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.scalefocus.TicketMessages.{Ticket, TicketFailed}

import scala.util.Random

class SupportManagerActor(teamCategories: Seq[(String, Props)]) extends Actor with ActorLogging {

  private val teams = scala.collection.mutable.Map[String, ActorRef] ()

  teamCategories.foreach { case (category, props) =>
    val teamActor = context.actorOf(props, s"$category-Team-${new Random().nextInt()}")
    teams.put(category, teamActor)
    context.watch(teamActor)
  }

  override def receive: Receive = {
    case ticket @ Ticket(ticketId, category, details) =>
      teams.get(category) match {
        case Some(team) => team ! ticket
        case None =>
          log.info(s"No team available for category: $category")
      }
  }

  private def createTeam(category: String, workerProps: Props): ActorRef = {
    val team = context.actorOf(SupportTeamActor.props(category, workerProps, 1), s"${category}Team")
    context.watch(team)
    team
  }
}

object SupportManagerActor {
  def props(teamCategories: Seq[(String, Props)]): Props = Props(new SupportManagerActor(teamCategories))
}
