package by.bsuir.orderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record DiscrepancyRequest(
		@NotEmpty(message = "At least one discrepancy item is required")
		@Valid
		List<DiscrepancyItemRequest> items,

		String notes
) {}
