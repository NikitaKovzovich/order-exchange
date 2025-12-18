package by.bsuir.documentservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DiscrepancyActRequest(
		@NotNull(message = "Order ID is required")
		Long orderId,

		@NotBlank(message = "Order number is required")
		String orderNumber,

		@NotBlank(message = "TTN number is required")
		String ttnNumber,

		@NotNull(message = "TTN date is required")
		LocalDate ttnDate,

		@NotNull(message = "Act date is required")
		LocalDate actDate,

		@NotNull(message = "Supplier is required")
		@Valid
		CompanyInfoDto supplier,

		@NotNull(message = "Buyer is required")
		@Valid
		CompanyInfoDto buyer,

		@NotEmpty(message = "Discrepancy items are required")
		@Valid
		List<DiscrepancyItemDto> items,

		BigDecimal totalDiscrepancyAmount,

		List<String> commissionMembers,

		String conclusion,

		String resolutionProposal,

		String notes
) {}
