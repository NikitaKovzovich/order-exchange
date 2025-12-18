package by.bsuir.orderservice.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentProofRequest(
		@NotBlank(message = "Document key is required")
		String documentKey,

		String paymentReference,

		String notes
) {}
