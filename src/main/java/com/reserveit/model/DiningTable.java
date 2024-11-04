package com.reserveit.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dining_tables")
public class DiningTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(value = 1, message = "Capacity must be at least 1")
    @NotNull(message = "Capacity is required")
    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private boolean available = true;

    @Min(value = 0, message = "X position cannot be negative")
    @Column(name = "x_position")
    private int xPosition;

    @Min(value = 0, message = "Y position cannot be negative")
    @Column(name = "y_position")
    private int yPosition;

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

    public enum TableStatus {
        AVAILABLE,
        OCCUPIED,
        RESERVED,
        OUT_OF_SERVICE,
        CLEANING
    }

    // Constructors
    public DiningTable() {
    }

    public DiningTable(int capacity, String tableNumber, Company company) {
        this.capacity = capacity;
        this.tableNumber = tableNumber;
        this.company = company;
    }

    // Helper methods
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
        return reservations.stream()
                .noneMatch(reservation ->
                        reservation.getReservationDate().equals(dateTime) &&
                                reservation.getStatus() != Reservation.ReservationStatus.CANCELLED
                );
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public boolean isAvailable() {
        return status == TableStatus.AVAILABLE;
    }

    public void setAvailable(boolean available) {
        this.status = available ? TableStatus.AVAILABLE : TableStatus.OCCUPIED;
    }

    public int getXPosition() {
        return xPosition;
    }

    public void setXPosition(int xPosition) {
        this.xPosition = xPosition;
    }

    public int getYPosition() {
        return yPosition;
    }

    public void setYPosition(int yPosition) {
        this.yPosition = yPosition;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    public TableStatus getStatus() {
        return status;
    }

    public void setStatus(TableStatus status) {
        this.status = status;
    }

    public int getFloorLevel() {
        return floorLevel;
    }

    public void setFloorLevel(int floorLevel) {
        this.floorLevel = floorLevel;
    }

    public boolean isOutdoor() {
        return outdoor;
    }

    public void setOutdoor(boolean outdoor) {
        this.outdoor = outdoor;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}