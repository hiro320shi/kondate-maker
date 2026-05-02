package com.kondatemaker.backend.dto.category;

import com.kondatemaker.backend.entity.Category;
import lombok.Getter;

@Getter
public class CategoryResponse {

    private final Long id;
    private final String name;
    private final Integer sortOrder;

    public CategoryResponse(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.sortOrder = category.getSortOrder();
    }
}
