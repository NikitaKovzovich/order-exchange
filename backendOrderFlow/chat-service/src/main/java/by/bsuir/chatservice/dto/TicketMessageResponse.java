package by.bsuir.chatservice.dto;

import java.time.LocalDateTime;

public record TicketMessageResponse(
		Long id,
		Long ticketId,
		Long senderId,
		Boolean isAdminReply,
		String messageText,
		String attachmentKey,
		Boolean isInternalNote,
		LocalDateTime sentAt
) {}
