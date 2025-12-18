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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

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

	@BeforeEach
	void setUp() {
		userRepository.deleteAll();
		companyRepository.deleteAll();

		testCompany = Company.builder()
				.name("Integration Test Company")
				.legalName("LLC Integration Test Company")
				.legalForm(Company.LegalForm.LLC)
				.taxId("1234567890123")
				.registrationDate(LocalDate.now())
				.status(Company.CompanyStatus.ACTIVE)
				.contactPhone("+375291234567")
				.build();
		testCompany = companyRepository.save(testCompany);

		testUser = User.builder()
				.email("integration@test.com")
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
				"email", "integration@test.com",
				"password", "password123"
		);

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(loginRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").exists())
				.andExpect(jsonPath("$.email").value("integration@test.com"))
				.andExpect(jsonPath("$.role").value("SUPPLIER"));
	}

	@Test
	@DisplayName("Should reject login with invalid password")
	void shouldRejectLoginWithInvalidPassword() throws Exception {
		Map<String, String> loginRequest = Map.of(
				"email", "integration@test.com",
				"password", "wrongpassword"
		);

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(loginRequest)))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("Should get user profile with valid token")
	@WithMockUser(username = "integration@test.com", roles = {"SUPPLIER"})
	void shouldGetUserProfileWithValidToken() throws Exception {
		mockMvc.perform(get("/api/auth/profile")
						.header("X-User-Email", "integration@test.com"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("integration@test.com"))
				.andExpect(jsonPath("$.role").value("SUPPLIER"));
	}

	@Test
	@DisplayName("Should get company profile")
	@WithMockUser(username = "integration@test.com", roles = {"SUPPLIER"})
	void shouldGetCompanyProfile() throws Exception {
		mockMvc.perform(get("/api/auth/company/" + testCompany.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.legalName").value("LLC Integration Test Company"))
				.andExpect(jsonPath("$.taxId").value("1234567890123"));
	}

	@Test
	@DisplayName("Should validate token")
	@WithMockUser(username = "integration@test.com", roles = {"SUPPLIER"})
	void shouldValidateToken() throws Exception {
		mockMvc.perform(get("/api/auth/validate")
						.header("X-User-Email", "integration@test.com"))
				.andExpect(status().isOk())
				.andExpect(content().string("Token is valid for user: integration@test.com"));
	}
}
