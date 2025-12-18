package by.bsuir.authservice.controller;

import by.bsuir.authservice.DTO.LoginRequest;
import by.bsuir.authservice.entity.Address;
import by.bsuir.authservice.entity.Company;
import by.bsuir.authservice.entity.User;
import by.bsuir.authservice.service.AddressService;
import by.bsuir.authservice.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private AuthService authService;

	@MockBean
	private AddressService addressService;

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
				.registrationDate(LocalDate.now())
				.status(Company.CompanyStatus.ACTIVE)
				.contactPhone("+375291234567")
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
		void shouldLoginSuccessfully() throws Exception {
			LoginRequest request = new LoginRequest("test@example.com", "password123");

			when(authService.login("test@example.com", "password123")).thenReturn("jwt-token");
			when(authService.getUserByEmail("test@example.com")).thenReturn(testUser);

			mockMvc.perform(post("/api/auth/login")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.token").value("jwt-token"))
					.andExpect(jsonPath("$.email").value("test@example.com"))
					.andExpect(jsonPath("$.role").value("SUPPLIER"))
					.andExpect(jsonPath("$.userId").value(1))
					.andExpect(jsonPath("$.companyId").value(1));
		}

		@Test
		@DisplayName("Should return 400 for invalid credentials")
		void shouldReturn400ForInvalidCredentials() throws Exception {
			LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");

			when(authService.login("test@example.com", "wrongpassword"))
					.thenThrow(new IllegalArgumentException("Invalid email or password"));

			mockMvc.perform(post("/api/auth/login")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isBadRequest());
		}
	}

	@Nested
	@DisplayName("Get Profile Tests")
	class GetProfileTests {

		@Test
		@DisplayName("Should get user profile")
		void shouldGetUserProfile() throws Exception {
			when(authService.getUserByEmail("test@example.com")).thenReturn(testUser);

			mockMvc.perform(get("/api/auth/profile")
							.header("X-User-Email", "test@example.com"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value(1))
					.andExpect(jsonPath("$.email").value("test@example.com"))
					.andExpect(jsonPath("$.role").value("SUPPLIER"))
					.andExpect(jsonPath("$.companyId").value(1));
		}

		@Test
		@DisplayName("Should get profile for user without company")
		void shouldGetProfileForUserWithoutCompany() throws Exception {
			testUser.setCompany(null);
			when(authService.getUserByEmail("test@example.com")).thenReturn(testUser);

			mockMvc.perform(get("/api/auth/profile")
							.header("X-User-Email", "test@example.com"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value(1))
					.andExpect(jsonPath("$.companyId").isEmpty());
		}
	}

	@Nested
	@DisplayName("Get Company Profile Tests")
	class GetCompanyProfileTests {

		@Test
		@DisplayName("Should get company profile")
		void shouldGetCompanyProfile() throws Exception {
			Address address = Address.builder()
					.id(1L)
					.company(testCompany)
					.addressType(Address.AddressType.legal)
					.fullAddress("г. Минск, ул. Тестовая, 1")
					.isDefault(true)
					.build();

			when(authService.getCompanyById(1L)).thenReturn(testCompany);
			when(addressService.getCompanyAddresses(1L)).thenReturn(List.of(address));

			mockMvc.perform(get("/api/auth/company/1"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value(1))
					.andExpect(jsonPath("$.legalName").value("ООО Test Company"))
					.andExpect(jsonPath("$.legalForm").value("LLC"))
					.andExpect(jsonPath("$.taxId").value("1234567890"))
					.andExpect(jsonPath("$.status").value("ACTIVE"))
					.andExpect(jsonPath("$.addresses").isArray())
					.andExpect(jsonPath("$.addresses[0].addressType").value("legal"));
		}

		@Test
		@DisplayName("Should get company profile with empty addresses")
		void shouldGetCompanyProfileWithEmptyAddresses() throws Exception {
			when(authService.getCompanyById(1L)).thenReturn(testCompany);
			when(addressService.getCompanyAddresses(1L)).thenReturn(List.of());

			mockMvc.perform(get("/api/auth/company/1"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.addresses").isArray())
					.andExpect(jsonPath("$.addresses").isEmpty());
		}
	}

	@Nested
	@DisplayName("Validate Token Tests")
	class ValidateTokenTests {

		@Test
		@DisplayName("Should validate token")
		void shouldValidateToken() throws Exception {
			mockMvc.perform(get("/api/auth/validate")
							.header("X-User-Email", "test@example.com"))
					.andExpect(status().isOk())
					.andExpect(content().string("Token is valid for user: test@example.com"));
		}
	}
}
