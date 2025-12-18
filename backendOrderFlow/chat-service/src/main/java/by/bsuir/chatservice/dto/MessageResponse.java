package by.bsuir.chatservice.dto;

import java.time.LocalDateTime;

public record MessageResponse(
		Long id,
		Long channelId,
		Long senderId,
		String messageText,
		String messageType,
		String attachmentKey,
		Boolean isRead,
		LocalDateTime sentAt,
		LocalDateTime readAt
) {}
