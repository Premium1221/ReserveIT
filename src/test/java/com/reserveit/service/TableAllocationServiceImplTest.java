package com.reserveit.service;

import com.reserveit.enums.TableShape;
import com.reserveit.enums.TableStatus;
import com.reserveit.logic.impl.TableAllocationServiceImpl;
import com.reserveit.model.DiningTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TableAllocationServiceImplTest {

    private TableAllocationServiceImpl tableAllocationService;

    @BeforeEach
    void setUp() {
        tableAllocationService = new TableAllocationServiceImpl();
    }

    @Nested
    class FindOptimalTableTests {
        @Test
        void findOptimalTable_ExactMatch() {
            // Arrange
            int partySize = 4;
            DiningTable perfectTable = createTable(4, TableStatus.AVAILABLE);
            List<DiningTable> availableTables = Arrays.asList(
                    perfectTable,
                    createTable(6, TableStatus.AVAILABLE),
                    createTable(2, TableStatus.AVAILABLE)
            );

            // Act
            Optional<DiningTable> result = tableAllocationService.findOptimalTable(availableTables, partySize);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(perfectTable.getCapacity(), result.get().getCapacity());
        }

        @Test
        void findOptimalTable_NoAvailableTables() {
            // Arrange
            List<DiningTable> emptyList = Collections.emptyList();

            // Act
            Optional<DiningTable> result = tableAllocationService.findOptimalTable(emptyList, 4);

            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        void findOptimalTable_OnlyOccupiedTables() {
            // Arrange
            List<DiningTable> occupiedTables = Arrays.asList(
                    createTable(4, TableStatus.OCCUPIED),
                    createTable(6, TableStatus.OCCUPIED)
            );

            // Act
            Optional<DiningTable> result = tableAllocationService.findOptimalTable(occupiedTables, 4);

            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        void findOptimalTable_PrefersSmallerValidTable() {
            // Arrange
            DiningTable smallerTable = createTable(4, TableStatus.AVAILABLE);
            DiningTable largerTable = createTable(6, TableStatus.AVAILABLE);
            List<DiningTable> tables = Arrays.asList(largerTable, smallerTable);

            // Act
            Optional<DiningTable> result = tableAllocationService.findOptimalTable(tables, 4);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(smallerTable.getCapacity(), result.get().getCapacity());
        }

        @Test
        void findOptimalTable_LargePartySize() {
            // Arrange
            int largePartySize = 10;
            List<DiningTable> tables = Arrays.asList(
                    createTable(4, TableStatus.AVAILABLE),
                    createTable(12, TableStatus.AVAILABLE),
                    createTable(8, TableStatus.AVAILABLE)
            );

            // Act
            Optional<DiningTable> result = tableAllocationService.findOptimalTable(tables, largePartySize);

            // Assert
            assertTrue(result.isPresent());
            assertTrue(result.get().getCapacity() >= largePartySize);
        }
    }

    @Nested
    class IsTableSuitableTests {
        @Test
        void isTableSuitable_PerfectMatch() {
            // Arrange
            DiningTable table = createTable(4, TableStatus.AVAILABLE);
            int partySize = 4;

            // Act
            boolean result = tableAllocationService.isTableSuitable(table, partySize);

            // Assert
            assertTrue(result);
        }

        @Test
        void isTableSuitable_TableTooSmall() {
            // Arrange
            DiningTable table = createTable(2, TableStatus.AVAILABLE);
            int partySize = 4;

            // Act
            boolean result = tableAllocationService.isTableSuitable(table, partySize);

            // Assert
            assertFalse(result);
        }

        @Test
        void isTableSuitable_TableNotAvailable() {
            // Arrange
            DiningTable table = createTable(4, TableStatus.OCCUPIED);
            int partySize = 4;

            // Act
            boolean result = tableAllocationService.isTableSuitable(table, partySize);

            // Assert
            assertFalse(result);
        }

        @Test
        void isTableSuitable_WithinAcceptableOverflow() {
            // Arrange
            DiningTable table = createTable(6, TableStatus.AVAILABLE);
            int partySize = 4;  // 2 extra seats is within acceptable overflow

            // Act
            boolean result = tableAllocationService.isTableSuitable(table, partySize);

            // Assert
            assertTrue(result);
        }

        @Test
        void isTableSuitable_TooMuchOverflow() {
            // Arrange
            DiningTable table = createTable(10, TableStatus.AVAILABLE);
            int partySize = 4;  // 6 extra seats is too much overflow

            // Act
            boolean result = tableAllocationService.isTableSuitable(table, partySize);

            // Assert
            assertFalse(result);
        }
    }
    @Nested
    class ComplexAllocationTests {
        @Test
        void findOptimalTable_MultipleValidOptions() {
            // Arrange
            int partySize = 4;
            List<DiningTable> tables = Arrays.asList(
                    createTable(4, TableStatus.AVAILABLE),
                    createTable(6, TableStatus.AVAILABLE),
                    createTable(8, TableStatus.AVAILABLE)
            );

            // Act
            Optional<DiningTable> result = tableAllocationService.findOptimalTable(tables, partySize);

            // Assert
            assertTrue(result.isPresent());
            assertEquals(4, result.get().getCapacity()); // Should choose the smallest valid table
        }

        @Test
        void findOptimalTable_AllTablesOccupied() {
            // Arrange
            int partySize = 4;
            List<DiningTable> tables = Arrays.asList(
                    createTable(4, TableStatus.OCCUPIED),
                    createTable(6, TableStatus.RESERVED),
                    createTable(8, TableStatus.OUT_OF_SERVICE)
            );

            // Act
            Optional<DiningTable> result = tableAllocationService.findOptimalTable(tables, partySize);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // Helper Methods
    private DiningTable createTable(int capacity, TableStatus status) {
        DiningTable table = new DiningTable();
        table.setCapacity(capacity);
        table.setStatus(status);
        table.setShape(TableShape.SQUARE);
        table.setTableNumber("T1");
        return table;
    }
}