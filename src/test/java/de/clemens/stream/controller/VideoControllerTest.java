package de.clemens.stream.controller;

import de.clemens.stream.entity.User;
import de.clemens.stream.entity.Video;
import de.clemens.stream.service.FilesStorageService;
import de.clemens.stream.service.UserService;
import de.clemens.stream.service.VideoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.*;
@SpringBootTest(properties = "spring.config.name=application-test")
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class VideoControllerTest {
    private MockMvc mockMvc;

    @MockBean
    private VideoService videoService;

    @MockBean
    private UserService userService;

    @MockBean
    private FilesStorageService storageService;

    @InjectMocks
    private VideoController videoController;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(videoController).build();

        // Mock Security Context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user@example.com");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void testUploadFile_Success() throws Exception {
        // Mock user
        User mockUser = new User();
        mockUser.setEmail("user@example.com");
        when(userService.getUserByEmail(any(String.class))).thenReturn(mockUser);

        // Mock files
        MockMultipartFile videoFile = new MockMultipartFile(
                "videoFile", "video.mp4", MediaType.MULTIPART_FORM_DATA_VALUE, "video content".getBytes());
        MockMultipartFile thumbnailFile = new MockMultipartFile(
                "thumbnailFile", "thumbnail.png", MediaType.MULTIPART_FORM_DATA_VALUE, "thumbnail content".getBytes());

        // Mock video service to return non-null (success)
        when(videoService.save(eq(videoFile), eq(thumbnailFile), eq(mockUser), eq("Video Title"), eq("Video Description")))
                .thenReturn(new Video());

        // Perform request and assert result
        mockMvc.perform(multipart("/api/videos/upload")
                        .file(videoFile)
                        .file(thumbnailFile)
                        .param("videoTitle", "Video Title")
                        .param("videoDescription", "Video Description")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(HttpStatus.OK.value())))
                .andExpect(jsonPath("$.message", is("Uploaded the file successfully: video.mp4")))
                .andExpect(jsonPath("$.data", nullValue()));

        // Verify interactions
        verify(storageService, times(1)).save(videoFile);
        verify(storageService, times(1)).save(thumbnailFile);
        verify(videoService, times(1)).save(videoFile, thumbnailFile, mockUser, "Video Title", "Video Description");
    }

    @Test
    public void testUploadFile_Failure() throws Exception {
        // Mock user
        User mockUser = new User();
        mockUser.setEmail("user@example.com");
        when(userService.getUserByEmail(any(String.class))).thenReturn(mockUser);

        // Mock files
        MockMultipartFile videoFile = new MockMultipartFile(
                "videoFile", "video.mp4", MediaType.MULTIPART_FORM_DATA_VALUE, "video content".getBytes());
        MockMultipartFile thumbnailFile = new MockMultipartFile(
                "thumbnailFile", "thumbnail.png", MediaType.MULTIPART_FORM_DATA_VALUE, "thumbnail content".getBytes());

        // Mock video service to throw exception
        when(videoService.save(eq(videoFile), eq(thumbnailFile), eq(mockUser), eq("Video Title"), eq("Video Description")))
                .thenReturn(null);  // simulate failure

        // Perform request and assert result
        mockMvc.perform(multipart("/api/videos/upload")
                        .file(videoFile)
                        .file(thumbnailFile)
                        .param("videoTitle", "Video Title")
                        .param("videoDescription", "Video Description")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("Could not upload the file: video.mp4"))
                .andExpect(jsonPath("$.data", nullValue()));

        // Verify interactions
        verify(storageService, times(1)).save(videoFile);
        verify(storageService, times(1)).save(thumbnailFile);
        verify(videoService, times(1)).save(videoFile, thumbnailFile, mockUser, "Video Title", "Video Description");
    }
}
