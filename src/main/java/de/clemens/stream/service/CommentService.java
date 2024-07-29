package de.clemens.stream.service;

import de.clemens.stream.entity.*;
import de.clemens.stream.repository.CommentRepository;
import de.clemens.stream.repository.LikeRepository;
import de.clemens.stream.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentService {
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    VideoRepository videoRepository;

    public List<Comment> getComments(String videoId) {
        Optional<Video> video = videoRepository.findById(videoId);
        if(video.isEmpty()) {
            return null;
        }

        List<Comment> comments = commentRepository.findByVideoId(videoId);
        for(Comment comment : comments) {
            comment.getUser().setPassword(null);
            comment.getUser().setEmail(null);
            comment.getUser().setRoles(null);
        }

        return commentRepository.findByVideoId(videoId);
    }

    public boolean addComment(User user, String videoId, String text) {
        Optional<Video> video = videoRepository.findById(videoId);
        if(video.isEmpty()) {
            return false;
        }

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setVideo(video.get());
        comment.setContent(text);

        commentRepository.save(comment);

        return true;
    }

    public boolean deleteComment(User user, String commentId) {
        Optional<Comment> comment = commentRepository.findById(commentId);
        if(comment.isEmpty()) {
            return false;
        }

        if(user.equals(comment.get().getUser()) || user.equals(comment.get().getVideo().getUser())) {
            commentRepository.delete(comment.get());

            return true;
        }

        return false;
    }
}
