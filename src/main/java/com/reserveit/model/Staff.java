package com.reserveit.model;

import com.reserveit.enums.UserRole;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import javax.management.relation.Role;

public class Staff extends User{
    @ManyToOne
    @JoinColumn(name="company_id")
    private Company company;
}
