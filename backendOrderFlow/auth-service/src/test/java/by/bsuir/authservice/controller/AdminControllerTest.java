package by.bsuir.authservice.controller;

import by.bsuir.authservice.entity.Company;
import by.bsuir.authservice.entity.User;
import by.bsuir.authservice.entity.VerificationRequest;
import by.bsuir.authservice.repository.*;
import by.bsuir.authservice.service.EventPublisher;
import by.bsuir.authservice.service.FileStorageService;
import by.bsuir.authservice.service.NotificationService;
import by.bsuir.authservice.service.OrderServiceClient;
import by.bsuir.authservice.service.ChatServiceClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
	private UserRepository userRepository;

	@MockBean
	private CompanyRepository companyRepository;

	@MockBean
	private VerificationRequestRepository verificationRequestRepository;

	@MockBean
	private BankAccountRepository bankAccountRepository;

	@MockBean
	private ResponsiblePersonRepository responsiblePersonRepository;

	@MockBean
	private CompanyDocumentRepository companyDocumentRepository;

	@MockBean
	private AddressRepository addressRepository;

	@MockBean
	private SupplierSettingsRepository supplierSettingsRepository;

	@MockBean
	private EventPublisher eventPublisher;

	@MockBean
	private FileStorageService fileStorageService;

	@MockBean
	private EventRepository eventRepository;

	@MockBean
	private NotificationService notificationService;

	@MockBean
	private OrderServiceClient orderServiceClient;

	@MockBean
	private ChatServiceClient chatServiceClient;

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
				.taxId("123456789")
				.status(Company.CompanyStatus.ACTIVE)
				.build();

		testUser = User.builder()
				.id(1L)
				.email("user@example.com")
				.role(User.Role.SUPPLIER)
				.status("ACTIVE")
				.isActive(true)
				.company(testCompany)
				.createdAt(LocalDateTime.now())
				.build();

		adminUser = User.builder()
				.id(2L)
				.email("admin@example.com")
				.role(User.Role.ADMIN)
				.status("ACTIVE")
				.isActive(true)
				.createdAt(LocalDateTime.now())
				.build();
	}

	@Nested
	@DisplayName("Get Users Tests")
	class GetUsersTests {

		@Test
		@DisplayName("Should return paginated users")
		void shouldReturnPaginatedUsers() throws Exception {
			when(userRepository.findAll(any(Pageable.class)))
					.thenReturn(new PageImpl<>(List.of(testUser, adminUser)));

			mockMvc.perform(get("/api/admin/users"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.content").isArray())
					.andExpect(jsonPath("$.content.length()").value(2))
					.andExpect(jsonPath("$.content[0].email").value("user@example.com"))
					.andExpect(jsonPath("$.totalElements").value(2));
		}

		@Test
		@DisplayName("Should return empty page when no users")
		void shouldReturnEmptyPage() throws Exception {
			when(userRepository.findAll(any(Pageable.class)))
					.thenReturn(new PageImpl<>(List.of()));

			mockMvc.perform(get("/api/admin/users"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.content").isArray())
					.andExpect(jsonPath("$.content.length()").value(0));
		}
	}

	@Nested
	@DisplayName("Get User By Id Tests")
	class GetUserByIdTests {

		@Test
		@DisplayName("Should return user by id")
		void shouldReturnUserById() throws Exception {
			when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
			when(addressRepository.findByCompanyId(anyLong())).thenReturn(Collections.emptyList());
			when(bankAccountRepository.findByCompanyId(anyLong())).thenReturn(Optional.empty());
			when(responsiblePersonRepository.findByCompanyId(anyLong())).thenReturn(Collections.emptyList());
			when(companyDocumentRepository.findByCompanyId(anyLong())).thenReturn(Collections.emptyList());

			mockMvc.perform(get("/api/admin/users/1"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value(1))
					.andExpect(jsonPath("$.email").value("user@example.com"))
					.andExpect(jsonPath("$.role").value("SUPPLIER"))
					.andExpect(jsonPath("$.companyProfile").exists());
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
			when(companyRepository.save(any(Company.class))).thenReturn(testCompany);

			mockMvc.perform(post("/api/admin/users/1/block"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.message").value("User blocked"));

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
			when(companyRepository.save(any(Company.class))).thenReturn(testCompany);

			mockMvc.perform(post("/api/admin/users/1/unblock"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.message").value("User unblocked"));

			verify(userRepository).save(argThat(user -> "ACTIVE".equals(user.getStatus())));
		}
	}

	@Nested
	@DisplayName("Dashboard Stats Tests")
	class DashboardStatsTests {

		@Test
		@DisplayName("Should return real dashboard stats")
		void shouldReturnDashboardStats() throws Exception {
			when(userRepository.count()).thenReturn(100L);
			when(userRepository.countByRole(User.Role.ADMIN)).thenReturn(5L);
			when(userRepository.countByCreatedAtAfter(any(LocalDateTime.class))).thenReturn(10L);
			when(verificationRequestRepository.countByStatus(VerificationRequest.VerificationStatus.PENDING))
					.thenReturn(3L);

			mockMvc.perform(get("/api/admin/dashboard/stats"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.totalUsers").value(95))
					.andExpect(jsonPath("$.usersWeekGrowth").value(10))
					.andExpect(jsonPath("$.pendingVerifications").value(3));
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
			when(userRepository.countByStatus("BLOCKED")).thenReturn(2L);
			when(userRepository.countByRoleAndCreatedAtAfter(any(User.Role.class), any(LocalDateTime.class)))
					.thenReturn(5L);

			mockMvc.perform(get("/api/admin/dashboard/users-stats"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.total").value(100))
					.andExpect(jsonPath("$.suppliers").value(50))
					.andExpect(jsonPath("$.retailers").value(40))
					.andExpect(jsonPath("$.admins").value(10))
					.andExpect(jsonPath("$.blocked").value(2));
		}
	}

	@Nested
	@DisplayName("Delete User Tests")
	class DeleteUserTests {

		@Test
		@DisplayName("Should soft delete user")
		void shouldSoftDeleteUser() throws Exception {
			when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
			when(userRepository.save(any(User.class))).thenReturn(testUser);

			mockMvc.perform(delete("/api/admin/users/1"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.message").value("User deleted"));

			verify(userRepository).save(argThat(user -> "DELETED".equals(user.getStatus())));
		}

		@Test
		@DisplayName("Should return 404 when deleting non-existent user")
		void shouldReturn404WhenDeletingNonExistentUser() throws Exception {
			when(userRepository.findById(999L)).thenReturn(Optional.empty());

			mockMvc.perform(delete("/api/admin/users/999"))
					.andExpect(status().isNotFound());
		}
	}
}
