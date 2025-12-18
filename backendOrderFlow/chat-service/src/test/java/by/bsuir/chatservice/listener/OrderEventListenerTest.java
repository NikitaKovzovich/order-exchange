package by.bsuir.chatservice.listener;

import by.bsuir.chatservice.dto.ChatChannelResponse;
import by.bsuir.chatservice.dto.CreateChannelRequest;
import by.bsuir.chatservice.exception.DuplicateResourceException;
import by.bsuir.chatservice.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderEventListenerTest {

	@Mock
	private ChatService chatService;

	@InjectMocks
	private OrderEventListener orderEventListener;

	private ChatChannelResponse mockChannelResponse;

	@BeforeEach
	void setUp() {
		mockChannelResponse = new ChatChannelResponse(
				1L, 1L, 10L, 20L, "Заказ #ORD-001",
				true, LocalDateTime.now(), 0, 0L, null
		);
	}

	@Test
	@DisplayName("Should create chat channel when order created")
	void shouldCreateChatChannelWhenOrderCreated() {
		Map<String, Object> event = new HashMap<>();
		event.put("orderId", 1L);
		event.put("supplierId", 10L);
		event.put("customerId", 20L);
		event.put("orderNumber", "ORD-001");

		when(chatService.createChannel(any(CreateChannelRequest.class))).thenReturn(mockChannelResponse);

		orderEventListener.handleOrderCreated(event);

		verify(chatService).createChannel(any(CreateChannelRequest.class));
	}

	@Test
	@DisplayName("Should handle Integer values in event")
	void shouldHandleIntegerValuesInEvent() {
		Map<String, Object> event = new HashMap<>();
		event.put("orderId", 1);
		event.put("supplierId", 10);
		event.put("customerId", 20);
		event.put("orderNumber", "ORD-001");

		when(chatService.createChannel(any(CreateChannelRequest.class))).thenReturn(mockChannelResponse);

		orderEventListener.handleOrderCreated(event);

		verify(chatService).createChannel(any(CreateChannelRequest.class));
	}

	@Test
	@DisplayName("Should not fail when channel already exists")
	void shouldNotFailWhenChannelAlreadyExists() {
		Map<String, Object> event = new HashMap<>();
		event.put("orderId", 1L);
		event.put("supplierId", 10L);
		event.put("customerId", 20L);

		when(chatService.createChannel(any(CreateChannelRequest.class)))
				.thenThrow(new DuplicateResourceException("ChatChannel", "orderId", 1L));

		orderEventListener.handleOrderCreated(event);

		verify(chatService).createChannel(any(CreateChannelRequest.class));
	}

	@Test
	@DisplayName("Should not create channel when data is invalid")
	void shouldNotCreateChannelWhenDataIsInvalid() {
		Map<String, Object> event = new HashMap<>();
		event.put("orderId", null);

		orderEventListener.handleOrderCreated(event);

		verify(chatService, never()).createChannel(any(CreateChannelRequest.class));
	}

	@Test
	@DisplayName("Should deactivate channel when order closed")
	void shouldDeactivateChannelWhenOrderClosed() {
		Map<String, Object> event = new HashMap<>();
		event.put("orderId", 1L);

		doNothing().when(chatService).deactivateChannel(1L);

		orderEventListener.handleOrderClosed(event);

		verify(chatService).deactivateChannel(1L);
	}

	@Test
	@DisplayName("Should not deactivate when orderId is null")
	void shouldNotDeactivateWhenOrderIdIsNull() {
		Map<String, Object> event = new HashMap<>();
		event.put("orderId", null);

		orderEventListener.handleOrderClosed(event);

		verify(chatService, never()).deactivateChannel(anyLong());
	}
}

