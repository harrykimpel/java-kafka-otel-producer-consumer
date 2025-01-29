package com.example.demoConsumer;

import com.example.demoProducer.Order;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapSetter;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.opentelemetry.context.Context;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;
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
    private final OpenTelemetry openTelemetry;

    /**
     * Listens for messages on the specified Kafka topic and processes incoming
     * orders.
     * 
     * This method is triggered when a message is received on the topic defined by
     * 'spring.kafka.order.topic.create-order'. It acknowledges the message, logs
     * the
     * order details, and performs several operations including calling demo
     * services,
     * executing a long-running task, and retrieving a user based on a random
     * number.
     * 
     * @param order the order payload received from the Kafka topic
     * @param ack   the acknowledgment object used to manually acknowledge message
     *              processing
     */
    @KafkaListener(topics = "${spring.kafka.order.topic.create-order}", containerFactory = "containerFactoryNotificationService")
    public void createOrderListener(@Payload Order order, Acknowledgment ack) {

        Span span = tracer.spanBuilder("createOrderListener").startSpan();

        // Make the span the current span
        try (Scope scope = span.makeCurrent()) {
            log.info("Notification service received order {} ", order);
            ack.acknowledge();

            CallDemoService1();

            CallDemoService2();

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
        // Make the span the current span
        try {
            Span sleepSpan = tracer.spanBuilder("WhyTheHeckDoWeSleepHere")
                    .setParent(Context.current().with(span))
                    .startSpan();
            try {
                sleepSpan.setAttribute("secondsToSleep", secondsToSleep);
                Thread.sleep(secondsToSleep * 1000);
                log.info("Executed some long running task that took " + secondsToSleep + " seconds to run.");

            } finally {
                sleepSpan.end();
            }

            SomeTinyTask(span);

            AnotherShortRunningTask();
        } catch (Exception t) {
            span.recordException(t);
            // throw t;
        } finally {
            span.end();
        }
    }

    private void SomeTinyTask(Span parentSpan) {
        Span childSpan = tracer.spanBuilder("SomeTinyTask")
                .setParent(Context.current().with(parentSpan))
                .startSpan();
        // Make the span the current span
        try {
            Thread.sleep(10);
        } catch (Exception t) {
            childSpan.recordException(t);
            // throw t;
        } finally {
            childSpan.end();
        }
    }

    @WithSpan
    private void AnotherShortRunningTask() {
        try {
            Thread.sleep(115);
        } catch (Exception t) {
            // throw t;
        } finally {
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

    // Tell OpenTelemetry to inject the context in the HTTP headers
    TextMapSetter<HttpURLConnection> setter = new TextMapSetter<HttpURLConnection>() {
        @Override
        public void set(HttpURLConnection carrier, String key, String value) {
            // Insert the context as Header
            carrier.setRequestProperty(key, value);
        }
    };

    private void CallDemoService1() {
        Span span = tracer.spanBuilder("get_demoService").setSpanKind(SpanKind.CLIENT).startSpan();
        // Make the span the current span
        try (Scope scope = span.makeCurrent()) {
            io.opentelemetry.context.Context context = io.opentelemetry.context.Context.current();

            String uri = "http://localhost:8082";
            String userAgent = "java.net.HttpURLConnection";

            // Span attributes
            span.setAttribute("http.method", "GET");
            span.setAttribute("http.request.method", "GET");
            span.setAttribute("http.url", uri);

            span.setAttribute("uri", uri);
            span.setAttribute("demo.service.request", 1);
            URL obj = URI.create(uri).toURL();
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            openTelemetry.getPropagators().getTextMapPropagator().inject(
                    context,
                    con,
                    setter);

            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", userAgent);
            int responseCode = con.getResponseCode();
            System.out.println("GET Response Code :: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) { // success
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // print result
                log.info("Demo Service response 1: " + response.toString());
            } else {
                log.info("GET request did not work.");
            }
        } catch (Exception t) {
            span.recordException(t);
            // throw t;
        } finally {
            span.end();
        }
    }

    private void CallDemoService2() {
        Span span = tracer.spanBuilder("get_demoService").setSpanKind(SpanKind.CLIENT).startSpan();
        // Make the span the current span
        try (Scope scope = span.makeCurrent()) {

            String uri = "http://localhost:8082";
            span.setAttribute("http.method", "GET");
            span.setAttribute("http.request.method", "GET");
            span.setAttribute("http.url", uri);
            span.setAttribute("uri", uri);
            span.setAttribute("demo.service.request", 2);

            RestTemplate restTemplate = new RestTemplate();
            String resp = restTemplate.getForObject(uri, String.class);

            span.setAttribute("demo.service.response", resp);
            log.info("Demo Service response 2: " + resp.toString());
        } catch (Exception t) {
            span.recordException(t);
            // throw t;
        } finally {
            span.end();
        }
    }

    @Autowired
    CreateOrderConsumer(OpenTelemetry openTelemetry) {

        this.openTelemetry = openTelemetry;
        tracer = openTelemetry.getTracer(DemoConsumerApplication.class.getName(), "0.1.0");
    }
}
