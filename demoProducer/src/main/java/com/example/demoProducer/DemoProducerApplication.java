package com.example.demoProducer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.ResourceAttributes;

@SpringBootApplication
public class DemoProducerApplication {

	@Value("${otel.exporter.otlp.endpoint}")
	private String otlpEndpoint;
	@Value("${otel.exporter.otlp.headers.api-key}")
	private String otlpHeadersApiKey;

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(DemoProducerApplication.class);
		app.setBannerMode(Banner.Mode.OFF);
		app.run(args);
	}

	@Bean
	public OpenTelemetry openTelemetry() {
		Resource resource = Resource.getDefault().toBuilder()
				.put(ResourceAttributes.SERVICE_NAME, "kafka-java-producer")
				.put(ResourceAttributes.SERVICE_VERSION, "0.1.0").build();

		SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
				.addSpanProcessor(BatchSpanProcessor.builder(OtlpGrpcSpanExporter.builder()
						.setEndpoint(
								otlpEndpoint)
						.addHeader("api-key",
								otlpHeadersApiKey)
						.build()).build())
				.setResource(resource)
				.build();

		SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
				.registerMetricReader(PeriodicMetricReader.builder(OtlpGrpcMetricExporter.builder()
						.setEndpoint(
								otlpEndpoint)
						.addHeader("api-key",
								otlpHeadersApiKey)
						.build()).build())
				.setResource(resource)
				.build();

		SdkLoggerProvider sdkLoggerProvider = SdkLoggerProvider.builder()
				.addLogRecordProcessor(
						BatchLogRecordProcessor.builder(OtlpGrpcLogRecordExporter.builder()
								.setEndpoint(
										otlpEndpoint)
								.addHeader("api-key",
										otlpHeadersApiKey)
								.build()).build())
				.setResource(resource)
				.build();

		OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
				.setTracerProvider(sdkTracerProvider)
				.setMeterProvider(sdkMeterProvider)
				.setLoggerProvider(sdkLoggerProvider)
				.setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
				.buildAndRegisterGlobal();

		return openTelemetry;
	}
}
