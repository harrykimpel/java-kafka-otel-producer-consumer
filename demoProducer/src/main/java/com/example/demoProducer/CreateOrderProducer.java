package com.example.demoProducer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.api.gax.rpc.ApiException;
import com.google.cloud.pubsub.v1.Publisher;

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

    public void publishWithErrorHandlerExample(String projectId, String topicId, Order order)
            throws IOException, InterruptedException {
        TopicName topicName = TopicName.of(projectId, topicId);
        Publisher publisher = null;

        try {
            // Create a publisher instance with default settings bound to the topic
            publisher = Publisher.newBuilder(topicName).build();

            List<String> messages = Arrays.asList("first message", "second message");

            // for (final String message : messages) {
            // publish order to Google Cloud Pub/Sub
            String message = order.toString();
            ByteString data = ByteString.copyFromUtf8(message);
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();

            // Once published, returns a server-assigned message id (unique within the
            // topic)
            ApiFuture<String> future = publisher.publish(pubsubMessage);

            // Add an asynchronous callback to handle success / failure
            ApiFutures.addCallback(
                    future,
                    new ApiFutureCallback<String>() {

                        @Override
                        public void onFailure(Throwable throwable) {
                            if (throwable instanceof ApiException) {
                                ApiException apiException = ((ApiException) throwable);
                                // details on the API exception
                                System.out.println(apiException.getStatusCode().getCode());
                                System.out.println(apiException.isRetryable());
                            }
                            System.out.println("Error publishing message : " + message);
                        }

                        @Override
                        public void onSuccess(String messageId) {
                            // Once published, returns server-assigned message ids (unique within the topic)
                            System.out.println("Published message ID: " + messageId);
                        }
                    },
                    MoreExecutors.directExecutor());
            // }
        } finally {
            if (publisher != null) {
                // When finished with the publisher, shutdown to free up resources.
                publisher.shutdown();
                publisher.awaitTermination(1, TimeUnit.MINUTES);
            }
        }
    }
}
