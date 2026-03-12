package by.bsuir.authservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyProfileDTO {
	private Long id;
	private String name;
	private String legalName;
	private String legalForm;
	private String legalFormText;
	private String taxId;
	private LocalDate registrationDate;
	private String status;
	private String contactPhone;
	private Boolean verified;
	private List<AddressResponseDTO> addresses;
	private String bankName;
	private String bic;
	private String accountNumber;
	private String directorName;
	private String chiefAccountantName;
	private List<DocumentInfo> documents;
	private String paymentTerms;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class DocumentInfo {
		private Long id;
		private String documentType;
		private String originalFilename;
		private String downloadUrl;
	}
}
