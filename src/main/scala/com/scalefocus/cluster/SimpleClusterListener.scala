package com.scalefocus.cluster

import akka.actor.Actor
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{MemberUp, UnreachableMember}

class SimpleClusterListener extends Actor {

  val cluster: Cluster = Cluster(context.system)

  override def preStart(): Unit = {
    cluster.subscribe(self, classOf[MemberUp], classOf[UnreachableMember])
  }

  override def postStop(): Unit = {
    cluster.unsubscribe(self)
  }

  override def receive: Receive = {
    case MemberUp(member) =>
      println(s"Member is Up: ${member.address}")

    case UnreachableMember(member) =>
      println(s"Member detected as unreachable: ${member.address}")
  }
}
