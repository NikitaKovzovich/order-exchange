package by.bsuir.authservice.controller;

import by.bsuir.authservice.entity.Company;
import by.bsuir.authservice.entity.User;
import by.bsuir.authservice.repository.CompanyRepository;
import by.bsuir.authservice.repository.UserRepository;
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
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private AuthService authService;

	@MockBean
	private UserRepository userRepository;

	@MockBean
	private CompanyRepository companyRepository;

	private User testUser;
	private User adminUser;
	private Company testCompany;

	@BeforeEach
	void setUp() {
		testCompany = Company.builder()
				.id(1L)
				.name("Test Company")
				.legalName("ООО Test Company")
				.legalForm(Company.LegalForm.LLC)
				.build();

		testUser = User.builder()
				.id(1L)
				.email("user@example.com")
				.role(User.Role.SUPPLIER)
				.status("ACTIVE")
				.company(testCompany)
				.createdAt(LocalDateTime.now())
				.build();

		adminUser = User.builder()
				.id(2L)
				.email("admin@example.com")
				.role(User.Role.ADMIN)
				.status("ACTIVE")
				.createdAt(LocalDateTime.now())
				.build();
	}

	@Nested
	@DisplayName("Get All Users Tests")
	class GetAllUsersTests {

		@Test
		@DisplayName("Should return all users")
		void shouldReturnAllUsers() throws Exception {
			when(userRepository.findAll()).thenReturn(List.of(testUser, adminUser));

			mockMvc.perform(get("/api/admin/users"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$").isArray())
					.andExpect(jsonPath("$.length()").value(2))
					.andExpect(jsonPath("$[0].email").value("user@example.com"))
					.andExpect(jsonPath("$[0].role").value("SUPPLIER"));
		}

		@Test
		@DisplayName("Should return empty list when no users")
		void shouldReturnEmptyList() throws Exception {
			when(userRepository.findAll()).thenReturn(List.of());

			mockMvc.perform(get("/api/admin/users"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$").isArray())
					.andExpect(jsonPath("$.length()").value(0));
		}

		@Test
		@DisplayName("Should return user with null status as ACTIVE")
		void shouldReturnUserWithNullStatusAsActive() throws Exception {
			testUser.setStatus(null);
			when(userRepository.findAll()).thenReturn(List.of(testUser));

			mockMvc.perform(get("/api/admin/users"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$[0].status").value("ACTIVE"));
		}
	}

	@Nested
	@DisplayName("Get User By Id Tests")
	class GetUserByIdTests {

		@Test
		@DisplayName("Should return user by id")
		void shouldReturnUserById() throws Exception {
			when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

			mockMvc.perform(get("/api/admin/users/1"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value(1))
					.andExpect(jsonPath("$.email").value("user@example.com"))
					.andExpect(jsonPath("$.role").value("SUPPLIER"));
		}

		@Test
		@DisplayName("Should return 404 when user not found")
		void shouldReturn404WhenUserNotFound() throws Exception {
			when(userRepository.findById(999L)).thenReturn(Optional.empty());

			mockMvc.perform(get("/api/admin/users/999"))
					.andExpect(status().isNotFound());
		}
	}

	@Nested
	@DisplayName("Block User Tests")
	class BlockUserTests {

		@Test
		@DisplayName("Should block user")
		void shouldBlockUser() throws Exception {
			when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
			when(userRepository.save(any(User.class))).thenReturn(testUser);

			mockMvc.perform(post("/api/admin/users/1/block"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.message").value("Пользователь заблокирован"));

			verify(userRepository).save(argThat(user -> "BLOCKED".equals(user.getStatus())));
		}

		@Test
		@DisplayName("Should return 404 when blocking non-existent user")
		void shouldReturn404WhenBlockingNonExistentUser() throws Exception {
			when(userRepository.findById(999L)).thenReturn(Optional.empty());

			mockMvc.perform(post("/api/admin/users/999/block"))
					.andExpect(status().isNotFound());
		}
	}

	@Nested
	@DisplayName("Unblock User Tests")
	class UnblockUserTests {

		@Test
		@DisplayName("Should unblock user")
		void shouldUnblockUser() throws Exception {
			testUser.setStatus("BLOCKED");
			when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
			when(userRepository.save(any(User.class))).thenReturn(testUser);

			mockMvc.perform(post("/api/admin/users/1/unblock"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.message").value("Пользователь разблокирован"));

			verify(userRepository).save(argThat(user -> "ACTIVE".equals(user.getStatus())));
		}

		@Test
		@DisplayName("Should return 404 when unblocking non-existent user")
		void shouldReturn404WhenUnblockingNonExistentUser() throws Exception {
			when(userRepository.findById(999L)).thenReturn(Optional.empty());

			mockMvc.perform(post("/api/admin/users/999/unblock"))
					.andExpect(status().isNotFound());
		}
	}

	@Nested
	@DisplayName("Dashboard Stats Tests")
	class DashboardStatsTests {

		@Test
		@DisplayName("Should return dashboard stats")
		void shouldReturnDashboardStats() throws Exception {
			when(userRepository.count()).thenReturn(100L);

			mockMvc.perform(get("/api/admin/dashboard/stats"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.totalUsers").value(100))
					.andExpect(jsonPath("$.activeOrders").value(42))
					.andExpect(jsonPath("$.pendingVerifications").value(5));
		}
	}

	@Nested
	@DisplayName("Users Stats Tests")
	class UsersStatsTests {

		@Test
		@DisplayName("Should return users stats")
		void shouldReturnUsersStats() throws Exception {
			when(userRepository.count()).thenReturn(100L);
			when(userRepository.countByRole(User.Role.SUPPLIER)).thenReturn(50L);
			when(userRepository.countByRole(User.Role.RETAIL_CHAIN)).thenReturn(40L);
			when(userRepository.countByRole(User.Role.ADMIN)).thenReturn(10L);

			mockMvc.perform(get("/api/admin/dashboard/users-stats"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.total").value(100))
					.andExpect(jsonPath("$.suppliers").value(50))
					.andExpect(jsonPath("$.retailers").value(40))
					.andExpect(jsonPath("$.admins").value(10));
		}
	}

	@Nested
	@DisplayName("Orders Stats Tests")
	class OrdersStatsTests {

		@Test
		@DisplayName("Should return orders stats")
		void shouldReturnOrdersStats() throws Exception {
			mockMvc.perform(get("/api/admin/dashboard/orders-stats"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.total").value(245))
					.andExpect(jsonPath("$.active").value(42))
					.andExpect(jsonPath("$.completed").value(180))
					.andExpect(jsonPath("$.cancelled").value(23));
		}
	}
}
