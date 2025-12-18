package by.bsuir.chatservice.dto;

import by.bsuir.chatservice.entity.SupportTicket;
import jakarta.validation.constraints.NotBlank;

public record CreateTicketRequest(
		@NotBlank(message = "Subject is required")
		String subject,

		@NotBlank(message = "Message is required")
		String message,

		SupportTicket.TicketCategory category,

		SupportTicket.TicketPriority priority
) {}
