package com.example.demoProducer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;

import java.util.concurrent.ExecutionException;

@RequestMapping("/orders")
@RestController
public class OrderController {

    private final CreateOrderProducer createOrderProducer;
    private final Tracer tracer;

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Order order) throws ExecutionException, InterruptedException {
        createOrderProducer.sendCreateOrderEvent(order, tracer);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Autowired
    OrderController(CreateOrderProducer createOrderProducer, OpenTelemetry openTelemetry) {
        this.createOrderProducer = createOrderProducer;
        tracer = openTelemetry.getTracer(DemoProducerApplication.class.getName(), "0.1.0");
    }
}
