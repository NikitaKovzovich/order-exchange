package by.bsuir.orderservice.dto;

import java.math.BigDecimal;




public record AcceptanceSummaryRecord(
		Long productId,
		String productName,
		String productSku,
		Integer totalQuantity,
		BigDecimal totalAmount,
		long uniqueSuppliers
) {}
