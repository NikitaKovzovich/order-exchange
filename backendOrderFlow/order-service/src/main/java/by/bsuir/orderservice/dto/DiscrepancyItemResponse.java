package by.bsuir.orderservice.dto;

import java.math.BigDecimal;

public record DiscrepancyItemResponse(
		Long orderItemId,
		String productName,
		String productSku,
		Integer expectedQuantity,
		Integer actualQuantity,
		Integer discrepancy,
		BigDecimal unitPrice,
		BigDecimal discrepancyAmount,
		String reason
) {}
