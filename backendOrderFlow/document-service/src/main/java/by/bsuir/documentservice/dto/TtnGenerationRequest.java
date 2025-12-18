package by.bsuir.documentservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TtnGenerationRequest(
		@NotNull(message = "Order ID is required")
		Long orderId,

		@NotBlank(message = "Order number is required")
		String orderNumber,

		@NotNull(message = "Document date is required")
		LocalDate documentDate,

		String series,

		@NotNull(message = "Shipper is required")
		@Valid
		CompanyInfoDto shipper,

		@NotNull(message = "Consignee is required")
		@Valid
		CompanyInfoDto consignee,

		CompanyInfoDto payer,

		@NotBlank(message = "Loading point is required")
		String loadingPoint,

		@NotBlank(message = "Unloading point is required")
		String unloadingPoint,

		@Valid
		TransportInfoDto transport,

		@NotEmpty(message = "Items are required")
		@Valid
		List<ProductItemDto> items,

		BigDecimal totalWithoutVat,

		BigDecimal totalVat,

		BigDecimal totalWithVat,

		BigDecimal totalWeight,

		Integer totalPackages,

		String releaseReason,

		String notes
) {}
