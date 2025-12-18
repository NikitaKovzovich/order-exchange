package by.bsuir.orderservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
		Long id,
		String orderNumber,
		Long supplierId,
		Long customerId,
		String statusCode,
		String statusName,
		String deliveryAddress,
		LocalDate desiredDeliveryDate,
		BigDecimal totalAmount,
		BigDecimal vatAmount,
		List<OrderItemResponse> items,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {}
