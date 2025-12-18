package by.bsuir.chatservice.service;

import by.bsuir.chatservice.dto.*;
import by.bsuir.chatservice.entity.SupportTicket;
import by.bsuir.chatservice.entity.TicketMessage;
import by.bsuir.chatservice.exception.AccessDeniedException;
import by.bsuir.chatservice.exception.ResourceNotFoundException;
import by.bsuir.chatservice.repository.SupportTicketRepository;
import by.bsuir.chatservice.repository.TicketMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupportServiceTest {

	@Mock
	private SupportTicketRepository ticketRepository;

	@Mock
	private TicketMessageRepository messageRepository;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@InjectMocks
	private SupportService supportService;

	private SupportTicket testTicket;
	private TicketMessage testMessage;

	@BeforeEach
	void setUp() {
		testTicket = SupportTicket.builder()
				.id(1L)
				.requesterCompanyId(100L)
				.requesterUserId(10L)
				.subject("Test Subject")
				.status(SupportTicket.TicketStatus.NEW)
				.priority(SupportTicket.TicketPriority.NORMAL)
				.category(SupportTicket.TicketCategory.TECHNICAL_ISSUE)
				.createdAt(LocalDateTime.now())
				.build();

		testMessage = TicketMessage.builder()
				.id(1L)
				.ticket(testTicket)
				.senderId(10L)
				.messageText("Test message")
				.isAdminReply(false)
				.sentAt(LocalDateTime.now())
				.build();
	}

	@Nested
	@DisplayName("Create Ticket Tests")
	class CreateTicketTests {

		@Test
		@DisplayName("Should create ticket successfully")
		void shouldCreateTicketSuccessfully() {
			CreateTicketRequest request = new CreateTicketRequest(
					"Test Subject", "Test message",
					SupportTicket.TicketCategory.TECHNICAL_ISSUE,
					SupportTicket.TicketPriority.NORMAL
			);

			when(ticketRepository.save(any(SupportTicket.class))).thenReturn(testTicket);
			when(messageRepository.save(any(TicketMessage.class))).thenReturn(testMessage);

			TicketResponse response = supportService.createTicket(100L, 10L, request);

			assertThat(response).isNotNull();
			assertThat(response.subject()).isEqualTo("Test Subject");
			verify(ticketRepository).save(any(SupportTicket.class));
			verify(messageRepository).save(any(TicketMessage.class));
		}
	}

	@Nested
	@DisplayName("Get Ticket Tests")
	class GetTicketTests {

		@Test
		@DisplayName("Should get ticket for owner")
		void shouldGetTicketForOwner() {
			when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
			when(messageRepository.countByTicketId(1L)).thenReturn(1L);
			when(messageRepository.findPublicMessagesByTicketId(1L)).thenReturn(List.of(testMessage));

			TicketResponse response = supportService.getTicket(1L, 10L, false);

			assertThat(response).isNotNull();
			assertThat(response.id()).isEqualTo(1L);
		}

		@Test
		@DisplayName("Should get ticket for admin")
		void shouldGetTicketForAdmin() {
			when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
			when(messageRepository.countByTicketId(1L)).thenReturn(1L);
			when(messageRepository.findByTicketIdOrderBySentAtAsc(1L)).thenReturn(List.of(testMessage));

			TicketResponse response = supportService.getTicket(1L, 999L, true);

			assertThat(response).isNotNull();
		}

		@Test
		@DisplayName("Should throw when non-owner tries to access")
		void shouldThrowWhenNonOwnerTriesToAccess() {
			when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));

			assertThatThrownBy(() -> supportService.getTicket(1L, 999L, false))
					.isInstanceOf(AccessDeniedException.class);
		}

		@Test
		@DisplayName("Should throw when ticket not found")
		void shouldThrowWhenTicketNotFound() {
			when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> supportService.getTicket(999L, 10L, false))
					.isInstanceOf(ResourceNotFoundException.class);
		}
	}

	@Nested
	@DisplayName("Get User Tickets Tests")
	class GetUserTicketsTests {

		@Test
		@DisplayName("Should get user tickets")
		void shouldGetUserTickets() {
			Page<SupportTicket> page = new PageImpl<>(List.of(testTicket));
			when(ticketRepository.findByRequesterCompanyId(eq(100L), any(Pageable.class))).thenReturn(page);
			when(messageRepository.countByTicketId(1L)).thenReturn(1L);
			when(messageRepository.findPublicMessagesByTicketId(1L)).thenReturn(List.of(testMessage));

			PageResponse<TicketResponse> response = supportService.getUserTickets(100L, 0, 20);

			assertThat(response.content()).hasSize(1);
		}
	}

	@Nested
	@DisplayName("Add Message Tests")
	class AddMessageTests {

		@Test
		@DisplayName("Should add message from user")
		void shouldAddMessageFromUser() {
			testTicket.setStatus(SupportTicket.TicketStatus.IN_PROGRESS);
			TicketMessageRequest request = new TicketMessageRequest("New message", null, null);

			when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
			when(messageRepository.save(any(TicketMessage.class))).thenReturn(testMessage);

			TicketMessageResponse response = supportService.addMessage(1L, 10L, false, request);

			assertThat(response).isNotNull();
			verify(messageRepository).save(any(TicketMessage.class));
		}

		@Test
		@DisplayName("Should add message from admin")
		void shouldAddMessageFromAdmin() {
			testTicket.setStatus(SupportTicket.TicketStatus.IN_PROGRESS);
			TicketMessageRequest request = new TicketMessageRequest("Admin reply", null, false);

			when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
			when(messageRepository.save(any(TicketMessage.class))).thenReturn(testMessage);
			when(ticketRepository.save(any(SupportTicket.class))).thenReturn(testTicket);

			TicketMessageResponse response = supportService.addMessage(1L, 999L, true, request);

			assertThat(response).isNotNull();
		}

		@Test
		@DisplayName("Should throw when ticket is closed")
		void shouldThrowWhenTicketIsClosed() {
			testTicket.setStatus(SupportTicket.TicketStatus.CLOSED);
			TicketMessageRequest request = new TicketMessageRequest("Message", null, null);

			when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));

			assertThatThrownBy(() -> supportService.addMessage(1L, 10L, false, request))
					.isInstanceOf(IllegalStateException.class);
		}
	}

	@Nested
	@DisplayName("Ticket Status Tests")
	class TicketStatusTests {

		@Test
		@DisplayName("Should assign ticket")
		void shouldAssignTicket() {
			when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
			when(ticketRepository.save(any(SupportTicket.class))).thenReturn(testTicket);
			when(messageRepository.countByTicketId(1L)).thenReturn(1L);

			TicketResponse response = supportService.assignTicket(1L, 50L);

			assertThat(response).isNotNull();
			verify(ticketRepository).save(any(SupportTicket.class));
		}

		@Test
		@DisplayName("Should resolve ticket")
		void shouldResolveTicket() {
			when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
			when(ticketRepository.save(any(SupportTicket.class))).thenReturn(testTicket);
			when(messageRepository.countByTicketId(1L)).thenReturn(1L);

			TicketResponse response = supportService.resolveTicket(1L);

			assertThat(response).isNotNull();
		}

		@Test
		@DisplayName("Should close ticket")
		void shouldCloseTicket() {
			when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
			when(ticketRepository.save(any(SupportTicket.class))).thenReturn(testTicket);
			when(messageRepository.countByTicketId(1L)).thenReturn(1L);

			TicketResponse response = supportService.closeTicket(1L);

			assertThat(response).isNotNull();
		}

		@Test
		@DisplayName("Should reopen ticket")
		void shouldReopenTicket() {
			testTicket.setStatus(SupportTicket.TicketStatus.RESOLVED);
			when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
			when(ticketRepository.save(any(SupportTicket.class))).thenReturn(testTicket);
			when(messageRepository.countByTicketId(1L)).thenReturn(1L);

			TicketResponse response = supportService.reopenTicket(1L);

			assertThat(response).isNotNull();
		}
	}
}
