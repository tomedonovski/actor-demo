package com.scalefocus.support;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.scalefocus.TicketMessages;
import scala.Predef;
import scala.collection.JavaConverters;
import scala.jdk.javaapi.CollectionConverters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SupportApp {
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final ActorSystem system = ActorSystem.create("SupportActor");

    public static void main(String[] args) {

        CompletableFuture.runAsync(() -> {
            List<ActorRef> techWorkers = List.of(
                    system.actorOf(Props.create(SupportWorkerActor.class), "tech-worker1"),
                    system.actorOf(Props.create(SupportWorkerActor.class), "tech-worker2"),
                    system.actorOf(Props.create(SupportWorkerActor.class), "tech-worker3"),
                    system.actorOf(Props.create(SupportWorkerActor.class), "tech-worker4")
            );

            List<ActorRef> billingWorkers = List.of(
                    system.actorOf(Props.create(SupportWorkerActor.class), "billing-worker1"),
                    system.actorOf(Props.create(SupportWorkerActor.class), "billing-worker2"),
                    system.actorOf(Props.create(SupportWorkerActor.class), "billing-worker3")
            );


            List<ActorRef> generalWorkers = List.of(
                    system.actorOf(Props.create(SupportWorkerActor.class), "general-worker1"),
                    system.actorOf(Props.create(SupportWorkerActor.class), "general-worker2"),
                    system.actorOf(Props.create(SupportWorkerActor.class), "general-worker3"),
                    system.actorOf(Props.create(SupportWorkerActor.class), "general-worker$"),
                    system.actorOf(Props.create(SupportWorkerActor.class), "general-worker5")
            );


            ActorRef techTeam = system.actorOf(SupportTeamActor.props(TicketMessages.TechnicalSupport(), CollectionConverters.asScala(techWorkers).toSeq()));
            ActorRef billingTeam = system.actorOf(SupportTeamActor.props(TicketMessages.BillingSupport(), CollectionConverters.asScala(billingWorkers).toSeq()));
            ActorRef generalTeam = system.actorOf(SupportTeamActor.props(TicketMessages.GeneralInquiry(), CollectionConverters.asScala(generalWorkers).toSeq()));

            Map<String, ActorRef> teams = new HashMap<>() {{
                put(TicketMessages.TechnicalSupport(), techTeam);
                put(TicketMessages.BillingSupport(), billingTeam);
                put(TicketMessages.GeneralInquiry(), generalTeam);
            }};

            ActorRef supportManager = system.actorOf(SupportManagerActor.props(JavaConverters.mapAsScalaMap(teams)), "support-manager");

            TicketMessages.Ticket ticket1 = TicketMessages.createTicket("ticket1", TicketMessages.TechnicalSupport(), "Fix software bug");
            TicketMessages.Ticket ticket2 = TicketMessages.createTicket("ticket1", TicketMessages.BillingSupport(), "Refund request");
            TicketMessages.Ticket ticket3 = TicketMessages.createTicket("ticket1", TicketMessages.GeneralInquiry(), "Question about new features");

            TicketMessages.Ticket ticket4 = TicketMessages.createTicket("ticket1", TicketMessages.TechnicalSupport(), "Simulate fail condition");

            supportManager.tell(ticket1, ActorRef.noSender());
            supportManager.tell(ticket2, ActorRef.noSender());
            supportManager.tell(ticket3, ActorRef.noSender());
            supportManager.tell(ticket4, ActorRef.noSender());

        }, executorService);
    }
}
