package ru.prod.buysell;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import ru.prod.buysell.dto.UserRegistrationRequest;
import tools.jackson.databind.json.JsonMapper;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {


    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public JsonMapper jsonMapper(ObjectMapper objectMapper) {
        return JsonMapper.builder().build();
    }


    @Bean
    public ConsumerFactory<String, UserRegistrationRequest> consumerFactory(
            ObjectMapper objectMapper
    )
    {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "wherehouse-group");

        JacksonJsonDeserializer<UserRegistrationRequest> isondeserializer =
                new JacksonJsonDeserializer<>(JsonMapper.builder().build());

        isondeserializer.addTrustedPackages("*");
        return new DefaultKafkaConsumerFactory<>(
                properties,
                new StringDeserializer(),
                isondeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserRegistrationRequest> kafkaListenerContainerFactory(
            ConsumerFactory<String, UserRegistrationRequest> consumerFactory) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, UserRegistrationRequest>();
        factory.setConcurrency(1);
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
}
