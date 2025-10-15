package com.reserveit.controller;

import com.reserveit.controller.CompanyController;
import com.reserveit.dto.CompanyDto;
import com.reserveit.dto.ReservationDto;
import com.reserveit.dto.TablePositionDto;
import com.reserveit.enums.TableStatus;
import com.reserveit.logic.interfaces.CompanyService;
import com.reserveit.logic.interfaces.DiningTableService;
import com.reserveit.logic.interfaces.ReservationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.mockito.ArgumentMatchers.any;



import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.xmlunit.util.Linqy.any;

@ExtendWith(MockitoExtension.class)
public class CompanyControllerTest {
    @Mock
    private CompanyService companyService;
    @Mock
    private ReservationService reservationService;
    @Mock
    private DiningTableService tableService;

    @InjectMocks
    private CompanyController companyController;

    @Test
    void getAllCompanies_Success() {
        // Arrange
        List<CompanyDto> companies = Arrays.asList(
                createCompanyDto("Company 1"),
                createCompanyDto("Company 2")
        );
        when(companyService.getAllCompanies()).thenReturn(companies);

        // Act
        ResponseEntity<?> response = companyController.getAllCompanies();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(companies, response.getBody());
    }

    @Test
    void getAllCompanies_ThrowsException_ReturnsInternalServerError() {
        // Arrange
        when(companyService.getAllCompanies())
                .thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<?> response = companyController.getAllCompanies();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Error fetching companies"));
    }

    @Test
    void getCompanyDashboard_Success() {
        // Arrange
        UUID companyId = UUID.randomUUID();
        CompanyDto company = createCompanyDto("Test Company");
        List<TablePositionDto> tables = Arrays.asList(
                createTableDto(TableStatus.AVAILABLE),
                createTableDto(TableStatus.OCCUPIED)
        );
        List<ReservationDto> reservations = Arrays.asList(
                createReservationDto()
        );

        when(companyService.getCompanyById(companyId)).thenReturn(company);
        when(tableService.getTablesByCompany(companyId)).thenReturn(tables);
        when(reservationService.getReservationsByDate(any(UUID.class), any(LocalDateTime.class)))
                .thenReturn(reservations);

        // Act
        ResponseEntity<?> response = companyController.getCompanyDashboard(companyId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertNotNull(responseBody);
        assertEquals(company, responseBody.get("company"));

        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) responseBody.get("stats");
        assertEquals(1L, stats.get("totalReservations"));
        assertEquals(1L, stats.get("availableTables"));
        assertEquals(1L, stats.get("occupiedTables"));
        assertEquals(2L, stats.get("totalTables"));
    }

    @Test
    void getCompanyDashboard_ThrowsException_ReturnsInternalServerError() {
        // Arrange
        UUID companyId = UUID.randomUUID();
        when(companyService.getCompanyById(companyId))
                .thenThrow(new RuntimeException("Error"));

        // Act
        ResponseEntity<?> response = companyController.getCompanyDashboard(companyId);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void createCompany_Success() {
        // Arrange
        CompanyDto companyDto = createCompanyDto("New Company");
        when(companyService.addCompany(companyDto)).thenReturn(companyDto);

        // Act
        ResponseEntity<CompanyDto> response = companyController.createCompany(companyDto);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(companyDto, response.getBody());
    }

    @Test
    void getCompanyNameById_Success() {
        // Arrange
        UUID companyId = UUID.randomUUID();
        String companyName = "Test Company";
        when(companyService.getCompanyNameById(companyId)).thenReturn(companyName);

        // Act
        ResponseEntity<String> response = companyController.getCompanyNameById(companyId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(companyName, response.getBody());
    }

    @Test
    void getCompanyNameById_NotFound_ReturnsNotFound() {
        // Arrange
        UUID companyId = UUID.randomUUID();
        when(companyService.getCompanyNameById(companyId))
                .thenThrow(new IllegalArgumentException("Company not found"));

        // Act
        ResponseEntity<String> response = companyController.getCompanyNameById(companyId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    private CompanyDto createCompanyDto(String name) {
        CompanyDto dto = new CompanyDto();
        dto.setId(UUID.randomUUID());
        dto.setName(name);
        dto.setEmail("test@company.com");
        return dto;
    }

    private TablePositionDto createTableDto(TableStatus status) {
        TablePositionDto dto = new TablePositionDto();
        dto.setId(1L);
        dto.setStatus(status);
        dto.setTableNumber("T1");
        dto.setCapacity(4);
        return dto;
    }

    private ReservationDto createReservationDto() {
        ReservationDto dto = new ReservationDto();
        dto.setId(1L);
        dto.setReservationDate(LocalDateTime.now().toString());
        return dto;
    }
}
