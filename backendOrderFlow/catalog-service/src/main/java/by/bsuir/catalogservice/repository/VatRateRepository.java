package by.bsuir.catalogservice.repository;

import by.bsuir.catalogservice.entity.VatRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface VatRateRepository extends JpaRepository<VatRate, Long> {
	Optional<VatRate> findByRatePercentage(BigDecimal ratePercentage);
	boolean existsByDescription(String description);
}
