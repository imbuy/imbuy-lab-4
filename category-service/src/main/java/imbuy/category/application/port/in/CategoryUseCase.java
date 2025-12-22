package imbuy.category.application.port.in;

import imbuy.category.application.dto.CategoryRequest;
import imbuy.category.application.dto.CategoryResponse;
import imbuy.category.application.dto.CategoryTreeDto;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CategoryUseCase {

    Mono<CategoryTreeDto> getTree();
    Flux<CategoryResponse> getAll(Pageable pageable);
    Mono<CategoryResponse> getById(Long id);
    Mono<CategoryResponse> create(CategoryRequest request);
    Mono<CategoryResponse> update(Long id, CategoryRequest request);
    Mono<Void> delete(Long id);
}
