package service;

import com.reserveit.database.interfaces.CompanyDatabase;
import com.reserveit.database.interfaces.StaffDatabase;
import com.reserveit.database.interfaces.UserDatabase;
import com.reserveit.dto.UserDto;
import com.reserveit.enums.UserRole;
import com.reserveit.logic.impl.StaffServiceImpl;
import com.reserveit.model.Company;
import com.reserveit.model.Staff;
import com.reserveit.util.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffServiceImplTest {

    @Mock
    private UserDatabase userDb;
    @Mock
    private CompanyDatabase companyDb;
    @Mock
    private PasswordHasher passwordHasher;
    @Mock
    private StaffDatabase staffDb;

    private StaffServiceImpl staffService;

    @BeforeEach
    void setUp() {
        staffService = new StaffServiceImpl(userDb, companyDb, passwordHasher, staffDb);
    }

    @Test
    void createStaffMember_Success() {
        // Arrange
        UserDto userDto = createSampleUserDto();
        Company company = createSampleCompany();
        String rawPassword = "password123";
        String hashedPassword = "hashedPassword";

        when(companyDb.findById(any())).thenReturn(Optional.of(company));
        when(passwordHasher.hashPassword(rawPassword)).thenReturn(hashedPassword);
        when(userDb.save(any(Staff.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Staff result = staffService.createStaffMember(userDto, company.getId(), rawPassword);

        // Assert
        assertNotNull(result);
        assertEquals(UserRole.STAFF, result.getUserRole());
        assertEquals(userDto.getEmail(), result.getEmail());
        assertEquals(company, result.getCompany());
        verify(userDb).save(any(Staff.class));
    }

    @Test
    void createStaffMember_CompanyNotFound() {
        // Arrange
        UserDto userDto = createSampleUserDto();
        UUID companyId = UUID.randomUUID();
        String rawPassword = "password123";

        when(companyDb.findById(any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> staffService.createStaffMember(userDto, companyId, rawPassword));
    }

    @Test
    void createManager_Success() {
        // Arrange
        UserDto userDto = createSampleUserDto();
        Company company = createSampleCompany();
        String rawPassword = "password123";
        String hashedPassword = "hashedPassword";

        when(companyDb.findById(any())).thenReturn(Optional.of(company));
        when(passwordHasher.hashPassword(rawPassword)).thenReturn(hashedPassword);
        when(userDb.save(any(Staff.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Staff result = staffService.createManager(userDto, company.getId(), rawPassword);

        // Assert
        assertNotNull(result);
        assertEquals(UserRole.MANAGER, result.getUserRole());
        assertEquals(userDto.getEmail(), result.getEmail());
        assertEquals(company, result.getCompany());
        verify(userDb).save(any(Staff.class));
    }

    @Test
    void createManager_CompanyNotFound() {
        // Arrange
        UserDto userDto = createSampleUserDto();
        UUID companyId = UUID.randomUUID();
        String rawPassword = "password123";

        when(companyDb.findById(any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalStateException.class,
                () -> staffService.createManager(userDto, companyId, rawPassword));
    }

    @Test
    void getRelatedCompany_Success() {
        // Arrange
        UUID staffId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        when(staffDb.getCompanyIdByStaffId(staffId)).thenReturn(companyId);

        // Act
        UUID result = staffService.getRelatedCompany(staffId);

        // Assert
        assertEquals(companyId, result);
        verify(staffDb).getCompanyIdByStaffId(staffId);
    }

    // Helper methods
    private UserDto createSampleUserDto() {
        UserDto dto = new UserDto();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john.doe@example.com");
        dto.setPhoneNumber("1234567890");
        return dto;
    }

    private Company createSampleCompany() {
        Company company = new Company();
        company.setId(UUID.randomUUID());
        company.setName("Test Company");
        return company;
    }
}