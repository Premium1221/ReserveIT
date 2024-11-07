package com.reserveit.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @NotBlank(message = "Message is required")
    @Column(nullable = false, length = 500)
    private String message;

    @NotNull(message = "Notification type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private boolean read = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    public enum NotificationType {
        RESERVATION_CONFIRMATION("Reservation Confirmed"),
        RESERVATION_REMINDER("Reservation Reminder"),
        RESERVATION_CANCELLED("Reservation Cancelled"),
        RESERVATION_MODIFIED("Reservation Modified"),
        TABLE_ASSIGNED("Table Assigned"),
        REVIEW_REQUEST("Review Request");

        private final String displayName;

        NotificationType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Helper methods
    public void markAsRead() {
        if (!this.read) {
            this.read = true;
            this.readAt = LocalDateTime.now();
        }
    }

    public boolean isNew() {
        return !read && readAt == null;
    }

    // Static factory methods
    public static Notification createReservationConfirmation(User user, Reservation reservation) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setReservation(reservation);
        notification.setType(NotificationType.RESERVATION_CONFIRMATION);
        notification.setMessage("Your reservation has been confirmed for " +
                reservation.getReservationDate().toString());
        return notification;
    }

    public static Notification createReservationReminder(User user, Reservation reservation) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setReservation(reservation);
        notification.setType(NotificationType.RESERVATION_REMINDER);
        notification.setMessage("Reminder: Your reservation is upcoming at " +
                reservation.getReservationDate().toString());
        return notification;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
        if (read && readAt == null) {
            this.readAt = LocalDateTime.now();
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }
}