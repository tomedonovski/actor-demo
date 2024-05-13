package com.scalefocus;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GraphDemo {

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final ActorSystem system = ActorSystem.create("AkkaActorDemo");

    public static void main(String[] args) {

        CompletableFuture.runAsync(() -> {
            ActorRef graphActor = system.actorOf(Props.create(GraphActor.class), "graphActor");
            ActorRef myActor = system.actorOf(Props.create(MyActor.class, graphActor), "myActor");

            myActor.tell("Start", ActorRef.noSender());
        }, executorService);

    }
}
