package by.bsuir.chatservice.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Collections;
import java.util.List;

public record TicketMessageRequest(
		@NotBlank(message = "Message is required")
		String message,

		List<String> attachmentKeys,

		String attachmentKey,

		Boolean isInternalNote
) {
	public TicketMessageRequest(String message, String attachmentKey, Boolean isInternalNote) {
		this(message, List.of(), attachmentKey, isInternalNote);
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
