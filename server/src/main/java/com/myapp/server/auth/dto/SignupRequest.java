package com.myapp.server.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SignupRequest {
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, message = "הסיסמה חייבת להיות באורך של לפחות 8 תווים")
    private String password;

    @NotBlank
    @Size(min = 2, message = "שם מלא חייב להיות באורך של לפחות 2 תווים")
    private String fullName;

    private String phone;
}
