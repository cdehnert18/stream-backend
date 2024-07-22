package de.clemens.stream.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @Column(name = "id", length = 36, columnDefinition = "CHAR(36)")
    private String id = UUID.randomUUID().toString();

    @ManyToOne
    @JoinColumn(name = "user_email", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @Column(name = "comment_date", columnDefinition = "TIMESTAMP")
    private LocalDateTime commentDate = LocalDateTime.now();

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
}
