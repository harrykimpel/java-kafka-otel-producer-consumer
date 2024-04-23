package com.example.demoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

@RestController
public class HelloController {

    private static final Logger log = LoggerFactory.getLogger(HelloController.class);
    private final Tracer tracer;

    @GetMapping("/")
    public String index() {
        Span span = tracer.spanBuilder("greetFromHomeController").startSpan();

        // Make the span the current span
        try (Scope scope = span.makeCurrent()) {
            String msg = "Greetings from Spring Boot service!";
            log.info("return message: " + msg);
            return msg;
        } catch (Throwable t) {
            span.recordException(t);
            throw t;
        } finally {
            span.end();
        }
    }

    @Autowired
    HelloController(OpenTelemetry openTelemetry) {
        tracer = openTelemetry.getTracer(DemoServiceApplication.class.getName(), "0.1.0");
    }
}