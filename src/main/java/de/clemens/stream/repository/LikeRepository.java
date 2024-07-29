package de.clemens.stream.repository;

import de.clemens.stream.entity.Like;
import de.clemens.stream.entity.LikeId;
import de.clemens.stream.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<Like, LikeId> {
    int countByVideo(Video video);
}
