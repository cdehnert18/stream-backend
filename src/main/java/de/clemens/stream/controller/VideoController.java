package de.clemens.stream.controller;

import de.clemens.stream.service.VideoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    @Autowired
    VideoService videoService;

    @GetMapping("/beispiel_video")
    public ResponseEntity<byte[]> streamVideo(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestHeader(value = "Range", required = false) String httpRangeList) {

        return videoService.prepareContent("Volkswagen", "mp4", httpRangeList);
    }
}
