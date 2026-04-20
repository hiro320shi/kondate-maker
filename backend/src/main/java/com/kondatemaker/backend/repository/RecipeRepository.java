package com.kondatemaker.backend.repository;

import com.kondatemaker.backend.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    @Query("SELECT r FROM Recipe r WHERE r.user.id = :userId AND r.deletedAt IS NULL " +
           "AND (:name IS NULL OR r.name LIKE %:name%) " +
           "AND (:categoryId IS NULL OR r.category.id = :categoryId) " +
           "ORDER BY r.createdAt DESC")
    List<Recipe> findByUserIdAndConditions(
            @Param("userId") Long userId,
            @Param("name") String name,
            @Param("categoryId") Long categoryId);

    Optional<Recipe> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);
}
