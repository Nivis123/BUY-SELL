package ru.prod.buysell.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import ru.prod.buysell.dto.LoginRequest;
import ru.prod.buysell.dto.UserRegistrationRequest;
import ru.prod.buysell.service.EmailService;
import ru.prod.buysell.service.RegistrationKafkaProducer;
import ru.prod.buysell.service.UserService;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import java.util.Collection;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
    private final EmailService emailService;
    private final RegistrationKafkaProducer registrationKafkaProducer;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            securityContextRepository.saveContext(SecurityContextHolder.getContext(), request, response);

            return ResponseEntity.ok(new AuthResponse(authentication.getName(), authentication.getAuthorities()));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Неверный email или пароль");
        }
    }


    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistrationRequest request) {
        //Send to Kafka
        //userService.createUser(request);
        registrationKafkaProducer.sendRegistrationRequestToKafka(request);
        return ResponseEntity.ok("Регистрация прошла успешно");
    }

    @GetMapping("/status")
    public ResponseEntity<AuthStatus> getAuthStatus(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(g -> g.getAuthority().equals("ROLE_ADMIN"));
            return ResponseEntity.ok(new AuthStatus(true, authentication.getName(), isAdmin));
        }
        return ResponseEntity.ok(new AuthStatus(false, null, false));
    }

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }

    @GetMapping("/hello")
    public void hello() {
        emailService.sendEmail(
                "a.zhusalin@g.nsu.ru",
                "Hello,world!",
                "Hello,from buysell!"
        );
    }

    record AuthResponse(String username, Collection<? extends GrantedAuthority> authorities) {}
    record AuthStatus(boolean authenticated, String username, boolean admin) {}
}