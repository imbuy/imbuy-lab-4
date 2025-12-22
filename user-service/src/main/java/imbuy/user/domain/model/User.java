package imbuy.user.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
public class User {

    private Long id;
    private String email;
    private String password;
    private String username;
    private Role role;
    private Boolean active;
    private LocalDateTime createdAt;

    public boolean isSupervisor() {
        return role == Role.SUPERVISOR;
    }
}
