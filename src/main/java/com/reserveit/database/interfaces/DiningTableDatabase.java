package com.reserveit.database.interfaces;

import com.reserveit.model.DiningTable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DiningTableDatabase {
    List<DiningTable> findByCompanyId(UUID companyId);
    List<DiningTable> findAvailableTables(UUID companyId, int numberOfPeople);
    Optional<DiningTable> findAvailableTable(UUID companyId, int numberOfPeople, LocalDateTime dateTime);
    Optional<DiningTable> findByCompanyIdAndTableNumber(UUID companyId, String tableNumber);
    DiningTable save(DiningTable diningTable);
    List<DiningTable> findAll();
    Optional<DiningTable> findById(Long id);
    void deleteById(Long id);
}