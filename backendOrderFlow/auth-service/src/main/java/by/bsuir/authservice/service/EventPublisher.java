package by.bsuir.authservice.service;

import by.bsuir.authservice.entity.Event;
import by.bsuir.authservice.repository.EventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

	private final EventRepository eventRepository;
	private final ObjectMapper objectMapper;
	private final PlatformTransactionManager transactionManager;

	public void publish(String aggregateType, String aggregateId, String eventType, Map<String, Object> payload) {
		try {
			TransactionTemplate tx = new TransactionTemplate(transactionManager);
			tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
			tx.executeWithoutResult(status -> {
				Integer currentVersion = eventRepository.findMaxVersionByAggregateId(aggregateId);
				int nextVersion = (currentVersion != null ? currentVersion : 0) + 1;

				Map<String, Object> mutablePayload = payload != null ? new HashMap<>(payload) : new HashMap<>();
				mutablePayload.put("timestamp", LocalDateTime.now().toString());

				String payloadJson;
				try {
					payloadJson = objectMapper.writeValueAsString(mutablePayload);
				} catch (Exception e) {
					throw new RuntimeException("Failed to serialize event payload", e);
				}

				Event event = Event.builder()
						.aggregateId(aggregateId)
						.aggregateType(aggregateType)
						.version(nextVersion)
						.eventType(eventType)
						.payload(payloadJson)
						.createdAt(LocalDateTime.now())
						.build();

				eventRepository.save(event);

				log.info("✓ Event published: {} - {} v{} ({})",
						aggregateType, aggregateId, nextVersion, eventType);
			});
		} catch (Exception e) {
			log.error("✗ Failed to publish event: {} - {} ({})",
					aggregateType, aggregateId, eventType, e);
		}
	}

	public void publishSimple(String aggregateType, String aggregateId, String eventType, String message) {
		Map<String, Object> payload = new HashMap<>();
		payload.put("message", message);
		publish(aggregateType, aggregateId, eventType, payload);
	}

	public void publishEmpty(String aggregateType, String aggregateId, String eventType) {
		publish(aggregateType, aggregateId, eventType, new HashMap<>());
	}
}
