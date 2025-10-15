package com.reserveit.database.impl;

import com.reserveit.model.Category;
import com.reserveit.model.Company;
import com.reserveit.repository.CompanyRepository;
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
class CompanyDatabaseImplTest {

    @Mock
    private CompanyRepository repository;

    @InjectMocks
    private CompanyDatabaseImpl db;

    @Test
    void delegates_all_methods(){
        Company c = new Company();
        c.setId(UUID.randomUUID());
        Category cat = new Category("Italian");

        when(repository.findByCategoriesContaining(cat)).thenReturn(List.of(c));
        when(repository.findByMinimumRating(4.0f)).thenReturn(List.of(c));
        when(repository.save(any(Company.class))).thenReturn(c);
        when(repository.findById(c.getId())).thenReturn(Optional.of(c));
        when(repository.findAll()).thenReturn(List.of(c));

        assertEquals(1, db.findByCategoriesContaining(cat).size());
        assertEquals(1, db.findByMinimumRating(4.0f).size());
        assertNotNull(db.save(c));
        assertTrue(db.findById(c.getId()).isPresent());
        assertEquals(1, db.findAll().size());

        db.deleteById(c.getId());
        verify(repository).deleteById(c.getId());
    }
}

