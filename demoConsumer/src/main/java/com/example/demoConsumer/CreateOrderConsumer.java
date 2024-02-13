package com.example.demoConsumer;

import com.example.demoProducer.Order;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;

import java.security.SecureRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service("orderConsumerNotificationService")
public class CreateOrderConsumer {

    private static final Logger log = LoggerFactory.getLogger(CreateOrderConsumer.class);
    private final Tracer tracer;

    @KafkaListener(topics = "${spring.kafka.order.topic.create-order}", containerFactory = "containerFactoryNotificationService")
    public void createOrderListener(@Payload Order order, Acknowledgment ack) {

        Span span = tracer.spanBuilder("createOrderListener").startSpan();

        // Make the span the current span
        try (Scope scope = span.makeCurrent()) {
            log.info("Notification service received order {} ", order);
            ack.acknowledge();

            Integer secondsToSleep = 3;
            ExecuteLongrunningTask(secondsToSleep);

            SecureRandom secureRandom = new SecureRandom();
            int randomWithSecureRandom = secureRandom.nextInt(10);
            log.info("randomWithSecureRandom: " + randomWithSecureRandom);
            GetUser(randomWithSecureRandom);
        } catch (Throwable t) {
            span.recordException(t);
            throw t;
        } finally {
            span.end();
        }
    }

    private void ExecuteLongrunningTask(Integer secondsToSleep) {
        Span span = tracer.spanBuilder("ExecuteLongrunningTask").startSpan();
        span.setAttribute("secondsToSleep", secondsToSleep);
        // Make the span the current span
        try (Scope scope = span.makeCurrent()) {
            Thread.sleep(secondsToSleep * 1000);
            log.info("Executed some long running task that took " + secondsToSleep + " seconds to run.");
        } catch (Exception t) {
            span.recordException(t);
            // throw t;
        } finally {
            span.end();
        }
    }

    private void GetUser(Integer randomUser) {
        Span span = tracer.spanBuilder("GetUser").startSpan();
        // Make the span the current span
        try (Scope scope = span.makeCurrent()) {
            String uri = "https://jsonplaceholder.typicode.com/users/" + randomUser;
            span.setAttribute("uri", uri);
            RestTemplate restTemplate = new RestTemplate();

            User user = restTemplate.getForObject(uri, User.class);
            log.info("User: " + user);
            log.info("User id: " + user.getId());
            span.setAttribute("user.id", user.getId());
        } catch (Throwable t) {
            span.recordException(t);
            throw t;
        } finally {
            span.end();
        }
    }

    @Autowired
    CreateOrderConsumer(OpenTelemetry openTelemetry) {
        tracer = openTelemetry.getTracer(DemoConsumerApplication.class.getName(), "0.1.0");
    }
}
