package by.bsuir.authservice.service;

import by.bsuir.authservice.entity.Event;
import by.bsuir.authservice.repository.EventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventPublisherTest {

	@Mock
	private EventRepository eventRepository;

	@Spy
	private ObjectMapper objectMapper = new ObjectMapper();

	@InjectMocks
	private EventPublisher eventPublisher;

	@Nested
	@DisplayName("Publish Tests")
	class PublishTests {

		@Test
		@DisplayName("Should publish event with payload")
		void shouldPublishEventWithPayload() {
			when(eventRepository.findMaxVersionByAggregateId("1")).thenReturn(null);
			when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

			Map<String, Object> payload = new HashMap<>();
			payload.put("userId", 1L);
			payload.put("email", "test@example.com");

			eventPublisher.publish("User", "1", "UserCreated", payload);

			ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
			verify(eventRepository).save(eventCaptor.capture());

			Event savedEvent = eventCaptor.getValue();
			assertThat(savedEvent.getAggregateType()).isEqualTo("User");
			assertThat(savedEvent.getAggregateId()).isEqualTo("1");
			assertThat(savedEvent.getEventType()).isEqualTo("UserCreated");
			assertThat(savedEvent.getVersion()).isEqualTo(1);
			assertThat(savedEvent.getPayload()).contains("userId");
		}

		@Test
		@DisplayName("Should increment version for existing aggregate")
		void shouldIncrementVersionForExistingAggregate() {
			when(eventRepository.findMaxVersionByAggregateId("1")).thenReturn(5);
			when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

			Map<String, Object> payload = new HashMap<>();
			payload.put("field", "value");
			eventPublisher.publish("User", "1", "UserUpdated", payload);

			ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
			verify(eventRepository).save(eventCaptor.capture());

			Event savedEvent = eventCaptor.getValue();
			assertThat(savedEvent.getVersion()).isEqualTo(6);
		}

		@Test
		@DisplayName("Should handle null payload")
		void shouldHandleNullPayload() {
			when(eventRepository.findMaxVersionByAggregateId("1")).thenReturn(null);
			when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

			eventPublisher.publish("Company", "1", "CompanyCreated", null);

			verify(eventRepository).save(any(Event.class));
		}

		@Test
		@DisplayName("Should not throw exception on error")
		void shouldNotThrowExceptionOnError() {
			when(eventRepository.findMaxVersionByAggregateId(any())).thenThrow(new RuntimeException("DB error"));

			eventPublisher.publish("User", "1", "UserCreated", Map.of("test", "value"));
		}
	}

	@Nested
	@DisplayName("Publish Simple Tests")
	class PublishSimpleTests {

		@Test
		@DisplayName("Should publish simple event with message")
		void shouldPublishSimpleEventWithMessage() {
			when(eventRepository.findMaxVersionByAggregateId("1")).thenReturn(null);
			when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

			eventPublisher.publishSimple("Notification", "1", "NotificationSent", "Email sent successfully");

			ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
			verify(eventRepository).save(eventCaptor.capture());

			Event savedEvent = eventCaptor.getValue();
			assertThat(savedEvent.getPayload()).contains("message");
			assertThat(savedEvent.getPayload()).contains("Email sent successfully");
		}
	}

	@Nested
	@DisplayName("Publish Empty Tests")
	class PublishEmptyTests {

		@Test
		@DisplayName("Should publish empty event")
		void shouldPublishEmptyEvent() {
			when(eventRepository.findMaxVersionByAggregateId("1")).thenReturn(null);
			when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

			eventPublisher.publishEmpty("System", "1", "SystemStarted");

			ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
			verify(eventRepository).save(eventCaptor.capture());

			Event savedEvent = eventCaptor.getValue();
			assertThat(savedEvent.getEventType()).isEqualTo("SystemStarted");
			assertThat(savedEvent.getPayload()).contains("timestamp");
		}
	}
}
