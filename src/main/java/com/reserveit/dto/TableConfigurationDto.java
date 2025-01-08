package com.reserveit.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class TableConfigurationDto {
    private Long id;

    @NotBlank(message = "Configuration name is required")
    private String name;

    @NotNull(message = "Company ID is required")
    private UUID companyId;

    private boolean active;

    private List<TablePositionDto> tables;

    // Helper method for checking if configuration is empty
    public boolean isEmpty() {
        return tables == null || tables.isEmpty();
    }

    // Method to compute configuration status
    public String getStatus() {
        if (active) {
            return "Active";
        }
        return isEmpty() ? "Empty" : "Inactive";
    }
}