package com.example.demoProducer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

// OTel imports

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

    public boolean sendCreateOrderEvent(Order order) throws ExecutionException, InterruptedException {

        SendResult<String, Order> sendResult = createOrderKafkaTemplate.send(createOrderTopic, order).get();
        log.info("Create order {} event sent via Kafka", order);
        log.info(sendResult.toString());
        return true;
    }
}
