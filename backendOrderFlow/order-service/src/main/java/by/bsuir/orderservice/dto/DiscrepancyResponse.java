package by.bsuir.orderservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record DiscrepancyResponse(
		Long id,
		Long orderId,
		String orderNumber,
		List<DiscrepancyItemResponse> items,
		BigDecimal totalDiscrepancyAmount,
		String status,
		String notes,
		LocalDateTime createdAt,
		Long createdBy
) {}
