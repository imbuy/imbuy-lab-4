package imbuy.user.infrastructure.web;

import imbuy.user.application.dto.*;
import imbuy.user.application.port.in.AuthUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthUseCase authUseCase;

    @PostMapping("/login")
    public Mono<AuthResponse> login(@RequestBody LoginRequest request) {
        return authUseCase.login(request);
    }

    @PostMapping("/register")
    public Mono<UserDto> register(@RequestBody RegisterRequest request) {
        return authUseCase.register(request);
    }

    @PostMapping("/token")
    public Mono<AuthResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return authUseCase.refresh(request);
    }
}

