package com.reserveit.database.interfaces;

import com.reserveit.model.TableConfiguration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TableConfigurationDatabase {
    List<TableConfiguration> findByCompanyId(UUID companyId);
    Optional<TableConfiguration> findById(Long id);
    Optional<TableConfiguration> findByCompanyIdAndActive(UUID companyId, boolean active);
    Optional<TableConfiguration> findByCompanyIdAndName(UUID companyId, String name);
    boolean existsByCompanyIdAndNameAndIdNot(UUID companyId, String name, Long excludeId);
    TableConfiguration save(TableConfiguration configuration);
    void deleteById(Long id);
}