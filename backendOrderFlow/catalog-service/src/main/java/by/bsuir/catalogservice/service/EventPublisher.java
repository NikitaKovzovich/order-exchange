package by.bsuir.catalogservice.service;

import by.bsuir.catalogservice.entity.Category;
import by.bsuir.catalogservice.entity.Event;
import by.bsuir.catalogservice.entity.Inventory;
import by.bsuir.catalogservice.entity.Product;
import by.bsuir.catalogservice.repository.EventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class EventPublisher {
	private final EventRepository eventRepository;
	private final ObjectMapper objectMapper;
	private final RabbitTemplate rabbitTemplate;

	@Autowired
	public EventPublisher(EventRepository eventRepository, ObjectMapper objectMapper,
						  @Autowired(required = false) RabbitTemplate rabbitTemplate) {
		this.eventRepository = eventRepository;
		this.objectMapper = objectMapper;
		this.rabbitTemplate = rabbitTemplate;
	}

	public void publishProductCreated(Product product) {
		publish("Product", product.getId().toString(), Event.EventTypes.PRODUCT_CREATED,
				Map.of("productId", product.getId(),
					"supplierId", product.getSupplierId(),
					"sku", product.getSku(),
					"name", product.getName(),
					"status", product.getStatus().name()));
	}

	public void publishProductUpdated(Product product) {
		publish("Product", product.getId().toString(), Event.EventTypes.PRODUCT_UPDATED,
				Map.of("productId", product.getId(),
					"name", product.getName(),
					"pricePerUnit", product.getPricePerUnit()));
	}

	public void publishProductPublished(Product product) {
		publish("Product", product.getId().toString(), Event.EventTypes.PRODUCT_PUBLISHED,
				Map.of("productId", product.getId(),
					"supplierId", product.getSupplierId(),
					"name", product.getName()));
	}

	public void publishProductArchived(Product product) {
		publish("Product", product.getId().toString(), Event.EventTypes.PRODUCT_ARCHIVED,
				Map.of("productId", product.getId()));
	}

	public void publishInventoryUpdated(Inventory inventory, String reason) {
		publish("Inventory", inventory.getProductId().toString(), Event.EventTypes.INVENTORY_UPDATED,
				Map.of("productId", inventory.getProductId(),
					"quantityAvailable", inventory.getQuantityAvailable(),
					"reason", reason != null ? reason : "manual update"));
	}

	public void publishStockReserved(Long productId, int quantity) {
		publish("Inventory", productId.toString(), Event.EventTypes.STOCK_RESERVED,
				Map.of("productId", productId, "quantity", quantity));

		if (rabbitTemplate != null) {
			rabbitTemplate.convertAndSend("catalog.exchange", "inventory.reserved",
					Map.of("productId", productId, "quantity", quantity));
		}
	}

	public void publishStockReleased(Long productId, int quantity) {
		publish("Inventory", productId.toString(), Event.EventTypes.STOCK_RELEASED,
				Map.of("productId", productId, "quantity", quantity));
	}

	public void publishCategoryCreated(Category category) {
		publish("Category", category.getId().toString(), Event.EventTypes.CATEGORY_CREATED,
				Map.of("categoryId", category.getId(), "name", category.getName()));
	}

	public void publishCategoryUpdated(Category category) {
		publish("Category", category.getId().toString(), Event.EventTypes.CATEGORY_UPDATED,
				Map.of("categoryId", category.getId(), "name", category.getName()));
	}

	public void publishCategoryDeleted(Long categoryId) {
		publish("Category", categoryId.toString(), Event.EventTypes.CATEGORY_DELETED,
				Map.of("categoryId", categoryId));
	}

	private void publish(String aggregateType, String aggregateId, String eventType, Map<String, Object> data) {
		try {
			int version = eventRepository.findMaxVersionByAggregateId(aggregateId).orElse(0) + 1;

			Event event = Event.builder()
					.aggregateType(aggregateType)
					.aggregateId(aggregateId)
					.eventType(eventType)
					.version(version)
					.payload(objectMapper.writeValueAsString(data))
					.build();

			eventRepository.save(event);
			log.info("Published event: {} for {}/{}", eventType, aggregateType, aggregateId);
		} catch (JsonProcessingException e) {
			log.error("Failed to serialize event payload", e);
		}
	}
}
