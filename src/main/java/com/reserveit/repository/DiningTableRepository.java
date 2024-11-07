package com.reserveit.repository;

import com.reserveit.model.DiningTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DiningTableRepository extends JpaRepository<DiningTable, Long> {

    List<DiningTable> findByCompanyId(UUID companyId);

    @Query("SELECT t FROM DiningTable t WHERE t.company.id = :companyId " +
            "AND t.capacity >= :numberOfPeople " +
            "AND t.available = true " +
            "AND t.status = 'AVAILABLE'")
    List<DiningTable> findAvailableTables(
            @Param("companyId") UUID companyId,
            @Param("numberOfPeople") int numberOfPeople
    );

    @Query("SELECT t FROM DiningTable t WHERE t.company.id = :companyId " +
            "AND t.capacity >= :numberOfPeople " +
            "AND t.available = true " +
            "AND t.status = 'AVAILABLE' " +
            "AND NOT EXISTS (" +
            "    SELECT r FROM Reservation r " +
            "    WHERE r.diningTable = t " +
            "    AND r.reservationDate = :dateTime " +
            "    AND r.status NOT IN ('CANCELLED', 'COMPLETED')" +
            ")")
    Optional<DiningTable> findAvailableTable(
            @Param("companyId") UUID companyId,
            @Param("numberOfPeople") int numberOfPeople,
            @Param("dateTime") LocalDateTime dateTime
    );

    @Query("SELECT t FROM DiningTable t WHERE t.company.id = :companyId " +
            "AND t.tableNumber = :tableNumber")
    Optional<DiningTable> findByCompanyIdAndTableNumber(
            @Param("companyId") UUID companyId,
            @Param("tableNumber") String tableNumber
    );
}