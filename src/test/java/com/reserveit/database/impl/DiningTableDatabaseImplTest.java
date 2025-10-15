package com.reserveit.database.impl;

import com.reserveit.enums.TableStatus;
import com.reserveit.model.DiningTable;
import com.reserveit.repository.DiningTableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiningTableDatabaseImplTest {

    @Mock
    private DiningTableRepository repository;

    @InjectMocks
    private DiningTableDatabaseImpl db;

    private UUID companyId;
    private DiningTable table;

    @BeforeEach
    void setup(){
        companyId = UUID.randomUUID();
        table = new DiningTable();
        table.setId(10L);
        table.setTableNumber("T1");
    }

    @Test
    void delegates_find_and_update_and_save_and_delete(){
        when(repository.findByCompanyId(companyId)).thenReturn(List.of(table));
        when(repository.findById(10L)).thenReturn(Optional.of(table));
        when(repository.findByIdAndCompanyId(10L, companyId)).thenReturn(Optional.of(table));
        when(repository.findByCompanyIdAndTableNumber(companyId, "T1")).thenReturn(Optional.of(table));
        when(repository.findAll()).thenReturn(List.of(table));
        when(repository.findByIdsAndCompanyId(List.of(10L), companyId)).thenReturn(List.of(table));
        when(repository.findAvailableTables(companyId, 2)).thenReturn(List.of(table));
        when(repository.findAvailableTable(eq(companyId), anyInt(), any(LocalDateTime.class))).thenReturn(Optional.of(table));
        when(repository.existsByPosition(companyId, 1, 2)).thenReturn(true);
        when(repository.existsByCompanyIdAndXPositionAndYPositionAndIdNot(companyId, 1, 2, 9L)).thenReturn(false);
        when(repository.updateTablePosition(10L, companyId, 3, 4)).thenReturn(1);
        when(repository.updateStatus(10L, TableStatus.RESERVED)).thenReturn(1);
        when(repository.save(any(DiningTable.class))).thenReturn(table);
        when(repository.saveAll(anyList())).thenReturn(List.of(table));

        assertEquals(1, db.findByCompanyId(companyId).size());
        assertTrue(db.findById(10L).isPresent());
        assertTrue(db.findByIdAndCompanyId(10L, companyId).isPresent());
        assertTrue(db.findByCompanyIdAndTableNumber(companyId, "T1").isPresent());
        assertEquals(1, db.findAll().size());
        assertEquals(1, db.findByIdsAndCompanyId(List.of(10L), companyId).size());
        assertEquals(1, db.findAvailableTables(companyId, 2).size());
        assertTrue(db.findAvailableTable(companyId, 2, LocalDateTime.now()).isPresent());
        assertTrue(db.isPositionOccupied(companyId, 1, 2));
        assertFalse(db.isPositionOccupiedExcludingTable(companyId, 1, 2, 9L));
        assertEquals(1, db.updateTablePosition(10L, companyId, 3, 4));
        assertEquals(1, db.updateTableStatus(10L, TableStatus.RESERVED));
        assertNotNull(db.save(table));
        assertEquals(1, db.saveAll(List.of(table)).size());
        db.deleteById(10L);
        verify(repository).deleteById(10L);
    }
}

