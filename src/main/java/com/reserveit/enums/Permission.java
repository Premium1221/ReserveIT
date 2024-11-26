package com.reserveit.enums;

public enum Permission {
    // Reservation permissions
    RESERVATION_READ("reservation:read"),
    RESERVATION_CREATE("reservation:create"),
    RESERVATION_UPDATE("reservation:update"),
    RESERVATION_UPDATE_OWN("reservation:update_own"),
    RESERVATION_DELETE("reservation:delete"),
    RESERVATION_CANCEL_OWN("reservation:cancel_own"),

    // Staff permissions
    STAFF_MANAGE("staff:manage"),
    CUSTOMER_SERVICE("customer:service"),

    // Table permissions
    TABLE_MANAGE("table:manage"),

    // Company permissions
    COMPANY_VIEW("company:view"),
    COMPANY_UPDATE("company:update"),
    COMPANY_MANAGE("company:manage"),

    // User management
    USER_MANAGE("user:manage"),

    // Reporting
    REPORT_VIEW("report:view"),

    // System level
    ADMIN_ACCESS("admin:access"),
    SYSTEM_MANAGE("system:manage");

    private final String permission;

    Permission(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}