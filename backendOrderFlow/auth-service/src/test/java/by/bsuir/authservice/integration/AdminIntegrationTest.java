package by.bsuir.authservice.integration;

import by.bsuir.authservice.entity.Company;
import by.bsuir.authservice.entity.User;
import by.bsuir.authservice.repository.CompanyRepository;
import by.bsuir.authservice.repository.UserRepository;
import by.bsuir.authservice.repository.VerificationRequestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CompanyRepository companyRepository;

	@Autowired
	private VerificationRequestRepository verificationRequestRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private User adminUser;
	private User supplierUser;
	private Company testCompany;
	private String uniqueId;
	private String adminEmail;
	private String supplierEmail;

	@BeforeEach
	void setUp() {
		// Generate unique identifiers for each test run
		uniqueId = UUID.randomUUID().toString().substring(0, 8);
		String uniqueTaxId = uniqueId + "12345";
		adminEmail = "admin" + uniqueId + "@test.com";
		supplierEmail = "supplier" + uniqueId + "@test.com";

		testCompany = Company.builder()
				.name("Admin Test Company " + uniqueId)
				.legalName("LLC Admin Test Company " + uniqueId)
				.legalForm(Company.LegalForm.LLC)
				.taxId(uniqueTaxId)
				.registrationDate(LocalDate.now())
				.status(Company.CompanyStatus.PENDING_VERIFICATION)
				.contactPhone("+375297654321")
				.build();
		testCompany = companyRepository.save(testCompany);

		adminUser = User.builder()
				.email(adminEmail)
				.passwordHash(passwordEncoder.encode("adminpass"))
				.role(User.Role.ADMIN)
				.isActive(true)
				.createdAt(LocalDateTime.now())
				.build();
		adminUser = userRepository.save(adminUser);

		supplierUser = User.builder()
				.email(supplierEmail)
				.passwordHash(passwordEncoder.encode("supplierpass"))
				.role(User.Role.SUPPLIER)
				.isActive(true)
				.company(testCompany)
				.createdAt(LocalDateTime.now())
				.build();
		supplierUser = userRepository.save(supplierUser);
	}

	@Test
	@DisplayName("Should get all users")
	void shouldGetAllUsers() throws Exception {
		mockMvc.perform(get("/api/admin/users")
						.header("X-User-Email", adminEmail)
						.header("X-User-Role", "ADMIN"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray());
	}

	@Test
	@DisplayName("Should get user by ID")
	void shouldGetUserById() throws Exception {
		mockMvc.perform(get("/api/admin/users/" + supplierUser.getId())
						.header("X-User-Email", adminEmail)
						.header("X-User-Role", "ADMIN"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value(supplierEmail));
	}

	@Test
	@DisplayName("Should return 404 for non-existent user")
	void shouldReturn404ForNonExistentUser() throws Exception {
		mockMvc.perform(get("/api/admin/users/99999")
						.header("X-User-Email", adminEmail)
						.header("X-User-Role", "ADMIN"))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("Should block user")
	void shouldBlockUser() throws Exception {
		mockMvc.perform(post("/api/admin/users/" + supplierUser.getId() + "/block")
						.header("X-User-Email", adminEmail)
						.header("X-User-Role", "ADMIN"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Пользователь заблокирован"));
	}

	@Test
	@DisplayName("Should unblock user")
	void shouldUnblockUser() throws Exception {
		supplierUser.setStatus("BLOCKED");
		userRepository.save(supplierUser);

		mockMvc.perform(post("/api/admin/users/" + supplierUser.getId() + "/unblock")
						.header("X-User-Email", adminEmail)
						.header("X-User-Role", "ADMIN"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Пользователь разблокирован"));
	}

	@Test
	@DisplayName("Should get dashboard stats")
	void shouldGetDashboardStats() throws Exception {
		mockMvc.perform(get("/api/admin/dashboard/stats")
						.header("X-User-Email", adminEmail)
						.header("X-User-Role", "ADMIN"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalUsers").exists());
	}

	@Test
	@DisplayName("Should get users stats")
	void shouldGetUsersStats() throws Exception {
		mockMvc.perform(get("/api/admin/dashboard/users-stats")
						.header("X-User-Email", adminEmail)
						.header("X-User-Role", "ADMIN"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total").exists());
	}
}
