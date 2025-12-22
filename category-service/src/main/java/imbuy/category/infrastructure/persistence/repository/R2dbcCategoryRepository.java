package imbuy.category.infrastructure.persistence.repository;

import imbuy.category.infrastructure.persistence.entity.CategoryEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface R2dbcCategoryRepository
        extends ReactiveCrudRepository<CategoryEntity, Long> {

    Mono<Boolean> existsByNameAndParentId(String name, Long parentId);

    Flux<CategoryEntity> findByParentId(Long parentId);

    @Query("SELECT * FROM categories WHERE parent_id IS NULL")
    Flux<CategoryEntity> findRoots();
}
