package com.reserveit.model;

import com.reserveit.enums.TableShape;
import com.reserveit.enums.TableStatus;
import com.reserveit.enums.ReservationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dining_tables")
@Getter
@Setter
@NoArgsConstructor
public class DiningTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(value = 1, message = "Capacity must be at least 1")
    @NotNull(message = "Capacity is required")
    @Column(nullable = false)
    private int capacity;

    @Column(name = "x_position", nullable = false)
    private int xPosition = 0;

    @Column(name = "y_position", nullable = false)
    private int yPosition = 0;

    @Column(name = "rotation")
    private int rotation = 0;

    @NotNull(message = "Table number is required")
    @Column(name = "table_number", nullable = false)
    private String tableNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @OneToMany(mappedBy = "diningTable", cascade = CascadeType.ALL)
    private List<Reservation> reservations = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TableStatus status = TableStatus.AVAILABLE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TableShape shape = TableShape.SQUARE;

    @Column(name = "floor_level")
    private int floorLevel = 1;

    @Column(name = "is_outdoor")
    private boolean outdoor = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "configuration_id")
    private TableConfiguration configuration;

    // Constructor with required fields
    public DiningTable(int capacity, String tableNumber, Company company) {
        this.capacity = capacity;
        this.tableNumber = tableNumber;
        this.company = company;
    }

    @PrePersist
    @PreUpdate
    private void validatePosition() {
        if (xPosition < 0 || yPosition < 0) {
            throw new IllegalArgumentException("Table position cannot be negative");
        }
    }

    // Position update methods
    public void updatePosition(int newX, int newY) {
        if (newX < 0 || newY < 0) {
            throw new IllegalArgumentException("Position values cannot be negative");
        }
        this.xPosition = newX;
        this.yPosition = newY;
    }

    // Business logic methods
    public void addReservation(Reservation reservation) {
        reservations.add(reservation);
        reservation.setDiningTable(this);
        this.status = TableStatus.RESERVED;
    }

    public void removeReservation(Reservation reservation) {
        reservations.remove(reservation);
        reservation.setDiningTable(null);
        updateStatusBasedOnReservations();
    }

    private void updateStatusBasedOnReservations() {
        this.status = reservations.isEmpty() ? TableStatus.AVAILABLE : TableStatus.RESERVED;
    }

    public boolean isAvailableForDateTime(LocalDateTime dateTime) {
        return status == TableStatus.AVAILABLE &&
                reservations.stream()
                        .noneMatch(reservation ->
                                reservation.getReservationDate().equals(dateTime) &&
                                        reservation.getStatus() != ReservationStatus.CANCELLED
                        );
    }

    public boolean isAvailable() {
        return status == TableStatus.AVAILABLE;
    }

    public void setAvailable(boolean available) {
        this.status = available ? TableStatus.AVAILABLE : TableStatus.OCCUPIED;
    }

    public void setConfiguration(TableConfiguration configuration) {
        if (this.configuration != null && this.configuration.getTables().contains(this)) {
            this.configuration.getTables().remove(this);
        }
        this.configuration = configuration;
        if (configuration != null && !configuration.getTables().contains(this)) {
            configuration.getTables().add(this);
        }
    }
}