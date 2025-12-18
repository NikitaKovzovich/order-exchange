package by.bsuir.orderservice.dto;

import jakarta.validation.constraints.NotBlank;

public record OrderActionRequest(
		@NotBlank(message = "Reason is required")
		String reason
) {}
