package service;

import com.reserveit.dto.ReservationDto;
import com.reserveit.model.Reservation;
import com.reserveit.repository.ReservationRepository;
import com.reserveit.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationServiceImpl reservationServiceImpl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateReservation() {
        ReservationDto reservationDto = new ReservationDto();
        reservationDto.setCustomerName("John Doe");
        reservationDto.setReservationDate("2024-09-24");
        reservationDto.setNumberOfPeople(4);

        Reservation savedReservation = new Reservation();
        savedReservation.setId(1L);
        savedReservation.setCustomerName("John Doe");
        savedReservation.setReservationDate(java.time.LocalDate.of(2024, 9, 24));
        savedReservation.setNumberOfPeople(4);

        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);

        ReservationDto result = reservationServiceImpl.createReservation(reservationDto);

        assertNotNull(result.getId());
        assertEquals("John Doe", result.getCustomerName());
        assertEquals("2024-09-24", result.getReservationDate());
        assertEquals(4, result.getNumberOfPeople());

        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    void testGetAllReservations() {
        Reservation reservation1 = new Reservation();
        reservation1.setId(1L);
        reservation1.setCustomerName("John Doe");
        reservation1.setReservationDate(java.time.LocalDate.of(2024, 9, 24));
        reservation1.setNumberOfPeople(4);

        Reservation reservation2 = new Reservation();
        reservation2.setId(1L);
        reservation2.setCustomerName("Jane Smith");
        reservation2.setReservationDate(java.time.LocalDate.of(2024, 9, 25));
        reservation2.setNumberOfPeople(2);

        when(reservationRepository.findAll()).thenReturn(Arrays.asList(reservation1, reservation2));

        List<ReservationDto> reservations = reservationServiceImpl.getAllReservations();

        assertEquals(2, reservations.size());
        assertEquals("John Doe", reservations.get(0).getCustomerName());
        assertEquals("Jane Smith", reservations.get(1).getCustomerName());

        verify(reservationRepository, times(1)).findAll();
    }

    @Test
    void testUpdateReservation() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setCustomerName("John Doe");
        reservation.setReservationDate(java.time.LocalDate.of(2024, 9, 24));
        reservation.setNumberOfPeople(4);

        when(reservationRepository.findById(any(UUID.class))).thenReturn(Optional.of(reservation));

        ReservationDto updatedReservationDto = new ReservationDto();
        updatedReservationDto.setCustomerName("Jane Doe");
        updatedReservationDto.setReservationDate("2024-09-24");
        updatedReservationDto.setNumberOfPeople(5);

        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        ReservationDto result = reservationServiceImpl.updateReservation(reservation.getId(), updatedReservationDto);

        assertEquals("Jane Doe", result.getCustomerName());
        assertEquals(5, result.getNumberOfPeople());

        verify(reservationRepository, times(1)).findById(any(UUID.class));
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }
}
