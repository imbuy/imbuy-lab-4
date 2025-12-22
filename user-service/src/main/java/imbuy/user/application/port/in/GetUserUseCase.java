package imbuy.user.application.port.in;

import imbuy.user.domain.model.User;
import reactor.core.publisher.Mono;

public interface GetUserUseCase {
    Mono<User> findById(Long userId);
}
