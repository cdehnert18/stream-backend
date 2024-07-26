package de.clemens.stream.controller;

import de.clemens.stream.dto.GenericResponse;
import de.clemens.stream.entity.User;
import de.clemens.stream.service.FilesStorageService;
import de.clemens.stream.service.UserService;
import de.clemens.stream.service.VideoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    @Autowired
    VideoService videoService;
    @Autowired
    UserService userService;
    @Autowired
    FilesStorageService storageService;

    @GetMapping("/stream-videos")
    public ResponseEntity<byte[]> streamVideo(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "videoId", required = true) String id,
            @RequestHeader(value = "Range", required = false) String httpRangeList) {

        return videoService.getVideo(id, httpRangeList);
    }

    @PostMapping("/upload")
    //@CrossOrigin("https://192.168.2.113:8080/video")
    public ResponseEntity<GenericResponse<String>> uploadFile(@RequestParam("file") MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.getUserByEmail(authentication.getName());
        String message = "";
        try {
            storageService.save(file);
            if(videoService.save(file, currentUser) == null) throw new Exception();

            message = "Uploaded the file successfully: " + file.getOriginalFilename();
            return ResponseEntity.status(HttpStatus.OK).body(new GenericResponse<>(HttpStatus.OK.value(), message, null));
        } catch (Exception e) {
            message = "Could not upload the file: " + file.getOriginalFilename() + ". Error: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.OK).body(new GenericResponse<>(HttpStatus.BAD_REQUEST.value(),message, null));
        }
    }
}
