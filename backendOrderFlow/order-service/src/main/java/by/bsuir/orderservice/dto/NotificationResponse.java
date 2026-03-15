package by.bsuir.orderservice.dto;

import java.time.LocalDateTime;

public record NotificationResponse(
		Long id,
		Long recipientId,
		String type,
		String typeDisplayName,
		String title,
		String message,
		Long orderId,
		String orderNumber,
		boolean read,
		LocalDateTime createdAt,
		LocalDateTime readAt
) {}
