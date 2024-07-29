package de.clemens.stream.controller;

import de.clemens.stream.dto.ApiResponse;
import de.clemens.stream.dto.ApiResponseStatus;
import de.clemens.stream.entity.User;
import de.clemens.stream.service.LikeService;
import de.clemens.stream.service.UserService;
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
    public ResponseEntity<ApiResponse<Integer>> getVideoLikes(@RequestParam(value = "videoId", required = true) String videoId) {
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<Integer>(ApiResponseStatus.success, "Got likes of video", likeService.getLikes(videoId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> toggleVideoLike(@RequestParam(value = "videoId", required = true) String videoId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.getUserByEmail(authentication.getName());

        String message = likeService.toggleLike(videoId, currentUser);
        if(message.contains("not")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<String>(ApiResponseStatus.error, message));
        }

        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<String>(ApiResponseStatus.success, message));
    }
}
