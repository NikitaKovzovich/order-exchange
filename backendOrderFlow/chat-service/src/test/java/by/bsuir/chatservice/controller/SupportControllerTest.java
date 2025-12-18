package by.bsuir.chatservice.controller;

import by.bsuir.chatservice.dto.*;
import by.bsuir.chatservice.service.SupportService;
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

@WebMvcTest(SupportController.class)
class SupportControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private SupportService supportService;

	private ObjectMapper objectMapper;
	private TicketResponse testTicketResponse;
	private TicketMessageResponse testMessageResponse;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper();
		objectMapper.findAndRegisterModules();

		testMessageResponse = new TicketMessageResponse(
				1L, 1L, 10L, false, "Test message",
				null, false, LocalDateTime.now()
		);

		testTicketResponse = new TicketResponse(
				1L, 100L, 10L, "Test Subject",
				"NEW", "NORMAL", "TECHNICAL_ISSUE",
				null, LocalDateTime.now(), null, null,
				1L, testMessageResponse
		);
	}

	@Test
	@DisplayName("Should create ticket")
	void shouldCreateTicket() throws Exception {
		CreateTicketRequest request = new CreateTicketRequest(
				"Test Subject", "Test message", null, null
		);

		when(supportService.createTicket(eq(100L), eq(10L), any(CreateTicketRequest.class)))
				.thenReturn(testTicketResponse);

		mockMvc.perform(post("/api/support/tickets")
						.header("X-User-Company-Id", "100")
						.header("X-User-Id", "10")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.subject").value("Test Subject"));
	}

	@Test
	@DisplayName("Should get ticket by ID")
	void shouldGetTicketById() throws Exception {
		when(supportService.getTicket(1L, 10L, false)).thenReturn(testTicketResponse);

		mockMvc.perform(get("/api/support/tickets/1")
						.header("X-User-Id", "10")
						.header("X-User-Role", "USER"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.id").value(1));
	}

	@Test
	@DisplayName("Should get user tickets")
	void shouldGetUserTickets() throws Exception {
		PageResponse<TicketResponse> pageResponse = new PageResponse<>(
				List.of(testTicketResponse), 0, 20, 1, 1, true, true
		);

		when(supportService.getUserTickets(100L, 0, 20)).thenReturn(pageResponse);

		mockMvc.perform(get("/api/support/tickets")
						.header("X-User-Company-Id", "100"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.content").isArray());
	}

	@Test
	@DisplayName("Should get all tickets for admin")
	void shouldGetAllTicketsForAdmin() throws Exception {
		PageResponse<TicketResponse> pageResponse = new PageResponse<>(
				List.of(testTicketResponse), 0, 20, 1, 1, true, true
		);

		when(supportService.getAllTickets(null, 0, 20)).thenReturn(pageResponse);

		mockMvc.perform(get("/api/support/tickets/admin"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
	}

	@Test
	@DisplayName("Should get ticket messages")
	void shouldGetTicketMessages() throws Exception {
		when(supportService.getTicketMessages(1L, 10L, false))
				.thenReturn(List.of(testMessageResponse));

		mockMvc.perform(get("/api/support/tickets/1/messages")
						.header("X-User-Id", "10")
						.header("X-User-Role", "USER"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data").isArray());
	}

	@Test
	@DisplayName("Should add message")
	void shouldAddMessage() throws Exception {
		TicketMessageRequest request = new TicketMessageRequest("New message", null, null);

		when(supportService.addMessage(eq(1L), eq(10L), eq(false), any(TicketMessageRequest.class)))
				.thenReturn(testMessageResponse);

		mockMvc.perform(post("/api/support/tickets/1/messages")
						.header("X-User-Id", "10")
						.header("X-User-Role", "USER")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true));
	}

	@Test
	@DisplayName("Should assign ticket")
	void shouldAssignTicket() throws Exception {
		when(supportService.assignTicket(1L, 50L)).thenReturn(testTicketResponse);

		mockMvc.perform(post("/api/support/tickets/1/assign")
						.header("X-User-Id", "50"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
	}

	@Test
	@DisplayName("Should resolve ticket")
	void shouldResolveTicket() throws Exception {
		when(supportService.resolveTicket(1L)).thenReturn(testTicketResponse);

		mockMvc.perform(post("/api/support/tickets/1/resolve"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
	}

	@Test
	@DisplayName("Should close ticket")
	void shouldCloseTicket() throws Exception {
		when(supportService.closeTicket(1L)).thenReturn(testTicketResponse);

		mockMvc.perform(post("/api/support/tickets/1/close"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
	}

	@Test
	@DisplayName("Should reopen ticket")
	void shouldReopenTicket() throws Exception {
		when(supportService.reopenTicket(1L)).thenReturn(testTicketResponse);

		mockMvc.perform(post("/api/support/tickets/1/reopen"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
	}
}
