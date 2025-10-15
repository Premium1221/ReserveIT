package com.reserveit.repository;

import com.reserveit.enums.ReservationStatus;
import com.reserveit.model.Company;
import com.reserveit.model.DiningTable;
import com.reserveit.model.Reservation;
import com.reserveit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    // Find all reservations for a company
    List<Reservation> findByCompany(Company company);

    // Find upcoming reservations for a company that aren't cancelled
    List<Reservation> findByCompanyAndReservationDateAfterAndStatusNot(Company company, LocalDateTime date, ReservationStatus status);

    List<Reservation> findByUser(User user);

    void deleteAll();
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
    @Query("SELECT COUNT(r) FROM Reservation r " +
            "WHERE r.diningTable.id = :tableId " +
            "AND r.status <> 'CANCELLED' " +
            "AND (" +
            "     (r.reservationDate BETWEEN :startTime AND :endTime) " +
            "     OR (r.reservationDate <= :startTime AND r.reservationDate >= :endTime)" +
            ")")
    long countOverlappingReservation(
            @Param("tableId") Long tableId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
    List<Reservation> findByStatus(ReservationStatus status);
    List<Reservation> findByReservationDateBetween(LocalDateTime startTime, LocalDateTime endTime);
    List<Reservation> findByCompanyAndStatus(Company company, ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.diningTable = :table " +
            "AND r.reservationDate BETWEEN :startDate AND :endDate")
    List<Reservation> findByDiningTableAndReservationDateBetween(
            @Param("table") DiningTable table,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT r FROM Reservation r WHERE r.company = :company " +
            "AND r.status = :status " +
            "AND r.reservationDate + r.duration < CURRENT_TIMESTAMP")
    List<Reservation> findExtendedStayReservations(
            @Param("company") Company company,
            @Param("status") ReservationStatus status
    );
    @Query("SELECT r FROM Reservation r WHERE r.user.id = :userId AND r.status <> 'CANCELLED'")
    List<Reservation> findActiveReservationsByUser(@Param("userId") UUID userId);

}