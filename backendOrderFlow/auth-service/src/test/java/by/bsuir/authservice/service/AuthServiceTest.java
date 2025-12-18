package by.bsuir.authservice.service;

import by.bsuir.authservice.DTO.RegisterRequest;
import by.bsuir.authservice.entity.*;
import by.bsuir.authservice.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private CompanyRepository companyRepository;

	@Mock
	private VerificationRequestRepository verificationRequestRepository;

	@Mock
	private VerificationDocumentRepository verificationDocumentRepository;

	@Mock
	private AddressRepository addressRepository;

	@Mock
	private CompanyDocumentRepository companyDocumentRepository;

	@Mock
	private BankAccountRepository bankAccountRepository;

	@Mock
	private ResponsiblePersonRepository responsiblePersonRepository;

	@Mock
	private SupplierSettingsRepository supplierSettingsRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtProvider jwtProvider;

	@Mock
	private FileStorageService fileStorageService;

	@Mock
	private EventPublisher eventPublisher;

	@InjectMocks
	private AuthService authService;

	private User testUser;
	private Company testCompany;

	@BeforeEach
	void setUp() {
		testCompany = Company.builder()
				.id(1L)
				.name("Test Company")
				.legalName("ООО Test Company")
				.legalForm(Company.LegalForm.LLC)
				.taxId("1234567890")
				.status(Company.CompanyStatus.ACTIVE)
				.build();

		testUser = User.builder()
				.id(1L)
				.email("test@example.com")
				.passwordHash("hashedPassword")
				.role(User.Role.SUPPLIER)
				.isActive(true)
				.company(testCompany)
				.createdAt(LocalDateTime.now())
				.build();
	}

	@Nested
	@DisplayName("Login Tests")
	class LoginTests {

		@Test
		@DisplayName("Should login successfully")
		void shouldLoginSuccessfully() {
			when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
			when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
			when(jwtProvider.generateToken("test@example.com", "SUPPLIER")).thenReturn("jwt-token");

			String token = authService.login("test@example.com", "password123");

			assertThat(token).isEqualTo("jwt-token");
			verify(jwtProvider).generateToken("test@example.com", "SUPPLIER");
		}

		@Test
		@DisplayName("Should throw exception for invalid email")
		void shouldThrowExceptionForInvalidEmail() {
			when(userRepository.findByEmail("invalid@example.com")).thenReturn(Optional.empty());

			assertThatThrownBy(() -> authService.login("invalid@example.com", "password"))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Invalid email or password");
		}

		@Test
		@DisplayName("Should throw exception for invalid password")
		void shouldThrowExceptionForInvalidPassword() {
			when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
			when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

			assertThatThrownBy(() -> authService.login("test@example.com", "wrongPassword"))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Invalid email or password");
		}

		@Test
		@DisplayName("Should throw exception for inactive user")
		void shouldThrowExceptionForInactiveUser() {
			testUser.setIsActive(false);
			when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

			assertThatThrownBy(() -> authService.login("test@example.com", "password"))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("User account is not active");
		}

		@Test
		@DisplayName("Should throw exception when isActive is null")
		void shouldThrowExceptionWhenIsActiveNull() {
			testUser.setIsActive(null);
			when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

			assertThatThrownBy(() -> authService.login("test@example.com", "password"))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("User account is not active");
		}
	}

	@Nested
	@DisplayName("Get User By Email Tests")
	class GetUserByEmailTests {

		@Test
		@DisplayName("Should return user by email")
		void shouldReturnUserByEmail() {
			when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

			User result = authService.getUserByEmail("test@example.com");

			assertThat(result).isNotNull();
			assertThat(result.getEmail()).isEqualTo("test@example.com");
		}

		@Test
		@DisplayName("Should throw exception when user not found")
		void shouldThrowExceptionWhenUserNotFound() {
			when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

			assertThatThrownBy(() -> authService.getUserByEmail("unknown@example.com"))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("User not found");
		}
	}

	@Nested
	@DisplayName("Get Company By Id Tests")
	class GetCompanyByIdTests {

		@Test
		@DisplayName("Should return company by id")
		void shouldReturnCompanyById() {
			when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));

			Company result = authService.getCompanyById(1L);

			assertThat(result).isNotNull();
			assertThat(result.getId()).isEqualTo(1L);
			assertThat(result.getName()).isEqualTo("Test Company");
		}

		@Test
		@DisplayName("Should throw exception when company not found")
		void shouldThrowExceptionWhenCompanyNotFound() {
			when(companyRepository.findById(999L)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> authService.getCompanyById(999L))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Company not found");
		}
	}

	@Nested
	@DisplayName("Register Tests")
	class RegisterTests {

		@Test
		@DisplayName("Should register supplier successfully")
		void shouldRegisterSupplierSuccessfully() {
			RegisterRequest request = RegisterRequest.builder()
					.type("SUPPLIER")
					.email("new@example.com")
					.password("password123")
					.passwordConfirm("password123")
					.name("New Company")
					.legalForm("LLC")
					.taxId("9876543210")
					.registrationDate(LocalDate.now())
					.contactPhone("+375291234567")
					.legalAddress("г. Минск, ул. Тестовая, 1")
					.build();

			when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
			when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
			when(companyRepository.save(any(Company.class))).thenAnswer(inv -> {
				Company c = inv.getArgument(0);
				c.setId(1L);
				return c;
			});
			when(userRepository.save(any(User.class))).thenAnswer(inv -> {
				User u = inv.getArgument(0);
				u.setId(1L);
				return u;
			});
			when(verificationRequestRepository.save(any(VerificationRequest.class))).thenAnswer(inv -> {
				VerificationRequest vr = inv.getArgument(0);
				vr.setId(1L);
				return vr;
			});
			when(jwtProvider.generateToken("new@example.com", "SUPPLIER")).thenReturn("jwt-token");

			String token = authService.register(request);

			assertThat(token).isEqualTo("jwt-token");
			verify(companyRepository).save(any(Company.class));
			verify(userRepository).save(any(User.class));
			verify(verificationRequestRepository).save(any(VerificationRequest.class));
		}

		@Test
		@DisplayName("Should throw exception for duplicate email")
		void shouldThrowExceptionForDuplicateEmail() {
			RegisterRequest request = RegisterRequest.builder()
					.email("existing@example.com")
					.type("SUPPLIER")
					.legalForm("LLC")
					.build();

			when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

			assertThatThrownBy(() -> authService.register(request))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("User with this email already exists");
		}

		@Test
		@DisplayName("Should register retail chain successfully")
		void shouldRegisterRetailChainSuccessfully() {
			RegisterRequest request = RegisterRequest.builder()
					.type("RETAIL_CHAIN")
					.email("retail@example.com")
					.password("password123")
					.passwordConfirm("password123")
					.legalName("Retail Chain LLC")
					.legalForm("LLC")
					.taxId("1111111111")
					.registrationDate(LocalDate.now())
					.contactPhone("+375291111111")
					.legalAddress("г. Минск, ул. Ритейл, 10")
					.build();

			when(userRepository.existsByEmail("retail@example.com")).thenReturn(false);
			when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
			when(companyRepository.save(any(Company.class))).thenAnswer(inv -> {
				Company c = inv.getArgument(0);
				c.setId(2L);
				return c;
			});
			when(userRepository.save(any(User.class))).thenAnswer(inv -> {
				User u = inv.getArgument(0);
				u.setId(2L);
				return u;
			});
			when(verificationRequestRepository.save(any(VerificationRequest.class))).thenAnswer(inv -> {
				VerificationRequest vr = inv.getArgument(0);
				vr.setId(2L);
				return vr;
			});
			when(jwtProvider.generateToken("retail@example.com", "RETAIL_CHAIN")).thenReturn("retail-jwt-token");

			String token = authService.register(request);

			assertThat(token).isEqualTo("retail-jwt-token");
		}

		@Test
		@DisplayName("Should register with bank account")
		void shouldRegisterWithBankAccount() {
			RegisterRequest request = RegisterRequest.builder()
					.type("SUPPLIER")
					.email("bank@example.com")
					.password("password123")
					.name("Bank Company")
					.legalName("Bank Company LLC")
					.legalForm("LLC")
					.taxId("2222222222")
					.registrationDate(LocalDate.now())
					.contactPhone("+375292222222")
					.legalAddress("Address")
					.bankName("Test Bank")
					.bic("TESTBIC123")
					.accountNumber("BY00TEST0000000000000000000")
					.build();

			when(userRepository.existsByEmail("bank@example.com")).thenReturn(false);
			when(passwordEncoder.encode(anyString())).thenReturn("encoded");
			when(companyRepository.save(any(Company.class))).thenAnswer(inv -> {
				Company c = inv.getArgument(0);
				c.setId(3L);
				return c;
			});
			when(userRepository.save(any(User.class))).thenAnswer(inv -> {
				User u = inv.getArgument(0);
				u.setId(3L);
				return u;
			});
			when(verificationRequestRepository.save(any(VerificationRequest.class))).thenAnswer(inv -> {
				VerificationRequest vr = inv.getArgument(0);
				vr.setId(3L);
				return vr;
			});
			when(addressRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
			when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(inv -> {
				BankAccount ba = inv.getArgument(0);
				ba.setId(1L);
				return ba;
			});
			doNothing().when(eventPublisher).publish(anyString(), anyString(), anyString(), any());
			when(jwtProvider.generateToken(anyString(), anyString())).thenReturn("token");

			authService.register(request);

			verify(bankAccountRepository).save(any(BankAccount.class));
		}

		@Test
		@DisplayName("Should register with responsible persons")
		void shouldRegisterWithResponsiblePersons() {
			RegisterRequest request = RegisterRequest.builder()
					.type("SUPPLIER")
					.email("persons@example.com")
					.password("password123")
					.name("Persons Company")
					.legalName("Persons Company LLC")
					.legalForm("LLC")
					.taxId("3333333333")
					.registrationDate(LocalDate.now())
					.contactPhone("+375293333333")
					.legalAddress("Address")
					.directorFio("Иванов Иван Иванович")
					.chiefAccountantFio("Петрова Мария Сергеевна")
					.build();

			when(userRepository.existsByEmail("persons@example.com")).thenReturn(false);
			when(passwordEncoder.encode(anyString())).thenReturn("encoded");
			when(companyRepository.save(any(Company.class))).thenAnswer(inv -> {
				Company c = inv.getArgument(0);
				c.setId(4L);
				return c;
			});
			when(userRepository.save(any(User.class))).thenAnswer(inv -> {
				User u = inv.getArgument(0);
				u.setId(4L);
				return u;
			});
			when(verificationRequestRepository.save(any(VerificationRequest.class))).thenAnswer(inv -> {
				VerificationRequest vr = inv.getArgument(0);
				vr.setId(4L);
				return vr;
			});
			when(addressRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
			when(responsiblePersonRepository.saveAll(anyList())).thenAnswer(inv -> {
				List<ResponsiblePerson> persons = inv.getArgument(0);
				long id = 1L;
				for (ResponsiblePerson p : persons) {
					p.setId(id++);
				}
				return persons;
			});
			doNothing().when(eventPublisher).publish(anyString(), anyString(), anyString(), any());
			when(jwtProvider.generateToken(anyString(), anyString())).thenReturn("token");

			authService.register(request);

			verify(responsiblePersonRepository).saveAll(anyList());
		}

		@Test
		@DisplayName("Should register admin as active")
		void shouldRegisterAdminAsActive() {
			RegisterRequest request = RegisterRequest.builder()
					.type("ADMIN")
					.email("admin@example.com")
					.password("adminpass")
					.name("Admin Company")
					.legalName("Admin Company LLC")
					.legalForm("LLC")
					.taxId("4444444444")
					.registrationDate(LocalDate.now())
					.contactPhone("+375294444444")
					.legalAddress("Admin Address")
					.build();

			when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);
			when(passwordEncoder.encode(anyString())).thenReturn("encoded");
			when(companyRepository.save(any(Company.class))).thenAnswer(inv -> {
				Company c = inv.getArgument(0);
				c.setId(5L);
				return c;
			});
			when(userRepository.save(any(User.class))).thenAnswer(inv -> {
				User u = inv.getArgument(0);
				u.setId(5L);
				return u;
			});
			when(verificationRequestRepository.save(any(VerificationRequest.class))).thenAnswer(inv -> {
				VerificationRequest vr = inv.getArgument(0);
				vr.setId(5L);
				return vr;
			});
			doNothing().when(eventPublisher).publish(anyString(), anyString(), anyString(), any());
			when(jwtProvider.generateToken(anyString(), anyString())).thenReturn("admin-token");

			authService.register(request);

			verify(userRepository).save(argThat(u -> u.getRole() == User.Role.ADMIN && u.getIsActive()));
		}
	}
}
