package com.reserveit.logic.interfaces;

import com.reserveit.model.DiningTable;
import java.util.List;
import java.util.Optional;

public interface TableAllocationService {
    Optional<DiningTable> findOptimalTable(List<DiningTable> availableTables, int partySize);
    boolean isTableSuitable(DiningTable table, int partySize);

}