package com.reserveit.controller;

import com.reserveit.dto.TablePositionDto;
import com.reserveit.enums.TableShape;
import com.reserveit.enums.TableStatus;
import com.reserveit.logic.impl.WebSocketServiceImpl;
import com.reserveit.logic.interfaces.DiningTableService;
import com.reserveit.logic.interfaces.ReservationService;
import com.reserveit.logic.interfaces.TableAllocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

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



    @Test
    void checkAvailability_InvalidDateTime() {
        // Arrange
        UUID companyId = UUID.randomUUID();
        Long tableId = 1L;
        String invalidDateTime = "invalid-date-time";
        Integer duration = 120;

        when(reservationService.isTimeSlotAvailable(
                any(), any(), any(), any()
        )).thenThrow(new IllegalArgumentException("Invalid date time format"));

        // Act
        ResponseEntity<?> response = tableController.checkTimeSlotAvailability(
                companyId,
                tableId,
                invalidDateTime,
                duration
        );

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateTable_UnauthorizedCompany() {
        // Arrange
        Long tableId = 1L;
        UUID wrongCompanyId = UUID.randomUUID();
        TablePositionDto updateDto = createSampleTableDto(1L);
        updateDto.setCompanyId(wrongCompanyId);

        when(tableService.updateTable(eq(tableId), any()))
                .thenThrow(new IllegalArgumentException("Unauthorized access"));

        // Act
        ResponseEntity<?> response = tableController.updateTable(tableId, updateDto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    @Test
    void addTable_ValidationFailure() {
        // Arrange
        UUID companyId = UUID.randomUUID();
        TablePositionDto inputDto = createSampleTableDto(null);

        when(tableService.addTable(any(TablePositionDto.class)))
                .thenThrow(new IllegalArgumentException("Validation failed"));

        // Mock the security context details
        Map<String, String> details = new HashMap<>();
        details.put("companyId", companyId.toString());
        when(authentication.getDetails()).thenReturn(details);

        // Act
        ResponseEntity<Object> response = tableController.addTable(companyId, inputDto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().toString().contains("Validation failed"));
    }

    @Test
    void addTable_UnauthorizedAccess() {
        // Arrange
        UUID companyId = UUID.randomUUID();
        TablePositionDto inputDto = createSampleTableDto(null);

        // Mock different company ID in security context
        Map<String, String> details = new HashMap<>();
        details.put("companyId", UUID.randomUUID().toString());
        when(authentication.getDetails()).thenReturn(details);

        // Act
        ResponseEntity<Object> response = tableController.addTable(companyId, inputDto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Unauthorized access"));
    }

    @Test
    void addTable_ServerError() {
        // Arrange
        UUID companyId = UUID.randomUUID();
        TablePositionDto inputDto = createSampleTableDto(null);

        // Mock security context with correct company ID
        Map<String, String> details = new HashMap<>();
        details.put("companyId", companyId.toString());
        when(authentication.getDetails()).thenReturn(details);

        when(tableService.addTable(any(TablePositionDto.class)))
                .thenThrow(new RuntimeException("Internal server error"));

        // Act
        ResponseEntity<Object> response = tableController.addTable(companyId, inputDto);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Failed to add table"));
    }

    @Test
    void addTable_AutoAssignShape() {
        // Arrange
        UUID companyId = UUID.randomUUID();
        TablePositionDto inputDto = createSampleTableDto(null);
        inputDto.setShape(null); // Set shape to null to test auto-assignment

        // Mock security context
        Map<String, String> details = new HashMap<>();
        details.put("companyId", companyId.toString());
        when(authentication.getDetails()).thenReturn(details);

        when(tableService.addTable(any(TablePositionDto.class)))
                .thenReturn(inputDto);

        // Act
        ResponseEntity<Object> response = tableController.addTable(companyId, inputDto);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        TablePositionDto savedTable = (TablePositionDto) response.getBody();
        assertEquals(TableShape.CIRCLE, savedTable.getShape());
    }

    @Test
    void getTablesForCustomer_Success() {
        // Arrange
        UUID restaurantId = UUID.randomUUID();
        List<TablePositionDto> expectedTables = List.of(
                createSampleTableDto(1L),
                createSampleTableDto(2L)
        );
        when(tableService.getTablesByCompany(restaurantId))
                .thenReturn(expectedTables);

        // Act
        ResponseEntity<List<TablePositionDto>> response =
                tableController.getTablesForCustomer(restaurantId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void getTablesForCustomer_ServerError() {
        // Arrange
        UUID restaurantId = UUID.randomUUID();
        when(tableService.getTablesByCompany(restaurantId))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<List<TablePositionDto>> response =
                tableController.getTablesForCustomer(restaurantId);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(tableService).getTablesByCompany(restaurantId);
    }

    @Test
    void getTablesForCustomer_EmptyResult() {
        // Arrange
        UUID restaurantId = UUID.randomUUID();
        when(tableService.getTablesByCompany(restaurantId))
                .thenReturn(List.of());

        // Act
        ResponseEntity<List<TablePositionDto>> response =
                tableController.getTablesForCustomer(restaurantId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
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