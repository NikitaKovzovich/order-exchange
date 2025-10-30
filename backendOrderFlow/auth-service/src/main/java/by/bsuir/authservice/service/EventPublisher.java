package by.bsuir.authservice.service;

import by.bsuir.authservice.entity.Event;
import by.bsuir.authservice.repository.EventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final EventRepository eventRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publish(String aggregateType, String aggregateId, String eventType, Map<String, Object> payload) {
        try {
            Integer currentVersion = eventRepository.findMaxVersionByAggregateId(aggregateId);
            int nextVersion = (currentVersion != null ? currentVersion : 0) + 1;

            if (payload == null) {
                payload = new HashMap<>();
            }
            payload.put("timestamp", LocalDateTime.now().toString());

            String payloadJson = objectMapper.writeValueAsString(payload);

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

