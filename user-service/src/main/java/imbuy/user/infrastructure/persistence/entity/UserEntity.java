package imbuy.user.infrastructure.persistence.entity;

import imbuy.user.domain.model.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    String email;
    String password;
    String username;
    @Enumerated(EnumType.STRING)
    Role role;
    Boolean active;
    @Column(name = "created_at", updatable = false, insertable = false)
    LocalDateTime createdAt;
}

