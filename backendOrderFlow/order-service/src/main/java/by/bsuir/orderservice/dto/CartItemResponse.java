package by.bsuir.orderservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;




public record CartItemResponse(
	Long id,
	Long productId,
	Long supplierId,
	String productName,
	String productSku,
	Integer quantity,
	BigDecimal unitPrice,
	BigDecimal vatRate,
	BigDecimal totalPrice,
	BigDecimal vatAmount,
	LocalDateTime addedAt
) {}
