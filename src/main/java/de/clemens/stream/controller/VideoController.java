package de.clemens.stream.controller;

import de.clemens.stream.dto.GenericResponse;
import de.clemens.stream.service.FilesStorageService;
import de.clemens.stream.service.VideoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    @Autowired
    VideoService videoService;
    @Autowired
    FilesStorageService storageService;

    @GetMapping("/beispiel_video")
    public ResponseEntity<byte[]> streamVideo(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestHeader(value = "Range", required = false) String httpRangeList) {

        return videoService.getVideo("Volkswagen", "mp4", httpRangeList);
    }

    @PostMapping("/upload")
    @CrossOrigin("*")
    public ResponseEntity<GenericResponse<String>> uploadFile(@RequestParam("file") MultipartFile file) {
        String message = "";
        try {
            storageService.save(file);

            message = "Uploaded the file successfully: " + file.getOriginalFilename();
            return ResponseEntity.status(HttpStatus.OK).body(new GenericResponse<>(HttpStatus.OK.value(), message, null));
        } catch (Exception e) {
            message = "Could not upload the file: " + file.getOriginalFilename() + ". Error: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.OK).body(new GenericResponse<>(HttpStatus.BAD_REQUEST.value(),message, null));
        }
    }
}
