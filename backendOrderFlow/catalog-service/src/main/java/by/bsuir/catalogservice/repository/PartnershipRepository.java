package by.bsuir.catalogservice.repository;

import by.bsuir.catalogservice.entity.Partnership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartnershipRepository extends JpaRepository<Partnership, Long> {

	List<Partnership> findBySupplierId(Long supplierId);

	List<Partnership> findByCustomerId(Long customerId);

	List<Partnership> findBySupplierIdAndStatus(Long supplierId, Partnership.PartnershipStatus status);

	List<Partnership> findByCustomerIdAndStatus(Long customerId, Partnership.PartnershipStatus status);

	Optional<Partnership> findBySupplierIdAndCustomerId(Long supplierId, Long customerId);

	boolean existsBySupplierIdAndCustomerId(Long supplierId, Long customerId);





	@Query("SELECT p.supplierId FROM Partnership p WHERE p.customerId = :customerId AND p.status = 'ACTIVE'")
	List<Long> findActiveSupplierIdsByCustomerId(@Param("customerId") Long customerId);




	@Query("SELECT p.customerId FROM Partnership p WHERE p.supplierId = :supplierId AND p.status = 'ACTIVE'")
	List<Long> findActiveCustomerIdsBySupplierId(@Param("supplierId") Long supplierId);

	long countBySupplierIdAndStatus(Long supplierId, Partnership.PartnershipStatus status);

	long countByCustomerIdAndStatus(Long customerId, Partnership.PartnershipStatus status);
}
