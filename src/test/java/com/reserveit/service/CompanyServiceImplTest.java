package com.reserveit.service;

import com.reserveit.database.interfaces.CompanyDatabase;
import com.reserveit.dto.CompanyDto;
import com.reserveit.model.Company;
import com.reserveit.model.Category;
import com.reserveit.logic.impl.CompanyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceImplTest {

    @Mock
    private CompanyDatabase companyDb;

    @InjectMocks
    private CompanyServiceImpl companyService;

    @Nested
    class AddCompanyTests {
        @Test
        void addCompany_Success() {
            // Arrange
            CompanyDto companyDto = createSampleCompanyDto();
            Company company = createSampleCompany();

            when(companyDb.save(any(Company.class))).thenReturn(company);

            // Act
            CompanyDto result = companyService.addCompany(companyDto);

            // Assert
            assertNotNull(result);
            assertEquals(company.getName(), result.getName());
            assertEquals(company.getEmail(), result.getEmail());
            verify(companyDb).save(any(Company.class));
        }
        @Test
        void addCompany_WithValidData_Success() {
            // Arrange
            CompanyDto dto = createSampleCompanyDto();
            Company company = createSampleCompany();
            when(companyDb.save(any(Company.class))).thenReturn(company);

            // Act
            CompanyDto result = companyService.addCompany(dto);

            // Assert
            assertNotNull(result);
            assertEquals(dto.getName(), result.getName());
            assertEquals(dto.getEmail(), result.getEmail());
            verify(companyDb).save(any(Company.class));
        }

        @Test
        void addCompany_WithNullName_ThrowsIllegalArgumentException() {
            // Arrange
            CompanyDto dto = createSampleCompanyDto();
            dto.setName(null);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> companyService.addCompany(dto));
            assertEquals("Company name cannot be null or empty", exception.getMessage());
        }

        @Test
        void addCompany_WithInvalidEmail_ThrowsIllegalArgumentException() {
            // Arrange
            CompanyDto dto = createSampleCompanyDto();
            dto.setEmail("invalid-email");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> companyService.addCompany(dto));
            assertEquals("Invalid email format", exception.getMessage());
        }


        @Test
        void addCompany_WithInvalidPhone_ThrowsIllegalArgumentException() {
            // Arrange
            CompanyDto dto = createSampleCompanyDto();
            dto.setPhone("123");  // too short

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> companyService.addCompany(dto));
            assertEquals("Phone number must be 10 digits", exception.getMessage());
        }

        @Test
        void addCompany_NullName_ThrowsException() {
            // Arrange
            CompanyDto dto = new CompanyDto();
            dto.setEmail("test@company.com");
            dto.setPhone("1234567890");
            dto.setName(null);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> companyService.addCompany(dto));
            assertEquals("Company name cannot be null or empty", exception.getMessage());
        }
        @Test
        void addCompany_InvalidEmail_ThrowsException() {
            // Arrange
            CompanyDto dto = new CompanyDto();
            dto.setName("Test Company");
            dto.setEmail("invalid-email");
            dto.setPhone("1234567890");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> companyService.addCompany(dto));
            assertEquals("Invalid email format", exception.getMessage());
        }
        @Test
        void addCompany_InvalidPhone_ThrowsException() {
            // Arrange
            CompanyDto dto = new CompanyDto();
            dto.setName("Test Company");
            dto.setEmail("test@company.com");
            dto.setPhone("123"); // Invalid phone number

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> companyService.addCompany(dto));
            assertEquals("Phone number must be 10 digits", exception.getMessage());
        }
        @Test
        void getAllCompanies_DatabaseError_ThrowsException() {
            // Arrange
            when(companyDb.findAll())
                    .thenThrow(new RuntimeException("Database connection error"));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> companyService.getAllCompanies());
            assertEquals("Database connection error", exception.getMessage());
        }

    }


    @Nested
    class GetCompanyTests {
        @Test
        void getAllCompanies_Success() {
            // Arrange
            List<Company> companies = Arrays.asList(
                    createSampleCompany(),
                    createSampleCompany()
            );
            when(companyDb.findAll()).thenReturn(companies);

            // Act
            List<CompanyDto> result = companyService.getAllCompanies();

            // Assert
            assertEquals(2, result.size());
            verify(companyDb).findAll();
        }

        @Test
        void getAllCompanies_HandlesException() {
            // Arrange
            when(companyDb.findAll()).thenThrow(new RuntimeException("Database error"));

            // Act & Assert
            assertThrows(RuntimeException.class, () -> companyService.getAllCompanies());
        }

        @Test
        void getCompanyById_Success() {
            // Arrange
            UUID id = UUID.randomUUID();
            Company company = createSampleCompany();
            when(companyDb.findById(id)).thenReturn(Optional.of(company));

            // Act
            CompanyDto result = companyService.getCompanyById(id);

            // Assert
            assertNotNull(result);
            assertEquals(company.getName(), result.getName());
            verify(companyDb).findById(id);
        }

        @Test
        void getCompanyById_NotFound() {
            // Arrange
            UUID id = UUID.randomUUID();
            when(companyDb.findById(id)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> companyService.getCompanyById(id));
        }

        @Test
        void getCompanyNameById_Success() {
            // Arrange
            UUID id = UUID.randomUUID();
            Company company = createSampleCompany();
            when(companyDb.findById(id)).thenReturn(Optional.of(company));

            // Act
            String result = companyService.getCompanyNameById(id);

            // Assert
            assertEquals(company.getName(), result);
            verify(companyDb).findById(id);
        }

        @Test
        void getCompanyNameById_NotFound() {
            // Arrange
            UUID id = UUID.randomUUID();
            when(companyDb.findById(id)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> companyService.getCompanyNameById(id));
        }
        @Test
        void getAllCompanies_WithData_ReturnsListSuccessfully() {
            // Arrange
            List<Company> companies = Arrays.asList(
                    createSampleCompany(),
                    createSampleCompany()
            );
            when(companyDb.findAll()).thenReturn(companies);

            // Act
            List<CompanyDto> result = companyService.getAllCompanies();

            // Assert
            assertEquals(2, result.size());
            verify(companyDb).findAll();
        }

        @Test
        void getAllCompanies_WhenEmpty_ReturnsEmptyList() {
            // Arrange
            when(companyDb.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<CompanyDto> result = companyService.getAllCompanies();

            // Assert
            assertTrue(result.isEmpty());
            verify(companyDb).findAll();
        }

        @Test
        void getAllCompanies_WhenDatabaseError_ThrowsException() {
            // Arrange
            when(companyDb.findAll()).thenThrow(new RuntimeException("Database error"));

            // Act & Assert
            assertThrows(RuntimeException.class,
                    () -> companyService.getAllCompanies());
        }

        @Test
        void getCompanyById_WhenExists_ReturnsCompany() {
            // Arrange
            UUID id = UUID.randomUUID();
            Company company = createSampleCompany();
            when(companyDb.findById(id)).thenReturn(Optional.of(company));

            // Act
            CompanyDto result = companyService.getCompanyById(id);

            // Assert
            assertNotNull(result);
            assertEquals(company.getName(), result.getName());
        }

        @Test
        void getCompanyById_WhenNotExists_ThrowsException() {
            // Arrange
            UUID id = UUID.randomUUID();
            when(companyDb.findById(id)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> companyService.getCompanyById(id));
        }
    }


    @Nested
    class UpdateCompanyTests {
        @Test
        void updateCompany_Success() {
            // Arrange
            UUID id = UUID.randomUUID();
            Company existingCompany = createSampleCompany();
            CompanyDto updateDto = createSampleCompanyDto();
            updateDto.setName("Updated Name");

            when(companyDb.findById(id)).thenReturn(Optional.of(existingCompany));
            when(companyDb.save(any(Company.class))).thenReturn(existingCompany);

            // Act
            companyService.updateCompany(id, updateDto);

            // Assert
            verify(companyDb).findById(id);
            verify(companyDb).save(any(Company.class));
            assertEquals("Updated Name", existingCompany.getName());
        }

        @Test
        void updateCompany_NoChanges() {
            // Arrange
            UUID id = UUID.randomUUID();
            Company existingCompany = createSampleCompany();
            CompanyDto updateDto = createSampleCompanyDto();
            // Set same values as existing company
            updateDto.setName(existingCompany.getName());

            when(companyDb.findById(id)).thenReturn(Optional.of(existingCompany));

            // Act
            companyService.updateCompany(id, updateDto);

            // Assert
            verify(companyDb).findById(id);
            verify(companyDb, never()).save(any(Company.class));
        }

        @Test
        void updateCompany_NotFound() {
            // Arrange
            UUID id = UUID.randomUUID();
            CompanyDto updateDto = createSampleCompanyDto();
            when(companyDb.findById(id)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> companyService.updateCompany(id, updateDto));
        }

        @Test
        void updateCompany_PartialUpdate() {
            // Arrange
            UUID id = UUID.randomUUID();
            Company existingCompany = createSampleCompany();
            CompanyDto updateDto = new CompanyDto();
            updateDto.setName("Updated Name");

            when(companyDb.findById(id)).thenReturn(Optional.of(existingCompany));
            when(companyDb.save(any(Company.class))).thenReturn(existingCompany);

            // Act
            companyService.updateCompany(id, updateDto);

            // Assert
            verify(companyDb).save(any(Company.class));
            assertEquals("Updated Name", existingCompany.getName());
            // Verify other fields remain unchanged
            assertEquals(createSampleCompany().getEmail(), existingCompany.getEmail());
        }

        @Test
        void updateCompany_WithValidData_Success() {
            // Arrange
            UUID id = UUID.randomUUID();
            Company existingCompany = createSampleCompany();
            CompanyDto updateDto = createSampleCompanyDto();
            updateDto.setName("Updated Name");

            when(companyDb.findById(id)).thenReturn(Optional.of(existingCompany));
            when(companyDb.save(any(Company.class))).thenReturn(existingCompany);

            // Act
            companyService.updateCompany(id, updateDto);

            // Assert
            verify(companyDb).save(any(Company.class));
            assertEquals("Updated Name", existingCompany.getName());
        }

        @Test
        void updateCompany_WithNonExistentId_ThrowsException() {
            // Arrange
            UUID id = UUID.randomUUID();
            CompanyDto updateDto = createSampleCompanyDto();
            when(companyDb.findById(id)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> companyService.updateCompany(id, updateDto));
        }

        @Test
        void addCompany_WithNullEmail_ThrowsIllegalArgumentException() {
            // Arrange
            CompanyDto dto = createSampleCompanyDto();
            dto.setEmail(null);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> companyService.addCompany(dto));
            assertEquals("Invalid email format", exception.getMessage());
        }


    }

    @Test
    void deleteCompany_Success() {
        // Arrange
        UUID id = UUID.randomUUID();
        Company existing = createSampleCompany();
        when(companyDb.findById(id)).thenReturn(java.util.Optional.of(existing));

        // Act
        companyService.deleteCompany(id);

        // Assert
        verify(companyDb).deleteById(id);
    }

    @Test
    void getCompanyNameById_Success() {
        // Arrange
        UUID id = UUID.randomUUID();
        Company company = createSampleCompany();
        when(companyDb.findById(id)).thenReturn(Optional.of(company));

        // Act
        String result = companyService.getCompanyNameById(id);

        // Assert
        assertEquals(company.getName(), result);
    }



    // Helper methods
    private CompanyDto createSampleCompanyDto() {
        CompanyDto dto = new CompanyDto();
        dto.setName("Test Company");
        dto.setEmail("test@company.com");
        dto.setPhone("1234567890");
        dto.setAddress("123 Test St");
        dto.setRating(4.5f);
        return dto;
    }

    private Company createSampleCompany() {
        Company company = new Company();
        company.setId(UUID.randomUUID());
        company.setName("Test Company");
        company.setAddress("123 Test St");
        company.setPhone("1234567890");
        company.setEmail("test@company.com");
        company.setCategories(new HashSet<>());
        company.setRating(4.5f);
        company.setPictureUrl("http://example.com/picture.jpg");
        return company;
    }
}
