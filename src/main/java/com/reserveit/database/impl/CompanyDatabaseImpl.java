package com.reserveit.database.impl;

import com.reserveit.database.interfaces.ICompanyDatabase;
import com.reserveit.model.Category;
import com.reserveit.model.Company;
import com.reserveit.repository.CompanyRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class CompanyDatabaseImpl implements ICompanyDatabase {
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
    public Company findById(UUID id) {
        return companyRepository.findById(id).orElse(null);
    }

    @Override
    public List<Company> findAll() {
        return companyRepository.findAll();
    }

    @Override
    public void deleteById(UUID id) {
        companyRepository.deleteById(id);
    }
}