package imbuy.category.presentation.controller;

import imbuy.category.application.dto.*;
import imbuy.category.application.port.in.CategoryUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management APIs")
public class CategoryController {

    private final CategoryUseCase useCase;

    @GetMapping("/tree")
    @Operation(summary = "Get category tree")
    public Mono<CategoryTreeDto> tree() {
        return useCase.getTree();
    }

    @GetMapping
    @Operation(summary = "Get all categories with pagination")
    public Flux<CategoryResponse> all(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return useCase.getAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public Mono<CategoryResponse> byId(@PathVariable Long id) {
        return useCase.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new category", security = @SecurityRequirement(name = "bearerAuth"))
    public Mono<CategoryResponse> create(@Valid @RequestBody CategoryRequest categoryRequest) {
        return useCase.create(categoryRequest);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update category", security = @SecurityRequirement(name = "bearerAuth"))
    public Mono<CategoryResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest req
    ) {
        return useCase.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete category", security = @SecurityRequirement(name = "bearerAuth"))
    public Mono<Void> delete(@PathVariable Long id) {
        return useCase.delete(id);
    }
}
