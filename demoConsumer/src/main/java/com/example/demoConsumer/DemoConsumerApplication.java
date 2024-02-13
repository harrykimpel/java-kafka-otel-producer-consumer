package com.example.demoConsumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoConsumerApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(DemoConsumerApplication.class);
		app.setBannerMode(Banner.Mode.OFF);
		app.run(args);
	}
}
