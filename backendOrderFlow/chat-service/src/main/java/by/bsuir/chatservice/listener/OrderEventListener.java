package by.bsuir.chatservice.listener;

import by.bsuir.chatservice.config.RabbitMQConfig;
import by.bsuir.chatservice.dto.CreateChannelRequest;
import by.bsuir.chatservice.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

	private final ChatService chatService;

	@RabbitListener(queues = RabbitMQConfig.ORDER_CREATED_QUEUE)
	public void handleOrderCreated(Map<String, Object> event) {
		try {
			Long orderId = extractLong(event.get("orderId"));
			Long supplierId = extractLong(event.get("supplierId"));
			Long customerId = extractLong(event.get("customerId"));
			String orderNumber = (String) event.get("orderNumber");

			if (orderId == null || supplierId == null || customerId == null) {
				log.warn("Invalid order event data: {}", event);
				return;
			}

			CreateChannelRequest request = new CreateChannelRequest(
					orderId,
					supplierId,
					customerId,
					"Заказ #" + (orderNumber != null ? orderNumber : orderId)
			);

			chatService.createChannel(request);
			log.info("Chat channel created for order {}", orderId);
		} catch (Exception e) {
			log.error("Failed to create chat channel for order event: {}", e.getMessage(), e);
		}
	}

	@RabbitListener(queues = RabbitMQConfig.ORDER_CLOSED_QUEUE)
	public void handleOrderClosed(Map<String, Object> event) {
		try {
			Long orderId = extractLong(event.get("orderId"));

			if (orderId == null) {
				log.warn("Invalid order closed event: {}", event);
				return;
			}

			chatService.deactivateChannel(orderId);
			log.info("Chat channel deactivated for order {}", orderId);
		} catch (Exception e) {
			log.error("Failed to deactivate chat channel: {}", e.getMessage(), e);
		}
	}

	private Long extractLong(Object value) {
		if (value == null) return null;
		if (value instanceof Long) return (Long) value;
		if (value instanceof Integer) return ((Integer) value).longValue();
		if (value instanceof String) {
			try {
				return Long.parseLong((String) value);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return null;
	}
}

