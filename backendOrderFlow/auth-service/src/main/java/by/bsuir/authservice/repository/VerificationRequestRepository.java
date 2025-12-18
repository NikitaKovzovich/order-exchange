package by.bsuir.authservice.repository;

import by.bsuir.authservice.entity.VerificationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, Long> {
	Optional<VerificationRequest> findByUserId(Long userId);
	List<VerificationRequest> findByStatus(VerificationRequest.VerificationStatus status);
	List<VerificationRequest> findByStatus(String status);
	long countByStatus(String status);
}
