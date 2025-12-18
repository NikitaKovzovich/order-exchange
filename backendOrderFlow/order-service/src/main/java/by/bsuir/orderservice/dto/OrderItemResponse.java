package by.bsuir.orderservice.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
		Long id,
		Long productId,
		String productName,
		String productSku,
		Integer quantity,
		BigDecimal unitPrice,
		BigDecimal vatRate,
		BigDecimal lineTotal,
		BigDecimal lineVat
) {}
