package imbuy.user.application.port.in;

import imbuy.user.application.dto.UpdateUserRequest;
import imbuy.user.application.dto.UserDto;
import imbuy.user.infrastructure.security.UserPrincipal;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.data.domain.Pageable;

public interface UserUseCase {

    Flux<UserDto> findAll(Pageable pageable);
    Mono<UserDto> findById(Long id, UserPrincipal requesterId);
    Mono<UserDto> updateProfile(Long id, UpdateUserRequest request, UserPrincipal requesterId);
}
