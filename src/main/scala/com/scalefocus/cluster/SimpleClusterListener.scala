package com.scalefocus.cluster

import akka.actor.Actor
import akka.cluster.{Cluster, Member}
import akka.cluster.ClusterEvent.{ClusterDomainEvent, LeaderChanged, MemberExited, MemberLeft, MemberRemoved, MemberUp, UnreachableMember}

case class GetClusterStatus() {}
case class CustomMessage(msg: String) {}

object SimpleClusterListener {
  def createCustomMessage(str: String): CustomMessage = {
    CustomMessage(str)
  }
  def createGetClusterStatus(): GetClusterStatus = {
    GetClusterStatus()
  }
}
class SimpleClusterListener extends Actor {

  val cluster: Cluster = Cluster(context.system)

  override def preStart(): Unit = {
    cluster.subscribe(self,
      classOf[MemberUp],
      classOf[UnreachableMember],
      classOf[MemberRemoved],
      classOf[MemberExited],
      classOf[MemberLeft],
      classOf[ClusterDomainEvent])
  }

  override def postStop(): Unit = {
    cluster.unsubscribe(self)
  }

  override def receive: Receive = {
    case MemberUp(member) =>
      println(s"Member is Up: ${member.address}")

    case MemberExited(member) =>
      println(s"Member exited: ${member.address}")

    case MemberRemoved(member, status) =>
      println(s"Member removed: ${member.address} with status: ${status}")

    case MemberLeft(member) =>
      println(s"Member left: ${member.address}")

    case UnreachableMember(member) =>
      println(s"Member detected as unreachable: ${member.address}")

    case LeaderChanged(leader) =>
      println(s"Leader changed: ${leader}")

    case GetClusterStatus() =>
      val members: Set[Member] = cluster.state.members
      println(s"Current members: ${members.mkString(", ")}")

    case CustomMessage(data) =>
      println(s"Received custom message with data: $data")

    case other: ClusterDomainEvent =>
      println(s"Cluster Domain Event: $other")
  }
}
