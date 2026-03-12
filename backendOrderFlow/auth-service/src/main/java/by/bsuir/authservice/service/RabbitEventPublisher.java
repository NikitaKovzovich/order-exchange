package by.bsuir.authservice.service;

import by.bsuir.authservice.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class RabbitEventPublisher {

	private final RabbitTemplate rabbitTemplate;
	private final boolean enabled;

	public RabbitEventPublisher(@Autowired(required = false) RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
		this.enabled = rabbitTemplate != null;
	}

	public void publish(String routingKey, Map<String, Object> payload) {
		if (!enabled) {
			log.debug("RabbitMQ not available, skipping event: {}", routingKey);
			return;
		}

		try {
			Map<String, Object> message = new HashMap<>(payload);
			message.put("timestamp", LocalDateTime.now().toString());
			message.put("source", "auth-service");

			rabbitTemplate.convertAndSend(RabbitMQConfig.AUTH_EXCHANGE, routingKey, message);
			log.info("✓ RabbitMQ event published: {} -> {}", RabbitMQConfig.AUTH_EXCHANGE, routingKey);
		} catch (Exception e) {
			log.warn("✗ Failed to publish RabbitMQ event {}: {}", routingKey, e.getMessage());
		}
	}
}

