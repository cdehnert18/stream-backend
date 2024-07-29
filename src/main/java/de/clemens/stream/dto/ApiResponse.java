package de.clemens.stream.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
public class ApiResponse<T> {
    private ApiResponseStatus apiResponseStatus;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public ApiResponse(ApiResponseStatus apiResponseStatus, String message) {
        this.apiResponseStatus = apiResponseStatus;
        this.message = message;
    }

    public ApiResponse(ApiResponseStatus apiResponseStatus, String message, T data) {
        this.apiResponseStatus = apiResponseStatus;
        this.message = message;
        this.data = data;
    }
}

