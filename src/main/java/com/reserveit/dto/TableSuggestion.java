package com.reserveit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TableSuggestion {
    private TablePositionDto table;
    private String message;
    private boolean highPriority;
    private int efficiencyScore;
}