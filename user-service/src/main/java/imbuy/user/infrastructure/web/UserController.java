package imbuy.user.infrastructure.web;

import imbuy.user.application.port.in.UserUseCase;
import imbuy.user.application.dto.UpdateUserRequest;
import imbuy.user.application.dto.UserDto;
import imbuy.user.infrastructure.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserUseCase userUseCase;

    @GetMapping
    public Flux<UserDto> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return userUseCase.findAll(PageRequest.of(page, Math.min(size, 50)));
    }

    @GetMapping("/{id}")
    public Mono<UserDto> findById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return userUseCase.findById(id, requesterId(authentication));
    }

    @PutMapping("/{id}")
    public Mono<UserDto> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication
    ) {
        return userUseCase.updateProfile(id, request, requesterId(authentication));
    }

    private Long requesterId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return null;
        }
        return principal.getId();
    }
}
