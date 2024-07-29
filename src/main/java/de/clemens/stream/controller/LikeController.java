package de.clemens.stream.controller;

import de.clemens.stream.dto.GenericResponse;
import de.clemens.stream.entity.User;
import de.clemens.stream.service.LikeService;
import de.clemens.stream.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/likes")
public class LikeController {

    @Autowired
    LikeService likeService;
    @Autowired
    UserService userService;

    @GetMapping
    public int getVideoLikes(@RequestParam(value = "videoId", required = true) String videoId) {
        return likeService.getLikes(videoId);
    }

    @PostMapping
    public GenericResponse<String> toggleVideoLike(@RequestParam(value = "videoId", required = true) String videoId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.getUserByEmail(authentication.getName());

        String message = likeService.toggleLike(videoId, currentUser);
        if(message.contains("Error")) {
            return new GenericResponse<>(HttpStatus.BAD_REQUEST.value(), message, null);
        }
        if(message.contains("unliked")) {
            return new GenericResponse<>(HttpStatus.OK.value(), message, null);
        }

        return new GenericResponse<>(HttpStatus.CREATED.value(), message, null);
    }
}
