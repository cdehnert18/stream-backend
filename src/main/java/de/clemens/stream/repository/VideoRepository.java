package de.clemens.stream.repository;

import de.clemens.stream.entity.User;
import de.clemens.stream.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video, String> {
    Optional<Video> findById(String id);
}
