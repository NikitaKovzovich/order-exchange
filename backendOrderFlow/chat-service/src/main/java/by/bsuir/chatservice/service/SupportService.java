package by.bsuir.chatservice.service;

import by.bsuir.chatservice.dto.*;
import by.bsuir.chatservice.entity.SupportTicket;
import by.bsuir.chatservice.entity.TicketMessage;
import by.bsuir.chatservice.exception.AccessDeniedException;
import by.bsuir.chatservice.exception.ResourceNotFoundException;
import by.bsuir.chatservice.repository.SupportTicketRepository;
import by.bsuir.chatservice.repository.TicketMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupportService {

	private static final String SUPPORT_EXCHANGE = "support.events";

	private final SupportTicketRepository ticketRepository;
	private final TicketMessageRepository messageRepository;
	private final RabbitTemplate rabbitTemplate;

	@Transactional
	public TicketResponse createTicket(Long companyId, Long userId, CreateTicketRequest request) {
		SupportTicket ticket = SupportTicket.builder()
				.requesterCompanyId(companyId)
				.requesterUserId(userId)
				.subject(request.subject())
				.category(request.category() != null ? request.category() : SupportTicket.TicketCategory.OTHER)
				.priority(request.priority() != null ? request.priority() : SupportTicket.TicketPriority.NORMAL)
				.build();

		ticket = ticketRepository.save(ticket);

		TicketMessage initialMessage = TicketMessage.builder()
				.ticket(ticket)
				.senderId(userId)
				.messageText(request.message())
				.isAdminReply(false)
				.build();

		messageRepository.save(initialMessage);
		log.info("Created support ticket {} for company {}", ticket.getId(), companyId);

		publishEvent("TicketCreated", Map.of(
				"ticketId", ticket.getId(),
				"companyId", companyId,
				"subject", ticket.getSubject(),
				"priority", ticket.getPriority().name()
		));

		return toResponse(ticket, 1L, toMessageResponse(initialMessage));
	}

	@Transactional(readOnly = true)
	public TicketResponse getTicket(Long ticketId, Long userId, boolean isAdmin) {
		SupportTicket ticket = ticketRepository.findById(ticketId)
				.orElseThrow(() -> new ResourceNotFoundException("SupportTicket", "id", ticketId));

		if (!isAdmin && !ticket.getRequesterUserId().equals(userId)) {
			throw new AccessDeniedException("Access denied to this ticket");
		}

		long messageCount = messageRepository.countByTicketId(ticketId);
		TicketMessage lastMessage = getLastMessage(ticketId, isAdmin);

		return toResponse(ticket, messageCount, lastMessage != null ? toMessageResponse(lastMessage) : null);
	}

	@Transactional(readOnly = true)
	public PageResponse<TicketResponse> getUserTickets(Long companyId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		Page<SupportTicket> tickets = ticketRepository.findByRequesterCompanyId(companyId, pageable);

		List<TicketResponse> responses = tickets.getContent().stream()
				.map(ticket -> {
					long count = messageRepository.countByTicketId(ticket.getId());
					TicketMessage last = getLastMessage(ticket.getId(), false);
					return toResponse(ticket, count, last != null ? toMessageResponse(last) : null);
				})
				.toList();

		return new PageResponse<>(responses, tickets.getNumber(), tickets.getSize(),
				tickets.getTotalElements(), tickets.getTotalPages(),
				tickets.isFirst(), tickets.isLast());
	}

	@Transactional(readOnly = true)
	public PageResponse<TicketResponse> getAllTickets(String status, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		Page<SupportTicket> tickets;

		if (status != null && !status.isEmpty()) {
			tickets = ticketRepository.findByStatus(SupportTicket.TicketStatus.valueOf(status.toUpperCase()), pageable);
		} else {
			tickets = ticketRepository.findAllOpen(pageable);
		}

		List<TicketResponse> responses = tickets.getContent().stream()
				.map(ticket -> {
					long count = messageRepository.countByTicketId(ticket.getId());
					TicketMessage last = getLastMessage(ticket.getId(), true);
					return toResponse(ticket, count, last != null ? toMessageResponse(last) : null);
				})
				.toList();

		return new PageResponse<>(responses, tickets.getNumber(), tickets.getSize(),
				tickets.getTotalElements(), tickets.getTotalPages(),
				tickets.isFirst(), tickets.isLast());
	}

	@Transactional(readOnly = true)
	public List<TicketMessageResponse> getTicketMessages(Long ticketId, Long userId, boolean isAdmin) {
		SupportTicket ticket = ticketRepository.findById(ticketId)
				.orElseThrow(() -> new ResourceNotFoundException("SupportTicket", "id", ticketId));

		if (!isAdmin && !ticket.getRequesterUserId().equals(userId)) {
			throw new AccessDeniedException("Access denied to this ticket");
		}

		List<TicketMessage> messages;
		if (isAdmin) {
			messages = messageRepository.findByTicketIdOrderBySentAtAsc(ticketId);
		} else {
			messages = messageRepository.findPublicMessagesByTicketId(ticketId);
		}

		return messages.stream().map(this::toMessageResponse).toList();
	}

	@Transactional
	public TicketMessageResponse addMessage(Long ticketId, Long senderId, boolean isAdmin, TicketMessageRequest request) {
		SupportTicket ticket = ticketRepository.findById(ticketId)
				.orElseThrow(() -> new ResourceNotFoundException("SupportTicket", "id", ticketId));

		if (!isAdmin && !ticket.getRequesterUserId().equals(senderId)) {
			throw new AccessDeniedException("Access denied to this ticket");
		}

		if (!ticket.isOpen()) {
			throw new IllegalStateException("Cannot add message to closed ticket");
		}

		TicketMessage message = TicketMessage.builder()
				.ticket(ticket)
				.senderId(senderId)
				.messageText(request.message())
				.attachmentKey(request.attachmentKey())
				.isAdminReply(isAdmin)
				.isInternalNote(isAdmin && Boolean.TRUE.equals(request.isInternalNote()))
				.build();

		message = messageRepository.save(message);

		if (isAdmin && !Boolean.TRUE.equals(request.isInternalNote())) {
			ticket.waitForCustomer();
			ticketRepository.save(ticket);
		} else if (!isAdmin) {
			if (ticket.getStatus() == SupportTicket.TicketStatus.WAITING_FOR_CUSTOMER) {
				ticket.setStatus(SupportTicket.TicketStatus.IN_PROGRESS);
				ticketRepository.save(ticket);
			}
		}

		log.info("Added message to ticket {} by user {}", ticketId, senderId);

		publishEvent("TicketMessageAdded", Map.of(
				"ticketId", ticketId,
				"senderId", senderId,
				"isAdmin", isAdmin
		));

		return toMessageResponse(message);
	}

	@Transactional
	public TicketResponse assignTicket(Long ticketId, Long adminId) {
		SupportTicket ticket = ticketRepository.findById(ticketId)
				.orElseThrow(() -> new ResourceNotFoundException("SupportTicket", "id", ticketId));

		ticket.assignTo(adminId);
		ticket = ticketRepository.save(ticket);

		log.info("Assigned ticket {} to admin {}", ticketId, adminId);

		long messageCount = messageRepository.countByTicketId(ticketId);
		return toResponse(ticket, messageCount, null);
	}

	@Transactional
	public TicketResponse resolveTicket(Long ticketId) {
		SupportTicket ticket = ticketRepository.findById(ticketId)
				.orElseThrow(() -> new ResourceNotFoundException("SupportTicket", "id", ticketId));

		ticket.resolve();
		ticket = ticketRepository.save(ticket);

		log.info("Resolved ticket {}", ticketId);

		publishEvent("TicketResolved", Map.of("ticketId", ticketId));

		long messageCount = messageRepository.countByTicketId(ticketId);
		return toResponse(ticket, messageCount, null);
	}

	@Transactional
	public TicketResponse closeTicket(Long ticketId) {
		SupportTicket ticket = ticketRepository.findById(ticketId)
				.orElseThrow(() -> new ResourceNotFoundException("SupportTicket", "id", ticketId));

		ticket.close();
		ticket = ticketRepository.save(ticket);

		log.info("Closed ticket {}", ticketId);

		long messageCount = messageRepository.countByTicketId(ticketId);
		return toResponse(ticket, messageCount, null);
	}

	@Transactional
	public TicketResponse reopenTicket(Long ticketId) {
		SupportTicket ticket = ticketRepository.findById(ticketId)
				.orElseThrow(() -> new ResourceNotFoundException("SupportTicket", "id", ticketId));

		ticket.reopen();
		ticket = ticketRepository.save(ticket);

		log.info("Reopened ticket {}", ticketId);

		long messageCount = messageRepository.countByTicketId(ticketId);
		return toResponse(ticket, messageCount, null);
	}

	private TicketMessage getLastMessage(Long ticketId, boolean includeInternal) {
		List<TicketMessage> messages;
		if (includeInternal) {
			messages = messageRepository.findByTicketIdOrderBySentAtAsc(ticketId);
		} else {
			messages = messageRepository.findPublicMessagesByTicketId(ticketId);
		}
		return messages.isEmpty() ? null : messages.getLast();
	}

	private void publishEvent(String eventType, Map<String, Object> payload) {
		try {
			rabbitTemplate.convertAndSend(SUPPORT_EXCHANGE, eventType.toLowerCase(), payload);
		} catch (Exception e) {
			log.warn("Failed to publish event {}: {}", eventType, e.getMessage());
		}
	}

	private TicketResponse toResponse(SupportTicket ticket, long messageCount, TicketMessageResponse lastMessage) {
		return new TicketResponse(
				ticket.getId(),
				ticket.getRequesterCompanyId(),
				ticket.getRequesterUserId(),
				ticket.getSubject(),
				ticket.getStatus().name(),
				ticket.getPriority().name(),
				ticket.getCategory() != null ? ticket.getCategory().name() : null,
				ticket.getAssignedAdminId(),
				ticket.getCreatedAt(),
				ticket.getUpdatedAt(),
				ticket.getResolvedAt(),
				messageCount,
				lastMessage
		);
	}

	private TicketMessageResponse toMessageResponse(TicketMessage message) {
		return new TicketMessageResponse(
				message.getId(),
				message.getTicket().getId(),
				message.getSenderId(),
				message.getIsAdminReply(),
				message.getMessageText(),
				message.getAttachmentKey(),
				message.getIsInternalNote(),
				message.getSentAt()
		);
	}
}
