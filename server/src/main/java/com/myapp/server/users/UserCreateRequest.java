package com.myapp.server.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserCreateRequest(
        @Email @NotBlank String email,
        @NotBlank String firstName,
        @NotBlank String lastName
) {}
