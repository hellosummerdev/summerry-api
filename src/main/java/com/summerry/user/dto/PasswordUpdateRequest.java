package com.summerry.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordUpdateRequest {
    @NotBlank
    private String currentPassword;

    @NotBlank
    private String newPassword;
}
