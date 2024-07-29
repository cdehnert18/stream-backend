package de.clemens.stream.controller;

import de.clemens.stream.dto.ApiResponse;
import de.clemens.stream.dto.ApiResponseStatus;
import de.clemens.stream.entity.Comment;
import de.clemens.stream.entity.User;
import de.clemens.stream.service.CommentService;
import de.clemens.stream.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    CommentService commentService;
    @Autowired
    UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Comment>>> getVideoComments(@RequestParam(value = "videoId", required = true) String videoId) {
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<List<Comment>>(ApiResponseStatus.success, "Got list of comments", commentService.getComments(videoId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> createComment(@RequestParam(value = "videoId", required = true) String videoId,
                                                             @RequestBody String text) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.getUserByEmail(authentication.getName());

        if(!commentService.addComment(currentUser, videoId, text)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<String>(ApiResponseStatus.error, "Could not create comment"));
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<String>(ApiResponseStatus.success, "Comment created"));
        }
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<String>> deleteComment(@RequestParam(value = "commentId", required = true) String commentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.getUserByEmail(authentication.getName());

        if(!commentService.deleteComment(currentUser, commentId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<String>(ApiResponseStatus.error, "Could not delete comment"));
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<String>(ApiResponseStatus.success, "Comment deleted"));
        }
    }
}
