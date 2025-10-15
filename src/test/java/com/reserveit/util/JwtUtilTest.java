package com.reserveit.util;

import com.reserveit.logic.interfaces.StaffService;
import com.reserveit.model.Company;
import com.reserveit.model.Staff;
import com.reserveit.model.User;
import com.reserveit.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @Mock
    private StaffService staffService;

    private static final String SECRET = "yourTestSecretKeyHereMustBeLongEnoughForHS256Algorithm";
    private static final Long ACCESS_TOKEN_EXPIRATION = 900000L; // 15 minutes

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "accessTokenExpiration", ACCESS_TOKEN_EXPIRATION);
        ReflectionTestUtils.setField(jwtUtil, "staffService", staffService);
    }

    @Test
    void generateAccessToken_ForRegularUser() {
        // Arrange
        User user = createSampleUser();

        // Act
        String token = jwtUtil.generateAccessToken(user);

        // Assert
        assertNotNull(token);
        assertEquals(user.getEmail(), jwtUtil.getEmailFromToken(token));
        assertNull(jwtUtil.getCompanyIdFromToken(token));
        assertTrue(jwtUtil.validateAccessToken(token, user));
    }

    @Test
    void generateAccessToken_ForStaffUser() {
        // Arrange
        Staff staff = createSampleStaff();

        // Act
        String token = jwtUtil.generateAccessToken(staff);

        // Assert
        assertNotNull(token);
        assertEquals(staff.getEmail(), jwtUtil.getEmailFromToken(token));
        assertNotNull(jwtUtil.getCompanyIdFromToken(token));
        assertTrue(jwtUtil.validateAccessToken(token, staff));
    }

    @Test
    void validateAccessToken_InvalidUser() {
        // Arrange
        User user = createSampleUser();
        User differentUser = createSampleUser();
        differentUser.setEmail("different@example.com");

        String token = jwtUtil.generateAccessToken(user);

        // Act & Assert
        assertFalse(jwtUtil.validateAccessToken(token, differentUser));
    }

    @Test
    void validateAccessToken_ExpiredToken() {
        // Arrange
        ReflectionTestUtils.setField(jwtUtil, "accessTokenExpiration", -1000L); // Expired
        User user = createSampleUser();

        String token = jwtUtil.generateAccessToken(user);

        // Act & Assert
        assertFalse(jwtUtil.validateAccessToken(token, user));
    }

    private User createSampleUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        return user;
    }

    private Staff createSampleStaff() {
        Staff staff = new Staff();
        staff.setId(UUID.randomUUID());
        staff.setEmail("staff@example.com");
        staff.setFirstName("Staff");
        staff.setLastName("User");

        Company company = new Company();
        company.setId(UUID.randomUUID());
        company.setName("Sample Company");
        staff.setCompany(company);

        return staff;

    }
}