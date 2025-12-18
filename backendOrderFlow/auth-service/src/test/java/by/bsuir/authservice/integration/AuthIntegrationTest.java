package by.bsuir.authservice.integration;

import by.bsuir.authservice.entity.Company;
import by.bsuir.authservice.entity.User;
import by.bsuir.authservice.repository.CompanyRepository;
import by.bsuir.authservice.repository.UserRepository;
import by.bsuir.authservice.service.JwtProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CompanyRepository companyRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtProvider jwtProvider;

	private User testUser;
	private Company testCompany;
	private String testEmail;
	private String uniqueId;

	@BeforeEach
	void setUp() {
		// Generate unique identifiers to avoid constraint violations
		uniqueId = UUID.randomUUID().toString().substring(0, 8);
		String uniqueTaxId = uniqueId + "12345";
		testEmail = "integration" + uniqueId + "@test.com";

		testCompany = Company.builder()
				.name("Integration Test Company " + uniqueId)
				.legalName("LLC Integration Test Company " + uniqueId)
				.legalForm(Company.LegalForm.LLC)
				.taxId(uniqueTaxId)
				.registrationDate(LocalDate.now())
				.status(Company.CompanyStatus.ACTIVE)
				.contactPhone("+375291234567")
				.build();
		testCompany = companyRepository.save(testCompany);

		testUser = User.builder()
				.email(testEmail)
				.passwordHash(passwordEncoder.encode("password123"))
				.role(User.Role.SUPPLIER)
				.isActive(true)
				.company(testCompany)
				.createdAt(LocalDateTime.now())
				.build();
		testUser = userRepository.save(testUser);
	}

	@Test
	@DisplayName("Should login with valid credentials")
	void shouldLoginWithValidCredentials() throws Exception {
		Map<String, String> loginRequest = Map.of(
				"email", testEmail,
				"password", "password123"
		);

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(loginRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").exists())
				.andExpect(jsonPath("$.email").value(testEmail))
				.andExpect(jsonPath("$.role").value("SUPPLIER"));
	}

	@Test
	@DisplayName("Should reject login with invalid password")
	void shouldRejectLoginWithInvalidPassword() throws Exception {
		Map<String, String> loginRequest = Map.of(
				"email", testEmail,
				"password", "wrongpassword"
		);

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(loginRequest)))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("Should get user profile with valid token")
	void shouldGetUserProfileWithValidToken() throws Exception {
		mockMvc.perform(get("/api/auth/profile")
						.header("X-User-Email", testEmail)
						.header("X-User-Role", "SUPPLIER"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value(testEmail))
				.andExpect(jsonPath("$.role").value("SUPPLIER"));
	}

	@Test
	@DisplayName("Should get company profile")
	void shouldGetCompanyProfile() throws Exception {
		mockMvc.perform(get("/api/auth/company/" + testCompany.getId())
						.header("X-User-Email", testEmail)
						.header("X-User-Role", "SUPPLIER"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.legalName").value("LLC Integration Test Company " + uniqueId))
				.andExpect(jsonPath("$.taxId").value(uniqueId + "12345"));
	}

	@Test
	@DisplayName("Should validate token")
	void shouldValidateToken() throws Exception {
		mockMvc.perform(get("/api/auth/validate")
						.header("X-User-Email", testEmail)
						.header("X-User-Role", "SUPPLIER"))
				.andExpect(status().isOk())
				.andExpect(content().string("Token is valid for user: " + testEmail));
	}
}
