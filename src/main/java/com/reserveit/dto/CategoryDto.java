package com.reserveit.dto;

public class CategoryDto {
    private Long id;
    private String name;
    private int companiesCount;

    // Constructors
    public CategoryDto() {
    }

    public CategoryDto(String name) {
        this.name = name;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCompaniesCount() {
        return companiesCount;
    }

    public void setCompaniesCount(int companiesCount) {
        this.companiesCount = companiesCount;
    }
}