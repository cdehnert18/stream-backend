package de.clemens.stream.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProfileUpdateRequest {
    String username;
    String currentPassword;
    String newPassword;
    String newPasswordConfirmed;
}
