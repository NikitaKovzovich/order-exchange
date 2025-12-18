package by.bsuir.chatservice.dto;

import java.time.LocalDateTime;

public record TicketResponse(
		Long id,
		Long requesterCompanyId,
		Long requesterUserId,
		String subject,
		String status,
		String priority,
		String category,
		Long assignedAdminId,
		LocalDateTime createdAt,
		LocalDateTime updatedAt,
		LocalDateTime resolvedAt,
		long messageCount,
		TicketMessageResponse lastMessage
) {}
