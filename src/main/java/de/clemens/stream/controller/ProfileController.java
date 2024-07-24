package de.clemens.stream.controller;

import de.clemens.stream.dto.GenericResponse;
import de.clemens.stream.dto.ProfileUpdateRequest;
import de.clemens.stream.entity.User;
import de.clemens.stream.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    @Autowired
    UserService userService;

    @GetMapping("/user_profile")
    public User getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User u = userService.getUserByEmail(authentication.getName());
        u.setPassword(null);
        u.setRoles(null);
        return userService.getUserByEmail(authentication.getName());
    }

    @PutMapping("/user_profile")
    public ResponseEntity<GenericResponse<String>> updateProfile(@RequestBody ProfileUpdateRequest profileUpdateRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.getUserByEmail(authentication.getName());

        // Validate username
        if (isNullOrEmpty(profileUpdateRequest.getUsername())) {
            return buildErrorResponse("Username is empty");
        }

        // Validate password fields
        if (isNullOrEmpty(profileUpdateRequest.getCurrentPassword()) ||
                isNullOrEmpty(profileUpdateRequest.getNewPassword()) ||
                isNullOrEmpty(profileUpdateRequest.getNewPasswordConfirmed())) {

            return buildErrorResponse("Password fields can not be empty");
        }

        // Validate new password confirmation
        if (!profileUpdateRequest.getNewPassword().equals(profileUpdateRequest.getNewPasswordConfirmed())) {
            return buildErrorResponse("New password confirmation failed");
        }

        // Validate current password
        if (!userService.validateUser(currentUser.getEmail(), profileUpdateRequest.getCurrentPassword())) {
            return buildErrorResponse("Current password is incorrect");
        }

        // Update user details
        currentUser.setUsername(profileUpdateRequest.getUsername());
        currentUser.setPassword(profileUpdateRequest.getNewPassword());

        // Save the updated user profile
        if (userService.saveUser(currentUser) != null) {
            return ResponseEntity.ok(new GenericResponse<>(HttpStatus.OK.value(), "Profile updated successfully", null));
        }

        return buildErrorResponse("Cannot update profile");
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private ResponseEntity<GenericResponse<String>> buildErrorResponse(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GenericResponse<>(HttpStatus.BAD_REQUEST.value(), message, null));
    }
}
