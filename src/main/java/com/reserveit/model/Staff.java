package com.reserveit.model;

import com.reserveit.enums.UserRole;
import jakarta.persistence.*;

@Entity
@Table(name = "staff")
public class Staff extends User {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // Constructor that sets appropriate role
    public Staff() {
        super();
        // Staff can be either MANAGER or STAFF based on their role
        this.setUserRole(UserRole.STAFF);
    }

    // For creating a manager
    public static Staff createManager() {
        Staff manager = new Staff();
        manager.setUserRole(UserRole.MANAGER);
        return manager;
    }

    // Helper methods
    public boolean canManageStaff() {
        return getUserRole() == UserRole.MANAGER || getUserRole() == UserRole.ADMIN;
    }

    public boolean canManageTables() {
        return getUserRole() == UserRole.MANAGER || getUserRole() == UserRole.STAFF;
    }

    public boolean canViewReports() {
        return getUserRole() == UserRole.MANAGER || getUserRole() == UserRole.ADMIN;
    }

    // Getters and Setters
    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}