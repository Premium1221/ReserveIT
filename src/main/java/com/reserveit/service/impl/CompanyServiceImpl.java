package com.reserveit.service.impl;

import com.reserveit.dto.CompanyDto;
import com.reserveit.model.Company;
import com.reserveit.repository.CompanyRepository;
import com.reserveit.service.CompanyService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRepository companyRepository;

    public CompanyServiceImpl(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public CompanyDto addCompany(CompanyDto companyDto) {
        Company company = convertToEntity(companyDto);
        Company savedCompany = companyRepository.save(company);
        return convertToDto(savedCompany);
    }

    @Override
    public List<CompanyDto> getAllCompanies() {
        return companyRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompanyDto getCompanyById(UUID id) {
        return companyRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
    }

    @Override
    public void deleteCompany(UUID id) {
        companyRepository.deleteById(id);
    }

    @Override
    public void updateCompany(UUID id, CompanyDto companyDto) {
        Optional<Company> existingCompanyOptional = companyRepository.findById(id);
        if (existingCompanyOptional.isPresent()) {
            Company existingCompany = existingCompanyOptional.get();

            boolean isUpdated = false;

            if (!existingCompany.getName().equals(companyDto.getName())) {
                existingCompany.setName(companyDto.getName());
                isUpdated = true;
            }
            if (!existingCompany.getAddress().equals(companyDto.getAddress())) {
                existingCompany.setAddress(companyDto.getAddress());
                isUpdated = true;
            }
            if (!existingCompany.getPhone().equals(companyDto.getPhone())) {
                existingCompany.setPhone(companyDto.getPhone());
                isUpdated = true;
            }
            if (!existingCompany.getEmail().equals(companyDto.getEmail())) {
                existingCompany.setEmail(companyDto.getEmail());
                isUpdated = true;
            }
            if (!existingCompany.getCategories().equals(companyDto.getCategories())) {
                existingCompany.setCategories(companyDto.getCategories());
                isUpdated = true;
            }
            if (existingCompany.getRating() != companyDto.getRating()) {
                existingCompany.setRating(companyDto.getRating());
                isUpdated = true;
            }
            if (!existingCompany.getPictureUrl().equals(companyDto.getPictureUrl())) {
                existingCompany.setPictureUrl(companyDto.getPictureUrl());
                isUpdated = true;
            }

            if (isUpdated) {
                companyRepository.save(existingCompany);
            }
        } else {
            throw new IllegalArgumentException("Company not found with id: " + id);
        }
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