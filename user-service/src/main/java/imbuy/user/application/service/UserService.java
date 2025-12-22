package imbuy.user.application.service;

import imbuy.user.application.port.in.UserUseCase;
import imbuy.user.application.port.out.PasswordEncoderPort;
import imbuy.user.application.port.out.UserRepositoryPort;
import imbuy.user.domain.model.User;
import imbuy.user.domain.service.UserPolicy;
import imbuy.user.application.dto.UpdateUserRequest;
import imbuy.user.application.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class UserService implements UserUseCase {

    private final UserRepositoryPort repository;
    private final PasswordEncoderPort encoder;
    private final UserPolicy policy = new UserPolicy();

    @Override
    public Flux<UserDto> findAll(Pageable pageable) {
        return repository.findAll(pageable.getPageNumber(), pageable.getPageSize())
                .map(UserDto::from);
    }

    @Override
    public Mono<UserDto> findById(Long id, Long requesterId) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found")))
                .map(UserDto::from);
    }

    @Override
    public Mono<UserDto> updateProfile(Long id, UpdateUserRequest request, Long requesterId) {
        return repository.findById(id)
                .flatMap(user -> {
                    User updated = user.toBuilder()
                            .username(request.username() != null ? request.username() : user.getUsername())
                            .password(request.password() != null ? encoder.encode(request.password()) : user.getPassword())
                            .build();
                    return repository.save(updated);
                })
                .map(UserDto::from);
    }
}
