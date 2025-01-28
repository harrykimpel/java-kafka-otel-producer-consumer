package com.example.demoConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class UserProducer {

    private static final Logger log = LoggerFactory.getLogger(UserProducer.class);

    private final KafkaTemplate<String, User> userKafkaTemplate;

    private final String userTopic;

    public UserProducer(KafkaTemplate<String, User> userKafkaTemplate,
            @Value("${spring.kafka.order.topic.user}") String userTopic) {
        this.userKafkaTemplate = userKafkaTemplate;
        this.userTopic = userTopic;
    }

    public boolean sendUserEvent(User user) throws ExecutionException, InterruptedException {

        SendResult<String, User> sendResult = userKafkaTemplate.send(userTopic, user).get();
        log.info("Create user {} event sent via Kafka", user);
        log.info(sendResult.toString());
        return true;
    }
}
