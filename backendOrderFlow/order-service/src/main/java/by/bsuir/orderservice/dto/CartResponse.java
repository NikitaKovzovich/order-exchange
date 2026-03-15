package by.bsuir.orderservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;




public record CartResponse(
	Long id,
	Long customerId,
	List<CartItemResponse> items,
	int itemCount,
	BigDecimal totalAmount,
	BigDecimal totalVat,
	List<Long> supplierIds,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {}
