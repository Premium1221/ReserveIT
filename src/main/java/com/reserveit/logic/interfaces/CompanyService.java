package com.reserveit.logic.interfaces;

import com.reserveit.dto.CompanyDto;

import java.util.List;
import java.util.UUID;

public interface CompanyService {
    CompanyDto addCompany(CompanyDto companyDto);
    CompanyDto getCompanyById(UUID id);
    List<CompanyDto> getAllCompanies();
    void deleteCompany(UUID id);
    void updateCompany(UUID id, CompanyDto companyDto);
    String getCompanyNameById(UUID id);


}
