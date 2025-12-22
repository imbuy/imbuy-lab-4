package imbuy.user.infrastructure.persistence.adapter;

import imbuy.user.application.port.out.UserRepositoryPort;
import imbuy.user.domain.model.Role;
import imbuy.user.domain.model.User;
import imbuy.user.infrastructure.persistence.mapper.UserPersistenceMapper;
import imbuy.user.infrastructure.persistence.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final JpaUserRepository repo;
    private final UserPersistenceMapper mapper;

    @Override
    public Mono<User> save(User user) {
        return Mono.fromCallable(() -> repo.save(mapper.toEntity(user)))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<User> findById(Long id) {
        return Mono.fromCallable(() -> repo.findById(id))
                .flatMap(Mono::justOrEmpty)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<User> findByEmail(String email) {
        return Mono.fromCallable(() -> repo.findByEmail(email))
                .flatMap(Mono::justOrEmpty)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByEmail(String email) {
        return Mono.fromCallable(() -> repo.existsByEmail(email));
    }

    @Override
    public Mono<Boolean> existsByRole(Role role) {
        return Mono.fromCallable(() -> repo.existsByRole(role));
    }

    @Override
    public Flux<User> findAll(int page, int size) {
        return Mono.fromCallable(() -> repo.findAll(PageRequest.of(page, size)))
                .flatMapMany(p -> Flux.fromIterable(p.getContent()))
                .map(mapper::toDomain);
    }
}

