package com.reserveit.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reserveit.config.SecurityTestConfig;
import com.reserveit.dto.ReservationDto;
import com.reserveit.dto.CompanyDto;
import com.reserveit.dto.TablePositionDto;
import com.reserveit.dto.RegisterRequest;
import com.reserveit.enums.TableShape;
import com.reserveit.enums.TableStatus;
import com.reserveit.enums.ReservationStatus;
import com.reserveit.logic.interfaces.*;
import com.reserveit.model.User;
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

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import({SecurityTestConfig.class, com.reserveit.config.NoopMailTestConfig.class})
class ReservationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private DiningTableService tableService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private UserService userService;

    @Autowired
    private AdminService adminService;

    private UUID companyId;
    private Long tableId;
    private ReservationDto validReservationDto;
    private User testCustomer;
    private User testStaff;

    @BeforeEach
    void setUp() {
        // Create a test company
        CompanyDto companyDto = new CompanyDto();
        companyDto.setName("Test Restaurant");
        companyDto.setEmail("restaurant@test.com");
        companyDto.setPhone("1234567890");
        companyDto.setAddress("Test Address");
        CompanyDto savedCompany = companyService.addCompany(companyDto);
        companyId = savedCompany.getId();

        // Create a test table
        TablePositionDto tableDto = new TablePositionDto();
        tableDto.setTableNumber("T1");
        tableDto.setCapacity(4);
        tableDto.setShape(TableShape.SQUARE);
        tableDto.setStatus(TableStatus.AVAILABLE);
        tableDto.setCompanyId(companyId);
        TablePositionDto savedTable = tableService.addTable(tableDto);
        tableId = savedTable.getId();

        // Create test customer
        RegisterRequest customerRequest = new RegisterRequest();
        customerRequest.setEmail("customer@test.com");
        customerRequest.setFirstName("Test");
        customerRequest.setLastName("Customer");
        customerRequest.setPassword("password123");
        customerRequest.setPhoneNumber("1234567890");
        customerRequest.setRole("CUSTOMER");
        adminService.createUser(customerRequest);
        testCustomer = userService.getUserEntityByEmail("customer@test.com");

        // Create test staff
        RegisterRequest staffRequest = new RegisterRequest();
        staffRequest.setEmail("staff@test.com");
        staffRequest.setFirstName("Test");
        staffRequest.setLastName("Staff");
        staffRequest.setPassword("password123");
        staffRequest.setPhoneNumber("1234567890");
        staffRequest.setRole("STAFF");
        staffRequest.setCompanyId(companyId);
        adminService.createUser(staffRequest);
        testStaff = userService.getUserEntityByEmail("staff@test.com");

        // Set up valid reservation data
        validReservationDto = new ReservationDto();
        // Use a deterministic business-hour time to avoid flakiness
        validReservationDto.setReservationDate(LocalDateTime.now()
                .withHour(12).withMinute(0).withSecond(0).withNano(0)
                .plusDays(1).toString());
        validReservationDto.setNumberOfPeople(2);
        validReservationDto.setCompanyId(companyId);
        validReservationDto.setTableId(tableId);
        validReservationDto.setSpecialRequests("No spicy food");
    }

    @Test
    @WithMockUser(username = "customer@test.com", roles = "CUSTOMER")
    void createReservation_WithValidData_ReturnsCreated() throws Exception {
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validReservationDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tableId", is(tableId.intValue())))
                .andExpect(jsonPath("$.status", is("CONFIRMED")));
    }

    @Test
    @WithMockUser(username = "customer@test.com", roles = "CUSTOMER")
    void createReservation_WithPastDate_ReturnsBadRequest() throws Exception {
        validReservationDto.setReservationDate(LocalDateTime.now().minusHours(1).toString());

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validReservationDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "staff@test.com", roles = "STAFF")
    void checkInReservation_WithValidId_ReturnsSuccess() throws Exception {
        // First create a reservation
        ReservationDto savedReservation = reservationService.createReservation(
                validReservationDto,
                testCustomer
        );

        mockMvc.perform(post("/api/staff/reservations/{id}/check-in", savedReservation.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ARRIVED")));
    }

    @Test
    @WithMockUser(username = "staff@test.com", roles = "STAFF")
    void checkOutReservation_WithValidId_ReturnsSuccess() throws Exception {
        // First create and check in a reservation
        ReservationDto savedReservation = reservationService.createReservation(
                validReservationDto,
                testCustomer
        );
        reservationService.checkInReservation(savedReservation.getId());

        mockMvc.perform(post("/api/staff/reservations/{id}/check-out", savedReservation.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")));
    }

    @Test
    @WithMockUser(username = "customer@test.com", roles = "CUSTOMER")
    void cancelReservation_WithValidId_ReturnsSuccess() throws Exception {
        // First create a reservation
        ReservationDto savedReservation = reservationService.createReservation(
                validReservationDto,
                testCustomer
        );

        mockMvc.perform(delete("/api/reservations/{id}", savedReservation.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("Reservation cancelled successfully"));
    }

    @Test
    @WithMockUser(username = "customer@test.com", roles = "CUSTOMER")
    void getMyReservations_ReturnsReservationsList() throws Exception {
        // First create a reservation
        reservationService.createReservation(validReservationDto, testCustomer);

        mockMvc.perform(get("/api/reservations/my-reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].tableId", is(tableId.intValue())));
    }

    @Test
    @WithMockUser(username = "staff@test.com", roles = "STAFF")
    void getReservationsByStatus_ReturnsFilteredList() throws Exception {
        // First create a reservation
        reservationService.createReservation(validReservationDto, testCustomer);

        mockMvc.perform(get("/api/reservations/status/{status}", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].status", is("CONFIRMED")));
    }

    @Test
    @WithMockUser(username = "customer@test.com", roles = "CUSTOMER")
    void checkTimeSlotAvailability_ReturnsTrue() throws Exception {
        // Pick a safe business-hour time regardless of current clock (ReservationUtil requires hour >= 6)
        String dateTime = LocalDateTime.now()
                .withHour(12)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
                .plusDays(1)
                .toString();

        mockMvc.perform(get("/api/reservations/check-availability")
                        .param("companyId", companyId.toString())
                        .param("tableId", tableId.toString())
                        .param("dateTime", dateTime))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void accessWithoutAuth_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/reservations/my-reservations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "staff@test.com", roles = "STAFF")
    void getUpcomingReservations_ReturnsSuccess() throws Exception {
        // First create a reservation
        reservationService.createReservation(validReservationDto, testCustomer);

        mockMvc.perform(get("/api/staff/reservations/upcoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @WithMockUser(username = "staff@test.com", roles = "STAFF")
    void getReservationsByCompany_ReturnsSuccess() throws Exception {
        // First create a reservation
        reservationService.createReservation(validReservationDto, testCustomer);

        mockMvc.perform(get("/api/staff/reservations/company/{companyId}", companyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].companyId", is(companyId.toString())));
    }

    @Test
    @WithMockUser(username = "staff@test.com", roles = "STAFF")
    void markAsNoShow_WithValidId_ReturnsSuccess() throws Exception {
        // First create a reservation
        ReservationDto savedReservation = reservationService.createReservation(
                validReservationDto,
                testCustomer
        );

        mockMvc.perform(post("/api/staff/reservations/{id}/no-show", savedReservation.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("NO_SHOW")));
    }

    @Test
    @WithMockUser(username = "staff@test.com", roles = "STAFF")
    void getPendingArrivalReservations_ReturnsSuccess() throws Exception {
        reservationService.createReservation(validReservationDto, testCustomer);

        mockMvc.perform(get("/api/staff/reservations/pending-arrival/{restaurantId}", companyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(0))));
    }
    @Test
    @WithMockUser(username = "staff@test.com", roles = "STAFF")
    void getExtendedStayReservations_ReturnsSuccess() throws Exception {
        // First create and check in a reservation
        ReservationDto savedReservation = reservationService.createReservation(
                validReservationDto,
                testCustomer
        );
        reservationService.checkInReservation(savedReservation.getId());

        mockMvc.perform(get("/api/staff/reservations/extended-stay/{restaurantId}", companyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(0))));
    }
}

