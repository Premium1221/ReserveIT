package com.reserveit.repository;

import com.reserveit.model.TableConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TableConfigurationRepository extends JpaRepository<TableConfiguration, Long> {
    List<TableConfiguration> findByCompanyId(UUID companyId);

    Optional<TableConfiguration> findByCompanyIdAndActive(UUID companyId, boolean active);

    @Query("SELECT tc FROM TableConfiguration tc WHERE tc.company.id = :companyId AND tc.name = :name")
    Optional<TableConfiguration> findByCompanyIdAndName(
            @Param("companyId") UUID companyId,
            @Param("name") String name
    );

    @Query("SELECT COUNT(tc) > 0 FROM TableConfiguration tc " +
            "WHERE tc.company.id = :companyId AND tc.name = :name AND tc.id <> :excludeId")
    boolean existsByCompanyIdAndNameAndIdNot(
            @Param("companyId") UUID companyId,
            @Param("name") String name,
            @Param("excludeId") Long excludeId
    );
}