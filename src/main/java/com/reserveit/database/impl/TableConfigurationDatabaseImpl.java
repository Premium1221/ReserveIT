package com.reserveit.database.impl;

import com.reserveit.database.interfaces.TableConfigurationDatabase;
import com.reserveit.model.TableConfiguration;
import com.reserveit.repository.TableConfigurationRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Transactional
public class TableConfigurationDatabaseImpl implements TableConfigurationDatabase {
    private final TableConfigurationRepository configurationRepository;

    public TableConfigurationDatabaseImpl(TableConfigurationRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
    }

    @Override
    public List<TableConfiguration> findByCompanyId(UUID companyId) {
        return configurationRepository.findByCompanyId(companyId);
    }

    @Override
    public Optional<TableConfiguration> findById(Long id) {
        return configurationRepository.findById(id);
    }

    @Override
    public Optional<TableConfiguration> findByCompanyIdAndActive(UUID companyId, boolean active) {
        return configurationRepository.findByCompanyIdAndActive(companyId, active);
    }

    @Override
    public Optional<TableConfiguration> findByCompanyIdAndName(UUID companyId, String name) {
        return configurationRepository.findByCompanyIdAndName(companyId, name);
    }

    @Override
    public boolean existsByCompanyIdAndNameAndIdNot(UUID companyId, String name, Long excludeId) {
        return configurationRepository.existsByCompanyIdAndNameAndIdNot(companyId, name, excludeId);
    }

    @Override
    @Transactional
    public TableConfiguration save(TableConfiguration configuration) {
        return configurationRepository.save(configuration);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        configurationRepository.deleteById(id);
    }
}