package by.bsuir.catalogservice.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PartnershipResponse(
	Long id,
	Long supplierId,
	String supplierCompanyName,
	Long customerId,
	String customerCompanyName,
	String customerUnp,
	String status,
	String contractNumber,
	LocalDate contractDate,
	LocalDate contractEndDate,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {}
