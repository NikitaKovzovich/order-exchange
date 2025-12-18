package by.bsuir.authservice.service;

import by.bsuir.authservice.entity.*;
import by.bsuir.authservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import by.bsuir.authservice.DTO.RegisterRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final UserRepository userRepository;
	private final CompanyRepository companyRepository;
	private final VerificationRequestRepository verificationRequestRepository;
	private final VerificationDocumentRepository verificationDocumentRepository;
	private final AddressRepository addressRepository;
	private final CompanyDocumentRepository companyDocumentRepository;
	private final BankAccountRepository bankAccountRepository;
	private final ResponsiblePersonRepository responsiblePersonRepository;
	private final SupplierSettingsRepository supplierSettingsRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;
	private final FileStorageService fileStorageService;
	private final EventPublisher eventPublisher;

	@Transactional
	public String register(RegisterRequest request) {

		if (userRepository.existsByEmail(request.getEmail())) {
			throw new IllegalArgumentException("User with this email already exists");
		}

		User.Role userRole = User.Role.valueOf(request.getType().toUpperCase());

		User user = User.builder()
				.email(request.getEmail())
				.passwordHash(passwordEncoder.encode(request.getPassword()))
				.role(userRole)
				.isActive(userRole == User.Role.ADMIN)
				.createdAt(LocalDateTime.now())
				.build();

		Company.LegalForm legalForm = Company.LegalForm.valueOf(request.getLegalForm());
		String companyName = request.getName() != null ? request.getName() : request.getLegalName();

		Company company = Company.builder()
				.name(request.getName())
				.legalForm(legalForm)
				.taxId(request.getTaxId())
				.inn(request.getTaxId())
				.registrationDate(request.getRegistrationDate())
				.contactPhone(request.getContactPhone())
				.status(Company.CompanyStatus.PENDING_VERIFICATION)
				.build();

		if (request.getName() != null && !request.getName().isEmpty()) {
			company.setNameAndBuildLegalName(request.getName());
		} else if (request.getLegalName() != null) {
			company.setLegalName(request.getLegalName());
		}

		company = companyRepository.save(company);

		eventPublisher.publish("Company", company.getId().toString(), "CompanyCreated",
			Map.of(
				"companyId", company.getId(),
				"name", company.getName() != null ? company.getName() : "",
				"legalName", company.getLegalName() != null ? company.getLegalName() : "",
				"legalForm", company.getLegalForm().name(),
				"taxId", company.getTaxId() != null ? company.getTaxId() : "",
				"status", company.getStatus().name()
			));

		user.setCompany(company);
		user = userRepository.save(user);

		eventPublisher.publish("User", user.getId().toString(), "UserRegistered",
			Map.of(
				"userId", user.getId(),
				"email", user.getEmail() != null ? user.getEmail() : "",
				"role", user.getRole().name(),
				"companyId", company.getId()
			));

		if (userRole == User.Role.SUPPLIER) {
			createSupplierAddresses(company, request);
			saveSupplierSettings(company, request);
		} else if (userRole == User.Role.RETAIL_CHAIN) {
			createRetailChainAddresses(company, request);
		}

		saveBankAccount(company, request);
		saveResponsiblePersons(company, request);
		saveCompanyDocuments(company, request);

		VerificationRequest verificationRequest = VerificationRequest.builder()
				.company(company)
				.user(user)
				.status(VerificationRequest.VerificationStatus.PENDING)
				.requestedAt(LocalDateTime.now())
				.build();
		verificationRequest = verificationRequestRepository.save(verificationRequest);

		eventPublisher.publish("VerificationRequest", verificationRequest.getId().toString(),
			"VerificationRequestCreated",
			Map.of(
				"verificationRequestId", verificationRequest.getId(),
				"companyId", company.getId(),
				"userId", user.getId(),
				"status", verificationRequest.getStatus().name()
			));

		saveVerificationDocuments(verificationRequest, request);

		return jwtProvider.generateToken(user.getEmail(), userRole.name());
	}

	private void saveCompanyDocuments(Company company, RegisterRequest request) {
		List<CompanyDocument> documents = new ArrayList<>();

		if (request.getLogo() != null && !request.getLogo().isEmpty()) {
			String filePath = fileStorageService.storeFile(request.getLogo(), "company/" + company.getId() + "/logo");
			String fileName = request.getLogo().getOriginalFilename();
			documents.add(CompanyDocument.builder()
					.company(company)
					.documentType(CompanyDocument.DocumentType.LOGO)
					.fileKey(fileName != null ? fileName : "logo")
					.filePath(filePath)
					.originalFilename(fileName)
					.build());
		}

		if (request.getEdsFile() != null && !request.getEdsFile().isEmpty()) {
			String filePath = fileStorageService.storeFile(request.getEdsFile(), "company/" + company.getId() + "/eds");
			String fileName = request.getEdsFile().getOriginalFilename();
			documents.add(CompanyDocument.builder()
					.company(company)
					.documentType(CompanyDocument.DocumentType.EDS_FILE)
					.fileKey(fileName != null ? fileName : "eds_file")
					.filePath(filePath)
					.originalFilename(fileName)
					.build());
		}

		if (request.getSealImage() != null && !request.getSealImage().isEmpty()) {
			String filePath = fileStorageService.storeFile(request.getSealImage(), "company/" + company.getId() + "/seal");
			String fileName = request.getSealImage().getOriginalFilename();
			documents.add(CompanyDocument.builder()
					.company(company)
					.documentType(CompanyDocument.DocumentType.SEAL_IMAGE)
					.fileKey(fileName != null ? fileName : "seal")
					.filePath(filePath)
					.originalFilename(fileName)
					.build());
		}

		if (!documents.isEmpty()) {
			companyDocumentRepository.saveAll(documents);
		}
	}

	private void saveVerificationDocuments(VerificationRequest verificationRequest, RegisterRequest request) {
		List<VerificationDocument> documents = new ArrayList<>();

		if (request.getRegistrationCertificate() != null && !request.getRegistrationCertificate().isEmpty()) {
			String filePath = fileStorageService.storeFile(
					request.getRegistrationCertificate(),
					"verification/" + verificationRequest.getId() + "/certificate"
			);
			documents.add(VerificationDocument.builder()
					.verificationRequest(verificationRequest)
					.documentType("REGISTRATION_CERTIFICATE")
					.documentPath(filePath)
					.documentName(request.getRegistrationCertificate().getOriginalFilename())
					.uploadedAt(LocalDateTime.now())
					.build());
		}

		if (request.getCharter() != null && !request.getCharter().isEmpty()) {
			String filePath = fileStorageService.storeFile(
					request.getCharter(),
					"verification/" + verificationRequest.getId() + "/charter"
			);
			documents.add(VerificationDocument.builder()
					.verificationRequest(verificationRequest)
					.documentType("CHARTER")
					.documentPath(filePath)
					.documentName(request.getCharter().getOriginalFilename())
					.uploadedAt(LocalDateTime.now())
					.build());
		}

		if (!documents.isEmpty()) {
			verificationDocumentRepository.saveAll(documents);
		}
	}

	private void createSupplierAddresses(Company company, RegisterRequest request) {
		List<Address> addresses = new ArrayList<>();

		if (request.getLegalAddress() != null && !request.getLegalAddress().isEmpty()) {
			addresses.add(Address.builder()
					.company(company)
					.addressType(Address.AddressType.legal)
					.fullAddress(request.getLegalAddress())
					.isDefault(true)
					.build());
		}

		if (request.getPostalAddress() != null && !request.getPostalAddress().isEmpty()) {
			addresses.add(Address.builder()
					.company(company)
					.addressType(Address.AddressType.postal)
					.fullAddress(request.getPostalAddress())
					.isDefault(false)
					.build());
		}

		if (request.getShippingAddress() != null && !request.getShippingAddress().isEmpty()) {
			addresses.add(Address.builder()
					.company(company)
					.addressType(Address.AddressType.shipping)
					.fullAddress(request.getShippingAddress())
					.isDefault(false)
					.build());
		}

		if (request.getDeliveryAddresses() != null && !request.getDeliveryAddresses().isEmpty()) {
			for (String deliveryAddress : request.getDeliveryAddresses()) {
				if (deliveryAddress != null && !deliveryAddress.trim().isEmpty()) {
					addresses.add(Address.builder()
							.company(company)
							.addressType(Address.AddressType.delivery)
							.fullAddress(deliveryAddress.trim())
							.isDefault(false)
							.build());
				}
			}
		}

		if (!addresses.isEmpty()) {
			addressRepository.saveAll(addresses);
		}
	}

	private void createRetailChainAddresses(Company company, RegisterRequest request) {
		List<Address> addresses = new ArrayList<>();

		if (request.getLegalAddress() != null && !request.getLegalAddress().isEmpty()) {
			addresses.add(Address.builder()
					.company(company)
					.addressType(Address.AddressType.legal)
					.fullAddress(request.getLegalAddress())
					.isDefault(true)
					.build());
		}

		if (request.getPostalAddress() != null && !request.getPostalAddress().isEmpty()) {
			addresses.add(Address.builder()
					.company(company)
					.addressType(Address.AddressType.postal)
					.fullAddress(request.getPostalAddress())
					.isDefault(false)
					.build());
		}

		if (request.getShippingAddress() != null && !request.getShippingAddress().isEmpty()) {
			addresses.add(Address.builder()
					.company(company)
					.addressType(Address.AddressType.shipping)
					.fullAddress(request.getShippingAddress())
					.isDefault(false)
					.build());
		}

		if (request.getDeliveryAddresses() != null && !request.getDeliveryAddresses().isEmpty()) {
			for (String deliveryAddress : request.getDeliveryAddresses()) {
				if (deliveryAddress != null && !deliveryAddress.trim().isEmpty()) {
					addresses.add(Address.builder()
							.company(company)
							.addressType(Address.AddressType.delivery)
							.fullAddress(deliveryAddress.trim())
							.isDefault(false)
							.build());
				}
			}
		}

		if (request.getAddresses() != null && !request.getAddresses().isEmpty()) {
			for (var addressDto : request.getAddresses()) {
				addresses.add(Address.builder()
						.company(company)
						.addressType(Address.AddressType.valueOf(addressDto.getAddressType()))
						.fullAddress(addressDto.getFullAddress())
						.isDefault(addressDto.getIsDefault() != null && addressDto.getIsDefault())
						.build());
			}
		}

		if (!addresses.isEmpty()) {
			addressRepository.saveAll(addresses);
		}
	}

	private void saveBankAccount(Company company, RegisterRequest request) {
		boolean bankNameValid = request.getBankName() != null && !request.getBankName().isEmpty();
		boolean bicValid = request.getBic() != null && !request.getBic().isEmpty();
		boolean accountNumberValid = request.getAccountNumber() != null && !request.getAccountNumber().isEmpty();

		if (bankNameValid && bicValid && accountNumberValid) {
			BankAccount bankAccount = BankAccount.builder()
					.company(company)
					.bankName(request.getBankName())
					.bic(request.getBic())
					.accountNumber(request.getAccountNumber())
					.build();

			BankAccount saved = bankAccountRepository.save(bankAccount);

			eventPublisher.publish("Company", company.getId().toString(), "BankAccountAdded",
				Map.of(
					"companyId", company.getId(),
					"bankAccountId", saved.getId(),
					"bankName", saved.getBankName(),
					"bic", saved.getBic()
				));
		}
	}

	private void saveResponsiblePersons(Company company, RegisterRequest request) {
		List<ResponsiblePerson> persons = new ArrayList<>();

		if (request.getDirectorFio() != null && !request.getDirectorFio().isEmpty()) {
			persons.add(ResponsiblePerson.builder()
					.company(company)
					.fullName(request.getDirectorFio())
					.position(ResponsiblePerson.Position.director)
					.build());
		}

		if (request.getChiefAccountantFio() != null && !request.getChiefAccountantFio().isEmpty()) {
			persons.add(ResponsiblePerson.builder()
					.company(company)
					.fullName(request.getChiefAccountantFio())
					.position(ResponsiblePerson.Position.chief_accountant)
					.build());
		}

		if (!persons.isEmpty()) {
			List<ResponsiblePerson> saved = responsiblePersonRepository.saveAll(persons);

			List<Map<String, Object>> personsData = new ArrayList<>();
			for (ResponsiblePerson person : saved) {
				personsData.add(Map.of(
					"id", person.getId(),
					"fullName", person.getFullName(),
					"position", person.getPosition().name()
				));
			}
			eventPublisher.publish("Company", company.getId().toString(), "ResponsiblePersonsAdded",
				Map.of(
					"companyId", company.getId(),
					"persons", personsData
				));
		}
	}

	private void saveSupplierSettings(Company company, RegisterRequest request) {
		if (request.getPaymentTerms() != null && !request.getPaymentTerms().isEmpty()) {
			try {
				SupplierSettings.PaymentTerms paymentTerms = SupplierSettings.fromString(request.getPaymentTerms());

				SupplierSettings settings = SupplierSettings.builder()
						.company(company)
						.paymentTerms(paymentTerms)
						.build();

				SupplierSettings saved = supplierSettingsRepository.save(settings);

				eventPublisher.publish("Company", company.getId().toString(), "SupplierSettingsConfigured",
					Map.of(
						"companyId", company.getId(),
						"paymentTerms", saved.getPaymentTerms().name()
					));
			} catch (IllegalArgumentException e) {
			} catch (Exception e) {
			}
		}
	}

	public String login(String email, String password) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

		if (user.getIsActive() == null || !user.getIsActive()) {
			throw new IllegalArgumentException("User account is not active");
		}

		if (!passwordEncoder.matches(password, user.getPasswordHash())) {
			throw new IllegalArgumentException("Invalid email or password");
		}

		eventPublisher.publish("User", user.getId().toString(), "UserLoggedIn",
			Map.of(
				"userId", user.getId(),
				"email", user.getEmail(),
				"role", user.getRole().name()
			));

		return jwtProvider.generateToken(email, user.getRole().name());
	}

	public User getUserByEmail(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));
	}

	public Company getCompanyById(Long companyId) {
		return companyRepository.findById(companyId)
				.orElseThrow(() -> new IllegalArgumentException("Company not found"));
	}
}
