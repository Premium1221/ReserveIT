package com.reserveit.enums;

public enum UserRole {
    CUSTOMER,    // Basic user who can make reservations
    STAFF,       // Restaurant staff who can manage reservations
    MANAGER,     // Restaurant manager with additional privileges
    ADMIN;       // System administrator with full access

    // Helper methods for common role checks
    public boolean hasAdminAccess() {
        return this == ADMIN;
    }

    public boolean hasManagementAccess() {
        return this == ADMIN || this == MANAGER;
    }

    public boolean canManageReservations() {
        return this != CUSTOMER;
    }

    // If you need specific role checks
    public boolean isCustomer() {
        return this == CUSTOMER;
    }

    public boolean isStaff() {
        return this == STAFF;
    }
}