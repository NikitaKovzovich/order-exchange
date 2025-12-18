package by.bsuir.chatservice.service;

import by.bsuir.chatservice.dto.*;
import by.bsuir.chatservice.entity.ChatChannel;
import by.bsuir.chatservice.entity.Message;
import by.bsuir.chatservice.exception.AccessDeniedException;
import by.bsuir.chatservice.exception.DuplicateResourceException;
import by.bsuir.chatservice.exception.ResourceNotFoundException;
import by.bsuir.chatservice.repository.ChatChannelRepository;
import by.bsuir.chatservice.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

	@Mock
	private ChatChannelRepository channelRepository;

	@Mock
	private MessageRepository messageRepository;

	@InjectMocks
	private ChatService chatService;

	private ChatChannel testChannel;

	@BeforeEach
	void setUp() {
		testChannel = ChatChannel.builder()
				.id(1L)
				.orderId(100L)
				.supplierUserId(1L)
				.customerUserId(2L)
				.channelName("Test Channel")
				.isActive(true)
				.createdAt(LocalDateTime.now())
				.build();
	}

	@Nested
	@DisplayName("Create Channel Tests")
	class CreateChannelTests {

		@Test
		@DisplayName("Should create channel successfully")
		void shouldCreateChannelSuccessfully() {
			CreateChannelRequest request = new CreateChannelRequest(100L, 1L, 2L, "Test Channel");

			when(channelRepository.existsByOrderId(100L)).thenReturn(false);
			when(channelRepository.save(any(ChatChannel.class))).thenReturn(testChannel);

			ChatChannelResponse response = chatService.createChannel(request);

			assertThat(response.orderId()).isEqualTo(100L);
			assertThat(response.supplierUserId()).isEqualTo(1L);
			verify(channelRepository).save(any(ChatChannel.class));
		}

		@Test
		@DisplayName("Should throw exception when channel exists")
		void shouldThrowExceptionWhenChannelExists() {
			CreateChannelRequest request = new CreateChannelRequest(100L, 1L, 2L, null);

			when(channelRepository.existsByOrderId(100L)).thenReturn(true);

			assertThatThrownBy(() -> chatService.createChannel(request))
					.isInstanceOf(DuplicateResourceException.class);
		}
	}

	@Nested
	@DisplayName("Get Channel Tests")
	class GetChannelTests {

		@Test
		@DisplayName("Should get channel by order ID")
		void shouldGetChannelByOrderId() {
			when(channelRepository.findByOrderId(100L)).thenReturn(Optional.of(testChannel));
			when(messageRepository.countUnreadMessages(1L, 1L)).thenReturn(5L);

			ChatChannelResponse response = chatService.getChannelByOrderId(100L, 1L);

			assertThat(response.orderId()).isEqualTo(100L);
			assertThat(response.unreadCount()).isEqualTo(5L);
		}

		@Test
		@DisplayName("Should throw when channel not found")
		void shouldThrowWhenChannelNotFound() {
			when(channelRepository.findByOrderId(999L)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> chatService.getChannelByOrderId(999L, 1L))
					.isInstanceOf(ResourceNotFoundException.class);
		}

		@Test
		@DisplayName("Should throw when user is not participant")
		void shouldThrowWhenUserIsNotParticipant() {
			when(channelRepository.findByOrderId(100L)).thenReturn(Optional.of(testChannel));

			assertThatThrownBy(() -> chatService.getChannelByOrderId(100L, 999L))
					.isInstanceOf(AccessDeniedException.class);
		}
	}

	@Nested
	@DisplayName("Get User Channels Tests")
	class GetUserChannelsTests {

		@Test
		@DisplayName("Should get user channels")
		void shouldGetUserChannels() {
			when(channelRepository.findActiveByUserId(1L)).thenReturn(List.of(testChannel));
			when(messageRepository.countUnreadMessages(1L, 1L)).thenReturn(0L);

			List<ChatChannelResponse> response = chatService.getUserChannels(1L);

			assertThat(response).hasSize(1);
			assertThat(response.get(0).orderId()).isEqualTo(100L);
		}
	}

	@Nested
	@DisplayName("Send Message Tests")
	class SendMessageTests {

		@Test
		@DisplayName("Should send message successfully")
		void shouldSendMessageSuccessfully() {
			SendMessageRequest request = new SendMessageRequest("Hello!", null);
			Message savedMessage = Message.builder()
					.id(1L)
					.channel(testChannel)
					.senderId(1L)
					.messageText("Hello!")
					.messageType(Message.MessageType.TEXT)
					.sentAt(LocalDateTime.now())
					.isRead(false)
					.build();

			when(channelRepository.findByOrderId(100L)).thenReturn(Optional.of(testChannel));
			when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);

			MessageResponse response = chatService.sendMessage(100L, 1L, request);

			assertThat(response.messageText()).isEqualTo("Hello!");
			assertThat(response.messageType()).isEqualTo("TEXT");
		}

		@Test
		@DisplayName("Should throw when sending to inactive channel")
		void shouldThrowWhenSendingToInactiveChannel() {
			testChannel.setIsActive(false);
			SendMessageRequest request = new SendMessageRequest("Hello!", null);

			when(channelRepository.findByOrderId(100L)).thenReturn(Optional.of(testChannel));

			assertThatThrownBy(() -> chatService.sendMessage(100L, 1L, request))
					.isInstanceOf(IllegalStateException.class);
		}

		@Test
		@DisplayName("Should detect file attachment")
		void shouldDetectFileAttachment() {
			SendMessageRequest request = new SendMessageRequest("See attached", "files/doc.pdf");
			Message savedMessage = Message.builder()
					.id(1L)
					.channel(testChannel)
					.senderId(1L)
					.messageText("See attached")
					.attachmentKey("files/doc.pdf")
					.messageType(Message.MessageType.ATTACHMENT)
					.sentAt(LocalDateTime.now())
					.build();

			when(channelRepository.findByOrderId(100L)).thenReturn(Optional.of(testChannel));
			when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);

			MessageResponse response = chatService.sendMessage(100L, 1L, request);

			assertThat(response.messageType()).isEqualTo("ATTACHMENT");
			assertThat(response.attachmentKey()).isEqualTo("files/doc.pdf");
		}
	}

	@Nested
	@DisplayName("Get Messages Tests")
	class GetMessagesTests {

		@Test
		@DisplayName("Should get paginated messages")
		void shouldGetPaginatedMessages() {
			Message message = Message.builder()
					.id(1L)
					.channel(testChannel)
					.senderId(1L)
					.messageText("Test message")
					.messageType(Message.MessageType.TEXT)
					.sentAt(LocalDateTime.now())
					.build();

			Page<Message> messagePage = new PageImpl<>(List.of(message), PageRequest.of(0, 50), 1);

			when(channelRepository.findByOrderId(100L)).thenReturn(Optional.of(testChannel));
			when(messageRepository.findByChannelIdOrderBySentAtDesc(eq(1L), any())).thenReturn(messagePage);

			PageResponse<MessageResponse> response = chatService.getMessages(100L, 1L, 0, 50);

			assertThat(response.content()).hasSize(1);
			assertThat(response.totalElements()).isEqualTo(1);
		}
	}

	@Nested
	@DisplayName("Mark As Read Tests")
	class MarkAsReadTests {

		@Test
		@DisplayName("Should mark messages as read")
		void shouldMarkMessagesAsRead() {
			when(channelRepository.findByOrderId(100L)).thenReturn(Optional.of(testChannel));
			when(messageRepository.markAsRead(1L, 1L)).thenReturn(3);

			chatService.markMessagesAsRead(100L, 1L);

			verify(messageRepository).markAsRead(1L, 1L);
		}
	}

	@Nested
	@DisplayName("Deactivate Channel Tests")
	class DeactivateChannelTests {

		@Test
		@DisplayName("Should deactivate channel")
		void shouldDeactivateChannel() {
			when(channelRepository.findByOrderId(100L)).thenReturn(Optional.of(testChannel));
			when(channelRepository.save(any(ChatChannel.class))).thenReturn(testChannel);

			chatService.deactivateChannel(100L);

			assertThat(testChannel.getIsActive()).isFalse();
			verify(channelRepository).save(testChannel);
		}
	}
}
