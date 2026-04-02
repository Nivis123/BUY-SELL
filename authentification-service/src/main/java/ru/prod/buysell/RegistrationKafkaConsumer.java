package ru.prod.buysell;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.prod.buysell.dto.UserRegistrationRequest;
import ru.prod.buysell.EmailVerificationService;

@Service
@Slf4j
@RequiredArgsConstructor
public class RegistrationKafkaConsumer {

    private final EmailVerificationService emailVerificationService;

    @KafkaListener(topics = "registration")
    public void consumeUserRegistrationRequest(UserRegistrationRequest userRegistrationRequest) {
        log.info("Получен запрос на регистрацию: {}", userRegistrationRequest);
        emailVerificationService.processRegistration(userRegistrationRequest);
    }
}