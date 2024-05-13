package com.scalefocus

import akka.actor.Actor
import org.graphstream.graph.{Graph, Node, Edge, Element}
import org.graphstream.graph.implementations.SingleGraph

class GraphActor extends Actor {

  val graph: Graph = new SingleGraph("Example")
  graph.display()

  def receive: Receive = {
    case AddNode(id, shape) =>
      if (graph.getNode(id) == null) {
        graph.addNode(id).asInstanceOf[Node].setAttribute("ui.label", id)
      }

    case AddEdge(source, target) =>
      val edgeId = s"$source-$target"
      if (graph.getEdge(edgeId) == null) {
        graph.addEdge(edgeId, source, target, true).asInstanceOf[Edge]
      }

  }
}
