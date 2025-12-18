package by.bsuir.orderservice.service;

import by.bsuir.orderservice.entity.Event;
import by.bsuir.orderservice.entity.Order;
import by.bsuir.orderservice.repository.EventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Slf4j
public class EventPublisher {
	private static final String EXCHANGE = "order.events";

	private final EventRepository eventRepository;
	private final RabbitTemplate rabbitTemplate;
	private final ObjectMapper objectMapper;

	@Autowired
	public EventPublisher(EventRepository eventRepository,
						  @Autowired(required = false) RabbitTemplate rabbitTemplate,
						  ObjectMapper objectMapper) {
		this.eventRepository = eventRepository;
		this.rabbitTemplate = rabbitTemplate;
		this.objectMapper = objectMapper;
	}

	@Transactional
	public void publishOrderCreated(Order order) {
		publish("OrderCreated", order.getId().toString(), Map.of(
				"orderId", order.getId(),
				"orderNumber", order.getOrderNumber(),
				"supplierId", order.getSupplierId(),
				"customerId", order.getCustomerId(),
				"totalAmount", order.getTotalAmount()
		));
	}

	@Transactional
	public void publishOrderConfirmed(Order order) {
		publish("OrderConfirmed", order.getId().toString(), Map.of(
				"orderId", order.getId(),
				"orderNumber", order.getOrderNumber(),
				"supplierId", order.getSupplierId()
		));
	}

	@Transactional
	public void publishOrderRejected(Order order, String reason) {
		publish("OrderRejected", order.getId().toString(), Map.of(
				"orderId", order.getId(),
				"orderNumber", order.getOrderNumber(),
				"reason", reason
		));
	}

	@Transactional
	public void publishOrderPaid(Order order) {
		publish("OrderPaid", order.getId().toString(), Map.of(
				"orderId", order.getId(),
				"orderNumber", order.getOrderNumber(),
				"totalAmount", order.getTotalAmount()
		));
	}

	@Transactional
	public void publishOrderShipped(Order order) {
		publish("OrderShipped", order.getId().toString(), Map.of(
				"orderId", order.getId(),
				"orderNumber", order.getOrderNumber(),
				"supplierId", order.getSupplierId()
		));
	}

	@Transactional
	public void publishOrderDelivered(Order order) {
		publish("OrderDelivered", order.getId().toString(), Map.of(
				"orderId", order.getId(),
				"orderNumber", order.getOrderNumber()
		));
	}

	@Transactional
	public void publishOrderClosed(Order order) {
		publish("OrderClosed", order.getId().toString(), Map.of(
				"orderId", order.getId(),
				"orderNumber", order.getOrderNumber()
		));
	}

	private void publish(String eventType, String aggregateId, Map<String, Object> payload) {
		try {
			String payloadJson = objectMapper.writeValueAsString(payload);

			Long version = eventRepository.findMaxVersionByAggregateId(aggregateId).orElse(0L) + 1;

			Event event = Event.builder()
					.aggregateId(aggregateId)
					.aggregateType("Order")
					.eventType(eventType)
					.payload(payloadJson)
					.version(version)
					.build();

			eventRepository.save(event);

			if (rabbitTemplate != null) {
				rabbitTemplate.convertAndSend(EXCHANGE, eventType.toLowerCase(), payloadJson);
			}
			log.info("Published event: {} for order: {}", eventType, aggregateId);

		} catch (JsonProcessingException e) {
			log.error("Failed to serialize event payload", e);
			throw new RuntimeException("Failed to publish event", e);
		}
	}
}
