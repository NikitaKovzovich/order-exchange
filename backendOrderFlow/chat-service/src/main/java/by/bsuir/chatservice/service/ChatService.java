package by.bsuir.chatservice.service;

import by.bsuir.chatservice.dto.*;
import by.bsuir.chatservice.entity.ChatChannel;
import by.bsuir.chatservice.entity.Message;
import by.bsuir.chatservice.exception.AccessDeniedException;
import by.bsuir.chatservice.exception.DuplicateResourceException;
import by.bsuir.chatservice.exception.ResourceNotFoundException;
import by.bsuir.chatservice.repository.ChatChannelRepository;
import by.bsuir.chatservice.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

	private final ChatChannelRepository channelRepository;
	private final MessageRepository messageRepository;

	@Transactional
	public ChatChannelResponse createChannel(CreateChannelRequest request) {
		if (channelRepository.existsByOrderId(request.orderId())) {
			throw new DuplicateResourceException("ChatChannel", "orderId", request.orderId());
		}

		ChatChannel channel = ChatChannel.builder()
				.orderId(request.orderId())
				.supplierUserId(request.supplierUserId())
				.customerUserId(request.customerUserId())
				.channelName(request.channelName() != null ? request.channelName() : "Order #" + request.orderId())
				.build();

		channel = channelRepository.save(channel);
		log.info("Created chat channel for order {}", request.orderId());

		return toChannelResponse(channel, 0L);
	}

	@Transactional(readOnly = true)
	public ChatChannelResponse getChannelByOrderId(Long orderId, Long userId) {
		ChatChannel channel = channelRepository.findByOrderId(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("ChatChannel", "orderId", orderId));

		validateParticipant(channel, userId);
		long unreadCount = messageRepository.countUnreadMessages(channel.getId(), userId);

		return toChannelResponse(channel, unreadCount);
	}

	@Transactional(readOnly = true)
	public List<ChatChannelResponse> getUserChannels(Long userId) {
		List<ChatChannel> channels = channelRepository.findActiveByUserId(userId);

		return channels.stream()
				.map(channel -> {
					long unreadCount = messageRepository.countUnreadMessages(channel.getId(), userId);
					return toChannelResponse(channel, unreadCount);
				})
				.toList();
	}

	@Transactional(readOnly = true)
	public PageResponse<MessageResponse> getMessages(Long orderId, Long userId, int page, int size) {
		ChatChannel channel = channelRepository.findByOrderId(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("ChatChannel", "orderId", orderId));

		validateParticipant(channel, userId);

		Pageable pageable = PageRequest.of(page, size);
		Page<Message> messagePage = messageRepository.findByChannelIdOrderBySentAtDesc(channel.getId(), pageable);

		List<MessageResponse> responses = messagePage.getContent().stream()
				.map(this::toMessageResponse)
				.toList();

		return new PageResponse<>(
				responses,
				messagePage.getNumber(),
				messagePage.getSize(),
				messagePage.getTotalElements(),
				messagePage.getTotalPages(),
				messagePage.isFirst(),
				messagePage.isLast()
		);
	}

	@Transactional
	public MessageResponse sendMessage(Long orderId, Long senderId, SendMessageRequest request) {
		ChatChannel channel = channelRepository.findByOrderId(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("ChatChannel", "orderId", orderId));

		validateParticipant(channel, senderId);

		if (!channel.getIsActive()) {
			throw new IllegalStateException("Cannot send message to inactive channel");
		}

		Message message = Message.builder()
				.channel(channel)
				.senderId(senderId)
				.messageText(request.messageText())
				.attachmentKey(request.attachmentKey())
				.messageType(request.attachmentKey() != null ? Message.MessageType.ATTACHMENT : Message.MessageType.TEXT)
				.build();

		message = messageRepository.save(message);
		log.info("Message sent in channel {} by user {}", channel.getId(), senderId);

		return toMessageResponse(message);
	}

	@Transactional
	public void markMessagesAsRead(Long orderId, Long userId) {
		ChatChannel channel = channelRepository.findByOrderId(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("ChatChannel", "orderId", orderId));

		validateParticipant(channel, userId);
		int count = messageRepository.markAsRead(channel.getId(), userId);
		log.debug("Marked {} messages as read in channel {}", count, channel.getId());
	}

	@Transactional
	public void deactivateChannel(Long orderId) {
		ChatChannel channel = channelRepository.findByOrderId(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("ChatChannel", "orderId", orderId));

		channel.deactivate();
		channelRepository.save(channel);
		log.info("Deactivated chat channel for order {}", orderId);
	}

	private void validateParticipant(ChatChannel channel, Long userId) {
		if (!channel.isParticipant(userId)) {
			throw new AccessDeniedException("User is not a participant of this channel");
		}
	}

	private ChatChannelResponse toChannelResponse(ChatChannel channel, long unreadCount) {
		Message lastMessage = channel.getLastMessage();
		return new ChatChannelResponse(
				channel.getId(),
				channel.getOrderId(),
				channel.getSupplierUserId(),
				channel.getCustomerUserId(),
				channel.getChannelName(),
				channel.getIsActive(),
				channel.getCreatedAt(),
				channel.getMessageCount(),
				unreadCount,
				lastMessage != null ? toMessageResponse(lastMessage) : null
		);
	}

	private MessageResponse toMessageResponse(Message message) {
		return new MessageResponse(
				message.getId(),
				message.getChannel().getId(),
				message.getSenderId(),
				message.getMessageText(),
				message.getMessageType().name(),
				message.getAttachmentKey(),
				message.getIsRead(),
				message.getSentAt(),
				message.getReadAt()
		);
	}
}
