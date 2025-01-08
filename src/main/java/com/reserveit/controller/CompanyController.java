package com.reserveit.controller;

import com.reserveit.dto.CompanyDto;
import com.reserveit.dto.ReservationDto;
import com.reserveit.dto.TablePositionDto;
import com.reserveit.enums.TableStatus;
import com.reserveit.logic.interfaces.CompanyService;
import com.reserveit.logic.interfaces.DiningTableService;
import com.reserveit.logic.interfaces.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;


@RestController
@RequestMapping("/api/companies")
@CrossOrigin(origins = "http://localhost:5200", allowCredentials = "true")
public class CompanyController {
    private final CompanyService companyService;
    private final ReservationService reservationService;
    private final DiningTableService tableService;

    public CompanyController(CompanyService companyService, ReservationService reservationService, DiningTableService tableService) {
        this.companyService = companyService;
        this.reservationService = reservationService;
        this.tableService = tableService;
    }

    @GetMapping
    public ResponseEntity<?> getAllCompanies() {
        try {
            List<CompanyDto> companies = companyService.getAllCompanies();
            return ResponseEntity.ok(companies);
        } catch (Exception e) {
            System.err.println("Error fetching companies: " + e.getMessage());
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

    @GetMapping("/{id}/dashboard")
    @PreAuthorize("hasRole('MANAGER')")  // Simplified security check
    public ResponseEntity<?> getCompanyDashboard(@PathVariable UUID id) {
        try {
            // Debug log
            System.out.println("Accessing dashboard for company: " + id);

            CompanyDto company = companyService.getCompanyById(id);

            // Debug log
            System.out.println("Found company: " + company.getName());

            List<TablePositionDto> tables = tableService.getTablesByCompany(id);
            List<ReservationDto> todayReservations = reservationService.getReservationsByDate(
                    id, LocalDateTime.now());

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalReservations", todayReservations.size());
            stats.put("availableTables", tables.stream()
                    .filter(t -> t.getStatus() == TableStatus.AVAILABLE)
                    .count());
            stats.put("occupiedTables", tables.stream()
                    .filter(t -> t.getStatus() == TableStatus.OCCUPIED)
                    .count());
            stats.put("totalTables", tables.size());

            Map<String, Object> response = new HashMap<>();
            response.put("company", company);
            response.put("stats", stats);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Debug log
            System.err.println("Error in dashboard endpoint: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching dashboard data: " + e.getMessage());
        }
    }
    @GetMapping("/{id}/name")
    public ResponseEntity<String> getCompanyNameById(@PathVariable UUID id) {
        try {
            String companyName = companyService.getCompanyNameById(id);
            return ResponseEntity.ok(companyName);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch company name");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateCompany(@PathVariable UUID id, @RequestBody CompanyDto companyDto) {
        companyService.updateCompany(id, companyDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable UUID id) {
        companyService.deleteCompany(id);
        return ResponseEntity.ok().build();
    }
}

