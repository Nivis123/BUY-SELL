package ru.prod.buysell;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.prod.buysell.exception.BusinessException;

@RestController
@RequestMapping("/api/verify")
@RequiredArgsConstructor
@Slf4j
public class VerificationController {

    private final VerificationCodeService verificationCodeService;
    private final UserService userService;   // <-- добавляем

    @PostMapping
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody VerificationRequest request) {
        String email = request.getEmail();
        String code = request.getCode();

        if (!verificationCodeService.isValid(email, code)) {
            log.warn("Неверный или истекший код для {}", email);
            return ResponseEntity.badRequest().body("Неверный или истекший код подтверждения.");
        }

        try {
            userService.activateUser(email);
            verificationCodeService.deleteCode(email);
            log.info("Email {} успешно подтверждён, пользователь активирован.", email);
            return ResponseEntity.ok("Email успешно подтверждён.");
        } catch (BusinessException e) {
            log.error("Ошибка активации пользователя {}: {}", email, e.getMessage());
            return ResponseEntity.badRequest().body("Ошибка активации: " + e.getMessage());
        }
    }
}