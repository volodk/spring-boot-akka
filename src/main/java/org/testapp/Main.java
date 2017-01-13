package org.testapp;

import static akka.pattern.Patterns.ask;
import static org.testapp.SpringExtension.SpringExtProvider;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.testapp.CountingActor.Count;
import org.testapp.CountingActor.Get;
import org.testapp.SpringExtension.SpringExt;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Terminated;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

            // get hold of the actor system
            ActorSystem system = ctx.getBean(ActorSystem.class);
            // use the Spring Extension to create props for a named actor bean
            SpringExt springExt = SpringExtProvider.get(system);
            ActorRef counter = system.actorOf(springExt.props("CountingActor"), "counter");

            // tell it to count three times
            counter.tell(new Count(), null);
            counter.tell(new Count(), null);
            counter.tell(new Count(), null);

            // print the result
            FiniteDuration duration = FiniteDuration.create(3, TimeUnit.SECONDS);
            Future<Object> result = ask(counter, new Get(), Timeout.durationToTimeout(duration));
            try {
                System.out.println("Got back " + Await.result(result, duration));
            } catch (Exception e) {
                System.err.println("Failed getting result: " + e.getMessage());
                throw e;
            } finally {
                Future<Terminated> whenTerminated = system.terminate();
                Await.result(whenTerminated, Duration.Inf());
            }
        };
    }

    @Bean
    public ActorSystem actorSystem(ApplicationContext ctx) {
        ActorSystem system = ActorSystem.create("AkkaJavaSpring");
        // initialize the application context in the Akka Spring Extension
        SpringExtProvider.get(system).initialize(ctx);
        return system;
    }
}
