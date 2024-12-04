package com.reserveit.database.impl;

import com.reserveit.database.interfaces.CategoryDatabase;
import com.reserveit.model.Category;
import com.reserveit.repository.CategoryRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CategoryDatabaseImpl implements CategoryDatabase {
    private final CategoryRepository categoryRepository;

    public CategoryDatabaseImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Optional<Category> findByName(String name) {
        return categoryRepository.findByName(name);
    }

    @Override
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }
}