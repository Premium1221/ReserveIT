package com.reserveit.controller;

import com.reserveit.dto.ReservationDto;
import com.reserveit.enums.ReservationStatus;
import com.reserveit.logic.impl.WebSocketService;
import com.reserveit.logic.interfaces.ReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/staff/reservations")
@PreAuthorize("hasRole('STAFF') or hasRole('MANAGER')")
public class StaffReservationController {
    private final ReservationService reservationService;
    private final WebSocketService webSocketService;

    public StaffReservationController(ReservationService reservationService,
                                      WebSocketService webSocketService) {
        this.reservationService = reservationService;
        this.webSocketService = webSocketService;
    }

    @PostMapping("/{id}/check-in")
    public ResponseEntity<?> checkInReservation(@PathVariable Long id) {
        try {
            reservationService.checkInReservation(id);
            // Notify clients through WebSocket about table status change
            webSocketService.notifyTableUpdate(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/check-out")
    public ResponseEntity<?> checkOutReservation(@PathVariable Long id) {
        try {
            reservationService.checkOutReservation(id);
            // Notify clients through WebSocket about table status change
            webSocketService.notifyTableUpdate(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<ReservationDto>> getUpcomingReservations() {
        LocalDateTime now = LocalDateTime.now();
        return ResponseEntity.ok(reservationService.getReservationsByTimeRange(now, now.plusHours(24)));
    }
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ReservationDto>> getReservationsByStatus(@PathVariable String status) {
        try {
            ReservationStatus reservationStatus = ReservationStatus.valueOf(status.toUpperCase());
            List<ReservationDto> reservations = reservationService.findByStatus(reservationStatus);
            return ResponseEntity.ok(reservations);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<ReservationDto>> getReservationsByCompany(@PathVariable UUID companyId) {
        try {
            List<ReservationDto> reservations = reservationService.getReservationsByCompany(companyId);
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
