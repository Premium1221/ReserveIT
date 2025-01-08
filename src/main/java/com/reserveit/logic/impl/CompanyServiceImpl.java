package com.reserveit.logic.impl;

import com.reserveit.database.interfaces.CompanyDatabase;
import com.reserveit.dto.CompanyDto;
import com.reserveit.model.Company;
import com.reserveit.logic.interfaces.CompanyService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class    CompanyServiceImpl implements CompanyService {
    private final CompanyDatabase companyDb;

    public CompanyServiceImpl(CompanyDatabase companyDb) {
        this.companyDb = companyDb;
    }

    @Override
    public CompanyDto addCompany(CompanyDto companyDto) {
        Company company = convertToEntity(companyDto);
        Company savedCompany = companyDb.save(company);
        return convertToDto(savedCompany);
    }

    @Override
    public List<CompanyDto> getAllCompanies() {
        try {
            List<Company> companies = companyDb.findAll();
            return companies.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.out.println("Error fetching companies: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public CompanyDto getCompanyById(UUID id) {
        return companyDb.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + id));
    }

    @Override
    public void deleteCompany(UUID id) {
        companyDb.deleteById(id);
    }

    @Override
    public void updateCompany(UUID id, CompanyDto companyDto) {
        Company existingCompany = companyDb.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + id));

        boolean isUpdated = false;

        if (companyDto.getName() != null && !companyDto.getName().equals(existingCompany.getName())) {
            existingCompany.setName(companyDto.getName());
            isUpdated = true;
        }
        if (companyDto.getAddress() != null && !companyDto.getAddress().equals(existingCompany.getAddress())) {
            existingCompany.setAddress(companyDto.getAddress());
            isUpdated = true;
        }
        if (companyDto.getPhone() != null && !companyDto.getPhone().equals(existingCompany.getPhone())) {
            existingCompany.setPhone(companyDto.getPhone());
            isUpdated = true;
        }
        if (companyDto.getEmail() != null && !companyDto.getEmail().equals(existingCompany.getEmail())) {
            existingCompany.setEmail(companyDto.getEmail());
            isUpdated = true;
        }
        if (companyDto.getCategories() != null && !companyDto.getCategories().equals(existingCompany.getCategories())) {
            existingCompany.setCategories(companyDto.getCategories());
            isUpdated = true;
        }
        if (companyDto.getRating() != null && !companyDto.getRating().equals(existingCompany.getRating())) {
            existingCompany.setRating(companyDto.getRating());
            isUpdated = true;
        }
        if (companyDto.getPictureUrl() != null && !companyDto.getPictureUrl().equals(existingCompany.getPictureUrl())) {
            existingCompany.setPictureUrl(companyDto.getPictureUrl());
            isUpdated = true;
        }

        if (isUpdated) {
            companyDb.save(existingCompany);
        }
    }
    @Override
    public String getCompanyNameById(UUID id) {
        return companyDb.findById(id)
                .map(Company::getName)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + id));
    }


    private Company convertToEntity(CompanyDto companyDto) {
        Company company = new Company();
        company.setId(companyDto.getId());
        company.setName(companyDto.getName());
        company.setAddress(companyDto.getAddress());
        company.setPhone(companyDto.getPhone());
        company.setEmail(companyDto.getEmail());
        company.setCategories(companyDto.getCategories());
        company.setRating(companyDto.getRating());
        company.setPictureUrl(companyDto.getPictureUrl());
        return company;
    }

    private CompanyDto convertToDto(Company company) {
        CompanyDto companyDto = new CompanyDto();
        companyDto.setId(company.getId());
        companyDto.setName(company.getName());
        companyDto.setAddress(company.getAddress());
        companyDto.setPhone(company.getPhone());
        companyDto.setEmail(company.getEmail());
        companyDto.setCategories(company.getCategories());
        companyDto.setRating(company.getRating());
        companyDto.setPictureUrl(company.getPictureUrl());
        return companyDto;
    }
}