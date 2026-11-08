package com.property.Property_Service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.property.Property_Service.dto.PropertyEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean
    public ProducerFactory<String, PropertyEvent> producerFactory(ObjectMapper objectMapper) {

        Serializer<PropertyEvent> valueSerializer = (topic, data) -> {
            try {
                return objectMapper.writeValueAsBytes(data);
            } catch (Exception e) {
                throw new RuntimeException("Erreur sérialisation PropertyEvent", e);
            }
        };

        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        DefaultKafkaProducerFactory<String, PropertyEvent> factory =
                new DefaultKafkaProducerFactory<>(config);
        factory.setValueSerializer(valueSerializer);

        return factory;
    }

    @Bean
    public KafkaTemplate<String, PropertyEvent> kafkaTemplate(
            ProducerFactory<String, PropertyEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public NewTopic propertyEventsTopic() {
        return TopicBuilder.name("property-events")
                .partitions(1)
                .replicas(1)
                .build();
    }
}