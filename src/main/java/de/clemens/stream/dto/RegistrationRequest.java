package de.clemens.stream.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequest {
    private String email;
    private String username;
    private String password;
    private String passwordConfirm;
}
