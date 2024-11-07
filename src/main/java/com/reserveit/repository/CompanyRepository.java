package com.reserveit.repository;

import com.reserveit.model.Company;
import com.reserveit.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {
    List<Company> findByCategoriesContaining(Category category);

    @Query("SELECT c FROM Company c WHERE c.rating >= :minRating")
    List<Company> findByMinimumRating(@Param("minRating") Float minRating);
}