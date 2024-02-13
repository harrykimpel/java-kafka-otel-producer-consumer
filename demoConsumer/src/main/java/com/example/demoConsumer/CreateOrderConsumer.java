package com.example.demoConsumer;

import com.example.demoProducer.Order;

import java.security.SecureRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service("orderConsumerNotificationService")
public class CreateOrderConsumer {

    private static final Logger log = LoggerFactory.getLogger(CreateOrderConsumer.class);

    @KafkaListener(topics = "${spring.kafka.order.topic.create-order}", containerFactory = "containerFactoryNotificationService")
    public void createOrderListener(@Payload Order order, Acknowledgment ack) {
        log.info("Notification service received order {} ", order);
        ack.acknowledge();

        Integer secondsToSleep = 3;
        ExecuteLongrunningTask(secondsToSleep);

        SecureRandom secureRandom = new SecureRandom();
        int randomWithSecureRandom = secureRandom.nextInt(10);
        log.info("randomWithSecureRandom: " + randomWithSecureRandom);
        GetUser(randomWithSecureRandom);
    }

    private void ExecuteLongrunningTask(Integer secondsToSleep) {
        try {
            Thread.sleep(secondsToSleep * 1000);
            log.info("Executed some long running task that took " + secondsToSleep + " seconds to run.");
        } catch (Exception ex) {

        }
    }

    private void GetUser(Integer randomUser) {
        String uri = "https://jsonplaceholder.typicode.com/users/" + randomUser;
        RestTemplate restTemplate = new RestTemplate();

        User user = restTemplate.getForObject(uri, User.class);
        log.info("User: " + user);
        log.info("User id: " + user.getId());
    }
}
