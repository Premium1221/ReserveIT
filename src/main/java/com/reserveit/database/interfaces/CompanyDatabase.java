package com.reserveit.database.interfaces;

import com.reserveit.model.Category;
import com.reserveit.model.Company;

import java.util.List;
import java.util.UUID;

public interface CompanyDatabase {
    List<Company> findByCategoriesContaining(Category category);
    List<Company> findByMinimumRating(Float minRating);
    Company save(Company company);
    Company findById(UUID id);
    List<Company> findAll();
    void deleteById(UUID id);
}