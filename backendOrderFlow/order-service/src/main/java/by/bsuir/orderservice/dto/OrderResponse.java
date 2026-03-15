package by.bsuir.orderservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
		Long id,
		String orderNumber,
		Long supplierId,
		String supplierName,
		Long customerId,
		String customerName,
		String statusCode,
		String statusName,
		String deliveryAddress,
		LocalDate desiredDeliveryDate,
		BigDecimal totalAmount,
		BigDecimal vatAmount,
		String contractNumber,
		LocalDate contractDate,
		LocalDate contractEndDate,
		Boolean ttnGenerated,
		List<OrderItemResponse> items,
		List<OrderHistoryResponse> history,
		List<OrderDocumentResponse> documents,
		List<DiscrepancyResponse> discrepancies,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {}
