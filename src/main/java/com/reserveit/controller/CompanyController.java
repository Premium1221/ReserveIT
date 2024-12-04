package com.reserveit.controller;

import com.reserveit.dto.CompanyDto;
import com.reserveit.logic.interfaces.CompanyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;



@RestController
@RequestMapping("/api/companies")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5200", "http://localhost:3000"," http://145.93.93.110:5200"},
        allowCredentials = "true")
public class CompanyController {
    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }
    @GetMapping
    public ResponseEntity<?> getAllCompanies() {
        try {
            List<CompanyDto> companies = companyService.getAllCompanies();
            return ResponseEntity.ok(companies);
        } catch (Exception e) {
            // Log the error
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching companies: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<CompanyDto> createCompany(@RequestBody CompanyDto companyDto) {
        CompanyDto savedCompany = companyService.addCompany(companyDto);
        return ResponseEntity.status(201).body(savedCompany);
    }

    @GetMapping("{id}")
    public ResponseEntity<CompanyDto> getCompanyById(@PathVariable UUID id) {
        CompanyDto company = companyService.getCompanyById(id);
        return ResponseEntity.ok(company);
    }

    @PutMapping("/id")
    public ResponseEntity<Void> updateCompany(@PathVariable UUID id, @RequestBody CompanyDto companyDto) {
        companyService.updateCompany((id), companyDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable UUID id) {
        companyService.deleteCompany(id);
        return ResponseEntity.ok().build();
    }
}

