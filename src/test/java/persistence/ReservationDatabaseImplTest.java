package persistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.reserveit.model.Company;
import com.reserveit.model.Reservation;
import com.reserveit.repository.ReservationRepository;
import com.reserveit.database.impl.ReservationDatabaseImpl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ReservationDatabaseImplTest {

    @Mock
    private ReservationRepository reservationRepository;

    private ReservationDatabaseImpl reservationDatabase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reservationDatabase = new ReservationDatabaseImpl(reservationRepository);
    }

    @Test
    void findByCompany_ShouldReturnCompanyReservations() {
        // Arrange
        Company company = new Company();
        company.setName("Test Restaurant");

        Reservation reservation1 = new Reservation();
        reservation1.setCompany(company);
        Reservation reservation2 = new Reservation();
        reservation2.setCompany(company);

        List<Reservation> expectedReservations = Arrays.asList(reservation1, reservation2);
        when(reservationRepository.findByCompany(company)).thenReturn(expectedReservations);

        // Act
        List<Reservation> actualReservations = reservationDatabase.findByCompany(company);

        // Assert
        assertEquals(expectedReservations, actualReservations);
        verify(reservationRepository).findByCompany(company);
    }

    @Test
    void findByCompanyAndReservationDateAfterAndStatusNot_ShouldReturnFilteredReservations() {
        // Arrange
        Company company = new Company();
        LocalDateTime date = LocalDateTime.now();
        Reservation.ReservationStatus status = Reservation.ReservationStatus.CANCELLED;

        List<Reservation> expectedReservations = Arrays.asList(new Reservation(), new Reservation());
        when(reservationRepository.findByCompanyAndReservationDateAfterAndStatusNot(company, date, status))
                .thenReturn(expectedReservations);

        // Act
        List<Reservation> actualReservations =
                reservationDatabase.findByCompanyAndReservationDateAfterAndStatusNot(company, date, status);

        // Assert
        assertEquals(expectedReservations, actualReservations);
        verify(reservationRepository).findByCompanyAndReservationDateAfterAndStatusNot(company, date, status);
    }

    @Test
    void findByCompanyAndReservationDateBetween_ShouldReturnReservationsInDateRange() {
        // Arrange
        Company company = new Company();
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusDays(1);

        List<Reservation> expectedReservations = Arrays.asList(new Reservation(), new Reservation());
        when(reservationRepository.findByCompanyAndReservationDateBetween(company, startDate, endDate))
                .thenReturn(expectedReservations);

        // Act
        List<Reservation> actualReservations =
                reservationDatabase.findByCompanyAndReservationDateBetween(company, startDate, endDate);

        // Assert
        assertEquals(expectedReservations, actualReservations);
        verify(reservationRepository).findByCompanyAndReservationDateBetween(company, startDate, endDate);
    }

    @Test
    void findByCompanyAndTableNumber_ShouldReturnMatchingReservation() {
        // Arrange
        Company company = new Company();
        String tableNumber = "A1";
        Reservation expectedReservation = new Reservation();

        when(reservationRepository.findByCompanyAndTableNumber(company, tableNumber))
                .thenReturn(Optional.of(expectedReservation));

        // Act
        Optional<Reservation> actualReservation = reservationDatabase.findByCompanyAndTableNumber(company, tableNumber);

        // Assert
        assertTrue(actualReservation.isPresent());
        assertEquals(expectedReservation, actualReservation.get());
        verify(reservationRepository).findByCompanyAndTableNumber(company, tableNumber);
    }

    @Test
    void existsOverlappingReservation_ShouldReturnTrue_WhenOverlappingExists() {
        // Arrange
        Long tableId = 1L;
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(2);

        when(reservationRepository.existsOverlappingReservation(tableId, startTime, endTime))
                .thenReturn(true);

        // Act
        boolean exists = reservationDatabase.existsOverlappingReservation(tableId, startTime, endTime);

        // Assert
        assertTrue(exists);
        verify(reservationRepository).existsOverlappingReservation(tableId, startTime, endTime);
    }

    @Test
    void save_ShouldReturnSavedReservation() {
        // Arrange
        Reservation reservation = new Reservation();
        when(reservationRepository.save(reservation)).thenReturn(reservation);

        // Act
        Reservation savedReservation = reservationDatabase.save(reservation);

        // Assert
        assertEquals(reservation, savedReservation);
        verify(reservationRepository).save(reservation);
    }

    @Test
    void findById_ShouldReturnReservationWhenExists() {
        // Arrange
        Long id = 1L;
        Reservation reservation = new Reservation();
        when(reservationRepository.findById(id)).thenReturn(Optional.of(reservation));

        // Act
        Optional<Reservation> foundReservation = reservationDatabase.findById(id);

        // Assert
        assertTrue(foundReservation.isPresent());
        assertEquals(reservation, foundReservation.get());
        verify(reservationRepository).findById(id);
    }

    @Test
    void deleteById_ShouldCallRepositoryDelete() {
        // Arrange
        Long id = 1L;

        // Act
        reservationDatabase.deleteById(id);

        // Assert
        verify(reservationRepository).deleteById(id);
    }
}
