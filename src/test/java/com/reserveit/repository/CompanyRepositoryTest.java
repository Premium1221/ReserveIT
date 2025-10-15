package com.reserveit.repository;

import com.reserveit.model.Category;
import com.reserveit.model.Company;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@AutoConfigureTestDatabase(replace = Replace.ANY)
class CompanyRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CompanyRepository companyRepository;

    @Test
    void findByMinimumRating_returns_companies_at_or_above_threshold() {
        Company c1 = company("Alpha Ristorante", "alpha@example.com", 4.5f);
        Company c2 = company("Bravo Bistro", "bravo@example.com", 3.0f);
        entityManager.persist(c1);
        entityManager.persist(c2);
        entityManager.flush();

        List<Company> result = companyRepository.findByMinimumRating(4.0f);

        assertEquals(1, result.size());
        assertEquals("Alpha Ristorante", result.get(0).getName());
    }

    @Test
    void findByCategoriesContaining_returns_companies_with_category() {
        Category italian = new Category("Italian");
        entityManager.persist(italian);

        Company c1 = company("Alpha Ristorante", "alpha2@example.com", 4.2f);
        c1.getCategories().add(italian);
        italian.getCompanies().add(c1);

        Company c2 = company("Bravo Bistro", "bravo2@example.com", 4.0f);

        entityManager.persist(c1);
        entityManager.persist(c2);
        entityManager.flush();

        List<Company> result = companyRepository.findByCategoriesContaining(italian);

        assertEquals(1, result.size());
        assertEquals("Alpha Ristorante", result.get(0).getName());
    }

    private static Company company(String name, String email, float rating) {
        Company c = new Company();
        c.setName(name);
        c.setEmail(email);
        c.setRating(rating);
        return c;
    }
}
