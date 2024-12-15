package com.example.demoProducer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

// OTel imports

@SpringBootApplication
public class DemoProducerApplication {

	// Get OTel config parameters from application.properties

	public static void main(String[] args) {
		// Set System property for OTel config

		SpringApplication app = new SpringApplication(DemoProducerApplication.class);
		app.setBannerMode(Banner.Mode.OFF);
		app.run(args);
	}

	// Configure the OpenTelemetry SDK with OTLP exporters
}
