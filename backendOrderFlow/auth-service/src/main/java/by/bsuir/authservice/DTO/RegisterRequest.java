package by.bsuir.authservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    private String type;
    private String email;
    private String password;
    private String passwordConfirm;
    private String name;
    private String legalName;
    private String legalForm;
    private String taxId;
    private LocalDate registrationDate;
    private String legalAddress;
    private String postalAddress;
    private String shippingAddress;
    private List<String> deliveryAddresses;
    private List<AddressDTO> addresses;
    private String contactPhone;
    private String directorFio;
    private String chiefAccountantFio;
    private String bankName;
    private String bic;
    private String accountNumber;
    private String paymentTerms;

    private MultipartFile logo;
    private MultipartFile registrationCertificate;
    private MultipartFile charter;
    private MultipartFile edsFile;
    private MultipartFile sealImage;
}

