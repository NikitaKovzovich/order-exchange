package by.bsuir.authservice.repository;

import by.bsuir.authservice.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

	Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

	long countByUserIdAndIsReadFalse(Long userId);

	@Modifying
	@Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
	int markAllAsReadByUserId(@Param("userId") Long userId);
	Page<Notification> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
