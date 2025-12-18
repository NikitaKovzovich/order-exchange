package by.bsuir.orderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record CreateOrderRequest(
		@NotNull(message = "Supplier ID is required")
		Long supplierId,

		@NotBlank(message = "Delivery address is required")
		String deliveryAddress,

		LocalDate desiredDeliveryDate,

		@NotEmpty(message = "Order must contain at least one item")
		@Valid
		List<OrderItemRequest> items
) {}
