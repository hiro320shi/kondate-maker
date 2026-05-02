package com.kondatemaker.backend.dto.recipe;

import com.kondatemaker.backend.entity.Recipe;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class RecipeResponse {

    private final Long id;
    private final String name;
    private final Long categoryId;
    private final String categoryName;
    private final BigDecimal point;
    private final String memo;
    private final LocalDateTime createdAt;

    public RecipeResponse(Recipe recipe) {
        this.id = recipe.getId();
        this.name = recipe.getName();
        this.categoryId = recipe.getCategory().getId();
        this.categoryName = recipe.getCategory().getName();
        this.point = recipe.getPoint();
        this.memo = recipe.getMemo();
        this.createdAt = recipe.getCreatedAt();
    }
}
