package com.reserveit.controller;

import com.reserveit.dto.ReservationDto;
import com.reserveit.enums.ReservationStatus;
import com.reserveit.logic.interfaces.ReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = {"http://localhost:5200"}, allowCredentials = "true")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<?> createReservation(@RequestBody ReservationDto reservationDto) {
        try {
            if (!reservationService.isTimeSlotAvailable(
                    reservationDto.getCompanyId(),
                    reservationDto.getTableId(),
                    reservationDto.getReservationDate(),
                    reservationDto.getDuration())) {
                return ResponseEntity.badRequest().body("Selected time slot is not available");
            }

            ReservationDto savedReservation = reservationService.createReservation(reservationDto);
            return ResponseEntity.status(201).body(savedReservation);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<ReservationDto>> getAllReservations() {
        List<ReservationDto> reservations = reservationService.getAllReservations();
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReservationById(@PathVariable Long id) {
        try {
            ReservationDto reservation = reservationService.getReservationById(id);
            return ResponseEntity.ok(reservation);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateReservation(@PathVariable Long id, @RequestBody ReservationDto reservationDto) {
        try {
            ReservationDto updatedReservation = reservationService.updateReservation(id, reservationDto);
            return ResponseEntity.ok(updatedReservation);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelReservation(@PathVariable Long id) {
        try {
            reservationService.cancelReservation(id);
            return ResponseEntity.ok().body("Reservation cancelled successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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

    @GetMapping("/check-availability")
    public ResponseEntity<?> checkTimeSlotAvailability(
            @RequestParam UUID companyId,
            @RequestParam Long tableId,
            @RequestParam String dateTime,
            @RequestParam(defaultValue = "120") Integer duration) {
        try {
            boolean isAvailable = reservationService.isTimeSlotAvailable(
                    companyId, tableId, dateTime, duration);
            return ResponseEntity.ok(isAvailable);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}