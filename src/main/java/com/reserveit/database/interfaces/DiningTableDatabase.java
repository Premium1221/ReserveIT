package com.reserveit.database.interfaces;

import com.reserveit.enums.TableShape;
import com.reserveit.enums.TableStatus;
import com.reserveit.model.DiningTable;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DiningTableDatabase {
    // Find operations
    List<DiningTable> findByCompanyId(UUID companyId);
    Optional<DiningTable> findById(Long id);
    Optional<DiningTable> findByIdAndCompanyId(Long id, UUID companyId);
    Optional<DiningTable> findByCompanyIdAndTableNumber(UUID companyId, String tableNumber);
    List<DiningTable> findAll();
    List<DiningTable> findByIdsAndCompanyId(List<Long> ids, UUID companyId);
    List<DiningTable> findAvailableTables(UUID companyId, int numberOfPeople);
    Optional<DiningTable> findAvailableTable(UUID companyId, int numberOfPeople, LocalDateTime dateTime);

    // Position validation
    boolean isPositionOccupied(UUID companyId, int xPosition, int yPosition);
    boolean isPositionOccupiedExcludingTable(UUID companyId, int xPosition, int yPosition, Long excludeTableId);

    // Update operations
    int updateTablePosition(Long tableId, UUID companyId, int xPosition, int yPosition);
    int updateTableStatus(Long tableId, TableStatus status);
    int updateTableProperties(
            Long tableId,
            UUID companyId,
            String tableNumber,
            int capacity,
            TableShape shape,
            TableStatus status,
            boolean isOutdoor,
            int floorLevel
    );

    // Save operations
    DiningTable save(DiningTable table);
    List<DiningTable> saveAll(List<DiningTable> tables);

    // Delete operation
    void deleteById(Long id);
}