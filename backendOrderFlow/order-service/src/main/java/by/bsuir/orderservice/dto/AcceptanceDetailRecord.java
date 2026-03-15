package by.bsuir.orderservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;




public record AcceptanceDetailRecord(
		LocalDateTime acceptanceDate,
		Long productId,
		String productName,
		String productSku,
		Long supplierId,
		String supplierName,
		Integer quantity,
		BigDecimal unitPrice,
		BigDecimal totalPrice,
		Long orderId,
		String orderNumber
) {}
