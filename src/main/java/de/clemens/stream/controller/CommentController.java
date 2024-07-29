package de.clemens.stream.controller;

import de.clemens.stream.dto.GenericResponse;
import de.clemens.stream.entity.Comment;
import de.clemens.stream.entity.User;
import de.clemens.stream.service.CommentService;
import de.clemens.stream.service.LikeService;
import de.clemens.stream.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    public List<Comment> getVideoComments(@RequestParam(value = "videoId", required = true) String videoId) {
        return commentService.getComments(videoId);
    }

    @PostMapping
    public GenericResponse<String> createComment(@RequestParam(value = "videoId", required = true) String videoId,
                                                 @RequestBody String text) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.getUserByEmail(authentication.getName());

        if(commentService.addComment(currentUser, videoId, text)) {
            return new GenericResponse<>(HttpStatus.CREATED.value(), "Comment created", null);
        } else {
            return new GenericResponse<>(HttpStatus.BAD_REQUEST.value(), "Error", null);
        }
    }

    @DeleteMapping
    public GenericResponse<String> deleteComment(@RequestParam(value = "commentId", required = true) String commentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.getUserByEmail(authentication.getName());

        if(commentService.deleteComment(currentUser, commentId)) {
            return new GenericResponse<>(HttpStatus.OK.value(), "Comment deleted", null);
        } else {
            return new GenericResponse<>(HttpStatus.BAD_REQUEST.value(), "Error", null);
        }
    }
}
