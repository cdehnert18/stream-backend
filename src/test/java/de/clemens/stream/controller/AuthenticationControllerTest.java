package de.clemens.stream.controller;

import de.clemens.stream.dto.AuthRequest;
import de.clemens.stream.dto.GenericResponse;
import de.clemens.stream.dto.RegistrationRequest;
import de.clemens.stream.entity.User;
import de.clemens.stream.service.AuthService;
import de.clemens.stream.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @Test
    public void testRegister() throws Exception {
        RegistrationRequest registrationRequest = new RegistrationRequest("test@example.com", "testuser", "password");

        when(userService.registerUser(any(String.class), any(String.class), any(String.class)))
                .thenReturn(new User());

        mockMvc.perform(post("/api/auth/register")
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\", \"password\":\"password\"}"))
                .andExpect(status().isOk());
    }
}
