package imbuy.user.application.service;

import imbuy.user.application.port.in.GetUserUseCase;
import imbuy.user.application.port.out.UserRepositoryPort;
import imbuy.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GetUserService implements GetUserUseCase {

    private final UserRepositoryPort userRepository;

    @Override
    public Mono<User> findById(Long userId) {
        return userRepository.findById(userId);
    }
}
