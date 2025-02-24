package com.example.demoProducer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;

@RequestMapping("/orders")
@RestController
public class OrderController {

    private final CreateOrderProducer createOrderProducer;
    // private final Tracer tracer;
    private final OpenTelemetry _openTelemetry;

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Order order) throws ExecutionException, InterruptedException {
        try {
            // createOrderProducer.sendCreateOrderEvent(order);
            createOrderProducer.publishWithErrorHandlerExample("gen-lang-client-0397825723",
                    "otel",
                    order, _openTelemetry);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Autowired
    OrderController(CreateOrderProducer createOrderProducer, OpenTelemetry openTelemetry) {
        this.createOrderProducer = createOrderProducer;
        _openTelemetry = openTelemetry;
        // tracer = openTelemetry.getTracer(DemoProducerApplication.class.getName(),
        // "0.1.0");
    }
}
