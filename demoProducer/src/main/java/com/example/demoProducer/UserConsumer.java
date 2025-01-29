package com.example.demoProducer;

import com.example.demoProducer.Order;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

import com.example.demoConsumer.User;

import org.springframework.beans.factory.annotation.Autowired;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.SecureRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import java.util.concurrent.ExecutionException;

@Service("userConsumerNotificationService")
public class UserConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserConsumer.class);
    private final Tracer tracer;
    private final OpenTelemetry openTelemetry;

    @KafkaListener(topics = "${spring.kafka.order.topic.user}", containerFactory = "containerFactoryNotificationService")
    public void userListener(@Payload User user, Acknowledgment ack) {
        Span span = tracer.spanBuilder("userListener").startSpan();

        // Make the span the current span
        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("user.id", user.getId());
            log.info("Notification service received user {} ", user.getId());
            ack.acknowledge();
        } catch (Throwable t) {
            span.recordException(t);
            //throw t;
        } finally {
            span.end();
        }
    }

    @Autowired
    UserConsumer(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
        tracer = openTelemetry.getTracer(DemoProducerApplication.class.getName(), "0.1.0");
    }
}
