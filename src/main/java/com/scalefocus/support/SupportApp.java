package com.scalefocus.support;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.japi.Pair;
import akka.pattern.Patterns;
import com.scalefocus.TicketMessages;
import scala.Predef;
import scala.Tuple2;
import scala.collection.JavaConverters;
import scala.collection.immutable.Seq;
import scala.compat.java8.FutureConverters;
import scala.jdk.javaapi.CollectionConverters;

import java.util.ArrayList;
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

            system.actorOf(DeadLetterListener.props(), "deadLetterListener");

            Props techTeam = SupportTeamActor.props(TicketMessages.TechnicalSupport(), Props.create(SupportWorkerActor.class), 2);
            Props billingTeam = SupportTeamActor.props(TicketMessages.BillingSupport(), Props.create(SupportWorkerActor.class), 3);
            Props generalTeam = SupportTeamActor.props(TicketMessages.GeneralInquiry(), Props.create(SupportWorkerActor.class), 1);

            Tuple2<String, Props> technicalKey = new Tuple2<>(TicketMessages.TechnicalSupport(), techTeam);
            Tuple2<String, Props> billingKey = new Tuple2<>(TicketMessages.BillingSupport(), billingTeam);
            Tuple2<String, Props> generalKey = new Tuple2<>(TicketMessages.GeneralInquiry(), generalTeam);
            List<Tuple2<String, Props>> teamPropsList = new ArrayList<>() {{
                add(technicalKey);
                add(billingKey);
                add(generalKey);
            }};

            Seq<Tuple2<String, Props>> sequence = JavaConverters.asScalaIteratorConverter(teamPropsList.iterator()).asScala().toSeq();

            ActorRef supportManager = system.actorOf(SupportManagerActor.props(sequence), "support-manager");

            TicketMessages.Ticket ticket1 = TicketMessages.createTicket("ticket1", TicketMessages.TechnicalSupport(), "Fix software bug");
            TicketMessages.Ticket ticket2 = TicketMessages.createTicket("ticket2", TicketMessages.BillingSupport(), "Refund request");
            TicketMessages.Ticket ticket3 = TicketMessages.createTicket("ticket3", TicketMessages.GeneralInquiry(), "Question about new features");

            TicketMessages.Ticket ticket4_1 = TicketMessages.createTicket("ticket4", TicketMessages.TechnicalSupport(), "Simulate fail condition 1");
            TicketMessages.Ticket ticket4_2 = TicketMessages.createTicket("ticket4", TicketMessages.TechnicalSupport(), "Simulate fail condition 2");
            TicketMessages.Ticket ticket4_3 = TicketMessages.createTicket("ticket4", TicketMessages.TechnicalSupport(), "Simulate fail condition 3");

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            supportManager.tell(ticket1, ActorRef.noSender());
            supportManager.tell(ticket2, ActorRef.noSender());
            supportManager.tell(ticket3, ActorRef.noSender());
            supportManager.tell(ticket4_1, ActorRef.noSender());

            supportManager.tell(ticket3, ActorRef.noSender());

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            supportManager.tell(ticket4_2, ActorRef.noSender());

            Executors.newScheduledThreadPool(1).schedule(() -> {
                supportManager.tell(ticket4_3, ActorRef.noSender());
            }, 3, TimeUnit.SECONDS);

            Executors.newScheduledThreadPool(1).schedule(() -> {
                TicketMessages.Ticket ticket5 = TicketMessages.createTicket("ticket5", TicketMessages.TechnicalSupport(), "Fix printer");
                TicketMessages.Ticket ticket6 = TicketMessages.createTicket("ticket6", TicketMessages.TechnicalSupport(), "Fix bug");
                TicketMessages.Ticket ticket7 = TicketMessages.createTicket("ticket7", TicketMessages.TechnicalSupport(), "Software update");
                TicketMessages.Ticket ticket8 = TicketMessages.createTicket("ticket8", TicketMessages.TechnicalSupport(), "Issue hardware");

                supportManager.tell(ticket5, ActorRef.noSender());
                supportManager.tell(ticket6, ActorRef.noSender());
                supportManager.tell(ticket7, ActorRef.noSender());
                supportManager.tell(ticket8, ActorRef.noSender());
            }, 6, TimeUnit.SECONDS);
        }, executorService);
    }
}
