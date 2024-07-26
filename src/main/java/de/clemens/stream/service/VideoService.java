package de.clemens.stream.service;
import de.clemens.stream.entity.User;
import de.clemens.stream.entity.Video;
import de.clemens.stream.repository.VideoRepository;
import de.clemens.stream.security.ApplicationUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

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
    //public static final String PATH = "/home/clemensd/Downloads/Volkswagen.mp4";
    public static final String STORAGE_LOCATION = "/home/clemensd/Downloads/";

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
            String path = optionalVideo.get().getPath();

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
    public Video save(MultipartFile file, User user) throws IOException {
        // Extrahiere Informationen aus der Datei
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        }

        // Speicherort für die Datei bestimmen
        String filePath = STORAGE_LOCATION + originalFilename;

        // Datei speichern
        File destinationFile = new File(filePath);
        file.transferTo(destinationFile);

        // Erstelle Video-Objekt und setze die Metadaten
        Video video = new Video();
        video.setTitle(originalFilename); // Titel auf den Dateinamen setzen
        video.setDescription("Beispielbeschreibung"); // Beschreibung
        video.setPath(filePath); // Dateipfad
        video.setFilename(originalFilename); // Dateiname
        video.setUser(user);

        // Video in der Datenbank speichern
        return videoRepository.save(video);
    }
}