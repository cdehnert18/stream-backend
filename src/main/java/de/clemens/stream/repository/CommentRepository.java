package de.clemens.stream.repository;

import de.clemens.stream.entity.Comment;
import de.clemens.stream.entity.Like;
import de.clemens.stream.entity.LikeId;
import de.clemens.stream.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {
    List<Comment> findByVideoId(String videoId);
}
