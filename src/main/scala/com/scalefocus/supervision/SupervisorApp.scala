package com.scalefocus.supervision

import akka.actor.ActorSystem

object SupervisorApp extends App {

  val system = ActorSystem("SupervisionSystem")

  val supervisor = system.actorOf(Supervisor.props(), "supervisor")

  // Send messages to the supervisor, which will forward them to the worker
  supervisor ! "Hello"
  supervisor ! "fail"  // Simulate a recoverable failure
  supervisor ! "fail"
  supervisor ! "fail"  // Simulate a recoverable failure
  supervisor ! "fail"
  supervisor ! "fail"  // Simulate a recoverable failure
  supervisor ! "fail"
  supervisor ! "fail"  // Simulate a recoverable failure
  supervisor ! "fail"
  supervisor ! "fail"  // Simulate a recoverable failure
  supervisor ! "fail"
  supervisor ! "fail"  // Simulate a recoverable failure
  supervisor ! "fail"
  supervisor ! "fail"  // Simulate a recoverable failure
  supervisor ! "fail"

  // Keep the system running to observe the actor behavior
  Thread.sleep(150000)
  system.terminate()
}
