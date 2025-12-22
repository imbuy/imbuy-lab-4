package imbuy.user.application.dto;

import imbuy.user.domain.model.Role;
import imbuy.user.domain.model.User;

public record UserDto(
        Long id,
        String email,
        String username,
        Role role
) {
    public static UserDto from(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getRole()
        );
    }
}
