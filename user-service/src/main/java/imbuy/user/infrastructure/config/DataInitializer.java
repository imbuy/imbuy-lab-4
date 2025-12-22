package imbuy.user.infrastructure.config;

import imbuy.user.application.port.out.PasswordEncoderPort;
import imbuy.user.application.port.out.UserRepositoryPort;
import imbuy.user.domain.model.Role;
import imbuy.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;

    @Value("${app.security.default-supervisor.email:supervisor@imbuy.local}")
    private String defaultEmail;

    @Value("${app.security.default-supervisor.password:Supervisor123!}")
    private String defaultPassword;

    @Value("${app.security.default-supervisor.username:Supervisor}")
    private String defaultUsername;

    @EventListener(ApplicationReadyEvent.class)
    public void ensureSupervisorExists() {
        userRepository.existsByRole(Role.SUPERVISOR)
                .filter(exists -> !exists)
                .flatMap(unused ->
                        userRepository.save(
                                User.builder()
                                        .email(defaultEmail)
                                        .password(passwordEncoder.encode(defaultPassword))
                                        .username(defaultUsername)
                                        .role(Role.SUPERVISOR)
                                        .active(true)
                                        .build()
                        )
                )
                .doOnSuccess(u ->
                        log.info("Created default supervisor account with email {}", defaultEmail)
                )
                .subscribe();
    }
}
