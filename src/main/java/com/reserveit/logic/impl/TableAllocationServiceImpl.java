package com.reserveit.logic.impl;

import com.reserveit.logic.interfaces.TableAllocationService;
import com.reserveit.model.DiningTable;
import com.reserveit.enums.TableStatus;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;

@Service
public class TableAllocationServiceImpl implements TableAllocationService {
    // Constants for table allocation
    private static final int MAX_ACCEPTABLE_OVERFLOW = 2;  // Maximum extra seats allowed
    private static final int PEAK_HOUR_PENALTY = 20;      // Extra penalty during peak hours

    @Override
    public Optional<DiningTable> findOptimalTable(List<DiningTable> availableTables, int partySize) {
        return availableTables.stream()
                .filter(table -> isTableSuitable(table, partySize))
                .min((table1, table2) ->
                        Integer.compare(
                                calculateEfficiencyScore(table1, partySize),
                                calculateEfficiencyScore(table2, partySize)
                        )
                );
    }

    @Override
    public boolean isTableSuitable(DiningTable table, int partySize) {
        // Check if table has enough capacity but not too much excess
        boolean hasAppropriateCapacity = table.getCapacity() >= partySize &&
                table.getCapacity() <= partySize + MAX_ACCEPTABLE_OVERFLOW;

        // Check if table is available and operational
        boolean isAvailable = table.getStatus() == TableStatus.AVAILABLE;

        return hasAppropriateCapacity && isAvailable;
    }

    // Calculate efficiency score - lower score is better
    private int calculateEfficiencyScore(DiningTable table, int partySize) {
        // Base score is the difference between table capacity and party size
        int baseScore = Math.abs(table.getCapacity() - partySize) * 10;

        // Add extra penalty for oversized tables during peak hours
        if (isPeakHour() && table.getCapacity() > partySize) {
            baseScore += PEAK_HOUR_PENALTY * (table.getCapacity() - partySize);
        }

        return baseScore;
    }

    // Check if current time is during peak hours
    private boolean isPeakHour() {
        LocalTime now = LocalTime.now();
        int hour = now.getHour();

        // Lunch rush: 11 AM - 2 PM
        boolean isLunchRush = hour >= 11 && hour <= 14;

        // Dinner rush: 6 PM - 9 PM
        boolean isDinnerRush = hour >= 18 && hour <= 21;

        return isLunchRush || isDinnerRush;
    }
}


    /*
    @Override
    public List<TableSuggestion> getAlternativeSuggestions(
            List<DiningTable> availableTables,
            int partySize,
            DiningTable selectedTable) {

        List<TableSuggestion> suggestions = new ArrayList<>();

        // If current table is too big (inefficient)
        if (selectedTable != null &&
                selectedTable.getCapacity() > partySize + MAX_ACCEPTABLE_OVERFLOW) {

            // Look for better fitting tables
            availableTables.stream()
                    .filter(table -> table.getStatus() == TableStatus.AVAILABLE)
                    .filter(table -> table.getId() != selectedTable.getId())
                    .filter(table -> isTableSuitable(table, partySize))
                    .min(Comparator.comparingInt(table ->
                            Math.abs(table.getCapacity() - partySize)))
                    .ifPresent(table -> suggestions.add(
                            createSuggestion(table, partySize, true)
                    ));
        }

        // During peak hours, suggest more efficient table arrangements
        if (isPeakHour() && selectedTable != null &&
                selectedTable.getCapacity() >= 6 && partySize <= 4) {

            availableTables.stream()
                    .filter(table -> table.getStatus() == TableStatus.AVAILABLE)
                    .filter(table -> table.getCapacity() >= partySize)
                    .filter(table -> table.getCapacity() < selectedTable.getCapacity())
                    .min(Comparator.comparingInt(DiningTable::getCapacity))
                    .ifPresent(table -> suggestions.add(
                            createSuggestion(table, partySize, false)
                    ));
        }

        return suggestions;
    }

    // Helper method to create a suggestion with appropriate message
    private TableSuggestion createSuggestion(DiningTable table, int partySize, boolean highPriority) {
        String message = getMessage(table, partySize);
        int efficiencyScore = calculateEfficiencyScore(table, partySize);
        return new TableSuggestion(null, message, highPriority, efficiencyScore);
    }

    // Generate appropriate message for the suggestion
    private String getMessage(DiningTable table, int partySize) {
        if (table.getCapacity() == partySize) {
            return "This table perfectly fits your party size";
        } else if (table.getCapacity() > partySize) {
            return String.format(
                    "This table can comfortably accommodate your party of %d",
                    partySize
            );
        } else {
            return "This table might be a bit cozy but can accommodate your party";
        }
    }
     */
