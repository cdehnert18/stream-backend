package de.clemens.stream.controller;

import de.clemens.stream.entity.User;
import de.clemens.stream.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SimpleController {

    @Autowired
    UserService userService;

    @GetMapping("/number")
    public int getNumber() {
        return 42;
    }
    @GetMapping("/userMail")
    public User getUserMail(@RequestParam String mail) {
        return userService.getUserByEmail(mail);
    }

    @GetMapping("/userUsername")
    public List<User> getUserUsername(@RequestParam String username) {
        return userService.getUserByUsername(username);
    }
}
