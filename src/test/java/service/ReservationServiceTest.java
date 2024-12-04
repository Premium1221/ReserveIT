package service;

import com.reserveit.database.interfaces.CompanyDatabase;
import com.reserveit.database.interfaces.DiningTableDatabase;
import com.reserveit.database.interfaces.ReservationDatabase;
import com.reserveit.dto.ReservationDto;
import com.reserveit.model.Company;
import com.reserveit.model.Reservation;
import com.reserveit.logic.impl.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReservationServiceTest {

    @Mock
    private ReservationDatabase reservationDb;

    @Mock
    private CompanyDatabase companyDb;

    @Mock
    private DiningTableDatabase tableDb;

    private ReservationServiceImpl reservationServiceImpl;
    private Company mockCompany;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reservationServiceImpl = new ReservationServiceImpl(
                reservationDb,
                companyDb,
                tableDb
        );

        mockCompany = new Company();
        mockCompany.setId(UUID.randomUUID());
        mockCompany.setName("Test Company");
    }

    @Test
    void testCreateReservation() {
        // Arrange
        UUID companyId = mockCompany.getId();
        LocalDateTime reservationTime = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0).withSecond(0).withNano(0);
        String reservationTimeStr = reservationTime.format(formatter);

        ReservationDto dto = new ReservationDto();
        dto.setCustomerName("John Doe");
        dto.setReservationDate(reservationTimeStr);
        dto.setNumberOfPeople(4);
        dto.setCompanyId(companyId);

        Reservation savedReservation = new Reservation();
        savedReservation.setId(1L);
        savedReservation.setCustomerName("John Doe");
        savedReservation.setReservationDate(reservationTime);
        savedReservation.setNumberOfPeople(4);
        savedReservation.setCompany(mockCompany);

        when(companyDb.findById(companyId)).thenReturn(mockCompany);
        when(reservationDb.save(any(Reservation.class))).thenReturn(savedReservation);

        // Act
        ReservationDto result = reservationServiceImpl.createReservation(dto);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getCustomerName());
        assertEquals(4, result.getNumberOfPeople());
        verify(reservationDb).save(any(Reservation.class));
    }

    @Test
    void testGetAllReservations() {
        // Arrange
        Reservation reservation1 = new Reservation();
        reservation1.setId(1L);
        reservation1.setCustomerName("John Doe");
        reservation1.setReservationDate(LocalDateTime.now().plusDays(1));
        reservation1.setNumberOfPeople(4);
        reservation1.setCompany(mockCompany);

        Reservation reservation2 = new Reservation();
        reservation2.setId(2L);
        reservation2.setCustomerName("Jane Smith");
        reservation2.setReservationDate(LocalDateTime.now().plusDays(2));
        reservation2.setNumberOfPeople(2);
        reservation2.setCompany(mockCompany);

        when(reservationDb.findAll()).thenReturn(Arrays.asList(reservation1, reservation2));

        // Act
        List<ReservationDto> reservations = reservationServiceImpl.getAllReservations();

        // Assert
        assertEquals(2, reservations.size());
        assertEquals("John Doe", reservations.get(0).getCustomerName());
        assertEquals("Jane Smith", reservations.get(1).getCustomerName());
    }

    @Test
    void testUpdateReservation() {
        // Arrange
        Long reservationId = 1L;
        LocalDateTime reservationTime = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0).withSecond(0).withNano(0);
        String reservationTimeStr = reservationTime.format(formatter);

        Reservation existingReservation = new Reservation();
        existingReservation.setId(reservationId);
        existingReservation.setCustomerName("John Doe");
        existingReservation.setReservationDate(reservationTime);
        existingReservation.setNumberOfPeople(4);
        existingReservation.setCompany(mockCompany);

        ReservationDto updateDto = new ReservationDto();
        updateDto.setCustomerName("Jane Doe");
        updateDto.setReservationDate(reservationTimeStr);
        updateDto.setNumberOfPeople(5);
        updateDto.setCompanyId(mockCompany.getId());

        when(reservationDb.findById(reservationId)).thenReturn(Optional.of(existingReservation));
        when(companyDb.findById(mockCompany.getId())).thenReturn(mockCompany);

        Reservation updatedReservation = new Reservation();
        updatedReservation.setId(reservationId);
        updatedReservation.setCustomerName("Jane Doe");
        updatedReservation.setReservationDate(reservationTime);
        updatedReservation.setNumberOfPeople(5);
        updatedReservation.setCompany(mockCompany);

        when(reservationDb.save(any(Reservation.class))).thenReturn(updatedReservation);

        // Act
        ReservationDto result = reservationServiceImpl.updateReservation(reservationId, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals("Jane Doe", result.getCustomerName());
        assertEquals(5, result.getNumberOfPeople());
        verify(reservationDb).findById(reservationId);
        verify(reservationDb).save(any(Reservation.class));
    }

    @Test
    void testCancelReservation() {
        // Arrange
        Long reservationId = 1L;
        LocalDateTime futureDate = LocalDateTime.now().plusDays(1);

        Reservation mockReservation = new Reservation();
        mockReservation.setId(reservationId);
        mockReservation.setReservationDate(futureDate);
        mockReservation.setStatus(Reservation.ReservationStatus.CONFIRMED);

        when(reservationDb.findById(reservationId)).thenReturn(Optional.of(mockReservation));
        when(reservationDb.save(any(Reservation.class))).thenReturn(mockReservation);

        // Act
        assertDoesNotThrow(() -> reservationServiceImpl.cancelReservation(reservationId));

        // Assert
        verify(reservationDb).findById(reservationId);
        verify(reservationDb).save(any(Reservation.class));
        assertEquals(Reservation.ReservationStatus.CANCELLED, mockReservation.getStatus());
    }

    @Test
    void testCancelReservation_PastReservation_ThrowsException() {
        // Arrange
        Long reservationId = 1L;
        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);

        Reservation mockReservation = new Reservation();
        mockReservation.setId(reservationId);
        mockReservation.setReservationDate(pastDate);

        when(reservationDb.findById(reservationId)).thenReturn(Optional.of(mockReservation));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> reservationServiceImpl.cancelReservation(reservationId));
        assertEquals("Cannot cancel past reservations", exception.getMessage());
    }

    @Test
    void testCreateReservation_InvalidCompany_ThrowsException() {
        // Arrange
        UUID invalidCompanyId = UUID.randomUUID();
        ReservationDto dto = new ReservationDto();
        dto.setCompanyId(invalidCompanyId);
        dto.setCustomerName("Valid Name");
        dto.setNumberOfPeople(1);

        when(companyDb.findById(invalidCompanyId)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationServiceImpl.createReservation(dto));
        assertEquals("Company not found with id: " + invalidCompanyId, exception.getMessage());
    }


    @Test
    void testGetReservationsByCompany() {
        // Arrange
        UUID companyId = mockCompany.getId();
        Reservation reservation = new Reservation();
        reservation.setCompany(mockCompany);
        reservation.setCustomerName("Test Customer");
        reservation.setReservationDate(LocalDateTime.now().plusDays(1)); // Ensure reservationDate is set

        when(companyDb.findById(companyId)).thenReturn(mockCompany);
        when(reservationDb.findByCompany(mockCompany)).thenReturn(Arrays.asList(reservation));

        // Act
        List<ReservationDto> result = reservationServiceImpl.getReservationsByCompany(companyId);

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Test Customer", result.get(0).getCustomerName());
    }

    @Test
    void testGetReservationById_NotFound_ThrowsException() {
        // Arrange
        Long nonExistentId = 999L;
        when(reservationDb.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationServiceImpl.getReservationById(nonExistentId));
        assertTrue(exception.getMessage().contains("Reservation not found"));
    }

    @Test
    void testGetUpcomingReservations() {
        // Arrange
        UUID companyId = mockCompany.getId();
        Reservation upcomingReservation = new Reservation();
        upcomingReservation.setCompany(mockCompany);
        upcomingReservation.setReservationDate(LocalDateTime.now().plusDays(1));
        upcomingReservation.setStatus(Reservation.ReservationStatus.CONFIRMED);

        when(companyDb.findById(companyId)).thenReturn(mockCompany);
        when(reservationDb.findByCompanyAndReservationDateAfterAndStatusNot(
                eq(mockCompany),
                any(LocalDateTime.class),
                eq(Reservation.ReservationStatus.CANCELLED)
        )).thenReturn(Arrays.asList(upcomingReservation));

        // Act
        List<ReservationDto> result = reservationServiceImpl.getUpcomingReservations(companyId);

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }
}
