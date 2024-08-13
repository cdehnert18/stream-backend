package de.clemens.stream.controller;

import de.clemens.stream.dto.ApiResponse;
import de.clemens.stream.dto.ApiResponseStatus;
import de.clemens.stream.entity.User;
import de.clemens.stream.entity.Video;
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

import java.util.List;

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
    @GetMapping("/search-videos")
    public ResponseEntity<ApiResponse<List<Video>>> searchVideo(HttpServletRequest request, HttpServletResponse response,
                                   @RequestParam(value = "keyword", required = true) String keyword) {

        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<List<Video>>(ApiResponseStatus.success, "Got videos for keyword: " + keyword, videoService.searchVideos(keyword)));
    }

    @GetMapping("/latest-videos")
    public ResponseEntity<ApiResponse<List<Video>>> latestVideo(HttpServletRequest request, HttpServletResponse response) {

        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<List<Video>>(ApiResponseStatus.success, "Got videos for keyword", videoService.getLatestVideos()));
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> uploadFile(@RequestParam("videoFile") MultipartFile videoFile,
                                                          @RequestParam("thumbnailFile") MultipartFile thumbnailFile,
                                                          @RequestParam(value = "videoTitle", required = true) String videoTitle,
                                                          @RequestParam(value = "videoDescription", required = true)  String videoDescription) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.getUserByEmail(authentication.getName());
        String message = "";
        try {
            storageService.save(videoFile);
            storageService.save(thumbnailFile);
            if(videoService.save(videoFile, thumbnailFile, currentUser, videoTitle, videoDescription) == null) throw new Exception();

            message = "Uploaded the file successfully: " + videoFile.getOriginalFilename();
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<String>(ApiResponseStatus.success, message));
        } catch (Exception e) {
            message = "Could not upload the file: " + videoFile.getOriginalFilename();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<String>(ApiResponseStatus.error, message));
        }
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<String>> deleteVideo(@RequestParam(value = "videoId", required = true) String videoId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.getUserByEmail(authentication.getName());

        if(videoService.deleteVideo(currentUser, videoId)) {
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<String>(ApiResponseStatus.success, "Video deleted"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<String>(ApiResponseStatus.error, "Could not delete video"));
        }
    }
}
