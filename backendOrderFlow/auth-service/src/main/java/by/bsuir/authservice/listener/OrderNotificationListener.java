package by.bsuir.authservice.listener;

import by.bsuir.authservice.config.RabbitMQConfig;
import by.bsuir.authservice.entity.Notification;
import by.bsuir.authservice.entity.Notification.NotificationType;
import by.bsuir.authservice.repository.UserRepository;
import by.bsuir.authservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;






@Slf4j
@Component
@RequiredArgsConstructor
public class OrderNotificationListener {

	private final NotificationService notificationService;
	private final UserRepository userRepository;

	@RabbitListener(queues = RabbitMQConfig.ORDER_NOTIFICATION_QUEUE)
	public void handleOrderNotification(Map<String, Object> event) {
		try {

			Long recipientId = extractLong(event.get("recipientId"));
			String typeStr = event.get("type") != null ? event.get("type").toString() : null;
			String title = event.get("title") != null ? event.get("title").toString() : "";
			String message = event.get("message") != null ? event.get("message").toString() : "";
			Long orderId = extractLong(event.get("orderId"));
			String orderNumber = event.get("orderNumber") != null ? event.get("orderNumber").toString() : null;

			if (recipientId == null || typeStr == null) {
				log.warn("Received incomplete order notification event: recipientId={}, type={}", recipientId, typeStr);
				return;
			}


			Long userId = userRepository.findUserIdByCompanyId(recipientId);
			if (userId == null) {
				log.warn("No user found for companyId={}, skipping notification", recipientId);
				return;
			}

			NotificationType type;
			try {
				type = NotificationType.valueOf(typeStr);
			} catch (IllegalArgumentException e) {
				log.warn("Unknown notification type: {}, defaulting to SYSTEM", typeStr);
				type = NotificationType.SYSTEM;
			}

			Notification notification = notificationService.createNotification(
					userId, title, message, type, "Order", orderId);
			notification.setOrderId(orderId);
			notification.setOrderNumber(orderNumber);

			log.info("Created auth-service notification for user {}: type={}, order={}", userId, type, orderNumber);

		} catch (Exception e) {
			log.error("Failed to process order notification event: {}", e.getMessage(), e);
		}
	}

	private Long extractLong(Object value) {
		if (value instanceof Number n) return n.longValue();
		if (value instanceof String s) {
			try {
				return Long.parseLong(s);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return null;
	}
}
