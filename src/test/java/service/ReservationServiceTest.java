package service;

import com.reserveit.database.interfaces.ICompanyDatabase;
import com.reserveit.database.interfaces.IDiningTableDatabase;
import com.reserveit.database.interfaces.IReservationDatabase;
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
    private IReservationDatabase reservationDb;

    @Mock
    private ICompanyDatabase companyDb;

    @Mock
    private IDiningTableDatabase tableDb;

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
}