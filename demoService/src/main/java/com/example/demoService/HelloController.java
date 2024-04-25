package com.example.demoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import jakarta.servlet.http.HttpServletRequest;

@RestController
public class HelloController {

    private static final Logger log = LoggerFactory.getLogger(HelloController.class);
    private final Tracer tracer;
    private final OpenTelemetry openTelemetry;
    @Autowired
    private HttpServletRequest request;

    TextMapGetter<HttpServletRequest> getter = new TextMapGetter<HttpServletRequest>() {
        @Override
        public String get(HttpServletRequest carrier, String key) {
            if (carrier.getHeader(key) != null) {
                return carrier.getHeader(key);
            }
            return null;
        }

        @Override
        public Iterable<String> keys(HttpServletRequest carrier) {
            // return carrier.getHeaderNames();
            return java.util.Collections.list(carrier.getHeaderNames());
        }
    };

    @GetMapping("/")
    public String index() {

        // Extract the SpanContext and other elements from the request.
        io.opentelemetry.context.Context extractedContext = openTelemetry.getPropagators()
                .getTextMapPropagator().extract(
                        io.opentelemetry.context.Context.root(),
                        request,
                        getter);

        try (Scope scope = extractedContext.makeCurrent()) {
            // Automatically use the extracted SpanContext as parent.
            Span serverSpan = tracer.spanBuilder("get_demoService")
                    .setSpanKind(SpanKind.SERVER)
                    .startSpan();

            try {
                // Add the attributes defined in the Semantic Conventions
                serverSpan.setAttribute("http.method", "GET");
                serverSpan.setAttribute("http.scheme", "http");
                serverSpan.setAttribute("http.host", "localhost:8082");
                serverSpan.setAttribute("http.target", "/");

                String msg = "Greetings from Spring Boot service!";
                log.info("return message: " + msg);
                return msg;
                /*
                 * } catch (Throwable t) {
                 * remoteContext.recordException(t);
                 * throw t;
                 */
            } finally {
                serverSpan.end();
            }
        }
    }

    @Autowired
    HelloController(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;

        tracer = openTelemetry.getTracer(DemoServiceApplication.class.getName(), "0.1.0");
    }
}