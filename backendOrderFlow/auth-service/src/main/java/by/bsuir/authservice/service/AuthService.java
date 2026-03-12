package by.bsuir.authservice.service;

import by.bsuir.authservice.entity.*;
import by.bsuir.authservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import by.bsuir.authservice.DTO.ProfileUpdateRequest;
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
	private final NotificationService notificationService;
	private final RabbitEventPublisher rabbitEventPublisher;

	@Transactional
	public String register(RegisterRequest request) {
		if (!request.getPassword().equals(request.getPasswordConfirm())) {
			throw new IllegalArgumentException("Passwords do not match");
		}
		if (request.getPassword().length() < 8) {
			throw new IllegalArgumentException("Password must be at least 8 characters");
		}

		if (request.getTaxId() == null || !request.getTaxId().matches("^\\d{9}$")) {
			throw new IllegalArgumentException("Tax ID (UNP) must be exactly 9 digits");
		}

		if (request.getRegistrationDate() != null && request.getRegistrationDate().isAfter(java.time.LocalDate.now())) {
			throw new IllegalArgumentException("Registration date cannot be in the future");
		}

		if (userRepository.existsByEmail(request.getEmail())) {
			throw new IllegalArgumentException("User with this email already exists");
		}

		User.Role userRole = User.Role.valueOf(request.getType().toUpperCase());

		User user = User.builder()
				.email(request.getEmail())
				.passwordHash(passwordEncoder.encode(request.getPassword()))
				.role(userRole)
				.isActive(userRole == User.Role.ADMIN)
				.status(userRole == User.Role.ADMIN ? "ACTIVE" : "PENDING_VERIFICATION")
				.createdAt(LocalDateTime.now())
				.build();

		Company.LegalForm legalForm = Company.LegalForm.valueOf(request.getLegalForm());

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

		saveVerificationDocuments(verificationRequest, company);

		notificationService.createNotification(user.getId(),
				"Регистрация отправлена",
				"Ваша заявка на регистрацию отправлена и ожидает проверки администратором.",
				Notification.NotificationType.REGISTRATION_SUBMITTED,
				"VerificationRequest", verificationRequest.getId());

		List<Long> adminIds = userRepository.findAllAdminUserIds();
		notificationService.notifyAllAdmins(
				"Новая заявка на регистрацию",
				"Получена новая заявка на регистрацию от компании «" +
						(company.getLegalName() != null ? company.getLegalName() : company.getName()) + "».",
				Notification.NotificationType.REGISTRATION_SUBMITTED,
				"VerificationRequest", verificationRequest.getId(), adminIds);

		rabbitEventPublisher.publish("userregistered", Map.of(
				"userId", user.getId(),
				"email", user.getEmail(),
				"role", user.getRole().name(),
				"companyId", company.getId(),
				"companyName", company.getLegalName() != null ? company.getLegalName() : ""
		));

		return jwtProvider.generateToken(user.getEmail(), userRole.name(), user.getId(), company.getId());
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

		if (request.getRegistrationCertificate() != null && !request.getRegistrationCertificate().isEmpty()) {
			String filePath = fileStorageService.storeFile(request.getRegistrationCertificate(),
					"company/" + company.getId() + "/certificate");
			String fileName = request.getRegistrationCertificate().getOriginalFilename();
			documents.add(CompanyDocument.builder()
					.company(company)
					.documentType(CompanyDocument.DocumentType.REGISTRATION_CERTIFICATE)
					.fileKey(fileName != null ? fileName : "registration_certificate")
					.filePath(filePath)
					.originalFilename(fileName)
					.build());
		}

		if (request.getCharter() != null && !request.getCharter().isEmpty()) {
			String filePath = fileStorageService.storeFile(request.getCharter(),
					"company/" + company.getId() + "/charter");
			String fileName = request.getCharter().getOriginalFilename();
			documents.add(CompanyDocument.builder()
					.company(company)
					.documentType(CompanyDocument.DocumentType.CHARTER)
					.fileKey(fileName != null ? fileName : "charter")
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

			List<Map<String, Object>> docsData = new ArrayList<>();
			for (CompanyDocument doc : documents) {
				docsData.add(Map.of(
					"documentType", doc.getDocumentType().name(),
					"filePath", doc.getFilePath() != null ? doc.getFilePath() : "",
					"originalFilename", doc.getOriginalFilename() != null ? doc.getOriginalFilename() : ""
				));
			}
			eventPublisher.publish("Company", company.getId().toString(), "CompanyDocumentsUploaded",
				Map.of(
					"companyId", company.getId(),
					"documentsCount", documents.size(),
					"documents", docsData
				));
		}
	}

	private void saveVerificationDocuments(VerificationRequest verificationRequest, Company company) {
		List<CompanyDocument> companyDocs = companyDocumentRepository.findByCompanyId(company.getId());
		if (companyDocs.isEmpty()) {
			return;
		}

		List<VerificationDocument> documents = new ArrayList<>();
		for (CompanyDocument cd : companyDocs) {
			documents.add(VerificationDocument.builder()
					.verificationRequest(verificationRequest)
					.documentType(cd.getDocumentType().name())
					.documentPath(cd.getFilePath())
					.documentName(cd.getOriginalFilename() != null ? cd.getOriginalFilename() : cd.getFileKey())
					.uploadedAt(LocalDateTime.now())
					.build());
		}

		verificationDocumentRepository.saveAll(documents);

		eventPublisher.publish("VerificationRequest", verificationRequest.getId().toString(),
			"VerificationDocumentsUploaded",
			Map.of(
				"verificationRequestId", verificationRequest.getId(),
				"documentsCount", documents.size()
			));
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

		if ("BLOCKED".equals(user.getStatus())) {
			throw new IllegalArgumentException("User account is blocked. Contact administrator.");
		}

		if (user.getIsActive() == null || !user.getIsActive()) {
			throw new IllegalArgumentException("User account is not active. Awaiting verification.");
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

		Long companyId = user.getCompany() != null ? user.getCompany().getId() : null;
		return jwtProvider.generateToken(email, user.getRole().name(), user.getId(), companyId);
	}

	public User getUserByEmail(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));
	}

	public Company getCompanyById(Long companyId) {
		return companyRepository.findById(companyId)
				.orElseThrow(() -> new IllegalArgumentException("Company not found"));
	}

	@Transactional
	public void updateProfile(Long userId, ProfileUpdateRequest request) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));

		Company company = user.getCompany();
		if (company == null) {
			throw new IllegalArgumentException("User has no company");
		}

		if (request.getName() != null && !request.getName().isEmpty()) {
			company.setNameAndBuildLegalName(request.getName());
		}
		if (request.getContactPhone() != null) {
			company.setContactPhone(request.getContactPhone());
		}
		companyRepository.save(company);

		if (request.getBankName() != null && request.getBic() != null && request.getAccountNumber() != null) {
			BankAccount bankAccount = bankAccountRepository.findByCompanyId(company.getId())
					.orElse(BankAccount.builder().company(company).build());
			bankAccount.setBankName(request.getBankName());
			bankAccount.setBic(request.getBic());
			bankAccount.setAccountNumber(request.getAccountNumber());
			bankAccountRepository.save(bankAccount);
		}

		if (request.getDirectorName() != null && !request.getDirectorName().isEmpty()) {
			upsertResponsiblePerson(company, ResponsiblePerson.Position.director, request.getDirectorName());
		}
		if (request.getChiefAccountantName() != null && !request.getChiefAccountantName().isEmpty()) {
			upsertResponsiblePerson(company, ResponsiblePerson.Position.chief_accountant, request.getChiefAccountantName());
		}

		if (request.getPaymentTerms() != null && user.getRole() == User.Role.SUPPLIER) {
			try {
				SupplierSettings.PaymentTerms terms = SupplierSettings.fromString(request.getPaymentTerms());
				SupplierSettings settings = supplierSettingsRepository.findById(company.getId())
						.orElse(SupplierSettings.builder().company(company).build());
				settings.setPaymentTerms(terms);
				supplierSettingsRepository.save(settings);
			} catch (Exception ignored) {}
		}

		user.setUpdatedAt(LocalDateTime.now());
		userRepository.save(user);

		eventPublisher.publish("User", userId.toString(), "ProfileUpdated",
				Map.of("userId", userId, "companyId", company.getId()));

		notificationService.createNotification(userId,
				"Профиль обновлён",
				"Данные вашего профиля были успешно обновлены.",
				Notification.NotificationType.PROFILE_UPDATED,
				"User", userId);
	}

	@Transactional
	public void updateCompanyLogo(Long userId, MultipartFile logo) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found"));

		Company company = user.getCompany();
		if (company == null) {
			throw new IllegalArgumentException("User has no company");
		}

		List<CompanyDocument> existingLogos = companyDocumentRepository.findByCompanyId(company.getId())
				.stream()
				.filter(d -> d.getDocumentType() == CompanyDocument.DocumentType.LOGO)
				.toList();
		for (CompanyDocument existing : existingLogos) {
			fileStorageService.deleteFile(existing.getFilePath());
			companyDocumentRepository.delete(existing);
		}

		String filePath = fileStorageService.storeFile(logo, "company/" + company.getId() + "/logo");
		String fileName = logo.getOriginalFilename();
		CompanyDocument doc = CompanyDocument.builder()
				.company(company)
				.documentType(CompanyDocument.DocumentType.LOGO)
				.fileKey(fileName != null ? fileName : "logo")
				.filePath(filePath)
				.originalFilename(fileName)
				.build();
		companyDocumentRepository.save(doc);

		eventPublisher.publish("Company", company.getId().toString(), "CompanyLogoUpdated",
				Map.of("companyId", company.getId(), "filePath", filePath));
	}

	private void upsertResponsiblePerson(Company company, ResponsiblePerson.Position position, String fullName) {
		List<ResponsiblePerson> persons = responsiblePersonRepository.findByCompanyId(company.getId());
		ResponsiblePerson person = persons.stream()
				.filter(p -> p.getPosition() == position)
				.findFirst()
				.orElse(ResponsiblePerson.builder().company(company).position(position).build());
		person.setFullName(fullName);
		responsiblePersonRepository.save(person);
	}
}
