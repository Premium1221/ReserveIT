package com.reserveit.service;

import com.reserveit.database.interfaces.CompanyDatabase;
import com.reserveit.database.interfaces.DiningTableDatabase;
import com.reserveit.database.interfaces.ReservationDatabase;
import com.reserveit.dto.ReservationDto;
import com.reserveit.enums.ReservationStatus;
import com.reserveit.enums.TableStatus;
import com.reserveit.logic.impl.ReservationServiceImpl;
import com.reserveit.logic.impl.TableAllocationServiceImpl;
import com.reserveit.logic.interfaces.UserService;
import com.reserveit.model.Company;
import com.reserveit.model.DiningTable;
import com.reserveit.model.Reservation;
import com.reserveit.model.User;
import com.reserveit.util.ReservationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private ReservationDatabase reservationDb;
    @Mock
    private CompanyDatabase companyDb;
    @Mock
    private DiningTableDatabase tableDb;
    @Mock
    private ReservationUtil reservationUtil;
    @Mock
    private TableAllocationServiceImpl tableAllocationService;
    @Mock
    private UserService userService;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private User testUser;
    private Company testCompany;
    private DiningTable testTable;
    private Reservation testReservation;

    @BeforeEach
    void setUp() {
        testUser = createTestUser();
        testCompany = createTestCompany();
        testTable = createTestTable(testCompany);
        testReservation = createTestReservation(testUser, testCompany, testTable);
    }

    @Nested
    class CreateReservationTests {
        @Test
        void createReservation_Success() {
            // Arrange
            ReservationDto dto = createTestReservationDto();
            testTable.setStatus(TableStatus.AVAILABLE);

            // Mock dependencies
            when(companyDb.findById(any(UUID.class))).thenReturn(Optional.of(testCompany));
            when(tableDb.findById(any(Long.class))).thenReturn(Optional.of(testTable));
            when(reservationUtil.isValidReservationTime(any(LocalDateTime.class))).thenReturn(true);
            when(reservationDb.save(any(Reservation.class))).thenReturn(testReservation);

            // Act
            ReservationDto result = reservationService.createReservation(dto, testUser);

            // Assert
            assertNotNull(result);
            assertEquals(testReservation.getId(), result.getId());
            verify(reservationDb, times(1)).save(any(Reservation.class));
        }


        @Test
        void createReservation_InvalidData_ThrowsException() {
            // Arrange
            ReservationDto dto = createTestReservationDto();
            dto.setNumberOfPeople(0);

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> reservationService.createReservation(dto, testUser));
        }
    }

    @Nested
    class CheckInTests {
        @Test
        void checkInReservation_Success() {
            // Arrange
            when(reservationDb.findById(anyLong())).thenReturn(Optional.of(testReservation));
            when(reservationDb.save(any())).thenReturn(testReservation);
            when(tableDb.save(any())).thenReturn(testTable);

            // Act
            ReservationDto result = reservationService.checkInReservation(1L);

            // Assert
            assertNotNull(result);
            assertEquals(ReservationStatus.ARRIVED, result.getStatus());
            verify(reservationDb).save(any(Reservation.class));
            verify(tableDb).save(any(DiningTable.class));
        }

        @Test
        void checkInReservation_WrongStatus_ThrowsException() {
            // Arrange
            testReservation.setStatus(ReservationStatus.COMPLETED);
            when(reservationDb.findById(anyLong())).thenReturn(Optional.of(testReservation));

            // Act & Assert
            assertThrows(IllegalStateException.class,
                    () -> reservationService.checkInReservation(1L));
        }
    }

    @Nested
    class CheckOutTests {
        @Test
        void checkOutReservation_Success() {
            // Arrange
            testReservation.setStatus(ReservationStatus.ARRIVED);
            when(reservationDb.findById(anyLong())).thenReturn(Optional.of(testReservation));
            when(reservationDb.save(any())).thenReturn(testReservation);
            when(tableDb.save(any())).thenReturn(testTable);

            // Act
            ReservationDto result = reservationService.checkOutReservation(1L);

            // Assert
            assertNotNull(result);
            assertEquals(ReservationStatus.COMPLETED, result.getStatus());
            verify(reservationDb).save(any(Reservation.class));
            verify(tableDb).save(any(DiningTable.class));
        }
    }

    @Nested
    class GetReservationsTests {
        @Test
        void getReservationsByUser_Success() {
            // Arrange
            List<Reservation> reservations = Arrays.asList(testReservation);
            when(reservationDb.findByUser(testUser)).thenReturn(reservations);

            // Act
            List<ReservationDto> result = reservationService.getReservationsByUser(testUser);

            // Assert
            assertFalse(result.isEmpty());
            assertEquals(1, result.size());
            verify(reservationDb).findByUser(testUser);
        }

        @Test
        void getReservationsByCompany_Success() {
            // Arrange
            List<Reservation> reservations = Arrays.asList(testReservation);
            when(companyDb.findById(testCompany.getId())).thenReturn(Optional.of(testCompany));
            when(reservationDb.findByCompany(testCompany)).thenReturn(reservations);

            // Act
            List<ReservationDto> result = reservationService.getReservationsByCompany(testCompany.getId());

            // Assert
            assertFalse(result.isEmpty());
            assertEquals(1, result.size());
            verify(reservationDb).findByCompany(testCompany);
        }

        @Test
        void getReservationsByDate_Success() {
            // Arrange
            List<Reservation> reservations = Arrays.asList(testReservation);
            when(companyDb.findById(testCompany.getId())).thenReturn(Optional.of(testCompany));
            when(reservationDb.findByCompanyAndReservationDateBetween(any(), any(), any()))
                    .thenReturn(reservations);

            // Act
            List<ReservationDto> result = reservationService.getReservationsByDate(
                    testCompany.getId(),
                    LocalDateTime.now()
            );

            // Assert
            assertFalse(result.isEmpty());
            assertEquals(1, result.size());
            verify(reservationDb).findByCompanyAndReservationDateBetween(any(), any(), any());
        }

        @Test
        void getExtendedStayReservations_Success() {
            // Arrange
            testReservation.setStatus(ReservationStatus.ARRIVED);
            testReservation.setReservationDate(LocalDateTime.now().minusHours(3));
            List<Reservation> reservations = Arrays.asList(testReservation);

            when(companyDb.findById(testCompany.getId())).thenReturn(Optional.of(testCompany));
            when(reservationDb.findByCompanyAndStatus(testCompany, ReservationStatus.ARRIVED))
                    .thenReturn(reservations);

            // Act
            List<ReservationDto> result = reservationService.getExtendedStayReservations(testCompany.getId());

            // Assert
            assertFalse(result.isEmpty());
            assertEquals(1, result.size());
            verify(reservationDb).findByCompanyAndStatus(testCompany, ReservationStatus.ARRIVED);
        }
        @Test
        void getReservationsForArrivalCheck_Success() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = now.minusMinutes(15);
            LocalDateTime end = now.plusMinutes(15);

            testReservation.setStatus(ReservationStatus.CONFIRMED);
            testReservation.setReservationDate(now); // Ensure reservation falls within the range

            List<Reservation> mockReservations = new ArrayList<>();
            mockReservations.add(testReservation);

            when(companyDb.findById(testCompany.getId())).thenReturn(Optional.of(testCompany));
            when(reservationDb.findByCompanyAndStatus(eq(testCompany), eq(ReservationStatus.CONFIRMED)))
                    .thenReturn(mockReservations);

            // Act
            List<ReservationDto> result = reservationService.getReservationsForArrivalCheck(
                    testCompany.getId(), start, end);

            // Assert
            assertNotNull(result);
            assertFalse(result.isEmpty()); // Should now pass
            verify(reservationDb).findByCompanyAndStatus(any(), any());
        }

        @Test
        void findByCompanyAndStatus_Success() {
            // Arrange
            List<Reservation> mockReservations = List.of(
                    createTestReservation(testUser, testCompany, testTable),
                    createTestReservation(testUser, testCompany, testTable)
            );
            mockReservations.get(0).setStatus(ReservationStatus.CONFIRMED);
            mockReservations.get(1).setStatus(ReservationStatus.CONFIRMED);

            when(companyDb.findById(testCompany.getId())).thenReturn(Optional.of(testCompany));
            when(reservationDb.findByCompanyAndStatus(testCompany, ReservationStatus.CONFIRMED))
                    .thenReturn(mockReservations);

            // Act
            List<ReservationDto> result = reservationService.findByCompanyAndStatus(
                    testCompany.getId(), ReservationStatus.CONFIRMED);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            verify(reservationDb).findByCompanyAndStatus(any(), any());
        }
        @Test
        void getReservationById_Success() {
            // Arrange
            Long reservationId = 1L;
            Reservation reservation = createTestReservation(testUser, testCompany, testTable);
            when(reservationDb.findById(reservationId)).thenReturn(Optional.of(reservation));

            // Act
            ReservationDto result = reservationService.getReservationById(reservationId, testUser);

            // Assert
            assertNotNull(result);
            assertEquals(reservation.getId(), result.getId());
            // Ensure DTO contains company name for UI display
            assertEquals("Test Restaurant", result.getCompanyName());
        }
        @Test
        void getReservationsByTimeRange_Success() {
            // Arrange
            LocalDateTime startTime = LocalDateTime.now();
            LocalDateTime endTime = startTime.plusHours(24);
            List<Reservation> reservations = List.of(createTestReservation(testUser, testCompany, testTable));

            when(reservationDb.findByReservationDateBetween(startTime, endTime))
                    .thenReturn(reservations);

            // Act
            List<ReservationDto> result = reservationService.getReservationsByTimeRange(startTime, endTime);

            // Assert
            assertNotNull(result);
            assertFalse(result.isEmpty());
            verify(reservationDb).findByReservationDateBetween(startTime, endTime);
        }
    }


    @Nested
    class QuickReservationTests {
        @Test
        void createQuickReservation_Success() {
            // Arrange
            ReservationDto dto = createTestReservationDto();

            // Mock company and table lookups
            when(companyDb.findById(testCompany.getId())).thenReturn(Optional.of(testCompany));
            when(tableDb.findById(testTable.getId())).thenReturn(Optional.of(testTable));



            // Mock reservation save
            when(reservationDb.save(any(Reservation.class))).thenReturn(testReservation);

            // Mock time validation
            when(reservationUtil.isValidReservationTime(any(LocalDateTime.class))).thenReturn(true);

            // Mock overlapping reservation check
            when(reservationDb.existsOverlappingReservation(
                    any(Long.class),
                    any(LocalDateTime.class),
                    any(LocalDateTime.class)
            )).thenReturn(false);

            // Set table as available
            testTable.setStatus(TableStatus.AVAILABLE);

            // Act
            ReservationDto result = reservationService.createQuickReservation(dto, true, testUser);

            // Assert
            assertNotNull(result);
            verify(reservationDb).save(any(Reservation.class));
            verify(tableDb).save(any(DiningTable.class));
        }
        @Test
        void createQuickReservation_ImmediateSuccess() {
            // Arrange
            ReservationDto dto = createTestReservationDto();
            when(companyDb.findById(testCompany.getId())).thenReturn(Optional.of(testCompany));
            when(tableDb.findById(any())).thenReturn(Optional.of(testTable));
            when(reservationUtil.isValidReservationTime(any())).thenReturn(true);
            when(reservationDb.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

            // Act
            ReservationDto result = reservationService.createQuickReservation(dto, true, testUser);

            // Assert
            assertNotNull(result);
            verify(tableDb).save(any(DiningTable.class));
            assertEquals(TableStatus.OCCUPIED, testTable.getStatus());
        }
        @Test
        void createQuickReservation_FutureSuccess() {
            // Arrange
            ReservationDto dto = createTestReservationDto();
            when(companyDb.findById(testCompany.getId())).thenReturn(Optional.of(testCompany));
            when(tableDb.findById(any())).thenReturn(Optional.of(testTable));
            when(reservationUtil.isValidReservationTime(any())).thenReturn(true);
            when(reservationDb.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

            // Act
            ReservationDto result = reservationService.createQuickReservation(dto, false, testUser);

            // Assert
            assertNotNull(result);
            verify(tableDb).save(any(DiningTable.class));
            assertEquals(TableStatus.RESERVED, testTable.getStatus());
        }


        @Test
        void createQuickReservation_InvalidData_ThrowsException() {
            // Arrange
            ReservationDto dto = createTestReservationDto();
            dto.setNumberOfPeople(0);

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> reservationService.createQuickReservation(dto, true, testUser));
        }
    }

    @Nested
    class NoShowTests {
        @Test
        void markAsNoShow_Success() {
            // Arrange
            when(reservationDb.findById(anyLong())).thenReturn(Optional.of(testReservation));
            when(reservationDb.save(any())).thenReturn(testReservation);
            when(tableDb.save(any())).thenReturn(testTable);

            // Act
            ReservationDto result = reservationService.markAsNoShow(1L);

            // Assert
            assertNotNull(result);
            assertEquals(ReservationStatus.NO_SHOW, result.getStatus());
            verify(reservationDb).save(any(Reservation.class));
            verify(tableDb).save(any(DiningTable.class));
        }

        @Test
        void markAsNoShow_WrongStatus_ThrowsException() {
            // Arrange
            testReservation.setStatus(ReservationStatus.COMPLETED);
            when(reservationDb.findById(anyLong())).thenReturn(Optional.of(testReservation));

            // Act & Assert
            assertThrows(IllegalStateException.class,
                    () -> reservationService.markAsNoShow(1L));
        }
    }

    @Nested
    class CancellationTests {
        @Test
        void cancelReservation_Success() {
            // Arrange
            when(reservationDb.findById(anyLong())).thenReturn(Optional.of(testReservation));
            when(reservationDb.save(any())).thenReturn(testReservation);

            // Act
            reservationService.cancelReservation(1L, testUser);

            // Assert
            assertEquals(ReservationStatus.CANCELLED, testReservation.getStatus());
            verify(reservationDb).save(any(Reservation.class));
        }

        @Test
        void cancelReservation_WrongUser_ThrowsException() {
            // Arrange
            User differentUser = createTestUser();
            differentUser.setId(UUID.randomUUID()); // Different ID
            when(reservationDb.findById(anyLong())).thenReturn(Optional.of(testReservation));

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> reservationService.cancelReservation(1L, differentUser));
        }
    }

    @Nested
    class ModificationTests {
        @Test
        void updateReservation_Success() {
            // Arrange
            ReservationDto updateDto = createTestReservationDto();
            updateDto.setNumberOfPeople(4);
            when(reservationDb.findById(anyLong())).thenReturn(Optional.of(testReservation));
            when(reservationDb.save(any())).thenReturn(testReservation);
            when(reservationUtil.isValidReservationTime(any())).thenReturn(true);

            // Act
            ReservationDto result = reservationService.updateReservation(1L, updateDto, testUser);

            // Assert
            assertNotNull(result);
            verify(reservationDb).save(any(Reservation.class));
            verify(reservationUtil).isValidReservationTime(any());
        }

        @Test
        void updateReservation_PastDate_ThrowsException() {
            // Arrange
            testReservation.setReservationDate(LocalDateTime.now().minusDays(1));
            ReservationDto updateDto = createTestReservationDto();
            when(reservationDb.findById(anyLong())).thenReturn(Optional.of(testReservation));

            // Act & Assert
            assertThrows(IllegalStateException.class,
                    () -> reservationService.updateReservation(1L, updateDto, testUser));
        }
    }

    @Nested
    class LateArrivalTests {
        @Test
        void checkForLateArrivals_Success() {
            // Arrange
            testReservation.setReservationDate(LocalDateTime.now().minusMinutes(20));
            List<Reservation> reservations = Arrays.asList(testReservation);
            when(reservationDb.findByStatus(ReservationStatus.CONFIRMED)).thenReturn(reservations);

            // Act
            List<Map<String, Object>> notifications = reservationService.checkForLateArrivals();

            // Assert
            assertFalse(notifications.isEmpty());
            assertEquals(1, notifications.size());
            verify(reservationDb).findByStatus(ReservationStatus.CONFIRMED);
        }
    }

    @Nested
    class ValidationTests {
        @Test
        void validateReservationOwnership_Success() {
            // Arrange
            when(reservationDb.findById(anyLong())).thenReturn(Optional.of(testReservation));

            // Act
            ReservationDto result = reservationService.getReservationById(1L, testUser);

            // Assert
            assertNotNull(result);
        }

        @Test
        void validateReservationOwnership_WrongUser_ThrowsException() {
            // Arrange
            User differentUser = createTestUser();
            differentUser.setId(UUID.randomUUID()); // Different ID
            when(reservationDb.findById(anyLong())).thenReturn(Optional.of(testReservation));

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> reservationService.getReservationById(1L, differentUser));
        }

        @Test
        void extendReservation_Success() {
            // Arrange
            testReservation.setStatus(ReservationStatus.ARRIVED);
            LocalDateTime reservationDate = LocalDateTime.now().minusHours(1);
            LocalDateTime endTime = reservationDate.plusMinutes(120);
            testReservation.setReservationDate(reservationDate);
            testReservation.setEndTime(endTime);

            when(reservationDb.findById(anyLong())).thenReturn(Optional.of(testReservation));
            when(reservationDb.save(any())).thenReturn(testReservation);

            // Act
            ReservationDto result = reservationService.extendReservation(1L, 30);

            // Assert
            assertNotNull(result);
            verify(reservationDb).save(any(Reservation.class));
        }
    }

    @Nested
    class TimeSlotAvailabilityTests {
        @Test
        void isTimeSlotAvailable_Success() {
            // Arrange
            when(tableDb.findById(anyLong())).thenReturn(Optional.of(testTable));
            when(reservationUtil.isValidReservationTime(any())).thenReturn(true);
            when(reservationDb.existsOverlappingReservation(any(), any(), any())).thenReturn(false);

            // Act
            boolean result = reservationService.isTimeSlotAvailable(
                    testCompany.getId(),
                    testTable.getId(),
                    LocalDateTime.now().plusHours(1).toString(),
                    120
            );

            // Assert
            assertTrue(result);
        }

        @Test
        void isTimeSlotAvailable_TableUnavailable_ReturnsFalse() {
            // Arrange
            testTable.setStatus(TableStatus.OUT_OF_SERVICE);
            when(tableDb.findById(anyLong())).thenReturn(Optional.of(testTable));

            // Act
            boolean result = reservationService.isTimeSlotAvailable(
                    testCompany.getId(),
                    testTable.getId(),
                    LocalDateTime.now().plusHours(1).toString(),
                    120
            );

            // Assert
            assertFalse(result);
        }
    }
    @Nested
    class ErrorHandlingTests {
        @Test
        void createReservation_InvalidDateTime_ThrowsException() {
            // Arrange
            ReservationDto dto = createTestReservationDto();
            dto.setReservationDate("invalid-date");

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> reservationService.createReservation(dto, testUser));
        }

        @Test
        void createReservation_TableNotAvailable_ThrowsException() {
            // Arrange
            ReservationDto dto = createTestReservationDto();
            testTable.setStatus(TableStatus.OUT_OF_SERVICE);

            when(companyDb.findById(any())).thenReturn(Optional.of(testCompany));

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> reservationService.createReservation(dto, testUser));
        }

        @Test
        void checkInReservation_NoTable_ThrowsException() {
            // Arrange
            testReservation.setDiningTable(null);
            when(reservationDb.findById(anyLong())).thenReturn(Optional.of(testReservation));

            // Act & Assert
            assertThrows(IllegalStateException.class,
                    () -> reservationService.checkInReservation(1L));
        }
    }
    @Test
    void getUpcomingReservations_CompanyNotFound_ThrowsException() {
        // Arrange
        UUID companyId = UUID.randomUUID();
        when(companyDb.findById(companyId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.getUpcomingReservations(companyId));
        assertEquals("Company ID not found" + companyId, exception.getMessage());
    }

    @Test
    void checkInReservation_NoDiningTable_ThrowsException() {
        // Arrange
        testReservation.setDiningTable(null);
        when(reservationDb.findById(anyLong())).thenReturn(Optional.of(testReservation));

        // Act & Assert
        Exception exception = assertThrows(IllegalStateException.class,
                () -> reservationService.checkInReservation(1L));
        assertEquals("Cannot check in reservation without an assigned table", exception.getMessage());
    }



    @Test
    void getUpcomingReservations_NoReservationsFound_ReturnsEmptyList() {
        // Arrange
        UUID companyId = testCompany.getId();
        when(companyDb.findById(companyId)).thenReturn(Optional.of(testCompany));
        when(reservationDb.findByCompanyAndReservationDateAfterAndStatusNot(
                eq(testCompany),
                any(LocalDateTime.class),
                eq(ReservationStatus.CANCELLED)))
                .thenReturn(Collections.emptyList());

        // Act
        List<ReservationDto> result = reservationService.getUpcomingReservations(companyId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(reservationDb).findByCompanyAndReservationDateAfterAndStatusNot(any(), any(), any());
    }


    // Helper methods
    private User createTestUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("test@example.com");
        return user;
    }

    private Company createTestCompany() {
        Company company = new Company();
        company.setId(UUID.randomUUID());
        company.setName("Test Restaurant");
        return company;
    }

    private DiningTable createTestTable(Company company) {
        DiningTable table = new DiningTable();
        table.setId(1L);
        table.setCompany(company);
        table.setStatus(TableStatus.AVAILABLE);
        table.setCapacity(4);
        return table;
    }

    private Reservation createTestReservation(User user, Company company, DiningTable table) {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setUser(user);
        reservation.setCompany(company);
        reservation.setDiningTable(table);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setReservationDate(LocalDateTime.now().plusHours(1));
        reservation.setDuration(120);
        reservation.setNumberOfPeople(2);
        return reservation;
    }

    private ReservationDto createTestReservationDto() {
        ReservationDto dto = new ReservationDto();
        dto.setCompanyId(testCompany.getId());
        dto.setTableId(testTable.getId());
        dto.setNumberOfPeople(2);
        dto.setDurationMinutes(120);
        dto.setReservationDate(LocalDateTime.now().plusHours(2).toString());
        return dto;
    }
}
