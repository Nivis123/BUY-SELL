package ru.prod.buysell.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.prod.buysell.dto.UserRegistrationRequest;

@Service
@AllArgsConstructor
@Slf4j
public class RegistrationKafkaProducer {

    private final KafkaTemplate<String, UserRegistrationRequest> kafkaTemplate;
    private static final Logger logger = LoggerFactory.getLogger(RegistrationKafkaProducer.class);

    public void sendRegistrationRequestToKafka(UserRegistrationRequest userRegistrationRequest) {
        kafkaTemplate.send("registration", userRegistrationRequest);
        log.info("Registration request sent to Kafka by username={}.",userRegistrationRequest.getName());
    }


}
