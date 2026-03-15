package by.bsuir.catalogservice.listener;

import by.bsuir.catalogservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;





@Slf4j
@Component
@RequiredArgsConstructor
public class StockReserveListener {

	private final InventoryService inventoryService;

	@RabbitListener(queues = "stock.reserve.queue")
	public void handleStockReserveRequest(Map<String, Object> event) {
		try {
			Long productId = toLong(event.get("productId"));
			int quantity = toInt(event.get("quantity"));
			Long orderId = toLong(event.get("orderId"));

			log.info("Received stock reserve request: productId={}, quantity={}, orderId={}", productId, quantity, orderId);

			inventoryService.reserveStock(productId, quantity);

			log.info("Stock reserved successfully: productId={}, quantity={}", productId, quantity);
		} catch (Exception e) {
			log.error("Failed to process stock reserve request: {}", e.getMessage(), e);

		}
	}

	private Long toLong(Object value) {
		if (value == null) return null;
		if (value instanceof Number n) return n.longValue();
		return Long.parseLong(value.toString());
	}

	private int toInt(Object value) {
		if (value instanceof Number n) return n.intValue();
		return Integer.parseInt(value.toString());
	}
}
