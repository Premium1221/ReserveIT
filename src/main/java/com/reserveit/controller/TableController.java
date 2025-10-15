package com.reserveit.controller;

import com.reserveit.dto.TablePositionDto;
import com.reserveit.enums.TableShape;
import com.reserveit.enums.TableStatus;
import com.reserveit.logic.interfaces.DiningTableService;
import com.reserveit.logic.impl.WebSocketServiceImpl;
import com.reserveit.logic.interfaces.ReservationService;
import com.reserveit.logic.interfaces.TableAllocationService;
import com.reserveit.logic.interfaces.WebSocketService;
import com.reserveit.model.DiningTable;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/tables")
@CrossOrigin(
        origins = {"http://localhost:5200", "http://127.0.0.1:5200", "http://172.29.96.1:5200"},
        allowCredentials = "true"
)
public class TableController {

    private final DiningTableService tableService;
    private final WebSocketService webSocketService;
    private final TableAllocationService tableAllocationService;
    private final ReservationService reservationService;

    public TableController(DiningTableService tableService, WebSocketServiceImpl webSocketService, TableAllocationService tableAllocationService, ReservationService reservationService) {
        this.tableService = tableService;
        this.webSocketService = webSocketService;
        this.tableAllocationService = tableAllocationService;
        this.reservationService = reservationService;
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<TablePositionDto>> getTablesByCompany(@PathVariable UUID companyId) {
        try {
            List<TablePositionDto> tables = tableService.getTablesByCompany(companyId);
            return ResponseEntity.ok(tables);
        } catch (Exception e) {
            logError("Failed to fetch tables for company ID: " + companyId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/company/{companyId}/positions")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> updateTablePositions(
            @PathVariable UUID companyId,
            @RequestBody List<TablePositionDto> positions) {
        try {
            if (positions.isEmpty()) {
                return ResponseEntity.badRequest().body("No positions provided");
            }

            tableService.updateTablePositions(companyId, positions);
            List<TablePositionDto> updatedTables = tableService.getTablesByCompany(companyId);

            return ResponseEntity.ok(updatedTables);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logError("Failed to update table positions", e);
            return ResponseEntity.internalServerError().body("Failed to update table positions");
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Object> updateStatus(@PathVariable Long id, @RequestBody TableStatus status) {
        try {
            tableService.updateTableStatus(id, status);
            notifyTableUpdate(id);
            return ResponseEntity.ok("Table status updated successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logError("Failed to update status for table ID: " + id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/company/{companyId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('STAFF')")
    public ResponseEntity<Object> addTable(
            @PathVariable UUID companyId,
            @Valid @RequestBody TablePositionDto tableDto) {
        try {
            validateAuthenticatedCompanyId(companyId);

            if (tableDto.getShape() == null) {
                tableDto.setShape(TableShape.CIRCLE);
            }

            tableDto.setCompanyId(companyId);

            TablePositionDto savedTable = tableService.addTable(tableDto);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(savedTable);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        } catch (Exception e) {
            logError("Failed to add table for company ID: " + companyId, e);
            return ResponseEntity.internalServerError()
                    .body("Failed to add table: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Object> updateTable(
            @PathVariable Long id,
            @Valid @RequestBody TablePositionDto tableDto) {
        try {
            validateAuthenticatedCompanyId(tableDto.getCompanyId());
            TablePositionDto updatedTable = tableService.updateTable(id, tableDto);
            notifyTableUpdate(id);
            return ResponseEntity.ok(updatedTable);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logError("Failed to update table: " + id, e);
            return ResponseEntity.internalServerError().body("Failed to update table");
        }
    }

    @GetMapping("/restaurant/{restaurantId}/tables")
    public ResponseEntity<List<TablePositionDto>> getTablesForCustomer(@PathVariable UUID restaurantId) {
        try {
            List<TablePositionDto> tables = tableService.getTablesByCompany(restaurantId);
            return ResponseEntity.ok(tables);
        } catch (Exception e) {
            logError("Failed to fetch tables for restaurant ID: " + restaurantId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
/*
    @GetMapping("/suggestions/{restaurantId}")
    public ResponseEntity<?> getTableSuggestions(
            @PathVariable UUID restaurantId,
            @RequestParam int partySize,
            @RequestParam(required = false) Long selectedTableId) {
        try {
            // Get available tables as DTOs
            List<TablePositionDto> availableTableDtos = tableService.getTablesByCompany(restaurantId)
                    .stream()
                    .filter(table -> table.getStatus() == TableStatus.AVAILABLE)
                    .collect(Collectors.toList());

            // Convert DTOs to entities for the allocation service
            List<DiningTable> availableTables = availableTableDtos.stream()
                    .map(this::convertDtoToEntity)
                    .collect(Collectors.toList());

            if (selectedTableId != null) {
                DiningTable selectedTable = tableService.findById(selectedTableId);
                if (selectedTable == null) {
                    return ResponseEntity.badRequest().body("Selected table not found");
                }

                List<TableSuggestion> suggestions =
                        tableAllocationService.getAlternativeSuggestions(availableTables, partySize, selectedTable);
                return ResponseEntity.ok(suggestions);
            } else {
                Optional<DiningTable> optimalTable =
                        tableAllocationService.findOptimalTable(availableTables, partySize);
                return optimalTable
                        .map(table -> ResponseEntity.ok(Collections.singletonList(
                                new TableSuggestion(null, "This is the best table for your party", true, 100))))
                        .orElse(ResponseEntity.ok(Collections.emptyList()));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error getting table suggestions: " + e.getMessage());
        }
    }
*/
    @GetMapping("/check-availability")
    public ResponseEntity<Object> checkTimeSlotAvailability(
            @RequestParam UUID companyId,
            @RequestParam Long tableId,
            @RequestParam String dateTime,
            @RequestParam(defaultValue = "120") Integer duration) {
        try {
            boolean isAvailable = reservationService.isTimeSlotAvailable(companyId, tableId, dateTime, duration);
            return ResponseEntity.ok(isAvailable);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/restaurant/{restaurantId}/availability")
    public ResponseEntity<Object> checkTablesAvailability(
            @PathVariable UUID restaurantId,
            @RequestParam String dateTime) {
        try {
            // Get all tables for the restaurant
            List<DiningTable> tables = tableService.getTablesByCompany(restaurantId)
                    .stream()
                    .map(dto -> tableService.findById(dto.getId()))
                    .toList();

            // Check availability for each table
            List<Map<String, Object>> availabilityInfo = tables.stream()
                    .map(table -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", table.getId());
                        map.put("isAvailable", table.isAvailableForDateTime(LocalDateTime.parse(dateTime)));
                        return map;
                    })
                    .toList();

            return ResponseEntity.ok(availabilityInfo);
        } catch (Exception e) {
            logError("Failed to check tables availability", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    // Utility Methods

    private void validateAuthenticatedCompanyId(UUID companyId) {
        Object details = SecurityContextHolder.getContext().getAuthentication().getDetails();
        if (details instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, String> detailsMap = (Map<String, String>) details;
            UUID authenticatedCompanyId = UUID.fromString(detailsMap.get("companyId"));
            if (!companyId.equals(authenticatedCompanyId)) {
                throw new IllegalArgumentException("Unauthorized access");
            }
        }
    }

    private void notifyTableUpdate(Long tableId) {
        try {
            webSocketService.notifyTableUpdate(tableId);
        } catch (Exception e) {
            logError("WebSocket notification failed for table ID: " + tableId, e);
        }
    }



    private void logError(String message, Exception e) {
        log.error("{} - Error: {}", message, e.getMessage());
    }
}
