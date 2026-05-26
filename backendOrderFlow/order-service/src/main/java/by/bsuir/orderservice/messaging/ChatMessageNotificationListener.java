package by.bsuir.orderservice.messaging;

import by.bsuir.orderservice.config.RabbitMQConfig;
import by.bsuir.orderservice.entity.Order;
import by.bsuir.orderservice.repository.OrderRepository;
import by.bsuir.orderservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageNotificationListener {

	private final NotificationService notificationService;
	private final OrderRepository orderRepository;

	@RabbitListener(queues = RabbitMQConfig.CHAT_NOTIFICATION_QUEUE)
	public void handleChatMessage(Map<String, Object> event) {
		try {
			Long orderId = extractLong(event.get("orderId"));
			Long senderCompanyId = extractLong(event.get("senderCompanyId"));
			if (orderId == null) {
				return;
			}

			Order order = orderRepository.findById(orderId).orElse(null);
			if (order == null) {
				log.warn("Chat notification: order {} not found", orderId);
				return;
			}

			Long recipientId = senderCompanyId != null && senderCompanyId.equals(order.getSupplierId())
					? order.getCustomerId()
					: order.getSupplierId();

			notificationService.createChatNotification(recipientId, orderId, order.getOrderNumber());
		} catch (Exception e) {
			log.error("Failed to process chat message notification: {}", e.getMessage(), e);
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
