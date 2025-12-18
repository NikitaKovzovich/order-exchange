package by.bsuir.authservice.repository;

import by.bsuir.authservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
}
