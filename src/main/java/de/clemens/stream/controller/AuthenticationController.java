package de.clemens.stream.controller;

import de.clemens.stream.dto.AuthRequest;
import de.clemens.stream.dto.GenericResponse;
import de.clemens.stream.dto.RegistrationRequest;
import de.clemens.stream.entity.User;
import de.clemens.stream.service.AuthService;
import de.clemens.stream.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    private UserService userService;
    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<GenericResponse<User>> register(@RequestBody RegistrationRequest registrationRequest) {
        User registeredUser = userService.registerUser(registrationRequest.getEmail(), registrationRequest.getUsername(), registrationRequest.getPassword());
        GenericResponse<User> response = new GenericResponse<>(HttpStatus.CREATED.value(), "User registered successfully", null);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    //@CrossOrigin("https://192.168.2.113:8080/video")
    public ResponseEntity<GenericResponse<Boolean>> login(@RequestBody AuthRequest authRequest, HttpServletRequest request, HttpServletResponse response) {
        GenericResponse<Boolean> authResponse = authService.authenticate(authRequest, request, response);
        if (authResponse.getStatusCode() == HttpStatus.OK.value()) {

            return ResponseEntity.ok(authResponse);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(authResponse);
        }
    }

    @GetMapping("/csrf")
    //@CrossOrigin("https://192.168.2.113:8080/video")
    public void csrf() {

    }
}
