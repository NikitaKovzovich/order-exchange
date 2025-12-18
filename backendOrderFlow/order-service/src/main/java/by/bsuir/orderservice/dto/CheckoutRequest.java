package by.bsuir.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

/**
 * Запрос на оформление заказа (checkout)
 */
public record CheckoutRequest(
	@NotBlank(message = "Delivery address is required")
	String deliveryAddress,

	LocalDate desiredDeliveryDate,

	String notes
) {}
