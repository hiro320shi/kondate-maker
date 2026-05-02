package com.kondatemaker.backend.config;

import com.kondatemaker.backend.entity.Category;
import com.kondatemaker.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        if (categoryRepository.count() > 0) {
            return;
        }

        List<String[]> initialCategories = List.of(
                new String[]{"主食", "1"},
                new String[]{"主菜", "2"},
                new String[]{"副菜", "3"},
                new String[]{"汁物", "4"},
                new String[]{"その他", "5"}
        );

        for (String[] entry : initialCategories) {
            Category category = new Category();
            category.setName(entry[0]);
            category.setSortOrder(Integer.parseInt(entry[1]));
            categoryRepository.save(category);
        }
    }
}
