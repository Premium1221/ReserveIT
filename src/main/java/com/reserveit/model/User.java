package com.reserveit.model;

import com.reserveit.enums.UserRole;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.persistence.Table;

import java.util.UUID;
@Entity
@Table(name = "users")

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String hashedPassword;
    private String phoneNumber;

    private String company;
    @Enumerated(EnumType.STRING)
    private UserRole userRole;


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String password) {
        this.hashedPassword = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public UserRole getRole() {
        return userRole;
    }

    public void setRole(UserRole userRole) {
        this.userRole = userRole;
    }
}
