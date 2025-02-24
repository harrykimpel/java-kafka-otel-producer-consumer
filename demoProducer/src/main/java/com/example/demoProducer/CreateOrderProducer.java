package com.example.demoProducer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.google.api.gax.rpc.ApiException;
import com.google.cloud.pubsub.v1.Publisher;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.api.OpenTelemetry;

@Service
public class CreateOrderProducer {

    private static final Logger log = LoggerFactory.getLogger(CreateOrderProducer.class);

    public void publishWithErrorHandlerExample(String projectId, String topicId, Order order,
            OpenTelemetry openTelemetry)
            throws IOException, InterruptedException {
        TopicName topicName = TopicName.of(projectId, topicId);
        Publisher publisher = null;

        Tracer tracer = openTelemetry.getTracer(DemoProducerApplication.class.getName(), "0.1.0");
        Span span = tracer.spanBuilder("sendCreateOrderEvent").startSpan();
        try {

            span.setAttribute("enduser.id", order.getCustomerId());
            span.setAttribute("order.id", order.getOrderID());

            log.info("Create order {} event sent via Google Cloud Pub/Sub", order);
            // Create a publisher instance with default settings bound to the topic
            publisher = Publisher.newBuilder(topicName)
                    .setOpenTelemetry(openTelemetry)
                    .setEnableOpenTelemetryTracing(true)
                    .build();

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
                            log.info("Error publishing message : " + message);
                        }

                        @Override
                        public void onSuccess(String messageId) {
                            // Once published, returns server-assigned message ids (unique within the topic)
                            System.out.println("Published message ID: " + messageId);
                            log.info("Published message ID: " + messageId);
                        }
                    },
                    MoreExecutors.directExecutor());
        } catch (Throwable t) {
            span.recordException(t);
            throw t;
        } finally {
            span.end();
            if (publisher != null) {
                // When finished with the publisher, shutdown to free up resources.
                publisher.shutdown();
                publisher.awaitTermination(1, TimeUnit.MINUTES);
            }
        }
    }
}
