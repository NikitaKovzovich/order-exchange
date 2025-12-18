package by.bsuir.orderservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Запрос на добавление товара в корзину
 */
public record AddToCartRequest(
	@NotNull(message = "Product ID is required")
	Long productId,

	@NotNull(message = "Supplier ID is required")
	Long supplierId,

	@NotNull(message = "Product name is required")
	String productName,

	String productSku,

	@NotNull(message = "Quantity is required")
	@Min(value = 1, message = "Quantity must be at least 1")
	Integer quantity,

	@NotNull(message = "Unit price is required")
	BigDecimal unitPrice,

	BigDecimal vatRate
) {}
