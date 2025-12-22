package imbuy.category.application.port.out;

import imbuy.category.domain.model.Category;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CategoryRepositoryPort {

    Mono<Category> save(Category category);
    Mono<Category> findById(Long id);
    Flux<Category> findAll();
    Flux<Category> findByParentId(Long parentId);
    Flux<Category> findRoots();
    Mono<Boolean> existsByNameAndParentId(String name, Long parentId);
    Mono<Void> deleteById(Long id);
}
