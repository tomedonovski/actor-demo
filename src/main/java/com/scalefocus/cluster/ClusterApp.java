package com.scalefocus.cluster;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ClusterApp {
    private static final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private static void startClusterNode(int port) {
        final Config config = ConfigFactory.parseString("akka.remote.artery.canonical.port=" + port)
                .withFallback(ConfigFactory.load("cluster-application.conf"));

        final ActorSystem system = ActorSystem.create("ClusterSystem", config);

        ActorRef clusterListener = system.actorOf(Props.create(SimpleClusterListener.class), "clusterListener");
        clusterListener.tell(new CustomMessage("message to local actor: %s".formatted(port)), ActorRef.noSender());

        Executors.newScheduledThreadPool(1).schedule(() -> {
            ActorSelection remoteActor = system.actorSelection("akka://ClusterSystem@127.0.0.1:2553/user/clusterListener");
            remoteActor.tell(SimpleClusterListener.createCustomMessage("greetings from: %s".formatted(port)), ActorRef.noSender());

            remoteActor.tell(new GetClusterStatus(), ActorRef.noSender());
        }, 15, TimeUnit.SECONDS);
    }
    public static void main(String[] args) {

        for (String portArg : args) {
            int port = Integer.parseInt(portArg);
            System.out.println("Starting cluster node on port: " + port);

            CompletableFuture.runAsync(() -> {
                startClusterNode(port);
            }, executorService);
        }
    }
}
