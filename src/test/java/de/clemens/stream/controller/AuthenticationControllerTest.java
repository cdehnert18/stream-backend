package de.clemens.stream.controller;

import de.clemens.stream.dto.AuthRequest;
import de.clemens.stream.dto.GenericResponse;
import de.clemens.stream.dto.RegistrationRequest;
import de.clemens.stream.entity.User;
import de.clemens.stream.service.AuthService;
import de.clemens.stream.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.http.ResponseCookie;

import java.util.Arrays;

@SpringBootTest(properties = "spring.config.name=application-test")
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthService authService;

    private MockHttpSession session;
    private String csrfToken;

    @BeforeEach
    public void setup() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/greeting"))
                .andExpect(status().isOk())
                .andReturn();

        // Extract CSRF Token from the response
        csrfToken = result.getResponse().getHeader("CSRF-TOKEN");

        // Extract the session from the result
        session = (MockHttpSession) result.getRequest().getSession();

        if (csrfToken == null) {
            throw new RuntimeException("CSRF Token is missing");
        }
        if (session == null) {
            throw new RuntimeException("Session is missing");
        }
    }

    @Test
    public void testRegister() throws Exception {
        RegistrationRequest registrationRequest = new RegistrationRequest("test@example.com", "testuser", "password");

        when(userService.registerUser(any(String.class), any(String.class), any(String.class)))
                .thenReturn(new User());

        mockMvc.perform(post("/api/auth/register")
                        .session(session)
                        .header("X-CSRF-TOKEN", csrfToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\", \"username\":\"testuser\", \"password\":\"password\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    public void testLogin() throws Exception {
        AuthRequest authRequest = new AuthRequest("test@example.com", "password");
        GenericResponse<Boolean> authResponse = new GenericResponse<>(HttpStatus.OK.value(), "Authenticated", true);

        when(authService.authenticate(any(AuthRequest.class), any(HttpServletRequest.class), any(HttpServletResponse.class)))
                .thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .session(session)
                        .header("X-CSRF-TOKEN", csrfToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\", \"password\":\"password\"}"))
                .andExpect(status().isOk());
    }
}
