package imbuy.user.application.service;

import imbuy.user.application.dto.*;
import imbuy.user.application.port.in.AuthUseCase;
import imbuy.user.application.port.out.*;
import imbuy.user.domain.model.Role;
import imbuy.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

    private final UserRepositoryPort userRepo;
    private final PasswordEncoderPort encoder;
    private final TokenPort tokenPort;

    @Override
    public Mono<AuthResponse> login(LoginRequest request) {
        return userRepo.findByEmail(request.email())
                .filter(User::getActive)
                .filter(u -> encoder.matches(request.password(), u.getPassword()))
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid credentials")))
                .map(this::buildResponse);
    }

    @Override
    public Mono<UserDto> register(RegisterRequest request) {
        Role role = request.role() != null ? request.role() : Role.USER;

        return userRepo.existsByEmail(request.email())
                .flatMap(exists -> {
                    if (exists) return Mono.error(new RuntimeException("Email exists"));

                    User user = User.builder()
                            .email(request.email())
                            .password(encoder.encode(request.password()))
                            .username(request.username())
                            .role(role)
                            .active(true)
                            .build();

                    return userRepo.save(user).map(UserDto::from);
                });
    }

    @Override
    public Mono<AuthResponse> refresh(RefreshTokenRequest request) {
        if (!tokenPort.isRefresh(request.refreshToken())) {
            return Mono.error(new RuntimeException("Invalid refresh token"));
        }

        String email = tokenPort.extractUsername(request.refreshToken());
        return userRepo.findByEmail(email)
                .filter(u -> tokenPort.isValid(request.refreshToken(), email))
                .map(this::buildResponse);
    }

    private AuthResponse buildResponse(User user) {
        return new AuthResponse(
                tokenPort.generateAccess(user),
                tokenPort.generateRefresh(user),
                UserDto.from(user)
        );
    }
}
