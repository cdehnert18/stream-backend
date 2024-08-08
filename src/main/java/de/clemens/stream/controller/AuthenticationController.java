package de.clemens.stream.controller;

import de.clemens.stream.dto.ApiResponse;
import de.clemens.stream.dto.ApiResponseStatus;
import de.clemens.stream.dto.AuthRequest;
import de.clemens.stream.dto.RegistrationRequest;
import de.clemens.stream.service.AuthService;
import de.clemens.stream.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    private UserService userService;
    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@RequestBody RegistrationRequest registrationRequest) {
        if(userService.registerUser(registrationRequest.getEmail(), registrationRequest.getUsername(), registrationRequest.getPassword(), registrationRequest.getPasswordConfirm()) == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<String>(ApiResponseStatus.error, "Could not create user"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<String>(ApiResponseStatus.success, "User created"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody AuthRequest authRequest, HttpServletRequest request, HttpServletResponse response) {
        if(!authService.authenticate(authRequest, request, response)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<String>(ApiResponseStatus.error, "Could not log in"));
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<String>(ApiResponseStatus.success, "You are logged in"));
    }

    @GetMapping("/csrf")
    public void csrf() {

    }
}
