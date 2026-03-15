package by.bsuir.authservice.config;

import by.bsuir.authservice.entity.*;
import by.bsuir.authservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

	private final UserRepository userRepository;
	private final CompanyRepository companyRepository;
	private final CompanyDocumentRepository companyDocumentRepository;
	private final VerificationRequestRepository verificationRequestRepository;
	private final VerificationDocumentRepository verificationDocumentRepository;
	private final BankAccountRepository bankAccountRepository;
	private final ResponsiblePersonRepository responsiblePersonRepository;
	private final AddressRepository addressRepository;
	private final PasswordEncoder passwordEncoder;
	private final JdbcTemplate jdbcTemplate;

	@Override
	public void run(String... args) {
		migrateEventsPayloadColumn();
		initAdmin();
		initSupplier();
		initRetailChain();
	}

	private void initAdmin() {
		if (userRepository.findByEmail("admin@test.com").isEmpty()) {
			User admin = User.builder()
					.email("admin@test.com")
					.passwordHash(passwordEncoder.encode("password123"))
					.role(User.Role.ADMIN)
					.isActive(true)
					.status("ACTIVE")
					.createdAt(LocalDateTime.now())
					.build();
			userRepository.save(admin);
			log.info("Created admin user: admin@test.com / password123");
		} else {
			User admin = userRepository.findByEmail("admin@test.com").get();
			if (!passwordEncoder.matches("password123", admin.getPasswordHash())) {
				admin.setPasswordHash(passwordEncoder.encode("password123"));
				userRepository.save(admin);
			}
		}
	}

	private void initSupplier() {
		if (userRepository.findByEmail("supplier@test.com").isPresent()) {
			User user = userRepository.findByEmail("supplier@test.com").get();
			if (!passwordEncoder.matches("password123", user.getPasswordHash())) {
				user.setPasswordHash(passwordEncoder.encode("password123"));
				userRepository.save(user);
			}
			return;
		}

		Company company = companyRepository.save(Company.builder()
				.legalName("ООО ТестПоставщик")
				.name("ТестПоставщик")
				.legalForm(Company.LegalForm.LLC)
				.taxId("1234567890")
				.registrationDate(LocalDate.of(2020, 1, 15))
				.status(Company.CompanyStatus.PENDING_VERIFICATION)
				.contactPhone("+375291234567")
				.verified(false)
				.build());

		User user = userRepository.save(User.builder()
				.email("supplier@test.com")
				.passwordHash(passwordEncoder.encode("password123"))
				.role(User.Role.SUPPLIER)
				.company(company)
				.isActive(true)
				.status("ACTIVE")
				.createdAt(LocalDateTime.now().minusDays(5))
				.build());

		bankAccountRepository.save(BankAccount.builder()
				.company(company)
				.bankName("ОАО Беларусбанк")
				.bic("AKBBBY2X")
				.accountNumber("BY20AKBB30120000000012345678")
				.build());

		responsiblePersonRepository.save(ResponsiblePerson.builder()
				.company(company)
				.position(ResponsiblePerson.Position.director)
				.fullName("Иванов Иван Иванович")
				.build());

		responsiblePersonRepository.save(ResponsiblePerson.builder()
				.company(company)
				.position(ResponsiblePerson.Position.chief_accountant)
				.fullName("Петрова Мария Сергеевна")
				.build());

		addressRepository.save(Address.builder()
				.company(company)
				.addressType(Address.AddressType.legal)
				.fullAddress("г. Минск, ул. Ленина, 1")
				.isDefault(true)
				.build());

		addressRepository.save(Address.builder()
				.company(company)
				.addressType(Address.AddressType.delivery)
				.fullAddress("г. Минск, ул. Заводская, 5")
				.isDefault(false)
				.build());

		CompanyDocument cert = companyDocumentRepository.save(CompanyDocument.builder()
				.company(company)
				.documentType(CompanyDocument.DocumentType.REGISTRATION_CERTIFICATE)
				.fileKey("registration_certificate.pdf")
				.filePath("company/" + company.getId() + "/certificate/registration_certificate.pdf")
				.originalFilename("Свидетельство_о_регистрации.pdf")
				.build());

		CompanyDocument charter = companyDocumentRepository.save(CompanyDocument.builder()
				.company(company)
				.documentType(CompanyDocument.DocumentType.CHARTER)
				.fileKey("charter.pdf")
				.filePath("company/" + company.getId() + "/charter/charter.pdf")
				.originalFilename("Устав.pdf")
				.build());

		CompanyDocument logo = companyDocumentRepository.save(CompanyDocument.builder()
				.company(company)
				.documentType(CompanyDocument.DocumentType.LOGO)
				.fileKey("logo.png")
				.filePath("company/" + company.getId() + "/logo/logo.png")
				.originalFilename("Логотип.png")
				.build());

		VerificationRequest vr = verificationRequestRepository.save(VerificationRequest.builder()
				.company(company)
				.user(user)
				.status(VerificationRequest.VerificationStatus.PENDING)
				.requestedAt(LocalDateTime.now().minusDays(5))
				.build());

		verificationDocumentRepository.save(VerificationDocument.builder()
				.verificationRequest(vr)
				.documentType("REGISTRATION_CERTIFICATE")
				.documentPath(cert.getFilePath())
				.documentName(cert.getOriginalFilename())
				.uploadedAt(LocalDateTime.now().minusDays(5))
				.build());

		verificationDocumentRepository.save(VerificationDocument.builder()
				.verificationRequest(vr)
				.documentType("CHARTER")
				.documentPath(charter.getFilePath())
				.documentName(charter.getOriginalFilename())
				.uploadedAt(LocalDateTime.now().minusDays(5))
				.build());

		verificationDocumentRepository.save(VerificationDocument.builder()
				.verificationRequest(vr)
				.documentType("LOGO")
				.documentPath(logo.getFilePath())
				.documentName(logo.getOriginalFilename())
				.uploadedAt(LocalDateTime.now().minusDays(5))
				.build());

		log.info("Created supplier user: supplier@test.com / password123 with company, docs, verification");
	}

	private void initRetailChain() {
		if (userRepository.findByEmail("retailchain@test.com").isPresent()) {
			User user = userRepository.findByEmail("retailchain@test.com").get();
			if (!passwordEncoder.matches("password123", user.getPasswordHash())) {
				user.setPasswordHash(passwordEncoder.encode("password123"));
				userRepository.save(user);
			}
			return;
		}

		Company company = companyRepository.save(Company.builder()
				.legalName("ОАО ТестРитейл")
				.name("ТестРитейл")
				.legalForm(Company.LegalForm.OJSC)
				.taxId("9876543210")
				.registrationDate(LocalDate.of(2019, 6, 20))
				.status(Company.CompanyStatus.PENDING_VERIFICATION)
				.contactPhone("+375339876543")
				.verified(false)
				.build());

		User user = userRepository.save(User.builder()
				.email("retailchain@test.com")
				.passwordHash(passwordEncoder.encode("password123"))
				.role(User.Role.RETAIL_CHAIN)
				.company(company)
				.isActive(true)
				.status("ACTIVE")
				.createdAt(LocalDateTime.now().minusDays(3))
				.build());

		bankAccountRepository.save(BankAccount.builder()
				.company(company)
				.bankName("ОАО Белагропромбанк")
				.bic("BAPBBY2X")
				.accountNumber("BY86BAPB30120000000098765432")
				.build());

		responsiblePersonRepository.save(ResponsiblePerson.builder()
				.company(company)
				.position(ResponsiblePerson.Position.director)
				.fullName("Сидоров Пётр Алексеевич")
				.build());

		addressRepository.save(Address.builder()
				.company(company)
				.addressType(Address.AddressType.legal)
				.fullAddress("г. Минск, пр. Независимости, 100")
				.isDefault(true)
				.build());

		CompanyDocument cert = companyDocumentRepository.save(CompanyDocument.builder()
				.company(company)
				.documentType(CompanyDocument.DocumentType.REGISTRATION_CERTIFICATE)
				.fileKey("registration_certificate.pdf")
				.filePath("company/" + company.getId() + "/certificate/registration_certificate.pdf")
				.originalFilename("Свидетельство_о_регистрации.pdf")
				.build());

		VerificationRequest vr = verificationRequestRepository.save(VerificationRequest.builder()
				.company(company)
				.user(user)
				.status(VerificationRequest.VerificationStatus.PENDING)
				.requestedAt(LocalDateTime.now().minusDays(3))
				.build());

		verificationDocumentRepository.save(VerificationDocument.builder()
				.verificationRequest(vr)
				.documentType("REGISTRATION_CERTIFICATE")
				.documentPath(cert.getFilePath())
				.documentName(cert.getOriginalFilename())
				.uploadedAt(LocalDateTime.now().minusDays(3))
				.build());

		log.info("Created retail chain user: retailchain@test.com / password123 with company, docs, verification");
	}

	private void migrateEventsPayloadColumn() {
		try {
			String dbUrl = jdbcTemplate.getDataSource() != null
					? jdbcTemplate.getDataSource().getConnection().getMetaData().getURL()
					: "";
			if (!dbUrl.contains("mysql") && !dbUrl.contains("mariadb")) {
				log.debug("Skipping events payload migration: not MySQL (url={})", dbUrl);
				return;
			}
			jdbcTemplate.execute("ALTER TABLE events MODIFY COLUMN payload JSON NOT NULL");
			log.info("✓ Events payload column migrated to JSON");
		} catch (Exception e) {
			log.debug("Events payload column migration skipped: {}", e.getMessage());
		}
	}
}
