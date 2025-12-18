package by.bsuir.authservice.repository;

import by.bsuir.authservice.entity.SupplierSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierSettingsRepository extends JpaRepository<SupplierSettings, Long> {
}
