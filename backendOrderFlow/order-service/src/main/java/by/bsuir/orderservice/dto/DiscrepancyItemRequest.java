package by.bsuir.orderservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DiscrepancyItemRequest(
		@NotNull(message = "Order item ID is required")
		Long orderItemId,

		@NotNull(message = "Actual quantity is required")
		@Min(value = 0, message = "Actual quantity must be non-negative")
		Integer actualQuantity,

		String reason
) {}
