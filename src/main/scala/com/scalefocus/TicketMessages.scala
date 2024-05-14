package com.scalefocus

import akka.actor.Props

object TicketMessages {
  sealed trait TicketMessage

  case class Ticket(ticketId: String, category: String, details: String) extends TicketMessage
  case class TicketCompleted(workerId: String, ticketId: String, result: String) extends TicketMessage
  case class TicketFailed(workerId: String, ticketId: String, reason: String) extends TicketMessage

  case class ReportMessage(lineId: String, status: String)

  val TechnicalSupport = "Technical_Support"
  val BillingSupport = "Billing_Support"
  val GeneralInquiry = "General_Inquiry"

  def createTicket(ticketId: String, category: String, details: String): Ticket = {
    Ticket(ticketId, category, details)
  }
}
