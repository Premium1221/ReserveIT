package com.reserveit.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.reserveit.model.Category;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyDto {
    private UUID id;
    private String name;
    private String address;
    private String phone;
    private String email;

    @JsonIgnoreProperties({"companies"})
    private Set<Category> categories = new HashSet<>();

    private Float rating;
    private String pictureUrl;
    // Constructors
    public CompanyDto() {
        // For Tests
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }
}