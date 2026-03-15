package by.bsuir.authservice.repository;

import by.bsuir.authservice.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);
	boolean existsByEmail(String email);

	List<User> findByRole(User.Role role);

	@Query("SELECT COUNT(u) FROM User u WHERE u.role = ?1")
	long countByRole(User.Role role);

	@Query("SELECT COUNT(u) FROM User u WHERE u.status = ?1")
	long countByStatus(String status);
	Page<User> findByRole(User.Role role, Pageable pageable);

	Page<User> findByRoleAndStatus(User.Role role, String status, Pageable pageable);
	@Query("SELECT u FROM User u LEFT JOIN u.company c WHERE u.role = :role " +
		"AND (LOWER(c.legalName) LIKE LOWER(CONCAT('%', :search, '%')) " +
		"OR c.taxId LIKE CONCAT('%', :search, '%'))")
	Page<User> findByRoleAndSearch(@Param("role") User.Role role,
								@Param("search") String search,
								Pageable pageable);

	@Query("SELECT u FROM User u LEFT JOIN u.company c WHERE u.role = :role AND u.status = :status " +
		"AND (LOWER(c.legalName) LIKE LOWER(CONCAT('%', :search, '%')) " +
		"OR c.taxId LIKE CONCAT('%', :search, '%'))")
	Page<User> findByRoleAndStatusAndSearch(@Param("role") User.Role role,
											@Param("status") String status,
											@Param("search") String search,
											Pageable pageable);
	long countByCreatedAtAfter(LocalDateTime date);

	long countByRoleAndCreatedAtAfter(User.Role role, LocalDateTime date);
	Page<User> findByStatus(String status, Pageable pageable);

	@Query("SELECT u FROM User u LEFT JOIN u.company c " +
		"WHERE (LOWER(c.legalName) LIKE LOWER(CONCAT('%', :search, '%')) " +
		"OR c.taxId LIKE CONCAT('%', :search, '%'))")
	Page<User> findBySearch(@Param("search") String search, Pageable pageable);

	@Query("SELECT u FROM User u LEFT JOIN u.company c WHERE u.status = :status " +
		"AND (LOWER(c.legalName) LIKE LOWER(CONCAT('%', :search, '%')) " +
		"OR c.taxId LIKE CONCAT('%', :search, '%'))")
	Page<User> findByStatusAndSearch(@Param("status") String status,
									@Param("search") String search,
									Pageable pageable);
	@Query("SELECT u.id FROM User u WHERE u.role = 'ADMIN' AND u.isActive = true")
	List<Long> findAllAdminUserIds();
	@Query("SELECT FUNCTION('DATE', u.createdAt) AS day, COUNT(u) AS cnt " +
			"FROM User u WHERE u.role <> 'ADMIN' AND u.createdAt >= :since " +
			"GROUP BY FUNCTION('DATE', u.createdAt) ORDER BY day ASC")
	List<Object[]> countRegistrationsPerDay(@Param("since") LocalDateTime since);
	@Query("SELECT u FROM User u LEFT JOIN FETCH u.company WHERE u.id = :id")
	Optional<User> findByIdWithCompany(@Param("id") Long id);

	@Query("SELECT u.id FROM User u WHERE u.company.id = :companyId")
	Long findUserIdByCompanyId(@Param("companyId") Long companyId);

	@Query("SELECT u FROM User u LEFT JOIN FETCH u.company c " +
		"WHERE u.role = 'SUPPLIER' AND u.status = 'ACTIVE' AND c IS NOT NULL")
	List<User> findAllActiveSupplierCompanies();
}
