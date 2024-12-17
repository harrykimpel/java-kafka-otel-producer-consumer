package com.example.demoProducer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

@Service
public class CreateOrderProducer {

    private static final Logger log = LoggerFactory.getLogger(CreateOrderProducer.class);

    private final KafkaTemplate<String, Order> createOrderKafkaTemplate;

    private final String createOrderTopic;

    public CreateOrderProducer(KafkaTemplate<String, Order> createOrderKafkaTemplate,
            @Value("${spring.kafka.order.topic.create-order}") String createOrderTopic) {
        this.createOrderKafkaTemplate = createOrderKafkaTemplate;
        this.createOrderTopic = createOrderTopic;
    }

    /**
     * Sends a create order event to a Kafka topic and traces the operation.
     * 
     * @param order  The order object to be sent as an event.
     * @param tracer The tracer used for creating and managing spans.
     * @return true if the event is successfully sent.
     * @throws ExecutionException   if the send operation fails.
     * @throws InterruptedException if the send operation is interrupted.
     */
    public boolean sendCreateOrderEvent(Order order, Tracer tracer) throws ExecutionException, InterruptedException {

        Span span = tracer.spanBuilder("sendCreateOrderEvent").startSpan();

        // Make the span the current span
        try (Scope scope = span.makeCurrent()) {
            SendResult<String, Order> sendResult = createOrderKafkaTemplate.send(createOrderTopic, order).get();
            log.info("Create order {} event sent via Kafka", order);
            log.info(sendResult.toString());
            return true;
        } catch (Throwable t) {
            span.recordException(t);
            throw t;
        } finally {
            span.end();
        }
    }
}
