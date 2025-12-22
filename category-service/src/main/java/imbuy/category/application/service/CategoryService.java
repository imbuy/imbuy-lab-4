package imbuy.category.application.service;

import imbuy.category.application.dto.*;
import imbuy.category.application.mapper.CategoryMapper;
import imbuy.category.application.port.in.CategoryUseCase;
import imbuy.category.application.port.out.CategoryRepositoryPort;
import imbuy.category.domain.model.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService implements CategoryUseCase {

    private final CategoryRepositoryPort repository;
    private final CategoryMapper mapper;

    @Override
    public Mono<CategoryTreeDto> getTree() {
        return repository.findRoots()
                .flatMap(this::buildNode)
                .collectList()
                .map(CategoryTreeDto::new);
    }

    private Mono<CategoryTreeDto.Node> buildNode(Category c) {
        return repository.findByParentId(c.getId())
                .flatMap(this::buildNode)
                .collectList()
                .map(children -> new CategoryTreeDto.Node(
                        c.getId(),
                        c.getName(),
                        c.getParentId(),
                        children.isEmpty() ? null : children
                ));
    }

    @Override
    public Flux<CategoryResponse> getAll(Pageable pageable) {
        return repository.findAll()
                .skip(pageable.getOffset())
                .take(pageable.getPageSize())
                .map(mapper::toResponse);
    }

    @Override
    public Mono<CategoryResponse> getById(Long id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Category not found"
                        )
                ))
                .flatMap(category ->
                        repository.findByParentId(id)
                                .map(mapper::toResponse)
                                .collectList()
                                .map(children ->
                                        mapper.toResponse(
                                                category,
                                                children.isEmpty() ? null : children
                                        )
                                )
                );
    }

    @Override
    public Mono<CategoryResponse> create(CategoryRequest request) {
        return repository.existsByNameAndParentId(request.name(), request.parentId())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(
                                new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Category with this name already exists"
                                )
                        );
                    }

                    if (request.parentId() != null) {
                        return repository.findById(request.parentId())
                                .switchIfEmpty(Mono.error(
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Parent category not found"
                                        )
                                ))
                                .then(createEntity(request));
                    }

                    return createEntity(request);
                });
    }

    private Mono<CategoryResponse> createEntity(CategoryRequest request) {
        Category category = mapper.toDomain(request);
        return repository.save(category)
                .map(mapper::toResponse);
    }

    @Override
    public Mono<CategoryResponse> update(Long id, CategoryRequest request) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Category not found"
                        )
                ))
                .flatMap(existing -> {
                    if (request.parentId() != null) {
                        return repository.findById(request.parentId())
                                .switchIfEmpty(Mono.error(
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Parent category not found"
                                        )
                                ))
                                .then(updateEntity(existing, request));
                    }
                    return updateEntity(existing, request);
                });
    }

    private Mono<CategoryResponse> updateEntity(Category existing, CategoryRequest request) {
        existing.setName(request.name());
        existing.setParentId(request.parentId());

        return repository.save(existing)
                .map(mapper::toResponse);
    }

    @Override
    public Mono<Void> delete(Long id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Category not found"
                        )
                ))
                .flatMap(category ->
                        repository.findByParentId(id)
                                .hasElements()
                                .flatMap(hasChildren -> {
                                    if (Boolean.TRUE.equals(hasChildren)) {
                                        return Mono.error(
                                                new ResponseStatusException(
                                                        HttpStatus.BAD_REQUEST,
                                                        "Cannot delete category with subcategories"
                                                )
                                        );
                                    }
                                    return repository.deleteById(id);
                                })
                );
    }
}
