package by.bsuir.authservice.service;

import by.bsuir.authservice.entity.Notification;
import by.bsuir.authservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

	private final NotificationRepository notificationRepository;


	public Notification createNotification(Long userId, String title, String message,
										Notification.NotificationType type,
										String relatedEntityType, Long relatedEntityId) {
		Notification notification = Notification.builder()
				.userId(userId)
				.title(title)
				.message(message)
				.type(type)
				.isRead(false)
				.createdAt(LocalDateTime.now())
				.relatedEntityType(relatedEntityType)
				.relatedEntityId(relatedEntityId)
				.build();

		notification = notificationRepository.save(notification);
		log.info("Notification created for user {}: {} ({})", userId, title, type);
		return notification;
	}


	public void notifyAllAdmins(String title, String message, Notification.NotificationType type,
								String relatedEntityType, Long relatedEntityId,
								java.util.List<Long> adminUserIds) {
		for (Long adminId : adminUserIds) {
			createNotification(adminId, title, message, type, relatedEntityType, relatedEntityId);
		}
	}


	@Transactional(readOnly = true)
	public Page<Notification> getUserNotifications(Long userId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
	}


	@Transactional(readOnly = true)
	public long getUnreadCount(Long userId) {
		return notificationRepository.countByUserIdAndIsReadFalse(userId);
	}


	@Transactional
	public boolean markAsRead(Long notificationId, Long userId) {
		return notificationRepository.findById(notificationId)
				.filter(n -> n.getUserId().equals(userId))
				.map(n -> {
					n.setIsRead(true);
					notificationRepository.save(n);
					return true;
				})
				.orElse(false);
	}


	@Transactional
	public int markAllAsRead(Long userId) {
		return notificationRepository.markAllAsReadByUserId(userId);
	}
}
