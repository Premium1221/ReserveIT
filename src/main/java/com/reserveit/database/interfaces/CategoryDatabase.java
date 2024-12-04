package com.reserveit.database.interfaces;

import com.reserveit.model.Category;

import java.util.Optional;

public interface CategoryDatabase {
    Optional<Category> findByName(String name);
    boolean existsByName(String name);
}
