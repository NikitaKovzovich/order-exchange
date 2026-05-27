package by.bsuir.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record OrderItemResponse(
		Long id,
		Long productId,
		String productName,
		String productSku,
		Integer quantity,
		BigDecimal unitPrice,
		BigDecimal vatRate,
		@JsonProperty("totalPrice") BigDecimal lineTotal,
		@JsonProperty("vatAmount") BigDecimal lineVat
) {}
