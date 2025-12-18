package by.bsuir.chatservice.dto;

import java.time.LocalDateTime;

public record ChatChannelResponse(
		Long id,
		Long orderId,
		Long supplierUserId,
		Long customerUserId,
		String channelName,
		Boolean isActive,
		LocalDateTime createdAt,
		int messageCount,
		long unreadCount,
		MessageResponse lastMessage
) {}
