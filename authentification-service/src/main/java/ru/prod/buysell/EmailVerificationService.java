package ru.prod.buysell;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.prod.buysell.dto.UserRegistrationRequest;
import ru.prod.buysell.entity.User;
import ru.prod.buysell.exception.BusinessException;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailVerificationService {

    private final VerificationCodeService verificationCodeService;
    private final EmailService emailService;
    private final UserService userService;

    private static final String ALLOWED_DOMAIN = "@g.nsu.ru";

    public void processRegistration(UserRegistrationRequest request) {
        String email = request.getEmail();
        if (!isUniversityEmail(email)) {
            log.warn("Попытка регистрации с недопустимым доменом email: {}", email);
            return;
        }

        User existingUser = userService.getUserByEmail(email);
        if (existingUser != null) {
            if (existingUser.isActive()) {
                log.warn("Попытка повторной регистрации активного пользователя: {}", email);
                throw new BusinessException("Пользователь с таким email уже зарегистрирован");
            }
            log.info("Пользователь {} уже ожидает верификации, отправляем код повторно", email);
        } else {
            userService.createPendingUser(request);
        }

        String code = verificationCodeService.generateCode();
        log.info("Сгенерирован код для {}: {}", email, code);

        String subject = "Подтверждение email для BuySellAcadem";
        String text = String.format(
                "Здравствуйте, %s!\n\nВаш код подтверждения: %s\n\nКод действителен в течение 5 минут.\n\nС уважением,\nКоманда BuySellAcadem",
                request.getName() != null ? request.getName() : email,
                code
        );
        emailService.sendEmail(email, subject, text);
        log.info("Письмо с кодом отправлено на: {}", email);

        verificationCodeService.storeCode(email, code);
    }

    private boolean isUniversityEmail(String email) {
        return email != null && email.endsWith(ALLOWED_DOMAIN);
    }
}