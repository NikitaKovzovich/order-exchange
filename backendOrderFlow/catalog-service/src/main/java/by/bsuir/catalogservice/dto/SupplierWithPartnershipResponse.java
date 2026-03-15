package by.bsuir.catalogservice.dto;








public record SupplierWithPartnershipResponse(
		Long companyId,
		String companyName,
		String taxId,
		String contactPhone,
		String partnershipStatus,
		Long partnershipId
) {}
