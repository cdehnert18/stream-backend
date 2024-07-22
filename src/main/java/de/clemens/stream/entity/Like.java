package de.clemens.stream.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "likes")
@IdClass(LikeId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Like {

    @Id
    @ManyToOne
    @JoinColumn(name = "user_email", nullable = false)
    private User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "video_id", nullable = false, columnDefinition = "CHAR(36)")
    private Video video;

    @Column(name = "like_date", columnDefinition = "TIMESTAMP")
    private LocalDateTime likeDate = LocalDateTime.now();
}
