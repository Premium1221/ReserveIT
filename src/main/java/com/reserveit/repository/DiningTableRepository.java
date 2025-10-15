package com.reserveit.repository;

import com.reserveit.enums.TableStatus;
import com.reserveit.model.DiningTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DiningTableRepository extends JpaRepository<DiningTable, Long> {
    // Basic find operations
    List<DiningTable> findByCompanyId(UUID companyId);

    @Query("SELECT t FROM DiningTable t WHERE t.id = :id AND t.company.id = :companyId")
    Optional<DiningTable> findByIdAndCompanyId(
            @Param("id") Long id,
            @Param("companyId") UUID companyId
    );

    Optional<DiningTable> findByCompanyIdAndTableNumber(
            @Param("companyId") UUID companyId,
            @Param("tableNumber") String tableNumber
    );

    // Available tables queries
    @Query("SELECT t FROM DiningTable t WHERE t.company.id = :companyId " +
            "AND t.capacity >= :numberOfPeople " +
            "AND t.status = com.reserveit.enums.TableStatus.AVAILABLE")
    List<DiningTable> findAvailableTables(
            @Param("companyId") UUID companyId,
            @Param("numberOfPeople") int numberOfPeople
    );

    @Query("SELECT t FROM DiningTable t WHERE t.company.id = :companyId " +
            "AND t.capacity >= :numberOfPeople " +
            "AND t.status = com.reserveit.enums.TableStatus.AVAILABLE " +
            "AND NOT EXISTS (SELECT r FROM Reservation r WHERE r.diningTable = t " +
            "AND r.reservationDate = :dateTime " +
            "AND r.status != com.reserveit.enums.ReservationStatus.CANCELLED)")
    Optional<DiningTable> findAvailableTable(
            @Param("companyId") UUID companyId,
            @Param("numberOfPeople") int numberOfPeople,
            @Param("dateTime") LocalDateTime dateTime
    );

    // Position-related queries
    @Query("SELECT COUNT(t) > 0 FROM DiningTable t " +
            "WHERE t.company.id = :companyId " +
            "AND t.xPosition = :xPosition " +
            "AND t.yPosition = :yPosition")
    boolean existsByPosition(
            @Param("companyId") UUID companyId,
            @Param("xPosition") int xPosition,
            @Param("yPosition") int yPosition
    );

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM DiningTable t " +
            "WHERE t.company.id = :companyId " +
            "AND t.xPosition = :xPosition " +
            "AND t.yPosition = :yPosition " +
            "AND t.id <> :excludeTableId")
    boolean existsByCompanyIdAndXPositionAndYPositionAndIdNot(
            @Param("companyId") UUID companyId,
            @Param("xPosition") int xPosition,
            @Param("yPosition") int yPosition,
            @Param("excludeTableId") Long excludeTableId
    );

    // Bulk operations
    @Query("SELECT t FROM DiningTable t WHERE t.id IN :ids AND t.company.id = :companyId")
    List<DiningTable> findByIdsAndCompanyId(
            @Param("ids") List<Long> tableIds,
            @Param("companyId") UUID companyId
    );

    // Update operations
    @Modifying
    @Transactional
    @Query("UPDATE DiningTable t SET t.status = :status WHERE t.id = :id")
    int updateStatus(
            @Param("id") Long id,
            @Param("status") TableStatus status
    );



    @Modifying
    @Transactional
    @Query("UPDATE DiningTable t SET t.xPosition = :xPosition, t.yPosition = :yPosition " +
            "WHERE t.id = :tableId AND t.company.id = :companyId")
    int updateTablePosition(
            @Param("tableId") Long tableId,
            @Param("companyId") UUID companyId,
            @Param("xPosition") int xPosition,
            @Param("yPosition") int yPosition
    );
}