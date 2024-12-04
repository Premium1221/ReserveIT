package com.reserveit.database.impl;

import com.reserveit.database.interfaces.DiningTableDatabase;
import com.reserveit.model.DiningTable;
import com.reserveit.repository.DiningTableRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class DiningTableDatabaseImpl implements DiningTableDatabase {
    private final DiningTableRepository diningTableRepository;

    public DiningTableDatabaseImpl(DiningTableRepository diningTableRepository) {
        this.diningTableRepository = diningTableRepository;
    }

    @Override
    public List<DiningTable> findByCompanyId(UUID companyId) {
        return diningTableRepository.findByCompanyId(companyId);
    }

    @Override
    public List<DiningTable> findAvailableTables(UUID companyId, int numberOfPeople) {
        return diningTableRepository.findAvailableTables(companyId, numberOfPeople);
    }

    @Override
    public Optional<DiningTable> findAvailableTable(UUID companyId, int numberOfPeople, LocalDateTime dateTime) {
        return diningTableRepository.findAvailableTable(companyId, numberOfPeople, dateTime);
    }

    @Override
    public Optional<DiningTable> findByCompanyIdAndTableNumber(UUID companyId, String tableNumber) {
        return diningTableRepository.findByCompanyIdAndTableNumber(companyId, tableNumber);
    }

    @Override
    public DiningTable save(DiningTable diningTable) {
        return diningTableRepository.save(diningTable);
    }

    @Override
    public List<DiningTable> findAll() {
        return diningTableRepository.findAll();
    }

    @Override
    public Optional<DiningTable> findById(Long id) {
        return diningTableRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        diningTableRepository.deleteById(id);
    }
}