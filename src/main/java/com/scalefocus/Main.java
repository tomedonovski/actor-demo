package com.scalefocus;

import akka.actor.*;
import akka.routing.RoundRobinPool;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSinkDOT;
import org.graphstream.ui.view.Viewer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static com.scalefocus.Messages.createSendMessage;

public class Main {

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static void main(String[] args) throws IOException {
        CompletableFuture.runAsync(() -> {
            try {
                ActorSystem system = ActorSystem.create("AkkaActorDemo");

                ActorRef router = system.actorOf(Props.create(RouterActor.class), "router");

//                ActorRef routerPool = system.actorOf(new RoundRobinPool(5).props(Props.create(RouterActor.class)), "routerPool");

                Graph graph = new SingleGraph("ActorSystemGraph");
                Viewer viewer = graph.display();

                List<ActorRef> users = new ArrayList<>();
                List<ActorRef> groups = new ArrayList<>();

                // Create user actors
                for (int i = 1; i <= 10; i++) {
                    ActorRef user = system.actorOf(Props.create(UserActor.class, "User" + i), "User" + i);
                    users.add(user);
                    graph.addNode(user.path().name()).setAttribute("ui.label", user.path().name());
                }

                // Create group actors
                for (int i = 1; i <= 3; i++) {
                    ActorRef group = system.actorOf(Props.create(GroupActor.class, "Group" + i), "Group" + i);
                    groups.add(group);
                    graph.addNode(group.path().name()).setAttribute("ui.label", group.path().name());
                }

//                users.forEach(user -> router.tell(user, ActorRef.noSender()));
//                groups.forEach((group -> router.tell(group, ActorRef.noSender())));

                // Create a pool of routers to handle message routing
                final ActorRef routerPool = system.actorOf(new RoundRobinPool(5).props(Props.create(RouterActor.class)), "routerPool");

                for (int i = 1; i < 20; i++) {
                    ActorRef sender = users.get(i % 10);
                    ActorRef recipient = (i % 3 == 0) ? groups.get(i % 3) : users.get((i + 1) % 10);

                    SendMessage sendMessage = createSendMessage(sender.path().name(), recipient.path().name(), "Message " + i);

                    routerPool.tell(sendMessage, ActorRef.noSender());

                    String edgeId = sender.path().name() + "->" + recipient.path().name();
                    if (graph.getEdge(edgeId) == null) {
                        graph.addEdge(edgeId, sender.path().name(), recipient.path().name(), true).setAttribute("ui.label", "Message " + i);
                    }
                }

                FileSinkDOT exporter  = new FileSinkDOT();
                exporter.writeAll(graph, "actorSystem.dot");

                system.terminate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, executorService);
    }
}