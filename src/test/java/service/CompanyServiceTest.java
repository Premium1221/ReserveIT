package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.reserveit.database.interfaces.CompanyDatabase;
import com.reserveit.dto.CompanyDto;
import com.reserveit.model.Company;
import com.reserveit.logic.impl.CompanyServiceImpl;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

class CompanyServiceTest {

    @Mock
    private CompanyDatabase companyDb;

    private CompanyServiceImpl companyService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        companyService = new CompanyServiceImpl(companyDb);
    }

    @Test
    void addCompany_ValidData_Success() {
        // Arrange
        CompanyDto dto = new CompanyDto();
        dto.setName("Test Company");
        dto.setEmail("test@company.com");

        Company mockCompany = new Company();
        mockCompany.setId(UUID.randomUUID());
        mockCompany.setName(dto.getName());

        when(companyDb.save(any(Company.class))).thenReturn(mockCompany);

        // Act
        CompanyDto result = companyService.addCompany(dto);

        // Assert
        assertNotNull(result);
        assertEquals(dto.getName(), result.getName());
    }

    @Test
    void getAllCompanies_ReturnsCorrectList() {
        // Arrange
        Company company1 = new Company();
        company1.setName("Company 1");
        Company company2 = new Company();
        company2.setName("Company 2");

        when(companyDb.findAll()).thenReturn(Arrays.asList(company1, company2));

        // Act
        List<CompanyDto> results = companyService.getAllCompanies();

        // Assert
        assertEquals(2, results.size());
    }

    @Test
    void getCompanyById_ValidId_ReturnsCompany() {
        // Arrange
        UUID companyId = UUID.randomUUID();
        Company mockCompany = new Company();
        mockCompany.setId(companyId);
        mockCompany.setName("Test Company");

        when(companyDb.findById(companyId)).thenReturn(mockCompany);

        // Act
        CompanyDto result = companyService.getCompanyById(companyId);

        // Assert
        assertNotNull(result);
        assertEquals(mockCompany.getName(), result.getName());
    }

    @Test
    void getCompanyById_InvalidId_ThrowsException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(companyDb.findById(nonExistentId)).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            companyService.getCompanyById(nonExistentId);
        });
    }
}
