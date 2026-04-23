package ru.prod.buysell.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.prod.buysell.CoreApplication;
import ru.prod.buysell.CoreApplication;
import ru.prod.buysell.dto.LoginRequest;
import ru.prod.buysell.dto.UserRegistrationRequest;
import ru.prod.buysell.entity.User;
import ru.prod.buysell.enums.Role;
import ru.prod.buysell.repository.UserRepository;
import ru.prod.buysell.service.EmailService;
import ru.prod.buysell.service.RegistrationKafkaProducer;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = CoreApplication.class
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private RegistrationKafkaProducer kafkaProducer;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        testUser = new User();
        testUser.setEmail("k.zimaltynov@g.nsu.ru");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setName("Петров Василий");
        testUser.setPhoneNumber("+79123456789");
        testUser.setActive(true);
        testUser.setRoles(Set.of(Role.ROLE_ADMIN));
        userRepository.save(testUser);
    }

    @Test
    void loginSuccess() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("k.zimaltynov@g.nsu.ru");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("k.zimaltynov@g.nsu.ru"))
                .andExpect(jsonPath("$.authorities").isArray());
    }

    @Test
    void loginInvalidCredentials() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("k.zimaltynov@g.nsu.ru");
        loginRequest.setPassword("wrong");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Неверный email или пароль"));
    }

    @Test
    void registerSendsToKafka() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("password");
        request.setName("New User");
        request.setPhoneNumber("+79123456789");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Регистрация прошла успешно"));
    }

    @Test
    void getAuthStatusAuthenticated() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("k.zimaltynov@g.nsu.ru");
        loginRequest.setPassword("password123");

        var loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        mockMvc.perform(get("/api/auth/status")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.username").value("k.zimaltynov@g.nsu.ru"))
                .andExpect(jsonPath("$.admin").value(true));
    }

    @Test
    void getAuthStatusUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/auth/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(false))
                .andExpect(jsonPath("$.username").isEmpty())
                .andExpect(jsonPath("$.admin").value(false));
    }

    @Test
    void csrfToken() throws Exception {
        mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk());
    }
}