package de.clemens.stream.controller;

import de.clemens.stream.dto.AuthRequest;
import de.clemens.stream.dto.GenericResponse;
import de.clemens.stream.entity.User;
import de.clemens.stream.service.AuthService;
import de.clemens.stream.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("/api")
public class SimpleController {

    @Autowired
    UserService userService;
    @Autowired
    private MessageSource messageSource;

    @GetMapping("/greeting")
    public String greeting(@RequestHeader(name = "Accept-Language", required = false) Locale locale) {
        return messageSource.getMessage("greeting", null, locale);
    }
    @GetMapping("/public")
    public String publicAccess() {
        return "Access granted for everyone";
    }
    @GetMapping("/user")
    public String userAccess() {
        return "Access granted for USER role";
    }
    @GetMapping("/admin")
    public String adminAccess() {
        return "Access granted for ADMIN role";
    }

    @PostMapping("/post")
    public String post() {
        return "TEst";
    }
}
