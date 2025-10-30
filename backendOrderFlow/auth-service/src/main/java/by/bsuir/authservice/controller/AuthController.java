package by.bsuir.authservice.controller;

import by.bsuir.authservice.DTO.AuthResponse;
import by.bsuir.authservice.DTO.LoginRequest;
import by.bsuir.authservice.DTO.RegisterRequest;
import by.bsuir.authservice.DTO.UserProfileResponse;
import by.bsuir.authservice.DTO.CompanyProfileDTO;
import by.bsuir.authservice.DTO.AddressResponseDTO;
import by.bsuir.authservice.entity.User;
import by.bsuir.authservice.entity.Company;
import by.bsuir.authservice.entity.Address;
import by.bsuir.authservice.service.AuthService;
import by.bsuir.authservice.service.AddressService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final AddressService addressService;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AuthResponse> register(
            @RequestParam("type") String type,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("passwordConfirm") String passwordConfirm,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "legalName", required = false) String legalName,
            @RequestParam("legalForm") String legalForm,
            @RequestParam("taxId") String taxId,
            @RequestParam("registrationDate") String registrationDate,
            @RequestParam("contactPhone") String contactPhone,
            @RequestParam("legalAddress") String legalAddress,
            @RequestParam(value = "postalAddress", required = false) String postalAddress,
            @RequestParam(value = "shippingAddress", required = false) String shippingAddress,
            @RequestParam(value = "deliveryAddresses", required = false) String deliveryAddresses,
            @RequestParam(value = "directorName", required = false) String directorName,
            @RequestParam(value = "accountantName", required = false) String accountantName,
            @RequestParam(value = "bankName", required = false) String bankName,
            @RequestParam(value = "bankBic", required = false) String bankBic,
            @RequestParam(value = "bankAccount", required = false) String bankAccount,
            @RequestParam(value = "paymentTerms", required = false) String paymentTerms,
            @RequestParam(value = "logo", required = false) MultipartFile logo,
            @RequestParam(value = "registrationCertificate", required = false) MultipartFile registrationCertificate,
            @RequestParam(value = "charter", required = false) MultipartFile charter,
            @RequestParam(value = "edsFile", required = false) MultipartFile edsFile,
            @RequestParam(value = "sealImage", required = false) MultipartFile sealImage
    ) {

        String trimmedTaxId = taxId != null ? taxId.trim() : null;
        String trimmedBankName = bankName != null && !bankName.trim().isEmpty() ? bankName.trim() : null;
        String trimmedBankBic = bankBic != null && !bankBic.trim().isEmpty() ? bankBic.trim() : null;
        String trimmedBankAccount = bankAccount != null && !bankAccount.trim().isEmpty() ? bankAccount.trim() : null;
        String trimmedDirectorName = directorName != null && !directorName.trim().isEmpty() ? directorName.trim() : null;
        String trimmedAccountantName = accountantName != null && !accountantName.trim().isEmpty() ? accountantName.trim() : null;
        String trimmedPaymentTerms = paymentTerms != null && !paymentTerms.trim().isEmpty() ? paymentTerms.trim() : null;

        List<String> deliveryAddressesList = null;
        if (deliveryAddresses != null && !deliveryAddresses.trim().isEmpty()) {
            try {
                deliveryAddressesList = objectMapper.readValue(deliveryAddresses,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
            } catch (Exception e) {
            }
        }

        RegisterRequest request = RegisterRequest.builder()
                .type(type)
                .email(email)
                .password(password)
                .passwordConfirm(passwordConfirm)
                .name(name)
                .legalName(legalName)
                .legalForm(legalForm)
                .taxId(trimmedTaxId)
                .registrationDate(LocalDate.parse(registrationDate))
                .contactPhone(contactPhone)
                .legalAddress(legalAddress)
                .postalAddress(postalAddress)
                .shippingAddress(shippingAddress)
                .deliveryAddresses(deliveryAddressesList)
                .directorFio(trimmedDirectorName)
                .chiefAccountantFio(trimmedAccountantName)
                .bankName(trimmedBankName)
                .bic(trimmedBankBic)
                .accountNumber(trimmedBankAccount)
                .paymentTerms(trimmedPaymentTerms)
                .logo(logo)
                .registrationCertificate(registrationCertificate)
                .charter(charter)
                .edsFile(edsFile)
                .sealImage(sealImage)
                .build();

        String token = authService.register(request);

        User user = authService.getUserByEmail(request.getEmail());

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole().name())
                .userId(user.getId())
                .companyId(user.getCompany() != null ? user.getCompany().getId() : null)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        String token = authService.login(request.getEmail(), request.getPassword());
        User user = authService.getUserByEmail(request.getEmail());

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole().name())
                .userId(user.getId())
                .companyId(user.getCompany() != null ? user.getCompany().getId() : null)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(@RequestHeader("X-User-Email") String email) {
        User user = authService.getUserByEmail(email);

        UserProfileResponse response = UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .companyId(user.getCompany() != null ? user.getCompany().getId() : null)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<CompanyProfileDTO> getCompanyProfile(@PathVariable Long companyId) {
        Company company = authService.getCompanyById(companyId);
        List<Address> addresses = addressService.getCompanyAddresses(companyId);

        List<AddressResponseDTO> addressDTOs = addresses.stream()
                .map(addr -> AddressResponseDTO.builder()
                        .id(addr.getId())
                        .addressType(addr.getAddressType().name())
                        .fullAddress(addr.getFullAddress())
                        .isDefault(addr.getIsDefault())
                        .build())
                .collect(Collectors.toList());

        CompanyProfileDTO response = CompanyProfileDTO.builder()
                .id(company.getId())
                .legalName(company.getLegalName())
                .legalForm(company.getLegalForm().name())
                .taxId(company.getTaxId())
                .registrationDate(company.getRegistrationDate())
                .status(company.getStatus().name())
                .contactPhone(company.getContactPhone())
                .addresses(addressDTOs)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    public ResponseEntity<String> validateToken(@RequestHeader("X-User-Email") String email) {
        return ResponseEntity.ok("Token is valid for user: " + email);
    }
}

