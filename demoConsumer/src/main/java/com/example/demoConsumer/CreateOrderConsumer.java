package com.example.demoConsumer;

import com.example.demoProducer.Order;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

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
