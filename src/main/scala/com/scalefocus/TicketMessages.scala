package com.scalefocus

object TicketMessages {
  sealed trait TicketMessage

  case class Ticket(ticketId: String, category: String, details: String) extends TicketMessage
  case class TicketCompleted(workerId: String, ticketId: String, result: String) extends TicketMessage
  case class TicketFailed(workerId: String, ticketId: String, reason: String) extends TicketMessage

  case class ReportMessage(lineId: String, status: String)

  val TechnicalSupport = "Technical Support"
  val BillingSupport = "Billing Support"
  val GeneralInquiry = "General Inquiry"

  def createTicket(ticketId: String, category: String, details: String): Ticket = {
    Ticket(ticketId, category, details)
  }
}
