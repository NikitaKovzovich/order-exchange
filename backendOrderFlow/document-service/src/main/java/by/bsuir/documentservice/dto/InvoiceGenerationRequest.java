package by.bsuir.documentservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record InvoiceGenerationRequest(
		@NotNull(message = "Order ID is required")
		Long orderId,

		@NotBlank(message = "Order number is required")
		String orderNumber,

		@NotNull(message = "Document date is required")
		LocalDate documentDate,

		@NotNull(message = "Seller info is required")
		@Valid
		CompanyInfoDto seller,

		@NotNull(message = "Buyer info is required")
		@Valid
		CompanyInfoDto buyer,

		@NotEmpty(message = "Items are required")
		@Valid
		List<ProductItemDto> items,

		BigDecimal totalWithoutVat,

		BigDecimal totalVat,

		BigDecimal totalWithVat,

		String paymentTerms,

		String bankDetails
) {}
