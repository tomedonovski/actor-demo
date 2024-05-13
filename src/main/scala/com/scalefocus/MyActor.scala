package com.scalefocus

import akka.actor.{Actor, ActorRef, Props}

class MyActor(graphActor: ActorRef) extends Actor {

  val id = self.path.name

   def receive: Receive = {
     case "Start" =>
       graphActor ! AddNode(id) //report itself
       val target = context.actorOf(Props(classOf[MyActor], graphActor), "TargetActor")
       graphActor ! AddNode(target.path.name)
       graphActor ! AddEdge(id, target.path.name)

     case _ =>
       println(s"In all match!")


  }
}
