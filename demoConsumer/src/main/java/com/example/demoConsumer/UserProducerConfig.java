package com.example.demoConsumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import io.opentelemetry.instrumentation.kafkaclients.v2_6.TracingProducerInterceptor;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class UserProducerConfig {

    @Value("${spring.kafka.order.bootstrap-servers}")
    private String bootstrapServers;

    // @Value("${sasl.jaas.config}")
    // private String saslJaaSConfig;

    // @Value("${security.protocol}")
    // private String securityProtocol;

    // @Value("${sasl.mechanism}")
    // private String saslMechanism;

    // @Value("${client.dns.lookup}")
    // private String clientDnsLookup;

    // @Value("${acks}")
    // private String acks;

    @Bean
    public <K, V> ProducerFactory<K, V> userProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(org.apache.kafka.clients.producer.ProducerConfig.INTERCEPTOR_CLASSES_CONFIG,
                TracingProducerInterceptor.class.getName());
        config.put(org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class);

        return new DefaultKafkaProducerFactory(config);
    }

    @Bean
    public <K, V> KafkaTemplate<K, V> userKafkaTemplate() {
        return new KafkaTemplate<>(userProducerFactory());
    }
}
