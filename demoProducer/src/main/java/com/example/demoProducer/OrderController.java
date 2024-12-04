package com.example.demoProducer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RequestMapping("/orders")
@RestController
public class OrderController {

    private final CreateOrderProducer createOrderProducer;

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Order order) throws ExecutionException, InterruptedException {
        try {
            // createOrderProducer.sendCreateOrderEvent(order);
            createOrderProducer.publishWithErrorHandlerExample("project-id",
                    "otel",
                    order);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Autowired
    OrderController(CreateOrderProducer createOrderProducer) {
        this.createOrderProducer = createOrderProducer;
    }
}
