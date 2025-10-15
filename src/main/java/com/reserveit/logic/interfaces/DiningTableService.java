package com.reserveit.logic.interfaces;

import com.reserveit.dto.TablePositionDto;
import com.reserveit.enums.TableStatus;
import com.reserveit.model.DiningTable;

import java.util.List;
import java.util.UUID;

public interface DiningTableService {
    List<TablePositionDto> getTablesByCompany(UUID companyId);

    TablePositionDto addTable(TablePositionDto tableDto);

    void updateTablePosition(Long tableId, TablePositionDto positionDto);

    void updateTableStatus(Long tableId, TableStatus status);

    TablePositionDto getTablePosition(Long tableId);

    void deleteTable(Long tableId);

    List<TablePositionDto> getAvailableTables(UUID companyId, int numberOfPeople);

    void updateTablePositions(UUID companyId, List<TablePositionDto> updatedPositions);

    TablePositionDto updateTable(Long tableId, TablePositionDto updates);

    void updateTableStatus(Long tableId);
    DiningTable findById(Long id);



    }