package by.bsuir.catalogservice.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record PartnershipRequest(
	@NotNull(message = "Supplier ID is required")
	Long supplierId,

	String contractNumber,
	LocalDate contractDate,
	LocalDate contractEndDate,

	String customerCompanyName,
	String customerUnp
) {}
