package de.clemens.stream.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GenericResponse<T> {

    private int statusCode;
    private String message;
    private T data;

    public static <T> GenericResponse<T> success(T data) {
        return new GenericResponse<>(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(), data);
    }

    public static <T> GenericResponse<T> error(int statusCode, String message) {
        return new GenericResponse<>(statusCode, message, null);
    }
}
