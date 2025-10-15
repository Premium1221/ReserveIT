package com.reserveit.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reserveit.config.SecurityTestConfig;
import com.reserveit.dto.CompanyDto;
import com.reserveit.dto.RegisterRequest;
import com.reserveit.dto.UserDto;
import com.reserveit.logic.interfaces.AdminService;
import com.reserveit.logic.interfaces.CompanyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(SecurityTestConfig.class)
class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminService adminService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterRequest validRequest;
    private UUID companyId;

    @BeforeEach
    void setUp() {
        // Create a test company first
        CompanyDto companyDto = new CompanyDto();
        companyDto.setName("Test Company");
        companyDto.setEmail("company@test.com");
        companyDto.setPhone("1234567890");
        companyDto.setAddress("Test Address");

        CompanyDto savedCompany = companyService.addCompany(companyDto);
        companyId = savedCompany.getId();

        // Set up the register request with the valid company ID
        validRequest = new RegisterRequest();
        validRequest.setFirstName("John");
        validRequest.setLastName("Doe");
        validRequest.setEmail("john.doe@example.com");
        validRequest.setPassword("password123");
        validRequest.setPhoneNumber("1234567890");
        validRequest.setRole("MANAGER");
        validRequest.setCompanyId(companyId);
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "admin@test.com")
    void createUser_WithValidData_ReturnsCreated() throws Exception {
        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("User created successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "admin@test.com")
    void createUser_WithInvalidRole_ReturnsBadRequest() throws Exception {
        validRequest.setRole("INVALID_ROLE");

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "admin@test.com")
    void createUser_WithoutCompanyId_ForManagerRole_ReturnsBadRequest() throws Exception {
        validRequest.setCompanyId(null);

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("companyId is required for MANAGER and STAFF roles")));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "admin@test.com")
    void createUser_WithInvalidCompanyId_ReturnsBadRequest() throws Exception {
        validRequest.setCompanyId(UUID.randomUUID());

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Failed to create manager: Company not found")));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "admin@test.com")
    void getAllUsers_ReturnsUsersList() throws Exception {
        adminService.createUser(validRequest);

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[*].email", hasItem(validRequest.getEmail())));
    }

    @Test
    void getAllUsers_WithoutAuth_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser(roles = "ADMIN", username = "admin@test.com")
    void deleteUser_WithValidId_ReturnsSuccess() throws Exception {
        adminService.createUser(validRequest);

        // Get the created user
        UserDto user = adminService.getAllUsers().stream()
                .filter(u -> u.getEmail().equals(validRequest.getEmail()))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(delete("/api/admin/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "admin@test.com")
    void deleteUser_WithInvalidId_ReturnsBadRequest() throws Exception {
        UUID invalidId = UUID.randomUUID();

        mockMvc.perform(delete("/api/admin/users/{id}", invalidId))
                .andExpect(status().isBadRequest());
    }
}
