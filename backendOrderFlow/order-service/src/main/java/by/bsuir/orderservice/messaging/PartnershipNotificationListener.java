package by.bsuir.orderservice.messaging;

import by.bsuir.orderservice.config.RabbitMQConfig;
import by.bsuir.orderservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PartnershipNotificationListener {

	private final NotificationService notificationService;

	@RabbitListener(queues = RabbitMQConfig.PARTNERSHIP_NOTIFICATION_QUEUE)
	public void handlePartnershipNotification(Map<String, Object> event) {
		try {
			Long recipientId = extractLong(event.get("recipientId"));
			String type = event.get("type") != null ? event.get("type").toString() : null;
			String title = event.get("title") != null ? event.get("title").toString() : "";
			String message = event.get("message") != null ? event.get("message").toString() : "";

			if (recipientId == null || type == null) {
				log.warn("Incomplete partnership notification event: recipientId={}, type={}", recipientId, type);
				return;
			}

			notificationService.createPartnershipNotification(recipientId, type, title, message);
		} catch (Exception e) {
			log.error("Failed to process partnership notification event: {}", e.getMessage(), e);
		}
	}

	private Long extractLong(Object value) {
		if (value instanceof Number n) {
			return n.longValue();
		}
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
