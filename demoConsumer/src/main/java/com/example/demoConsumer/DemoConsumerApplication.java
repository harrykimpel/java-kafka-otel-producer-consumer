package com.example.demoConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import com.google.cloud.spring.pubsub.integration.AckMode;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.Header;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.core.publisher.PubSubPublisherTemplate;
import com.google.cloud.spring.pubsub.core.subscriber.PubSubSubscriberTemplate;
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.annotation.ServiceActivator;

@SpringBootApplication
public class DemoConsumerApplication {

	private static final Logger log = LoggerFactory.getLogger(CreateOrderConsumer.class);

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(DemoConsumerApplication.class);
		app.setBannerMode(Banner.Mode.OFF);
		app.run(args);
	}

	// Create a message channel for messages arriving from the subscription
	// `sub-one`.
	@Bean
	public MessageChannel inputMessageChannel() {
		return new PublishSubscribeChannel();
	}

	// Create an inbound channel adapter to listen to the subscription `sub-one` and
	// send
	// messages to the input message channel.
	@Bean
	public PubSubInboundChannelAdapter inboundChannelAdapter(
			@Qualifier("inputMessageChannel") MessageChannel messageChannel,
			PubSubTemplate pubSubTemplate) {
		PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, "otel-sub");
		adapter.setOutputChannel(messageChannel);
		adapter.setAckMode(AckMode.MANUAL);
		adapter.setPayloadType(String.class);
		return adapter;
	}

	// Define what happens to the messages arriving in the message channel.
	@ServiceActivator(inputChannel = "inputMessageChannel")
	public void messageReceiver(
			String payload,
			@Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) BasicAcknowledgeablePubsubMessage message) {
		// LOGGER.info("Message arrived via an inbound channel adapter from sub-one!
		// Payload: " + payload);
		log.info("message received: " + message);
		message.ack();
	}
}
