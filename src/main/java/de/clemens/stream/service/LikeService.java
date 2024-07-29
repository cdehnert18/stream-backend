package de.clemens.stream.service;

import de.clemens.stream.entity.Like;
import de.clemens.stream.entity.LikeId;
import de.clemens.stream.entity.User;
import de.clemens.stream.entity.Video;
import de.clemens.stream.repository.LikeRepository;
import de.clemens.stream.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LikeService {
    @Autowired
    LikeRepository likeRepository;
    @Autowired
    VideoRepository videoRepository;

    public int getLikes(String videoId) {
        Optional<Video> video = videoRepository.findById(videoId);
        return video.map(value -> likeRepository.countByVideo(value)).orElse(0);
    }

    public String toggleLike(String videoId, User user) {
        // Check, if video exists
        Optional<Video> video = videoRepository.findById(videoId);
        if(video.isEmpty()) {
            return "Could not found video";
        }

        Like like = new Like();
        like.setUser(user);
        like.setVideo(video.get());

        // User already liked video
        if(likeRepository.existsById(new LikeId(user.getEmail(), videoId))) {
            likeRepository.delete(like);
            return "Video unliked";
        } else {
            // User does not already like video
            likeRepository.save(like);
            return "Video liked";
        }
    }
}
