package by.bsuir.chatservice.dto;

import jakarta.validation.constraints.NotBlank;

public record TicketMessageRequest(
		@NotBlank(message = "Message is required")
		String message,

		String attachmentKey,

		Boolean isInternalNote
) {}
