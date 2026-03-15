package by.bsuir.orderservice.repository;

import by.bsuir.orderservice.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	Page<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

	Page<Notification> findByRecipientIdAndReadOrderByCreatedAtDesc(Long recipientId, boolean read, Pageable pageable);

	long countByRecipientIdAndReadFalse(Long recipientId);

	List<Notification> findByOrderId(Long orderId);

	@Modifying
	@Query("UPDATE Notification n SET n.read = true, n.readAt = CURRENT_TIMESTAMP WHERE n.recipientId = :recipientId AND n.read = false")
	int markAllAsRead(@Param("recipientId") Long recipientId);
}
