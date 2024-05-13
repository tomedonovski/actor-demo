package com.scalefocus.support

import akka.actor.{Actor, ActorLogging, Props}
import com.scalefocus.TicketMessages.ReportMessage

class MonitoringActor extends Actor with ActorLogging {

  override def receive: Receive = {
    case ReportMessage(lineId, status) =>
      log.info(s"Monitoring report from $lineId: $status")
  }
}

object MonitoringActor {
  def props(): Props = Props[MonitoringActor]
}
