package imbuy.user.application.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserDto user
) {
}

