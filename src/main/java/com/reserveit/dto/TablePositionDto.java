package com.reserveit.dto;

import com.reserveit.enums.TableShape;
import com.reserveit.enums.TableStatus;
import com.reserveit.model.DiningTable;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class TablePositionDto {
    private Long id;

    @NotNull(message = "Table number is required")
    private String tableNumber;

    @Min(value = 0, message = "X position cannot be negative")
    @JsonProperty("xPosition")
    private int xPosition;

    @Min(value = 0, message = "Y position cannot be negative")
    @JsonProperty("yPosition")
    private int yPosition;

    @NotNull(message = "Shape is required")
    private TableShape shape;

    private int rotation;

    @NotNull(message = "Status is required")
    private TableStatus status;

    @NotNull(message = "Capacity must be at least 1")
    private int capacity;

    @NotNull(message = "Company ID cannot be null")
    private UUID companyId;

    private boolean isOutdoor;
    private int floorLevel;

    // Copy constructor
    public TablePositionDto(TablePositionDto other) {
        this.id = other.id;
        this.tableNumber = other.tableNumber;
        this.xPosition = other.xPosition;
        this.yPosition = other.yPosition;
        this.shape = other.shape;
        this.rotation = other.rotation;
        this.status = other.status;
        this.capacity = other.capacity;
        this.companyId = other.companyId;
        this.isOutdoor = other.isOutdoor;
        this.floorLevel = other.floorLevel;
    }

    public static TablePositionDto fromDiningTable(DiningTable table) {
        TablePositionDto dto = new TablePositionDto();
        dto.setId(table.getId());
        dto.setTableNumber(table.getTableNumber());
        dto.setXPosition(table.getXPosition());
        dto.setYPosition(table.getYPosition());
        dto.setShape(table.getShape());
        dto.setStatus(table.getStatus());
        dto.setCapacity(table.getCapacity());
        dto.setCompanyId(table.getCompany().getId());
        dto.setOutdoor(table.isOutdoor());
        dto.setFloorLevel(table.getFloorLevel());
        dto.setRotation(table.getRotation());
        return dto;
    }

    @Override
    public String toString() {
        return String.format(
                "TablePositionDto{id=%d, position=(%d,%d), shape=%s, status=%s}",
                id, xPosition, yPosition, shape, status
        );
    }

    public void validate() {
        if (id == null) {
            throw new IllegalArgumentException("Table ID cannot be null");
        }
        if (xPosition < 0 || yPosition < 0) {
            throw new IllegalArgumentException(
                    String.format("Invalid position values: (%d,%d)", xPosition, yPosition)
            );
        }
        if (companyId == null) {
            throw new IllegalArgumentException("Company ID cannot be null");
        }
    }

    public void updatePosition(int newX, int newY) {
        if (newX < 0 || newY < 0) {
            throw new IllegalArgumentException(
                    String.format("Invalid position values: (%d,%d)", newX, newY)
            );
        }
        this.xPosition = newX;
        this.yPosition = newY;
    }
}