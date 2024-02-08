package com.example.demoProducer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Scope;

@RestController
public class HelloController {

    private final Tracer tracer;

    @GetMapping("/")
    public String index() {
        Span span = tracer.spanBuilder("greetFromHomeController").startSpan();

        // Make the span the current span
        try (Scope scope = span.makeCurrent()) {
            return "Greetings from Spring Boot!";
        } catch (Throwable t) {
            span.recordException(t);
            throw t;
        } finally {
            span.end();
        }
    }

    @Autowired
    HelloController(OpenTelemetry openTelemetry) {
        tracer = openTelemetry.getTracer(DemoProducerApplication.class.getName(), "0.1.0");
    }

}