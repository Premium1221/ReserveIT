package controller;

import com.reserveit.controller.TableController;
import com.reserveit.dto.TablePositionDto;
import com.reserveit.enums.TableShape;
import com.reserveit.enums.TableStatus;
import com.reserveit.logic.impl.WebSocketServiceImpl;
import com.reserveit.logic.interfaces.DiningTableService;
import com.reserveit.logic.interfaces.ReservationService;
import com.reserveit.logic.interfaces.TableAllocationService;
import com.reserveit.logic.interfaces.WebSocketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class TableControllerTest {

    @Mock
    private DiningTableService tableService;
    @Mock
    private WebSocketServiceImpl webSocketService;
    @Mock
    private TableAllocationService tableAllocationService;
    @Mock
    private ReservationService reservationService;

    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;

    private TableController tableController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup Security Context mock
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        tableController = new TableController(tableService, webSocketService, tableAllocationService, reservationService);
    }

    @Test
    void getTablesByCompany_Success() {
        // Arrange
        UUID companyId = UUID.randomUUID();
        List<TablePositionDto> expectedTables = Arrays.asList(
                createSampleTableDto(1L),
                createSampleTableDto(2L)
        );
        when(tableService.getTablesByCompany(companyId)).thenReturn(expectedTables);

        // Act
        ResponseEntity<List<TablePositionDto>> response = tableController.getTablesByCompany(companyId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void addTable_Success() {
        // Arrange
        UUID companyId = UUID.randomUUID();
        TablePositionDto inputDto = createSampleTableDto(null);
        TablePositionDto savedDto = createSampleTableDto(1L);
        when(tableService.addTable(any(TablePositionDto.class))).thenReturn(savedDto);

        // Act
        ResponseEntity<?> response = tableController.addTable(companyId, inputDto);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void updateTablePositions_Success() {
        // Arrange
        UUID companyId = UUID.randomUUID();
        List<TablePositionDto> positions = Arrays.asList(
                createSampleTableDto(1L),
                createSampleTableDto(2L)
        );
        List<TablePositionDto> updatedPositions = Arrays.asList(
                createSampleTableDto(1L),
                createSampleTableDto(2L)
        );
        when(tableService.getTablesByCompany(companyId)).thenReturn(updatedPositions);

        // Act
        ResponseEntity<?> response = tableController.updateTablePositions(companyId, positions);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(tableService).updateTablePositions(eq(companyId), any());
    }

    @Test
    void updateTableStatus_Success() {
        // Arrange
        Long tableId = 1L;
        TableStatus newStatus = TableStatus.OCCUPIED;

        // Act
        ResponseEntity<?> response = tableController.updateStatus(tableId, newStatus);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(tableService).updateTableStatus(tableId, newStatus);
        verify(webSocketService).notifyTableUpdate(tableId);
    }

    @Test
    void checkTimeSlotAvailability_Success() {
        // Arrange
        UUID companyId = UUID.randomUUID();
        Long tableId = 1L;
        String dateTime = "2024-01-20T18:00:00";
        Integer duration = 120;

        when(reservationService.isTimeSlotAvailable(companyId, tableId, dateTime, duration))
                .thenReturn(true);

        // Act
        ResponseEntity<?> response = tableController.checkTimeSlotAvailability(
                companyId, tableId, dateTime, duration);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody());
    }

    @Test
    void updateTable_Success() {
        // Arrange
        Long tableId = 1L;
        TablePositionDto updateDto = createSampleTableDto(tableId);
        when(tableService.updateTable(eq(tableId), any(TablePositionDto.class)))
                .thenReturn(updateDto);

        // Act
        ResponseEntity<?> response = tableController.updateTable(tableId, updateDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(webSocketService).notifyTableUpdate(tableId);
    }
    @Test
    void updateTablePositions_EmptyList_BadRequest() {
        // Arrange
        UUID companyId = UUID.randomUUID();
        List<TablePositionDto> emptyList = Collections.emptyList();

        // Act
        ResponseEntity<?> response = tableController.updateTablePositions(companyId, emptyList);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("No positions provided", response.getBody());
    }

    @Test
    void updateTableStatus_NotifyWebSocket_Success() {
        // Arrange
        Long tableId = 1L;
        TableStatus newStatus = TableStatus.OCCUPIED;

        doNothing().when(webSocketService).notifyTableUpdate(tableId);

        // Act
        ResponseEntity<?> response = tableController.updateStatus(tableId, newStatus);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(webSocketService).notifyTableUpdate(tableId);
    }

    // Helper method to create sample TablePositionDto
    private TablePositionDto createSampleTableDto(Long id) {
        TablePositionDto dto = new TablePositionDto();
        dto.setId(id);
        dto.setTableNumber("T" + (id != null ? id : "1"));
        dto.setCapacity(4);
        dto.setShape(TableShape.CIRCLE);
        dto.setStatus(TableStatus.AVAILABLE);
        dto.setXPosition(100);
        dto.setYPosition(100);
        dto.setCompanyId(UUID.randomUUID());
        return dto;
    }
}