package by.bsuir.chatservice.controller;

import by.bsuir.chatservice.dto.*;
import by.bsuir.chatservice.exception.ResourceNotFoundException;
import by.bsuir.chatservice.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private ChatService chatService;

	private ChatChannelResponse testChannelResponse;
	private MessageResponse testMessageResponse;

	@BeforeEach
	void setUp() {
		testChannelResponse = new ChatChannelResponse(
				1L, 100L, 1L, 2L, "Test Channel",
				true, LocalDateTime.now(), 5, 2L, null
		);

		testMessageResponse = new MessageResponse(
				1L, 1L, 1L, "Hello!", "TEXT", null,
				false, LocalDateTime.now(), null
		);
	}

	@Test
	@DisplayName("Should create channel")
	void shouldCreateChannel() throws Exception {
		CreateChannelRequest request = new CreateChannelRequest(100L, 1L, 2L, "Test Channel");

		when(chatService.createChannel(any(CreateChannelRequest.class))).thenReturn(testChannelResponse);

		mockMvc.perform(post("/api/chats")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.orderId").value(100));
	}

	@Test
	@DisplayName("Should get channel by order ID")
	void shouldGetChannelByOrderId() throws Exception {
		when(chatService.getChannelByOrderId(100L, 1L)).thenReturn(testChannelResponse);

		mockMvc.perform(get("/api/chats/order/100")
						.header("X-User-Id", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.orderId").value(100))
				.andExpect(jsonPath("$.data.unreadCount").value(2));
	}

	@Test
	@DisplayName("Should get user channels")
	void shouldGetUserChannels() throws Exception {
		when(chatService.getUserChannels(1L)).thenReturn(List.of(testChannelResponse));

		mockMvc.perform(get("/api/chats")
						.header("X-User-Id", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data").isArray())
				.andExpect(jsonPath("$.data[0].orderId").value(100));
	}

	@Test
	@DisplayName("Should get messages")
	void shouldGetMessages() throws Exception {
		PageResponse<MessageResponse> pageResponse = new PageResponse<>(
				List.of(testMessageResponse), 0, 50, 1, 1, true, true
		);

		when(chatService.getMessages(100L, 1L, 0, 50)).thenReturn(pageResponse);

		mockMvc.perform(get("/api/chats/order/100/messages")
						.header("X-User-Id", "1")
						.param("page", "0")
						.param("size", "50"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content").isArray())
				.andExpect(jsonPath("$.data.totalElements").value(1));
	}

	@Test
	@DisplayName("Should send message")
	void shouldSendMessage() throws Exception {
		SendMessageRequest request = new SendMessageRequest("Hello!", null);

		when(chatService.sendMessage(eq(100L), eq(1L), any(SendMessageRequest.class)))
				.thenReturn(testMessageResponse);

		mockMvc.perform(post("/api/chats/order/100/messages")
						.header("X-User-Id", "1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.messageText").value("Hello!"));
	}

	@Test
	@DisplayName("Should mark messages as read")
	void shouldMarkMessagesAsRead() throws Exception {
		doNothing().when(chatService).markMessagesAsRead(100L, 1L);

		mockMvc.perform(post("/api/chats/order/100/read")
						.header("X-User-Id", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
	}

	@Test
	@DisplayName("Should deactivate channel")
	void shouldDeactivateChannel() throws Exception {
		doNothing().when(chatService).deactivateChannel(100L);

		mockMvc.perform(post("/api/chats/order/100/deactivate"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
	}

	@Test
	@DisplayName("Should return 404 when channel not found")
	void shouldReturn404WhenChannelNotFound() throws Exception {
		when(chatService.getChannelByOrderId(999L, 1L))
				.thenThrow(new ResourceNotFoundException("ChatChannel", "orderId", 999L));

		mockMvc.perform(get("/api/chats/order/999")
						.header("X-User-Id", "1"))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("Should return 400 for invalid request")
	void shouldReturn400ForInvalidRequest() throws Exception {
		CreateChannelRequest invalidRequest = new CreateChannelRequest(null, null, null, null);

		mockMvc.perform(post("/api/chats")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(invalidRequest)))
				.andExpect(status().isBadRequest());
	}
}
