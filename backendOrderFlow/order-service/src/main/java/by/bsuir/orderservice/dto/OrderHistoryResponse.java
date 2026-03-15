package by.bsuir.orderservice.dto;

import java.time.LocalDateTime;

public record OrderHistoryResponse(
		Long id,
		String eventDescription,
		String previousStatus,
		String previousStatusName,
		String newStatus,
		String newStatusName,
		Long userId,
		LocalDateTime timestamp
) {}
