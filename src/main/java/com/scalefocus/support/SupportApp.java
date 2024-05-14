package com.scalefocus.support;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import com.scalefocus.TicketMessages;
import scala.Predef;
import scala.collection.JavaConverters;
import scala.compat.java8.FutureConverters;
import scala.jdk.javaapi.CollectionConverters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SupportApp {
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final ActorSystem system = ActorSystem.create("SupportActor");

    public static void main(String[] args) {

        CompletableFuture.runAsync(() -> {
            ActorRef techTeam = system.actorOf(SupportTeamActor.props(TicketMessages.TechnicalSupport(), Props.create(SupportWorkerActor.class), 1));
            ActorRef billingTeam = system.actorOf(SupportTeamActor.props(TicketMessages.BillingSupport(), Props.create(SupportWorkerActor.class), 3));
            ActorRef generalTeam = system.actorOf(SupportTeamActor.props(TicketMessages.GeneralInquiry(), Props.create(SupportWorkerActor.class), 1));

            ActorRef deadLetterListener = system.actorOf(DeadLetterListener.props(), "deadLetterListener");

            Map<String, ActorRef> teams = new HashMap<>() {{
                put(TicketMessages.TechnicalSupport(), techTeam);
                put(TicketMessages.BillingSupport(), billingTeam);
                put(TicketMessages.GeneralInquiry(), generalTeam);
            }};

            ActorRef supportManager = system.actorOf(SupportManagerActor.props(JavaConverters.mapAsScalaMap(teams)), "support-manager");

            TicketMessages.Ticket ticket1 = TicketMessages.createTicket("ticket1", TicketMessages.TechnicalSupport(), "Fix software bug");
            TicketMessages.Ticket ticket2 = TicketMessages.createTicket("ticket2", TicketMessages.BillingSupport(), "Refund request");
            TicketMessages.Ticket ticket3 = TicketMessages.createTicket("ticket3", TicketMessages.GeneralInquiry(), "Question about new features");

            TicketMessages.Ticket ticket4 = TicketMessages.createTicket("ticket4", TicketMessages.TechnicalSupport(), "Simulate fail condition");

            supportManager.tell(ticket1, ActorRef.noSender());
            supportManager.tell(ticket2, ActorRef.noSender());
            supportManager.tell(ticket3, ActorRef.noSender());
            supportManager.tell(ticket4, ActorRef.noSender());

            supportManager.tell(ticket3, ActorRef.noSender());
            supportManager.tell(ticket4, ActorRef.noSender());

            Executors.newScheduledThreadPool(1).schedule(() -> {
                supportManager.tell(ticket4, ActorRef.noSender());
                supportManager.tell(ticket4, ActorRef.noSender());
                supportManager.tell(ticket4, ActorRef.noSender());
                supportManager.tell(ticket4, ActorRef.noSender());
            }, 3, TimeUnit.SECONDS);

        }, executorService);
    }
}
