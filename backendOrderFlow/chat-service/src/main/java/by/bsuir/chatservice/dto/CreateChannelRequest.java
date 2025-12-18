package by.bsuir.chatservice.dto;

import jakarta.validation.constraints.NotNull;

public record CreateChannelRequest(
		@NotNull(message = "Order ID is required")
		Long orderId,

		@NotNull(message = "Supplier user ID is required")
		Long supplierUserId,

		@NotNull(message = "Customer user ID is required")
		Long customerUserId,

		String channelName
) {}
