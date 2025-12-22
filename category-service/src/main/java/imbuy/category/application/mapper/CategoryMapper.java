package imbuy.category.application.mapper;

import imbuy.category.application.dto.*;
import imbuy.category.domain.model.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toDomain(CategoryRequest req) {
        return Category.builder()
                .name(req.name())
                .parentId(req.parentId())
                .build();
    }

    public CategoryResponse toResponse(Category c) {
        return new CategoryResponse(
                c.getId(),
                c.getName(),
                c.getParentId(),
                null
        );
    }

    public CategoryResponse toResponse(Category c, java.util.List<CategoryResponse> children) {
        return new CategoryResponse(
                c.getId(),
                c.getName(),
                c.getParentId(),
                children
        );
    }
}
