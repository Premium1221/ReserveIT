package com.reserveit.repository;

import com.reserveit.model.Company;
import com.reserveit.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    // Find all reservations for a company
    List<Reservation> findByCompany(Company company);

    // Find upcoming reservations for a company that aren't cancelled
    List<Reservation> findByCompanyAndReservationDateAfterAndStatusNot(
            Company company,
            LocalDateTime date,
            Reservation.ReservationStatus status
    );

    // Find reservations between two dates for a company
    List<Reservation> findByCompanyAndReservationDateBetween(
            Company company,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    // Custom query to find reservations by table number
    @Query("SELECT r FROM Reservation r WHERE r.company = :company AND r.diningTable.tableNumber = :tableNumber")
    Optional<Reservation> findByCompanyAndTableNumber(
            @Param("company") Company company,
            @Param("tableNumber") String tableNumber
    );

    // Find overlapping reservations for a table
    @Query("SELECT COUNT(r) > 0 FROM Reservation r " +
            "WHERE r.diningTable.id = :tableId " +
            "AND r.status != 'CANCELLED' " +
            "AND ((r.reservationDate BETWEEN :startTime AND :endTime) " +
            "OR (r.reservationDate <= :startTime AND r.reservationDate >= :endTime))")
    boolean existsOverlappingReservation(
            @Param("tableId") Long tableId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}