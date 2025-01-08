package com.reserveit.database.impl;

import com.reserveit.database.interfaces.CompanyDatabase;
import com.reserveit.model.Category;
import com.reserveit.model.Company;
import com.reserveit.repository.CompanyRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class CompanyDatabaseImpl implements CompanyDatabase {
    private final CompanyRepository companyRepository;

    public CompanyDatabaseImpl(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public List<Company> findByCategoriesContaining(Category category) {
        return companyRepository.findByCategoriesContaining(category);
    }

    @Override
    public List<Company> findByMinimumRating(Float minRating) {
        return companyRepository.findByMinimumRating(minRating);
    }

    @Override
    public Company save(Company company) {
        return companyRepository.save(company);
    }

    @Override
    public Optional<Company> findById(UUID id) {
        return companyRepository.findById(id);
    }

    @Override
    public List<Company> findAll() {
        try {
            List<Company> companies = companyRepository.findAll();
            System.out.println("Found " + companies.size() + " companies in database");
            return companies;
        } catch (Exception e) {
            System.err.println("Error in CompanyDatabaseImpl.findAll(): " + e.getMessage());
            throw e;
        }
    }
    @Override
    public void deleteById(UUID id) {
        companyRepository.deleteById(id);
    }
}