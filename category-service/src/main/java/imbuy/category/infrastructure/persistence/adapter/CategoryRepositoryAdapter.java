package imbuy.category.infrastructure.persistence.adapter;

import imbuy.category.application.port.out.CategoryRepositoryPort;
import imbuy.category.domain.model.Category;
import imbuy.category.infrastructure.persistence.entity.CategoryEntity;
import imbuy.category.infrastructure.persistence.repository.R2dbcCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CategoryRepositoryAdapter implements CategoryRepositoryPort {

    private final R2dbcCategoryRepository repository;

    private Category toDomain(CategoryEntity e) {
        return new Category(e.getId(), e.getName(), e.getParentId());
    }

    private CategoryEntity toEntity(Category d) {
        return new CategoryEntity(d.getId(), d.getName(), d.getParentId());
    }

    @Override
    public Mono<Category> save(Category category) {
        return repository.save(toEntity(category)).map(this::toDomain);
    }

    @Override
    public Mono<Category> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Flux<Category> findAll() {
        return repository.findAll().map(this::toDomain);
    }

    @Override
    public Flux<Category> findByParentId(Long parentId) {
        return repository.findByParentId(parentId).map(this::toDomain);
    }

    @Override
    public Flux<Category> findRoots() {
        return repository.findRoots().map(this::toDomain);
    }

    @Override
    public Mono<Boolean> existsByNameAndParentId(String name, Long parentId) {
        return repository.existsByNameAndParentId(name, parentId);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return repository.deleteById(id);
    }
}
