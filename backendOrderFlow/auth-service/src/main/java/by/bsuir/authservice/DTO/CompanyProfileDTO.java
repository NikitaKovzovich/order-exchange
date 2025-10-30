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
    private String legalName;
    private String legalForm;
    private String taxId;
    private LocalDate registrationDate;
    private String status;
    private String contactPhone;
    private List<AddressResponseDTO> addresses;
}

