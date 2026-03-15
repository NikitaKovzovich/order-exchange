package by.bsuir.authservice.repository;

import by.bsuir.authservice.entity.User;
import by.bsuir.authservice.entity.VerificationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, Long> {
	Optional<VerificationRequest> findByUserId(Long userId);

	List<VerificationRequest> findByStatus(VerificationRequest.VerificationStatus status);

	long countByStatus(VerificationRequest.VerificationStatus status);
	Page<VerificationRequest> findByStatus(VerificationRequest.VerificationStatus status, Pageable pageable);

	Page<VerificationRequest> findAllBy(Pageable pageable);
	@Query("SELECT vr FROM VerificationRequest vr WHERE vr.user.role = :role")
	Page<VerificationRequest> findByUserRole(@Param("role") User.Role role, Pageable pageable);
	@Query("SELECT vr FROM VerificationRequest vr WHERE vr.user.role = :role AND vr.status = :status")
	Page<VerificationRequest> findByUserRoleAndStatus(
			@Param("role") User.Role role,
			@Param("status") VerificationRequest.VerificationStatus status,
			Pageable pageable);
	@Query("SELECT vr FROM VerificationRequest vr JOIN vr.company c " +
		"WHERE vr.user.role = :role " +
		"AND (LOWER(c.legalName) LIKE LOWER(CONCAT('%', :search, '%')) " +
		"OR c.taxId LIKE CONCAT('%', :search, '%'))")
	Page<VerificationRequest> searchByUserRoleAndCompanyNameOrTaxId(
			@Param("role") User.Role role,
			@Param("search") String search,
			Pageable pageable);
	@Query("SELECT vr FROM VerificationRequest vr JOIN vr.company c " +
		"WHERE vr.user.role = :role AND vr.status = :status " +
		"AND (LOWER(c.legalName) LIKE LOWER(CONCAT('%', :search, '%')) " +
		"OR c.taxId LIKE CONCAT('%', :search, '%'))")
	Page<VerificationRequest> searchByUserRoleAndStatusAndCompanyNameOrTaxId(
			@Param("role") User.Role role,
			@Param("status") VerificationRequest.VerificationStatus status,
			@Param("search") String search,
			Pageable pageable);
	@Query("SELECT vr FROM VerificationRequest vr JOIN vr.company c " +
		"WHERE (LOWER(c.legalName) LIKE LOWER(CONCAT('%', :search, '%')) " +
		"OR c.taxId LIKE CONCAT('%', :search, '%'))")
	Page<VerificationRequest> searchByCompanyNameOrTaxId(@Param("search") String search, Pageable pageable);

	@Query("SELECT vr FROM VerificationRequest vr JOIN vr.company c " +
		"WHERE vr.status = :status " +
		"AND (LOWER(c.legalName) LIKE LOWER(CONCAT('%', :search, '%')) " +
		"OR c.taxId LIKE CONCAT('%', :search, '%'))")
	Page<VerificationRequest> searchByStatusAndCompanyNameOrTaxId(
			@Param("status") VerificationRequest.VerificationStatus status,
			@Param("search") String search,
			Pageable pageable);
	List<VerificationRequest> findTop5ByStatusOrderByCreatedAtDesc(VerificationRequest.VerificationStatus status);
	Optional<VerificationRequest> findByCompanyId(Long companyId);
}
