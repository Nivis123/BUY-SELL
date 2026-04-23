package ru.prod.buysell.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import ru.prod.buysell.dto.UserRegistrationRequest;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RegistrationKafkaProducerTest {

    @Mock
    private KafkaTemplate<String, UserRegistrationRequest> kafkaTemplate;

    @InjectMocks
    private RegistrationKafkaProducer producer;

    @Test
    void sendRegistrationRequestToKafka() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("test@example.com");
        request.setName("Test");

        producer.sendRegistrationRequestToKafka(request);

        verify(kafkaTemplate).send("registration", request);
    }
}