package com.reserveit.model;

import com.reserveit.enums.UserRole;
import jakarta.persistence.*;

@Entity
@Table(name = "staff")
public class Staff extends User {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    public Staff() {
        super();
    }
    public Staff(UserRole role) {
        super();
        if (role != UserRole.MANAGER && role != UserRole.STAFF) {
            throw new IllegalArgumentException(
                    "Invalid role for Staff entity. Must be either MANAGER or STAFF"
            );
        }
        setUserRole(role);
    }


    @Override
    public void setUserRole(UserRole userRole) {
        if (userRole != UserRole.MANAGER && userRole != UserRole.STAFF) {
            throw new IllegalArgumentException(
                    "Invalid role for Staff entity. Must be either MANAGER or STAFF"
            );
        }
        super.setUserRole(userRole);
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        if (company == null) {
            throw new IllegalArgumentException("Company cannot be null for Staff entity");
        }
        this.company = company;
    }
}
