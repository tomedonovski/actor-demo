package com.scalefocus.cluster;

import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClusterApp {
    private static final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private static void startClusterNode(int port) {
        final Config config = ConfigFactory.parseString("akka.remote.artery.canonical.port=" + port)
                .withFallback(ConfigFactory.load("cluster-application.conf"));

        final ActorSystem system = ActorSystem.create("ClusterSystem", config);

        system.actorOf(Props.create(SimpleClusterListener.class), "clusterListener");
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
