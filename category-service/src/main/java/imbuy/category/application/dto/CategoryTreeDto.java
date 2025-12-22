package imbuy.category.application.dto;

import java.util.List;

public record CategoryTreeDto(List<Node> categories) {

    public record Node(
            Long id,
            String name,
            Long parentId,
            List<Node> children
    ) {}
}
