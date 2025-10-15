package service;

import com.reserveit.database.interfaces.CompanyDatabase;
import com.reserveit.database.interfaces.DiningTableDatabase;
import com.reserveit.dto.TablePositionDto;
import com.reserveit.enums.TableShape;
import com.reserveit.enums.TableStatus;
import com.reserveit.logic.impl.DiningTableServiceImpl;
import com.reserveit.model.Company;
import com.reserveit.model.DiningTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DiningTableServiceImplTest {

    @Mock
    private DiningTableDatabase tableDb;

    @Mock
    private CompanyDatabase companyDb;

    @InjectMocks
    private DiningTableServiceImpl diningTableService;

    private UUID companyId;
    private Company testCompany;

    @BeforeEach
    void setUp() {
        diningTableService = new DiningTableServiceImpl(tableDb, companyDb);
        companyId = UUID.randomUUID();
        testCompany = createSampleCompany();
    }
    private static final UUID SAMPLE_COMPANY_ID = UUID.randomUUID();


    @Nested
    class GetTablesTests {
        @Test
        void getTablesByCompany_Success() {
            // Arrange
            UUID companyId = UUID.randomUUID();
            List<DiningTable> tables = Arrays.asList(
                    createSampleTable(),
                    createSampleTable()
            );
            when(tableDb.findByCompanyId(companyId)).thenReturn(tables);

            // Act
            List<TablePositionDto> result = diningTableService.getTablesByCompany(companyId);

            // Assert
            assertEquals(2, result.size());
            verify(tableDb).findByCompanyId(companyId);
        }


        @Test
        void getAvailableTables_Success() {
            // Arrange
            UUID companyId = UUID.randomUUID();
            int numberOfPeople = 4;
            List<DiningTable> availableTables = Arrays.asList(
                    createSampleTable(),
                    createSampleTable()
            );
            when(tableDb.findAvailableTables(companyId, numberOfPeople)).thenReturn(availableTables);

            // Act
            List<TablePositionDto> result = diningTableService.getAvailableTables(companyId, numberOfPeople);

            // Assert
            assertEquals(2, result.size());
            verify(tableDb).findAvailableTables(companyId, numberOfPeople);
        }
        @Test
        void getTablePosition_Success() {
            // Arrange
            Long tableId = 1L;
            DiningTable table = createSampleTable();
            when(tableDb.findById(tableId)).thenReturn(Optional.of(table));

            // Act
            TablePositionDto result = diningTableService.getTablePosition(tableId);

            // Assert
            assertNotNull(result);
            assertEquals(table.getTableNumber(), result.getTableNumber());
            verify(tableDb).findById(tableId);
        }
    }

    @Nested
    class AddTableTests {
        @Test
        void addTable_Success() {
            // Arrange
            TablePositionDto dto = createSampleTableDto();
            Company company = createSampleCompany();
            DiningTable table = createSampleTable();
            table.setCompany(company);

            when(companyDb.findById(dto.getCompanyId())).thenReturn(Optional.of(company));
            when(tableDb.save(any(DiningTable.class))).thenReturn(table);

            // Act
            TablePositionDto result = diningTableService.addTable(dto);

            // Assert
            assertNotNull(result);
            assertEquals(dto.getTableNumber(), result.getTableNumber());
            verify(companyDb).findById(dto.getCompanyId());
            verify(tableDb).save(any(DiningTable.class));
        }


        @Test
        void addTable_PositionOccupied() {
            // Arrange
            TablePositionDto dto = createSampleTableDto();
            Company company = createSampleCompany();

            when(companyDb.findById(any())).thenReturn(Optional.of(company));
            when(tableDb.isPositionOccupied(any(), anyInt(), anyInt())).thenReturn(true);

            // Act & Assert
            assertThrows(IllegalStateException.class, () -> diningTableService.addTable(dto));
        }

        @Test
        void addTable_CompanyNotFound() {
            // Arrange
            TablePositionDto dto = createSampleTableDto();
            when(companyDb.findById(any())).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> diningTableService.addTable(dto));
        }

        @Test
        void addTable_AutoGeneratesTableNumber() {
            // Arrange
            Company company = createSampleCompany();
            DiningTable existingTable = createSampleTable();
            TablePositionDto newTableDto = createSampleTableDto();
            newTableDto.setTableNumber(null); // Trigger auto-generation

            DiningTable savedTable = createSampleTable();
            savedTable.setTableNumber("T2"); // Simulate the next table number being auto-generated

            List<DiningTable> existingTables = List.of(existingTable);

            when(companyDb.findById(SAMPLE_COMPANY_ID)).thenReturn(Optional.of(company));
            when(tableDb.findByCompanyId(SAMPLE_COMPANY_ID)).thenReturn(existingTables);
            when(tableDb.save(any(DiningTable.class))).thenReturn(savedTable); // Mock save operation

            // Act
            TablePositionDto result = diningTableService.addTable(newTableDto);

            // Assert
            assertNotNull(result);
            assertEquals("T2", result.getTableNumber()); // Auto-generated table number
            verify(tableDb).save(any(DiningTable.class)); // Verify save was called
        }

    }

    @Nested
    class UpdateTableTests {
        @Test
        void updateTablePosition_Success() {
            // Arrange
            Long tableId = 1L;
            UUID companyId = UUID.randomUUID();

            TablePositionDto positionDto = createSampleTableDto();
            positionDto.setCompanyId(companyId);

            DiningTable table = createSampleTable();
            Company company = createSampleCompany();
            company.setId(companyId);
            table.setCompany(company);

            when(tableDb.findById(tableId)).thenReturn(Optional.of(table));
            when(tableDb.isPositionOccupiedExcludingTable(
                    any(UUID.class), anyInt(), anyInt(), anyLong())).thenReturn(false);

            // Act
            diningTableService.updateTablePosition(tableId, positionDto);

            // Assert
            verify(tableDb).save(any(DiningTable.class));
        }

        @Test
        void updateTable_PositionOccupied() {
            // Arrange
            Long tableId = 1L;
            UUID companyId = UUID.randomUUID();

            TablePositionDto positionDto = createSampleTableDto();
            positionDto.setCompanyId(companyId);

            DiningTable table = createSampleTable();
            Company company = createSampleCompany();
            company.setId(companyId);
            table.setCompany(company);

            when(tableDb.findById(tableId)).thenReturn(Optional.of(table));
            when(tableDb.isPositionOccupiedExcludingTable(
                    companyId, positionDto.getXPosition(), positionDto.getYPosition(), tableId))
                    .thenReturn(true);

            // Act & Assert
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> diningTableService.updateTablePosition(tableId, positionDto));
            assertEquals("Position is already occupied by another table", exception.getMessage());
        }
        @Test
        void updateTable_Success() {
            // Arrange
            Long tableId = 1L;
            UUID validCompanyId = UUID.randomUUID();

            TablePositionDto updates = createSampleTableDto();
            updates.setCompanyId(validCompanyId);

            DiningTable table = createSampleTable();
            Company validCompany = createSampleCompany();
            validCompany.setId(validCompanyId);
            table.setCompany(validCompany);

            when(tableDb.findById(tableId)).thenReturn(Optional.of(table));
            when(tableDb.save(any(DiningTable.class))).thenReturn(table);

            // Act
            TablePositionDto result = diningTableService.updateTable(tableId, updates);

            // Assert
            assertNotNull(result);
            verify(tableDb).save(any(DiningTable.class));
        }


        @Test
        void updateTablePosition_WrongCompany() {
            // Arrange
            Long tableId = 1L;
            UUID validCompanyId = UUID.randomUUID();
            UUID invalidCompanyId = UUID.randomUUID();

            TablePositionDto positionDto = createSampleTableDto();
            positionDto.setCompanyId(invalidCompanyId);

            DiningTable table = createSampleTable();
            Company company = createSampleCompany();
            company.setId(validCompanyId);
            table.setCompany(company);

            when(tableDb.findById(tableId)).thenReturn(Optional.of(table));

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> diningTableService.updateTablePosition(tableId, positionDto));
            assertEquals("Table does not belong to the specified company", exception.getMessage());
        }
        @Test
        void updateTableStatus_Success() {
            // Arrange
            Long tableId = 1L;
            DiningTable existingTable = createSampleTable();
            when(tableDb.findById(tableId)).thenReturn(Optional.of(existingTable));
            when(tableDb.save(any(DiningTable.class))).thenReturn(existingTable);

            // Act
            diningTableService.updateTableStatus(tableId, TableStatus.OCCUPIED);

            // Assert
            assertEquals(TableStatus.OCCUPIED, existingTable.getStatus());
            verify(tableDb).save(existingTable);
        }
    }

    @Nested
    class DeleteTableTests {
        @Test
        void deleteTable_Success() {
            // Arrange
            Long tableId = 1L;
            DiningTable table = createSampleTable();
            when(tableDb.findById(tableId)).thenReturn(Optional.of(table));

            // Act
            diningTableService.deleteTable(tableId);

            // Assert
            verify(tableDb).deleteById(tableId);
        }

        @Test
        void deleteTable_NotFound() {
            // Arrange
            Long tableId = 1L;
            when(tableDb.findById(tableId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> diningTableService.deleteTable(tableId));
        }
    }

    @Nested
    class TableStatusTests {
        @Test
        void updateTableStatus_Success() {
            // Arrange
            Long tableId = 1L;
            DiningTable table = createSampleTable();
            when(tableDb.findById(tableId)).thenReturn(Optional.of(table));

            // Act
            diningTableService.updateTableStatus(tableId, TableStatus.OCCUPIED);

            // Assert
            assertEquals(TableStatus.OCCUPIED, table.getStatus());
            verify(tableDb).save(table);
        }

        @Test
        void updateTableStatus_TableNotFound() {
            // Arrange
            Long tableId = 1L;
            when(tableDb.findById(tableId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> diningTableService.updateTableStatus(tableId, TableStatus.OCCUPIED));
        }
    }

    // Helper methods
    private TablePositionDto createSampleTableDto() {
        TablePositionDto dto = new TablePositionDto();
        dto.setId(1L);
        dto.setTableNumber("T1");
        dto.setCapacity(4);
        dto.setXPosition(100);
        dto.setYPosition(100);
        dto.setCompanyId(SAMPLE_COMPANY_ID);
        return dto;
    }

    private DiningTable createSampleTable() {
        DiningTable table = new DiningTable();
        table.setId(1L);
        table.setTableNumber("T1");
        table.setCapacity(4);
        table.setXPosition(100);
        table.setYPosition(100);
        table.setStatus(TableStatus.AVAILABLE);
        return table;
    }

    private Company createSampleCompany() {
        Company company = new Company();
        company.setId(SAMPLE_COMPANY_ID);
        company.setName("Test Company");
        return company;
    }
}