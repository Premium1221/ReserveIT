package com.reserveit.logic.impl;

import com.reserveit.database.interfaces.DiningTableDatabase;
import com.reserveit.database.interfaces.CompanyDatabase;
import com.reserveit.dto.TablePositionDto;
import com.reserveit.enums.TableShape;
import com.reserveit.enums.TableStatus;
import com.reserveit.model.Company;
import com.reserveit.model.DiningTable;
import com.reserveit.logic.interfaces.DiningTableService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class DiningTableServiceImpl implements DiningTableService {
    private final DiningTableDatabase tableDb;
    private final CompanyDatabase companyDb;

    public DiningTableServiceImpl(DiningTableDatabase tableDb, CompanyDatabase companyDb) {
        this.tableDb = tableDb;
        this.companyDb = companyDb;
    }

    // Read operations
    @Override
    public List<TablePositionDto> getTablesByCompany(UUID companyId) {
        return tableDb.findByCompanyId(companyId)
                .stream()
                .map(TablePositionDto::fromDiningTable)
                .collect(Collectors.toList());
    }

    @Override
    public List<TablePositionDto> getAvailableTables(UUID companyId, int numberOfPeople) {
        return tableDb.findAvailableTables(companyId, numberOfPeople)
                .stream()
                .map(TablePositionDto::fromDiningTable)
                .collect(Collectors.toList());
    }

    @Override
    public TablePositionDto getTablePosition(Long tableId) {
        return TablePositionDto.fromDiningTable(getTableById(tableId));
    }

    // Create operation
    @Override
    @Transactional
    public TablePositionDto addTable(TablePositionDto tableDto) {
        // Get and verify company
        Company company = getCompanyById(tableDto.getCompanyId());

        // Generate table number if not provided
        if (tableDto.getTableNumber() == null || tableDto.getTableNumber().isEmpty()) {
            List<DiningTable> existingTables = tableDb.findByCompanyId(company.getId());
            int nextNumber = existingTables.stream()
                    .map(table -> {
                        String num = table.getTableNumber().replaceAll("\\D", "");
                        return Integer.parseInt(num);
                    })
                    .max(Integer::compareTo)
                    .orElse(0) + 1;
            tableDto.setTableNumber("T" + nextNumber);
        }

        if (tableDto.getShape() == null) {
            tableDto.setShape(TableShape.CIRCLE);
        }
        if (tableDto.getStatus() == null) {
            tableDto.setStatus(TableStatus.AVAILABLE);
        }
        if (tableDto.getCapacity() <= 0) {
            tableDto.setCapacity(4);
        }

        validateNewTablePosition(tableDto.getCompanyId(), tableDto.getXPosition(), tableDto.getYPosition());

        DiningTable table = createDiningTableFromDto(tableDto, company);
        DiningTable savedTable = tableDb.save(table);

        return TablePositionDto.fromDiningTable(savedTable);
    }

    // Update operations
    @Override
    @Transactional
    public void updateTablePosition(Long tableId, TablePositionDto positionDto) {
        validatePositionData(positionDto);
        DiningTable table = getTableById(tableId);

        // Validate company ownership
        if (!table.getCompany().getId().equals(positionDto.getCompanyId())) {
            throw new IllegalArgumentException("Table does not belong to the specified company");
        }

        // Check position availability
        if (tableDb.isPositionOccupiedExcludingTable(
                positionDto.getCompanyId(),
                positionDto.getXPosition(),
                positionDto.getYPosition(),
                tableId)) {
            throw new IllegalStateException("Position is already occupied by another table");
        }

        // Update position
        table.setXPosition(positionDto.getXPosition());
        table.setYPosition(positionDto.getYPosition());
        tableDb.save(table);
    }

    @Override
    @Transactional
    public void updateTablePositions(UUID companyId, List<TablePositionDto> positions) {
        // Validate incoming positions
        positions.forEach(TablePositionDto::validate);

        // Fetch tables from the database
        List<Long> positionIds = positions.stream()
                .map(TablePositionDto::getId)
                .collect(Collectors.toList());
        List<DiningTable> tables = tableDb.findByIdsAndCompanyId(positionIds, companyId);

        if (tables.size() != positions.size()) {
            throw new IllegalArgumentException("Some tables do not belong to the specified company");
        }

        // Update table positions
        Map<Long, TablePositionDto> positionMap = positions.stream()
                .collect(Collectors.toMap(TablePositionDto::getId, Function.identity()));

        tables.forEach(table -> {
            TablePositionDto positionDto = positionMap.get(table.getId());
            if (positionDto != null) {
                table.setXPosition(positionDto.getXPosition());
                table.setYPosition(positionDto.getYPosition());
                table.setRotation(positionDto.getRotation());
                System.out.println("Updating Table ID: " + table.getId() +
                        " to Position (" + positionDto.getXPosition() + ", " + positionDto.getYPosition() + ")");
            }
        });

        // Save updated tables
        tableDb.saveAll(tables);
    }
    @Override
    @Transactional
    public TablePositionDto updateTable(Long tableId, TablePositionDto updates) {
        DiningTable table = getTableById(tableId);

        // Validate company ownership
        if (!table.getCompany().getId().equals(updates.getCompanyId())) {
            throw new IllegalArgumentException("Table does not belong to the specified company");
        }

        // Update all provided fields
        if (updates.getTableNumber() != null) {
            table.setTableNumber(updates.getTableNumber());
        }
        if (updates.getCapacity() > 0) {
            table.setCapacity(updates.getCapacity());
        }
        if (updates.getShape() != null) {
            table.setShape(updates.getShape());
        }
        if (updates.getStatus() != null) {
            table.setStatus(updates.getStatus());
        }
        if (updates.getXPosition() >= 0 && updates.getYPosition() >= 0) {
            // Check if the new position is available (if position is being updated)
            if (table.getXPosition() != updates.getXPosition() ||
                    table.getYPosition() != updates.getYPosition()) {

                if (tableDb.isPositionOccupiedExcludingTable(
                        updates.getCompanyId(),
                        updates.getXPosition(),
                        updates.getYPosition(),
                        tableId)) {
                    throw new IllegalStateException("Position is already occupied by another table");
                }

                table.setXPosition(updates.getXPosition());
                table.setYPosition(updates.getYPosition());
            }
        }

        table.setOutdoor(updates.isOutdoor());
        if (updates.getFloorLevel() > 0) {
            table.setFloorLevel(updates.getFloorLevel());
        }
        if (updates.getRotation() >= 0) {
            table.setRotation(updates.getRotation());
        }

        // Save and return updated table
        DiningTable savedTable = tableDb.save(table);
        return TablePositionDto.fromDiningTable(savedTable);
    }

    @Override
    @Transactional
    public void updateTableStatus(Long tableId, TableStatus status) {
        DiningTable table = getTableById(tableId);
        table.setStatus(status);
        tableDb.save(table);
    }

    // Delete operation
    @Override
    @Transactional
    public void deleteTable(Long tableId) {
        getTableById(tableId); // Verify existence
        tableDb.deleteById(tableId);
    }

    // Helper methods
    private void validatePositionData(TablePositionDto position) {
        if (position.getXPosition() < 0 || position.getYPosition() < 0) {
            throw new IllegalArgumentException(
                    "Invalid position values: x=" + position.getXPosition() +
                            ", y=" + position.getYPosition());
        }
    }

    private void validateNewTablePosition(UUID companyId, int xPosition, int yPosition) {
        if (tableDb.isPositionOccupied(companyId, xPosition, yPosition)) {
            throw new IllegalStateException("Position is already occupied by another table");
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