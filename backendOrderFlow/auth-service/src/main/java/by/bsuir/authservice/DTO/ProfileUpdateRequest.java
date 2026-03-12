package by.bsuir.authservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileUpdateRequest {
	private String name;
	private String contactPhone;
	private String bankName;
	private String bic;
	private String accountNumber;
	private String directorName;
	private String chiefAccountantName;
	private String paymentTerms;
}

