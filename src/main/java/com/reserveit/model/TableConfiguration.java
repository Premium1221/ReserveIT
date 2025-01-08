package com.reserveit.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "table_configurations")
@Getter
@Setter
@NoArgsConstructor
public class TableConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Configuration name is required")
    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @OneToMany(mappedBy = "configuration", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiningTable> tables = new ArrayList<>();

    @Column(nullable = false)
    private boolean active = false;

    // Business logic methods
    public void addTable(DiningTable table) {
        tables.add(table);
        table.setConfiguration(this);
    }

    public void removeTable(DiningTable table) {
        if (tables.remove(table)) {
            table.setConfiguration(null);
        }
    }

    public void clearTables() {
        tables.forEach(table -> table.setConfiguration(null));
        tables.clear();
    }

    // Configuration management
    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    // Helper method to check if configuration is empty
    public boolean isEmpty() {
        return tables.isEmpty();
    }

    @PreRemove
    private void preRemove() {
        clearTables();
    }
}