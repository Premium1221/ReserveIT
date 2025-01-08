package persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.reserveit.model.Company;
import com.reserveit.model.Category;
import com.reserveit.repository.CompanyRepository;
import com.reserveit.database.impl.CompanyDatabaseImpl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CompanyDatabaseImplTest {

    @Mock
    private CompanyRepository companyRepository;

    private CompanyDatabaseImpl companyDatabase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        companyDatabase = new CompanyDatabaseImpl(companyRepository);
    }

    @Test
    void findByCategoriesContaining_ShouldReturnMatchingCompanies() {
        // Arrange
        Category category = new Category();
        category.setId(1L);
        category.setName("Italian");

        Company company1 = new Company();
        company1.setId(UUID.randomUUID());
        company1.setName("Restaurant 1");

        Company company2 = new Company();
        company2.setId(UUID.randomUUID());
        company2.setName("Restaurant 2");

        List<Company> expectedCompanies = Arrays.asList(company1, company2);
        when(companyRepository.findByCategoriesContaining(category)).thenReturn(expectedCompanies);

        // Act
        List<Company> actualCompanies = companyDatabase.findByCategoriesContaining(category);

        // Assert
        assertEquals(expectedCompanies, actualCompanies);
        verify(companyRepository).findByCategoriesContaining(category);
    }

    @Test
    void findByMinimumRating_ShouldReturnCompaniesAboveRating() {
        // Arrange
        Float minRating = 4.0f;
        Company company1 = new Company();
        company1.setRating(4.5f);
        Company company2 = new Company();
        company2.setRating(4.2f);

        List<Company> expectedCompanies = Arrays.asList(company1, company2);
        when(companyRepository.findByMinimumRating(minRating)).thenReturn(expectedCompanies);

        // Act
        List<Company> actualCompanies = companyDatabase.findByMinimumRating(minRating);

        // Assert
        assertEquals(expectedCompanies, actualCompanies);
        verify(companyRepository).findByMinimumRating(minRating);
    }

    @Test
    void save_ShouldReturnSavedCompany() {
        // Arrange
        Company company = new Company();
        company.setName("Test Restaurant");
        when(companyRepository.save(company)).thenReturn(company);

        // Act
        Company savedCompany = companyDatabase.save(company);

        // Assert
        assertEquals(company, savedCompany);
        verify(companyRepository).save(company);
    }

    @Test
    void findById_ShouldReturnCompanyWhenExists() {
        UUID id = UUID.randomUUID();
        Company company = new Company();
        company.setId(id);
        when(companyRepository.findById(id)).thenReturn(Optional.of(company));

        Company foundCompany = companyDatabase.findById(id).orElseThrow(() -> new IllegalArgumentException("Company not found"));
        assertNotNull(foundCompany);
        assertEquals(id, foundCompany.getId());
        verify(companyRepository).findById(id);
    }

    @Test
    void findById_ShouldReturnNullWhenCompanyNotFound() {
        UUID id = UUID.randomUUID();
        when(companyRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Company> foundCompany = companyDatabase.findById(id);
        assertTrue(foundCompany.isEmpty());
        verify(companyRepository).findById(id);
    }

    @Test
    void findAll_ShouldReturnAllCompanies() {
        // Arrange
        List<Company> companies = Arrays.asList(
                new Company(),
                new Company()
        );
        when(companyRepository.findAll()).thenReturn(companies);

        // Act
        List<Company> foundCompanies = companyDatabase.findAll();

        // Assert
        assertEquals(companies, foundCompanies);
        verify(companyRepository).findAll();
    }

    @Test
    void deleteById_ShouldCallRepositoryDelete() {
        // Arrange
        UUID id = UUID.randomUUID();

        // Act
        companyDatabase.deleteById(id);

        // Assert
        verify(companyRepository).deleteById(id);
    }
}