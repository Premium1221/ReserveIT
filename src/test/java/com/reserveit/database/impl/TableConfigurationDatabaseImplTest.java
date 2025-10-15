package com.reserveit.database.impl;

import com.reserveit.model.TableConfiguration;
import com.reserveit.repository.TableConfigurationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TableConfigurationDatabaseImplTest {

    @Mock
    private TableConfigurationRepository repository;

    @InjectMocks
    private TableConfigurationDatabaseImpl db;

    private UUID companyId;

    @BeforeEach
    void init(){
        companyId = UUID.randomUUID();
    }

    @Test
    void delegates_findByCompanyId_and_findById_and_save_and_delete(){
        TableConfiguration cfg = new TableConfiguration();
        cfg.setId(1L);

        when(repository.findByCompanyId(companyId)).thenReturn(List.of(cfg));
        when(repository.findById(1L)).thenReturn(Optional.of(cfg));
        when(repository.findByCompanyIdAndActive(companyId, true)).thenReturn(Optional.of(cfg));
        when(repository.findByCompanyIdAndName(companyId, "Summer")).thenReturn(Optional.of(cfg));
        when(repository.existsByCompanyIdAndNameAndIdNot(companyId, "Summer", 2L)).thenReturn(true);
        when(repository.save(any(TableConfiguration.class))).thenReturn(cfg);

        assertEquals(1, db.findByCompanyId(companyId).size());
        assertTrue(db.findById(1L).isPresent());
        assertTrue(db.findByCompanyIdAndActive(companyId, true).isPresent());
        assertTrue(db.findByCompanyIdAndName(companyId, "Summer").isPresent());
        assertTrue(db.existsByCompanyIdAndNameAndIdNot(companyId, "Summer", 2L));
        assertNotNull(db.save(cfg));

        db.deleteById(1L);
        verify(repository).deleteById(1L);
    }
}

