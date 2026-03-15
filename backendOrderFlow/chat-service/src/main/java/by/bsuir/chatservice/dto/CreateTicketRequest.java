package by.bsuir.chatservice.dto;

import by.bsuir.chatservice.entity.SupportTicket;
import jakarta.validation.constraints.NotBlank;

import java.util.Collections;
import java.util.List;

public record CreateTicketRequest(
		@NotBlank(message = "Subject is required")
		String subject,

		@NotBlank(message = "Message is required")
		String message,

		SupportTicket.TicketCategory category,

		SupportTicket.TicketPriority priority,

		List<String> attachmentKeys,

		String attachmentKey
) {
	public CreateTicketRequest(String subject, String message,
			SupportTicket.TicketCategory category,
			SupportTicket.TicketPriority priority) {
		this(subject, message, category, priority, List.of(), null);
	}

	public CreateTicketRequest(String subject, String message,
			SupportTicket.TicketCategory category,
			SupportTicket.TicketPriority priority,
			List<String> attachmentKeys) {
		this(subject, message, category, priority, attachmentKeys, null);
	}

	public List<String> resolvedAttachmentKeys() {
		if (attachmentKeys != null && !attachmentKeys.isEmpty()) {
			return attachmentKeys;
		}
		if (attachmentKey != null && !attachmentKey.isBlank()) {
			return List.of(attachmentKey);
		}
		return Collections.emptyList();
	}
}
