package imbuy.user.application.dto;

import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(max = 100)
        String username,
        String password
) {
}

