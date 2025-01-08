package com.reserveit.controller;

import com.reserveit.dto.TablePositionDto;
import com.reserveit.enums.TableShape;
import com.reserveit.enums.TableStatus;
import com.reserveit.logic.interfaces.DiningTableService;
import com.reserveit.logic.impl.WebSocketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/tables")
@CrossOrigin(origins = "http://localhost:5200", allowCredentials = "true")
public class TableController {

    private final DiningTableService tableService;
    private final WebSocketService webSocketService;

    public TableController(DiningTableService tableService, WebSocketService webSocketService) {
        this.tableService = tableService;
        this.webSocketService = webSocketService;
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
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody TableStatus status) {
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
    public ResponseEntity<?> addTable(
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
    public ResponseEntity<?> updateTable(
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
        System.err.println(message + " - Error: " + e.getMessage());
    }
}
