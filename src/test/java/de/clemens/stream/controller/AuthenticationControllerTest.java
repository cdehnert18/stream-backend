package de.clemens.stream.controller;

import de.clemens.stream.dto.AuthRequest;
import de.clemens.stream.dto.RegistrationRequest;
import de.clemens.stream.entity.User;
import de.clemens.stream.service.AuthService;
import de.clemens.stream.service.UserService;
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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    private MockHttpSession session;
    private String csrfToken;

    @BeforeEach
    public void setup() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/auth/csrf"))
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

        when(userService.registerUser(any(String.class), any(String.class), eq("pw"), eq("pw")))
                .thenReturn(new User());

        mockMvc.perform(post("/api/auth/register")
                        .session(session)
                        .header("X-CSRF-TOKEN", csrfToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\", \"username\":\"testuser\", \"password\":\"pw\", \"passwordConfirm\":\"pw\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    public void testLogin() throws Exception {

        when(authService.authenticate(any(AuthRequest.class), any(HttpServletRequest.class), any(HttpServletResponse.class))).thenReturn(true);

        mockMvc.perform(post("/api/auth/login")
                        .session(session)
                        .header("X-CSRF-TOKEN", csrfToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\", \"password\":\"password\"}"))
                .andExpect(status().isOk());
    }

    @Test
    public void testLogout() throws Exception {

        when(authService.authenticate(any(AuthRequest.class), any(HttpServletRequest.class), any(HttpServletResponse.class))).thenReturn(true);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .session(session)
                        .header("X-CSRF-TOKEN", csrfToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\", \"password\":\"password\"}"))
                .andExpect(status().isOk()).andReturn();
        csrfToken = result.getResponse().getHeader("CSRF-TOKEN");

        // Now perform logout
        MvcResult logoutResult = mockMvc.perform(post("/api/auth/logout")
                        .session(session)
                        .header("X-CSRF-TOKEN", csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Verify that the session is invalidated
        assertTrue(session.isInvalid());

        // Verify that the response includes a Set-Cookie header that invalidates the JSESSIONID
        String setCookieHeader = logoutResult.getResponse().getHeader("Set-Cookie");
        assertTrue(setCookieHeader.contains("JSESSIONID=; Path=/; Max-Age=0;"));
    }
}
