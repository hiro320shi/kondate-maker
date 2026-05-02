package com.kondatemaker.backend.service;

import com.kondatemaker.backend.dto.recipe.RecipeRequest;
import com.kondatemaker.backend.dto.recipe.RecipeResponse;
import com.kondatemaker.backend.entity.Category;
import com.kondatemaker.backend.entity.Recipe;
import com.kondatemaker.backend.entity.User;
import com.kondatemaker.backend.repository.CategoryRepository;
import com.kondatemaker.backend.repository.RecipeRepository;
import com.kondatemaker.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public List<RecipeResponse> getRecipes(String email, String name, Long categoryId) {
        User user = getUser(email);
        return recipeRepository
                .findByUserIdAndConditions(user.getId(), name, categoryId)
                .stream()
                .map(RecipeResponse::new)
                .toList();
    }

    @Transactional
    public RecipeResponse createRecipe(String email, RecipeRequest request) {
        User user = getUser(email);
        Category category = getCategory(request.getCategoryId());

        Recipe recipe = new Recipe();
        recipe.setUser(user);
        recipe.setCategory(category);
        recipe.setName(request.getName());
        recipe.setPoint(request.getPoint());
        recipe.setMemo(request.getMemo());

        return new RecipeResponse(recipeRepository.save(recipe));
    }

    @Transactional
    public RecipeResponse updateRecipe(String email, Long id, RecipeRequest request) {
        User user = getUser(email);
        Recipe recipe = recipeRepository
                .findByIdAndUserIdAndDeletedAtIsNull(id, user.getId())
                .orElseThrow(() -> new NoSuchElementException("レシピが見つかりません"));

        Category category = getCategory(request.getCategoryId());
        recipe.setCategory(category);
        recipe.setName(request.getName());
        recipe.setPoint(request.getPoint());
        recipe.setMemo(request.getMemo());

        return new RecipeResponse(recipeRepository.save(recipe));
    }

    @Transactional
    public void deleteRecipe(String email, Long id) {
        User user = getUser(email);
        Recipe recipe = recipeRepository
                .findByIdAndUserIdAndDeletedAtIsNull(id, user.getId())
                .orElseThrow(() -> new NoSuchElementException("レシピが見つかりません"));

        recipe.setDeletedAt(LocalDateTime.now());
        recipeRepository.save(recipe);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません"));
    }

    private Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("カテゴリが見つかりません"));
    }
}
