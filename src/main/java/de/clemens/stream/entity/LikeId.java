package de.clemens.stream.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LikeId implements Serializable {
    private String user;
    private String video;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LikeId likeId = (LikeId) o;
        return user.equals(likeId.user) && video.equals(likeId.video);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, video);
    }
}