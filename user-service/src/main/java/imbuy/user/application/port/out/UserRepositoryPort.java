package imbuy.user.application.port.out;

import imbuy.user.domain.model.Role;
import imbuy.user.domain.model.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepositoryPort {

    Mono<User> save(User user);
    Mono<User> findById(Long id);
    Mono<User> findByEmail(String email);

    Mono<Boolean> existsByEmail(String email);
    Mono<Boolean> existsByRole(Role role);

    Flux<User> findAll(int page, int size);
}
