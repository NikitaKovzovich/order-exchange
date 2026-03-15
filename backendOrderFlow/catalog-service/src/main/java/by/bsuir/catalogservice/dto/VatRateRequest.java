package by.bsuir.catalogservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record VatRateRequest(
	@NotNull(message = "Rate percentage is required")
	@DecimalMin(value = "0", message = "Rate percentage must be non-negative")
	BigDecimal ratePercentage,

	@NotBlank(message = "Description is required")
	@Size(max = 50, message = "Description must not exceed 50 characters")
	String description
) {}
