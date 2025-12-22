package imbuy.user.application.port.in;

import imbuy.user.application.dto.*;
import reactor.core.publisher.Mono;

public interface AuthUseCase {

    Mono<AuthResponse> login(LoginRequest request);
    Mono<UserDto> register(RegisterRequest request);
    Mono<AuthResponse> refresh(RefreshTokenRequest request);
}
