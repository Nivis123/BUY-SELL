package ru.prod.buysell;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.prod.buysell.VerificationRequest;
import ru.prod.buysell.VerificationCodeService;

@RestController
@RequestMapping("/api/verify")
@RequiredArgsConstructor
@Slf4j
public class VerificationController {

    private final VerificationCodeService verificationCodeService;

    @PostMapping
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody VerificationRequest request) {
        boolean isValid = verificationCodeService.validateCode(request.getEmail(), request.getCode());
        if (isValid) {
            log.info("Email {} успешно подтверждён.", request.getEmail());
            return ResponseEntity.ok("Email успешно подтверждён.");
        } else {
            log.warn("Неудачная попытка подтверждения для email: {}", request.getEmail());
            return ResponseEntity.badRequest().body("Неверный или истекший код подтверждения.");
        }
    }
}