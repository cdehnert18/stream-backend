package de.clemens.stream.service;
import de.clemens.stream.entity.User;
import de.clemens.stream.entity.Video;
import de.clemens.stream.repository.VideoRepository;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class VideoService {
    @Autowired
    VideoRepository videoRepository;
    private final Logger logger = LoggerFactory.getLogger(VideoService.class);
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String FILE_TYPE = "mp4";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String VIDEO_CONTENT = "video/";
    public static final String CONTENT_RANGE = "Content-Range";
    public static final String ACCEPT_RANGES = "Accept-Ranges";
    public static final String BYTES = "bytes";
    public static final int CHUNK_SIZE = 314700;

    @Value("${fileStorage.path}")
    public String STORAGE_LOCATION;

    /**
     * Prepare the content.
     *
     * @param id String.
     * @param range    String.
     * @return ResponseEntity.
     */
    public ResponseEntity<byte[]> getVideo(final String id, final String range) {

        Optional<Video> optionalVideo = videoRepository.findById(id);

        try {
            if (!optionalVideo.isPresent()) throw new IOException();
            String path = optionalVideo.get().getVideoPath();

            long rangeStart = 0;
            long rangeEnd = CHUNK_SIZE;
            final Long fileSize = getFileSize(path);
            if (range == null) {
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .header(CONTENT_TYPE, VIDEO_CONTENT + FILE_TYPE)
                        .header(ACCEPT_RANGES, BYTES)
                        .header(CONTENT_LENGTH, String.valueOf(rangeEnd))
                        .header(CONTENT_RANGE, BYTES + " " + rangeStart + "-" + rangeEnd + "/" + fileSize)
                        .header(CONTENT_LENGTH, String.valueOf(fileSize))
                        .body(readByteRange(id, rangeStart, rangeEnd)); // Read the object and convert it as bytes
            }
            String[] ranges = range.split("-");
            rangeStart = Long.parseLong(ranges[0].substring(6));
            if (ranges.length > 1) {
                rangeEnd = Long.parseLong(ranges[1]);
            } else {
                rangeEnd = rangeStart + CHUNK_SIZE;
            }

            rangeEnd = Math.min(rangeEnd, fileSize - 1);
            final byte[] data = readByteRange(path, rangeStart, rangeEnd);
            final String contentLength = String.valueOf((rangeEnd - rangeStart) + 1);
            HttpStatus httpStatus = HttpStatus.PARTIAL_CONTENT;
            if (rangeEnd >= fileSize) {
                httpStatus = HttpStatus.OK;
            }
            return ResponseEntity.status(httpStatus)
                    .header(CONTENT_TYPE, VIDEO_CONTENT + FILE_TYPE)
                    .header(ACCEPT_RANGES, BYTES)
                    .header(CONTENT_LENGTH, contentLength)
                    .header(CONTENT_RANGE, BYTES + " " + rangeStart + "-" + rangeEnd + "/" + fileSize)
                    .body(data);
        } catch (IOException e) {
            logger.error("Exception while reading the file {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }


    }

    /**
     * ready file byte by byte.
     *
     * @param path String.
     * @param start    long.
     * @param end      long.
     * @return byte array.
     * @throws IOException exception.
     */
    public byte[] readByteRange(String path, long start, long end) throws IOException {
        Path p = Path.of(path);
        byte[] data = Files.readAllBytes(p);
        byte[] result = new byte[(int) (end - start) + 1];
        System.arraycopy(data, (int) start, result, 0, (int) (end - start) + 1);
        return result;
    }

    /**
     * Content length.
     *
     * @param path String.
     * @return Long.
     */
    public Long getFileSize(String path) throws IOException {
        /*return Optional.ofNullable(fileName)
                //.map(file -> Paths.get(getFilePath(), file))
                .map(file -> path)
                .map(this::sizeFromFile)
                .orElse(0L);*/
        return Files.size(Path.of(path));
    }

    /**
     * Getting the size from the path.
     *
     * @param path Path.
     * @return Long.
     */
    private Long sizeFromFile(Path path) {
        try {
            return Files.size(path);
        } catch (IOException ioException) {
            logger.error("Error while getting the file size", ioException);
        }
        return 0L;
    }
    public Video save(MultipartFile videoFile, MultipartFile thumbnailFile, User user, String videoTitle, String videoDescription) throws IOException {

        String originalVideoFilename = videoFile.getOriginalFilename();
        if (originalVideoFilename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        }
        String originalThumbnailFilename = thumbnailFile.getOriginalFilename();
        if (originalThumbnailFilename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        }

        // Speicherort für die Datei bestimmen
        String videoFilePath = STORAGE_LOCATION + originalVideoFilename;
        String thumbnailFilePath = STORAGE_LOCATION + originalThumbnailFilename;

        // Datei speichern
        File videoDestinationFile = new File(videoFilePath);
        videoFile.transferTo(videoDestinationFile);

        File thumbnailDestinationFile = new File(thumbnailFilePath);
        thumbnailFile.transferTo(thumbnailDestinationFile);

        // Erstelle Video-Objekt und setze die Metadaten
        Video video = new Video();
        video.setTitle(videoTitle);
        video.setDescription(videoDescription);
        video.setVideoPath(videoFilePath);
        video.setThumbnailPath(thumbnailFilePath);
        video.setUser(user);

        // Video in der Datenbank speichern
        return videoRepository.save(video);
    }

    private String removeExtension(final String filename) {
        if (filename == null || filename.isEmpty()) {
            return filename;
        }

        int lastDotIndex = filename.lastIndexOf('.');

        if (lastDotIndex == -1 || lastDotIndex == 0) {
            return filename;
        }

        return filename.substring(0, lastDotIndex);
    }

    public List<Video> searchVideos(String keyword) {
        User u = new User();

        List<Video> videos = videoRepository.findTop10ByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
        for(Video video : videos) {
            u.setUsername(video.getUser().getUsername());
            video.setUser(u);
            video.setVideoPath(null);
            video.setThumbnailPath(null);
        }
        return videoRepository.findTop10ByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
    }
}