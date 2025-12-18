package by.bsuir.documentservice.dto;

import jakarta.validation.constraints.NotBlank;

public record CompanyInfoDto(
		@NotBlank(message = "Company name is required")
		String name,
		String taxId,
		String legalAddress,
		String bankDetails,
		String phone,
		String directorName,
		String accountantName
) {
	public static CompanyInfoDto of(String name, String taxId, String legalAddress) {
		return new CompanyInfoDto(name, taxId, legalAddress, null, null, null, null);
	}
}
