package ru.prod.buysell.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import ru.prod.buysell.dto.LoginRequest;
import ru.prod.buysell.dto.UserRegistrationRequest;
import ru.prod.buysell.service.EmailService;
import ru.prod.buysell.service.RegistrationKafkaProducer;
import ru.prod.buysell.service.UserService;

import java.util.Collection;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication management endpoints")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
    private final EmailService emailService;
    private final RegistrationKafkaProducer registrationKafkaProducer;

    @Operation(summary = "User login", description = "Authenticate user and create session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful login",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
    })
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

    @Operation(summary = "Register new user", description = "Send registration request to Kafka for async processing")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registration request accepted"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistrationRequest request) {
        registrationKafkaProducer.sendRegistrationRequestToKafka(request);
        return ResponseEntity.ok("Регистрация прошла успешно");
    }

    @Operation(summary = "Get authentication status", description = "Check if user is authenticated and admin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status retrieved",
                    content = @Content(schema = @Schema(implementation = AuthStatus.class)))
    })
    @GetMapping("/status")
    public ResponseEntity<AuthStatus> getAuthStatus(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(g -> g.getAuthority().equals("ROLE_ADMIN"));
            return ResponseEntity.ok(new AuthStatus(true, authentication.getName(), isAdmin));
        }
        return ResponseEntity.ok(new AuthStatus(false, null, false));
    }

    @Operation(summary = "Get CSRF token", description = "Retrieve CSRF token for state-changing operations")
    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }

    @Operation(summary = "Test email sending", description = "Send a test email (for debugging)")
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