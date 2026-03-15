package by.bsuir.chatservice.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TicketMessageResponse(
		Long id,
		Long ticketId,
		Long senderId,
		Boolean isAdminReply,
		String messageText,
		List<String> attachmentKeys,
		String attachmentKey,
		Boolean isInternalNote,
		LocalDateTime sentAt
) {
	public TicketMessageResponse(Long id, Long ticketId, Long senderId, Boolean isAdminReply,
			String messageText, String attachmentKey, Boolean isInternalNote, LocalDateTime sentAt) {
		this(id, ticketId, senderId, isAdminReply, messageText,
				attachmentKey != null && !attachmentKey.isBlank() ? List.of(attachmentKey) : List.of(),
				attachmentKey, isInternalNote, sentAt);
	}
}
