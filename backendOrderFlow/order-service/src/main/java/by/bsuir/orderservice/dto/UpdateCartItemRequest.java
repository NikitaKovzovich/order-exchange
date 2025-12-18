package by.bsuir.orderservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Запрос на обновление количества товара в корзине
 */
public record UpdateCartItemRequest(
	@NotNull(message = "Quantity is required")
	@Min(value = 0, message = "Quantity must be at least 0")
	Integer quantity
) {}
