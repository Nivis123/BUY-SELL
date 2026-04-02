package ru.prod.buysell.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import ru.prod.buysell.dto.UserRegistrationRequest;
import tools.jackson.databind.json.JsonMapper;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public JsonMapper jsonMapper(ObjectMapper objectMapper) {
        return JsonMapper.builder().build();
    }

    @Bean
    public ProducerFactory<String, UserRegistrationRequest> producerFactory(JsonMapper jsonMapper) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        JacksonJsonSerializer<UserRegistrationRequest> serializer =
                new JacksonJsonSerializer<>(jsonMapper);


        return new DefaultKafkaProducerFactory<>(
                configProps,
                new StringSerializer(),
                serializer
        );
    }

    @Bean
    public KafkaTemplate<String, UserRegistrationRequest> kafkaTemplate(
            ProducerFactory<String, UserRegistrationRequest> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}