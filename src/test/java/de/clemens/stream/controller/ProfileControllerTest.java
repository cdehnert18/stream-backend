package de.clemens.stream.controller;

import de.clemens.stream.dto.ProfileUpdateRequest;
import de.clemens.stream.entity.User;
import de.clemens.stream.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = "spring.config.name=application-test")
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private User mockUser;
    private MockHttpSession session;
    private String csrfToken;

    @BeforeEach
    public void setup() throws Exception {
        // Initialize a mock User
        mockUser = new User();
        mockUser.setEmail("test@example.com");
        mockUser.setUsername("testuser");
        mockUser.setPassword("encrypted-password");
        mockUser.setRoles(null);  // Set roles as null for the test

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
    public void testIsApiSecured() throws Exception {
        mockMvc.perform(get("/api/profiles/user"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    public void testGetUserProfile() throws Exception {
        // Arrange: Mock the UserService to return the mock user
        given(userService.getUserByEmail("test@example.com")).willReturn(mockUser);

        // Act & Assert: Perform the GET request and check the response
        mockMvc.perform(get("/api/profiles/user"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.password", nullValue()))
                .andExpect(jsonPath("$.roles", nullValue()));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    public void testUpdateProfileSuccess() throws Exception {
        // Arrange: Mock the UserService to validate and save the user
        ProfileUpdateRequest profileUpdateRequest = new ProfileUpdateRequest();
        profileUpdateRequest.setUsername("newusername");
        profileUpdateRequest.setCurrentPassword("currentpassword");
        profileUpdateRequest.setNewPassword("newpassword");
        profileUpdateRequest.setNewPasswordConfirmed("newpassword");

        given(userService.getUserByEmail(mockUser.getEmail())).willReturn(mockUser);
        given(userService.validateUser(anyString(), anyString())).willReturn(true);
        given(userService.saveUser(any(User.class))).willReturn(mockUser);

        // Act & Assert: Perform the PUT request and check the response
        mockMvc.perform(put("/api/profiles/user")
                        .session(session)
                        .header("X-CSRF-TOKEN", csrfToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(profileUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.message", is("Profile updated successfully")))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    public void testUpdateProfileInvalidUsername() throws Exception {
        // Arrange: Set an invalid username in ProfileUpdateRequest
        ProfileUpdateRequest profileUpdateRequest = new ProfileUpdateRequest();
        profileUpdateRequest.setUsername("");  // Invalid username
        profileUpdateRequest.setCurrentPassword("currentpassword");
        profileUpdateRequest.setNewPassword("newpassword");
        profileUpdateRequest.setNewPasswordConfirmed("newpassword");

        // Act & Assert: Perform the PUT request and expect a bad request status
        mockMvc.perform(put("/api/profiles/user")
                        .session(session)
                        .header("X-CSRF-TOKEN", csrfToken)

                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(profileUpdateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.message", is("Username is empty")))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    public void testUpdateProfilePasswordMismatch() throws Exception {
        // Arrange: Set mismatched new passwords in ProfileUpdateRequest
        ProfileUpdateRequest profileUpdateRequest = new ProfileUpdateRequest();
        profileUpdateRequest.setUsername("newusername");
        profileUpdateRequest.setCurrentPassword("currentpassword");
        profileUpdateRequest.setNewPassword("newpassword");
        profileUpdateRequest.setNewPasswordConfirmed("differentpassword"); // Mismatch

        // Act & Assert: Perform the PUT request and expect a bad request status
        mockMvc.perform(put("/api/profiles/user")
                        .session(session)
                        .header("X-CSRF-TOKEN", csrfToken)

                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(profileUpdateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.message", is("New password confirmation failed")))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    public void testUpdateProfileIncorrectCurrentPassword() throws Exception {
        // Arrange: Mock the UserService to invalidate the current password
        ProfileUpdateRequest profileUpdateRequest = new ProfileUpdateRequest();
        profileUpdateRequest.setUsername("newusername");
        profileUpdateRequest.setCurrentPassword("wrongpassword");
        profileUpdateRequest.setNewPassword("newpassword");
        profileUpdateRequest.setNewPasswordConfirmed("newpassword");

        given(userService.getUserByEmail(anyString())).willReturn(mockUser);
        given(userService.validateUser(anyString(), anyString())).willReturn(false); // Invalid password

        // Act & Assert: Perform the PUT request and expect a bad request status
        mockMvc.perform(put("/api/profiles/user")
                        .session(session)
                        .header("X-CSRF-TOKEN", csrfToken)

                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(profileUpdateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.message", is("Current password is incorrect")))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    // Utility method to convert an object to a JSON string
    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
