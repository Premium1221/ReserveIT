package com.reserveit.model;

import com.reserveit.enums.TableStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.reserveit.enums.ReservationStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@AllArgsConstructor
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "Reservation date is required")
    @Column(name = "reservation_date", nullable = false)
    private LocalDateTime reservationDate;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;


    @Column(name = "duration", nullable = false)
    private Integer duration = 180;

    @Min(value = 1, message = "Number of people must be at least 1")
    @Column(name = "number_of_people", nullable = false)
    private int numberOfPeople;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dining_table_id")
    private DiningTable diningTable;

    @Column(name = "special_requests")
    @Size(max = 500, message = "Special requests cannot exceed 500 characters")
    private String specialRequests;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status = ReservationStatus.CONFIRMED;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    public Reservation() {

    }


    // Helper methods
    public void checkIn() {
        if (this.status != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Cannot check in: reservation is not confirmed");
        }
        this.status = ReservationStatus.ARRIVED;
        this.checkInTime = LocalDateTime.now();
        if (this.diningTable != null) {
            this.diningTable.setStatus(TableStatus.OCCUPIED);
        }
    }

    public void checkOut() {
        if (this.status != ReservationStatus.ARRIVED) {
            throw new IllegalStateException("Cannot check out: reservation is not arrived");
        }
        this.status = ReservationStatus.COMPLETED;
        this.checkOutTime = LocalDateTime.now();
        if (this.diningTable != null) {
            this.diningTable.setStatus(TableStatus.AVAILABLE);
        }
    }

    public void extend(int additionalMinutes) {
        if (!canExtend()) {
            throw new IllegalStateException("Cannot extend: reservation is not active");
        }
        this.duration += additionalMinutes;
        this.endTime = this.endTime.plusMinutes(additionalMinutes);
    }

    public boolean canExtend() {
        return this.status == ReservationStatus.ARRIVED;
    }
}