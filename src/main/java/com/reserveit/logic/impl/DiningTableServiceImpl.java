package com.reserveit.logic.impl;

import com.reserveit.database.interfaces.DiningTableDatabase;
import com.reserveit.database.interfaces.CompanyDatabase;
import com.reserveit.dto.TablePositionDto;
import com.reserveit.enums.ReservationStatus;
import com.reserveit.enums.TableShape;
import com.reserveit.enums.TableStatus;
import com.reserveit.model.Company;
import com.reserveit.model.DiningTable;
import com.reserveit.logic.interfaces.DiningTableService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class DiningTableServiceImpl implements DiningTableService {
    private final DiningTableDatabase tableDb;
    private final CompanyDatabase companyDb;
    private static final String POSITION_OCCUPIED = "Position is already occupied by another table";


    public DiningTableServiceImpl(DiningTableDatabase tableDb, CompanyDatabase companyDb) {
        this.tableDb = tableDb;
        this.companyDb = companyDb;
    }

    @Override
    public List<TablePositionDto> getTablesByCompany(UUID companyId) {
        return tableDb.findByCompanyId(companyId)
                .stream()
                .map(TablePositionDto::fromDiningTable)
                .toList();
    }

    @Override
    public List<TablePositionDto> getAvailableTables(UUID companyId, int numberOfPeople) {
        return tableDb.findAvailableTables(companyId, numberOfPeople)
                .stream()
                .map(TablePositionDto::fromDiningTable)
                .toList();
    }

    @Override
    public void updateTableStatus(Long tableId) {
        DiningTable table = getTableById(tableId);

        boolean hasActiveReservations = table.getReservations().stream()
                .anyMatch(reservation ->
                        reservation.getStatus() != ReservationStatus.CANCELLED &&
                                reservation.getReservationDate().isBefore(LocalDateTime.now()) &&
                                reservation.getEndTime().isAfter(LocalDateTime.now()));

        table.setStatus(hasActiveReservations ? TableStatus.RESERVED : TableStatus.AVAILABLE);
        tableDb.save(table);
    }

    @Override
    public void updateTableStatus(Long tableId, TableStatus status) {
        DiningTable table = getTableById(tableId);
        table.setStatus(status);
        tableDb.save(table);
    }

    @Override
    public TablePositionDto getTablePosition(Long tableId) {
        return TablePositionDto.fromDiningTable(getTableById(tableId));
    }

    @Override
    public TablePositionDto addTable(TablePositionDto tableDto) {
        Company company = getCompanyById(tableDto.getCompanyId());

        if (tableDto.getTableNumber() == null || tableDto.getTableNumber().isEmpty()) {
            List<DiningTable> existingTables = tableDb.findByCompanyId(company.getId());
            int nextNumber = existingTables.stream()
                    .map(table -> table.getTableNumber().replaceAll("\\D", ""))
                    .mapToInt(Integer::parseInt)
                    .max()
                    .orElse(0) + 1;
            tableDto.setTableNumber("T" + nextNumber);
        }

        tableDto.setShape(Optional.ofNullable(tableDto.getShape()).orElse(TableShape.CIRCLE));
        tableDto.setStatus(Optional.ofNullable(tableDto.getStatus()).orElse(TableStatus.AVAILABLE));
        if (tableDto.getCapacity() <= 0) tableDto.setCapacity(4);

        validateNewTablePosition(tableDto.getCompanyId(), tableDto.getXPosition(), tableDto.getYPosition());

        DiningTable table = createDiningTableFromDto(tableDto, company);
        DiningTable savedTable = tableDb.save(table);

        return TablePositionDto.fromDiningTable(savedTable);
    }

    @Override
    public void updateTablePosition(Long tableId, TablePositionDto positionDto) {
        validatePositionData(positionDto);
        DiningTable table = getTableById(tableId);

        if (!table.getCompany().getId().equals(positionDto.getCompanyId())) {
            throw new IllegalArgumentException("Table does not belong to the specified company");
        }

        if (tableDb.isPositionOccupiedExcludingTable(
                positionDto.getCompanyId(),
                positionDto.getXPosition(),
                positionDto.getYPosition(),
                tableId)) {
            throw new IllegalStateException(POSITION_OCCUPIED);
        }

        table.setXPosition(positionDto.getXPosition());
        table.setYPosition(positionDto.getYPosition());
        tableDb.save(table);
    }

    @Override
    public void updateTablePositions(UUID companyId, List<TablePositionDto> positions) {
        positions.forEach(TablePositionDto::validate);
        List<Long> positionIds = positions.stream().map(TablePositionDto::getId).toList();
        List<DiningTable> tables = tableDb.findByIdsAndCompanyId(positionIds, companyId);

        if (tables.size() != positions.size()) {
            throw new IllegalArgumentException("Some tables do not belong to the specified company");
        }

        Map<Long, TablePositionDto> positionMap = positions.stream()
                .collect(Collectors.toMap(TablePositionDto::getId, Function.identity()));

        tables.forEach(table -> {
            TablePositionDto positionDto = positionMap.get(table.getId());
            if (positionDto != null) {
                table.setXPosition(positionDto.getXPosition());
                table.setYPosition(positionDto.getYPosition());
                table.setRotation(positionDto.getRotation());
            }
        });

        tableDb.saveAll(tables);
    }

    @Override
    public TablePositionDto updateTable(Long tableId, TablePositionDto updates) {
        DiningTable table = getTableById(tableId);

        if (!table.getCompany().getId().equals(updates.getCompanyId())) {
            throw new IllegalArgumentException("Table does not belong to the specified company");
        }

        Optional.ofNullable(updates.getTableNumber()).ifPresent(table::setTableNumber);
        if (updates.getCapacity() > 0) table.setCapacity(updates.getCapacity());
        Optional.ofNullable(updates.getShape()).ifPresent(table::setShape);
        Optional.ofNullable(updates.getStatus()).ifPresent(table::setStatus);
        if (updates.getXPosition() >= 0 && updates.getYPosition() >= 0) {
            if (tableDb.isPositionOccupiedExcludingTable(
                    updates.getCompanyId(),
                    updates.getXPosition(),
                    updates.getYPosition(),
                    tableId)) {
                throw new IllegalStateException(POSITION_OCCUPIED);
            }

            table.setXPosition(updates.getXPosition());
            table.setYPosition(updates.getYPosition());
        }

        table.setOutdoor(updates.isOutdoor());
        if (updates.getFloorLevel() > 0) table.setFloorLevel(updates.getFloorLevel());
        if (updates.getRotation() >= 0) table.setRotation(updates.getRotation());

        DiningTable savedTable = tableDb.save(table);
        return TablePositionDto.fromDiningTable(savedTable);
    }

    @Override
    public void deleteTable(Long tableId) {
        getTableById(tableId);
        tableDb.deleteById(tableId);
    }

    @Override
    public DiningTable findById(Long id) {
        return getTableById(id);
    }

    private void validatePositionData(TablePositionDto position) {
        if (position.getXPosition() < 0 || position.getYPosition() < 0) {
            throw new IllegalArgumentException(
                    "Invalid position values: x=" + position.getXPosition() +
                            ", y=" + position.getYPosition());
        }
    }

    private void validateNewTablePosition(UUID companyId, int xPosition, int yPosition) {
        if (tableDb.isPositionOccupied(companyId, xPosition, yPosition)) {
            throw new IllegalStateException(POSITION_OCCUPIED);
        }
    }

    private Company getCompanyById(UUID companyId) {
        return companyDb.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with ID: " + companyId));
    }

    private DiningTable getTableById(Long tableId) {
        return tableDb.findById(tableId)
                .orElseThrow(() -> new IllegalArgumentException("Table not found with ID: " + tableId));
    }

    private DiningTable createDiningTableFromDto(TablePositionDto tableDto, Company company) {
        DiningTable table = new DiningTable();
        table.setTableNumber(tableDto.getTableNumber());
        table.setCapacity(tableDto.getCapacity());
        table.setXPosition(tableDto.getXPosition());
        table.setYPosition(tableDto.getYPosition());
        table.setShape(tableDto.getShape());
        table.setStatus(tableDto.getStatus());
        table.setOutdoor(tableDto.isOutdoor());
        table.setFloorLevel(tableDto.getFloorLevel());
        table.setCompany(company);
        table.setAvailable(true);
        return table;
    }
}