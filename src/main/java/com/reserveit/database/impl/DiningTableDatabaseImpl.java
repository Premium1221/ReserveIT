package com.reserveit.database.impl;

import com.reserveit.database.interfaces.DiningTableDatabase;
import com.reserveit.enums.TableStatus;
import com.reserveit.model.DiningTable;
import com.reserveit.repository.DiningTableRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Transactional
public class DiningTableDatabaseImpl implements DiningTableDatabase {
    private final DiningTableRepository diningTableRepository;

    public DiningTableDatabaseImpl(DiningTableRepository diningTableRepository) {
        this.diningTableRepository = diningTableRepository;
    }

    // Find operations
    @Override
    public List<DiningTable> findByCompanyId(UUID companyId) {
        return diningTableRepository.findByCompanyId(companyId);
    }

    @Override
    public Optional<DiningTable> findById(Long id) {
        return diningTableRepository.findById(id);
    }

    @Override
    public Optional<DiningTable> findByIdAndCompanyId(Long id, UUID companyId) {
        return diningTableRepository.findByIdAndCompanyId(id, companyId);
    }

    @Override
    public Optional<DiningTable> findByCompanyIdAndTableNumber(UUID companyId, String tableNumber) {
        return diningTableRepository.findByCompanyIdAndTableNumber(companyId, tableNumber);
    }

    @Override
    public List<DiningTable> findAll() {
        return diningTableRepository.findAll();
    }

    @Override
    public List<DiningTable> findByIdsAndCompanyId(List<Long> ids, UUID companyId) {
        return diningTableRepository.findByIdsAndCompanyId(ids, companyId);
    }

    @Override
    public List<DiningTable> findAvailableTables(UUID companyId, int numberOfPeople) {
        return diningTableRepository.findAvailableTables(companyId, numberOfPeople);
    }

    @Override
    public Optional<DiningTable> findAvailableTable(UUID companyId, int numberOfPeople, LocalDateTime dateTime) {
        return diningTableRepository.findAvailableTable(companyId, numberOfPeople, dateTime);
    }

    // Position validation
    @Override
    public boolean isPositionOccupied(UUID companyId, int xPosition, int yPosition) {
        return diningTableRepository.existsByPosition(companyId, xPosition, yPosition);
    }

    @Override
    public boolean isPositionOccupiedExcludingTable(UUID companyId, int xPosition, int yPosition, Long excludeTableId) {
        return diningTableRepository.existsByCompanyIdAndXPositionAndYPositionAndIdNot(
                companyId, xPosition, yPosition, excludeTableId);
    }

    // Update operations
    @Override
    @Transactional
    public int updateTablePosition(Long tableId, UUID companyId, int xPosition, int yPosition) {
        return diningTableRepository.updateTablePosition(tableId, companyId, xPosition, yPosition);
    }

    @Override
    @Transactional
    public int updateTableStatus(Long tableId, TableStatus status) {
        return diningTableRepository.updateStatus(tableId, status);
    }

    // Save operations
    @Override
    @Transactional
    public DiningTable save(DiningTable diningTable) {
        return diningTableRepository.save(diningTable);
    }

    @Override
    @Transactional
    public List<DiningTable> saveAll(List<DiningTable> tables) {
        return diningTableRepository.saveAll(tables);
    }

    // Delete operation
    @Override
    @Transactional
    public void deleteById(Long id) {
        diningTableRepository.deleteById(id);
    }
}