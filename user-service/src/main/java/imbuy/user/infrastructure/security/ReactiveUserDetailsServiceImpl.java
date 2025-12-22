package imbuy.user.infrastructure.security;

import imbuy.user.application.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ReactiveUserDetailsServiceImpl implements ReactiveUserDetailsService {

    private final UserRepositoryPort repository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return repository.findByEmail(username)
                .map(UserPrincipal::new);
    }
}
