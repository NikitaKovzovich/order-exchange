package by.bsuir.authservice.controller;

import by.bsuir.authservice.DTO.*;
import by.bsuir.authservice.entity.*;
import by.bsuir.authservice.repository.*;
import by.bsuir.authservice.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin management API")
public class AdminController {
	private final UserRepository userRepository;
	private final CompanyRepository companyRepository;
	private final VerificationRequestRepository verificationRequestRepository;
	private final BankAccountRepository bankAccountRepository;
	private final ResponsiblePersonRepository responsiblePersonRepository;
	private final CompanyDocumentRepository companyDocumentRepository;
	private final AddressRepository addressRepository;
	private final SupplierSettingsRepository supplierSettingsRepository;
	private final EventPublisher eventPublisher;
	private final EventRepository eventRepository;
	private final FileStorageService fileStorageService;
	private final NotificationService notificationService;
	private final OrderServiceClient orderServiceClient;
	private final ChatServiceClient chatServiceClient;

	@GetMapping("/dashboard/stats")
	@Operation(summary = "Get dashboard KPI stats")
	public ResponseEntity<Map<String, Object>> getDashboardStats() {
		Map<String, Object> stats = new HashMap<>();

		long totalUsers = userRepository.count() - userRepository.countByRole(User.Role.ADMIN);
		long weekGrowth = userRepository.countByCreatedAtAfter(LocalDateTime.now().minusWeeks(1));
		long pendingVerifications = verificationRequestRepository.countByStatus(
				VerificationRequest.VerificationStatus.PENDING);

		stats.put("totalUsers", totalUsers);
		stats.put("usersWeekGrowth", weekGrowth);
		stats.put("pendingVerifications", pendingVerifications);

		return ResponseEntity.ok(stats);
	}

	@GetMapping("/dashboard/users-stats")
	@Operation(summary = "Get users statistics breakdown")
	public ResponseEntity<Map<String, Object>> getUsersStats() {
		Map<String, Object> stats = new HashMap<>();

		stats.put("total", userRepository.count());
		stats.put("suppliers", userRepository.countByRole(User.Role.SUPPLIER));
		stats.put("retailers", userRepository.countByRole(User.Role.RETAIL_CHAIN));
		stats.put("admins", userRepository.countByRole(User.Role.ADMIN));
		stats.put("blocked", userRepository.countByStatus("BLOCKED"));
		stats.put("suppliersWeekGrowth", userRepository.countByRoleAndCreatedAtAfter(
				User.Role.SUPPLIER, LocalDateTime.now().minusWeeks(1)));
		stats.put("retailersWeekGrowth", userRepository.countByRoleAndCreatedAtAfter(
				User.Role.RETAIL_CHAIN, LocalDateTime.now().minusWeeks(1)));

		return ResponseEntity.ok(stats);
	}

	@GetMapping("/dashboard/recent-registrations")
	@Operation(summary = "Get recent pending registration requests for dashboard")
	public ResponseEntity<List<Map<String, Object>>> getRecentRegistrations() {
		List<VerificationRequest> recent = verificationRequestRepository
				.findTop5ByStatusOrderByCreatedAtDesc(VerificationRequest.VerificationStatus.PENDING);

		List<Map<String, Object>> result = recent.stream().map(req -> {
			Map<String, Object> map = new LinkedHashMap<>();
			map.put("id", req.getId());
			map.put("companyName", req.getCompany().getLegalName());
			map.put("role", req.getUser().getRole().name());
			map.put("date", req.getCreatedAt());
			return map;
		}).collect(Collectors.toList());

		return ResponseEntity.ok(result);
	}

	@GetMapping("/dashboard/orders-stats")
	@Operation(summary = "Get orders and revenue stats from order-service (cross-service)")
	public ResponseEntity<Map<String, Object>> getOrdersStats() {
		Map<String, Object> analytics = orderServiceClient.getOverallAnalytics();
		Map<String, Object> stats = new LinkedHashMap<>();
		stats.put("totalOrdersThisMonth", analytics.getOrDefault("totalOrders", 0));
		stats.put("totalRevenue", analytics.getOrDefault("totalRevenue", 0));
		stats.put("revenueGrowthPercent", analytics.getOrDefault("revenueGrowthPercent", 0));
		stats.put("orderGrowthPercent", analytics.getOrDefault("orderGrowthPercent", 0));
		return ResponseEntity.ok(stats);
	}

	@GetMapping("/dashboard/registration-activity")
	@Operation(summary = "Get registration activity data for chart")
	public ResponseEntity<List<Map<String, Object>>> getRegistrationActivity(
			@RequestParam(defaultValue = "30") int days) {
		LocalDateTime since = LocalDateTime.now().minusDays(days);
		List<Object[]> raw = userRepository.countRegistrationsPerDay(since);

		List<Map<String, Object>> result = new ArrayList<>();
		for (Object[] row : raw) {
			Map<String, Object> point = new LinkedHashMap<>();
			point.put("date", row[0] != null ? row[0].toString() : null);
			point.put("count", row[1]);
			result.add(point);
		}

		return ResponseEntity.ok(result);
	}

	@GetMapping("/dashboard/recent-activity")
	@Operation(summary = "Get recent system activity feed (last N events)")
	public ResponseEntity<List<Map<String, Object>>> getRecentActivity(
			@RequestParam(defaultValue = "10") int limit) {
		Pageable pageable = PageRequest.of(0, Math.min(limit, 50));
		Page<Event> events = eventRepository.findAllByOrderByCreatedAtDesc(pageable);

		List<Map<String, Object>> result = events.getContent().stream().map(e -> {
			Map<String, Object> map = new LinkedHashMap<>();
			map.put("id", e.getId());
			map.put("aggregateType", e.getAggregateType());
			map.put("aggregateId", e.getAggregateId());
			map.put("eventType", e.getEventType());
			map.put("createdAt", e.getCreatedAt());
			return map;
		}).collect(Collectors.toList());

		return ResponseEntity.ok(result);
	}

	@GetMapping("/dashboard/recent-support-tickets")
	@Operation(summary = "Get recent support tickets from chat-service (ТЗ: Последние обращения в поддержку)")
	public ResponseEntity<List<Map<String, Object>>> getRecentSupportTickets(
			@RequestParam(defaultValue = "5") int limit) {
		List<Map<String, Object>> tickets = chatServiceClient.getRecentTickets(limit);
		return ResponseEntity.ok(tickets);
	}

	@GetMapping("/dashboard/verification-rate")
	@Operation(summary = "Get verification completion rate")
	public ResponseEntity<Map<String, Object>> getVerificationRate() {
		long total = verificationRequestRepository.count();
		long approved = verificationRequestRepository.countByStatus(
				VerificationRequest.VerificationStatus.APPROVED);
		long rejected = verificationRequestRepository.countByStatus(
				VerificationRequest.VerificationStatus.REJECTED);
		long pending = verificationRequestRepository.countByStatus(
				VerificationRequest.VerificationStatus.PENDING);

		Map<String, Object> stats = new LinkedHashMap<>();
		stats.put("total", total);
		stats.put("approved", approved);
		stats.put("rejected", rejected);
		stats.put("pending", pending);
		stats.put("approvalRate", total > 0 ? Math.round((double) approved / total * 100) : 0);
		return ResponseEntity.ok(stats);
	}

	@GetMapping("/users")
	@Operation(summary = "Get users with pagination, role filter, status filter and search")
	public ResponseEntity<Map<String, Object>> getUsers(
			@RequestParam(value = "role", required = false) String role,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "search", required = false) String search,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {

		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		Page<User> usersPage;

		User.Role roleFilter = null;
		if (role != null && !role.isEmpty() && !"ALL".equalsIgnoreCase(role)) {
			roleFilter = User.Role.valueOf(role.toUpperCase());
		}

		boolean hasSearch = search != null && !search.trim().isEmpty();
		boolean hasStatus = status != null && !status.isEmpty() && !"ALL".equalsIgnoreCase(status);

		if (roleFilter != null) {
			if (hasSearch && hasStatus) {
				usersPage = userRepository.findByRoleAndStatusAndSearch(roleFilter, status, search.trim(), pageable);
			} else if (hasSearch) {
				usersPage = userRepository.findByRoleAndSearch(roleFilter, search.trim(), pageable);
			} else if (hasStatus) {
				usersPage = userRepository.findByRoleAndStatus(roleFilter, status, pageable);
			} else {
				usersPage = userRepository.findByRole(roleFilter, pageable);
			}
		} else {
			if (hasSearch && hasStatus) {
				usersPage = userRepository.findByStatusAndSearch(status, search.trim(), pageable);
			} else if (hasSearch) {
				usersPage = userRepository.findBySearch(search.trim(), pageable);
			} else if (hasStatus) {
				usersPage = userRepository.findByStatus(status, pageable);
			} else {
				usersPage = userRepository.findAll(pageable);
			}
		}

		List<Map<String, Object>> content = usersPage.getContent().stream().map(this::mapUserToResponse).collect(Collectors.toList());

		Map<String, Object> response = new LinkedHashMap<>();
		response.put("content", content);
		response.put("page", usersPage.getNumber());
		response.put("size", usersPage.getSize());
		response.put("totalElements", usersPage.getTotalElements());
		response.put("totalPages", usersPage.getTotalPages());
		response.put("first", usersPage.isFirst());
		response.put("last", usersPage.isLast());

		return ResponseEntity.ok(response);
	}

	@GetMapping("/users/{id}")
	@Operation(summary = "Get user full profile with company details and statistics")
	public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
		User user = userRepository.findById(id)
				.orElse(null);
		if (user == null) {
			return ResponseEntity.notFound().build();
		}

		Map<String, Object> response = mapUserToResponse(user);
		if (user.getCompany() != null) {
			Long companyId = user.getCompany().getId();
			Company company = user.getCompany();

			Map<String, Object> companyProfile = new LinkedHashMap<>();
			companyProfile.put("id", company.getId());
			companyProfile.put("name", company.getName());
			companyProfile.put("legalName", company.getLegalName());
			companyProfile.put("legalForm", company.getLegalForm().name());
			companyProfile.put("legalFormText", company.getLegalFormText());
			companyProfile.put("taxId", company.getTaxId());
			companyProfile.put("registrationDate", company.getRegistrationDate());
			companyProfile.put("status", company.getStatus().name());
			companyProfile.put("contactPhone", company.getContactPhone());
			companyProfile.put("verified", company.getVerified());
			List<Address> addresses = addressRepository.findByCompanyId(companyId);
			companyProfile.put("addresses", addresses.stream().map(addr -> {
				Map<String, Object> a = new LinkedHashMap<>();
				a.put("id", addr.getId());
				a.put("type", addr.getAddressType().name());
				a.put("fullAddress", addr.getFullAddress());
				a.put("isDefault", addr.getIsDefault());
				return a;
			}).collect(Collectors.toList()));
			bankAccountRepository.findByCompanyId(companyId).ifPresent(bank -> {
				companyProfile.put("bankName", bank.getBankName());
				companyProfile.put("bic", bank.getBic());
				companyProfile.put("accountNumber", bank.getAccountNumber());
			});
			List<ResponsiblePerson> persons = responsiblePersonRepository.findByCompanyId(companyId);
			persons.forEach(p -> {
				if (p.getPosition() == ResponsiblePerson.Position.director) {
					companyProfile.put("directorName", p.getFullName());
				} else if (p.getPosition() == ResponsiblePerson.Position.chief_accountant) {
					companyProfile.put("chiefAccountantName", p.getFullName());
				}
			});
			List<CompanyDocument> docs = companyDocumentRepository.findByCompanyId(companyId);
			companyProfile.put("documents", docs.stream().map(doc -> {
				Map<String, Object> d = new LinkedHashMap<>();
				d.put("id", doc.getId());
				d.put("type", doc.getDocumentType().name());
				d.put("originalFilename", doc.getOriginalFilename());
				d.put("downloadUrl", fileStorageService.getPresignedUrl(doc.getFilePath()));
				return d;
			}).collect(Collectors.toList()));
			if (user.getRole() == User.Role.SUPPLIER) {
				supplierSettingsRepository.findById(companyId).ifPresent(settings -> {
					companyProfile.put("paymentTerms", settings.getPaymentTerms().name());
				});
			}

			response.put("companyProfile", companyProfile);
			try {
				Map<String, Object> orderStats = orderServiceClient.getCompanyOrderStats(
						companyId, user.getRole().name());
				response.put("orderStats", orderStats);
			} catch (Exception e) {
				response.put("orderStats", Map.of("totalOrders", 0, "totalRevenue", 0));
			}
		}

		return ResponseEntity.ok(response);
	}

	@GetMapping("/users/{id}/events")
	@Operation(summary = "Get event history (audit trail) for a specific user and their company")
	public ResponseEntity<Map<String, Object>> getUserEvents(
			@PathVariable Long id,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {

		User user = userRepository.findById(id).orElse(null);
		if (user == null) {
			return ResponseEntity.notFound().build();
		}

		List<String> aggregateIds = new ArrayList<>();
		aggregateIds.add(id.toString());
		if (user.getCompany() != null) {
			aggregateIds.add(user.getCompany().getId().toString());
		}

		Pageable pageable = PageRequest.of(page, size);
		Page<Event> eventsPage = eventRepository.findByAggregateIdInOrderByCreatedAtDesc(aggregateIds, pageable);

		List<Map<String, Object>> content = eventsPage.getContent().stream().map(e -> {
			Map<String, Object> map = new LinkedHashMap<>();
			map.put("id", e.getId());
			map.put("aggregateType", e.getAggregateType());
			map.put("aggregateId", e.getAggregateId());
			map.put("eventType", e.getEventType());
			map.put("version", e.getVersion());
			map.put("createdAt", e.getCreatedAt());
			map.put("payload", e.getPayload());
			return map;
		}).collect(Collectors.toList());

		Map<String, Object> response = new LinkedHashMap<>();
		response.put("content", content);
		response.put("page", eventsPage.getNumber());
		response.put("totalElements", eventsPage.getTotalElements());
		response.put("totalPages", eventsPage.getTotalPages());

		return ResponseEntity.ok(response);
	}

	@PostMapping("/users/{id}/block")
	@Operation(summary = "Block user account")
	public ResponseEntity<Map<String, String>> blockUser(@PathVariable Long id) {
		User user = userRepository.findById(id).orElse(null);
		if (user == null) {
			return ResponseEntity.notFound().build();
		}

		user.setStatus("BLOCKED");
		user.setIsActive(false);
		user.setUpdatedAt(LocalDateTime.now());
		userRepository.save(user);

		if (user.getCompany() != null) {
			Company company = user.getCompany();
			company.setStatus(Company.CompanyStatus.BLOCKED);
			companyRepository.save(company);
		}

		eventPublisher.publish("User", id.toString(), "UserBlocked",
				Map.of("userId", id, "blockedAt", LocalDateTime.now().toString()));

		notificationService.createNotification(id,
				"Аккаунт заблокирован",
				"Ваш аккаунт был заблокирован администратором. Обратитесь в службу поддержки.",
				Notification.NotificationType.USER_BLOCKED,
				"User", id);

		return ResponseEntity.ok(Map.of("message", "User blocked"));
	}

	@PostMapping("/users/{id}/unblock")
	@Operation(summary = "Unblock user account")
	public ResponseEntity<Map<String, String>> unblockUser(@PathVariable Long id) {
		User user = userRepository.findById(id).orElse(null);
		if (user == null) {
			return ResponseEntity.notFound().build();
		}

		user.setStatus("ACTIVE");
		user.setIsActive(true);
		user.setUpdatedAt(LocalDateTime.now());
		userRepository.save(user);

		if (user.getCompany() != null) {
			Company company = user.getCompany();
			company.setStatus(Company.CompanyStatus.ACTIVE);
			companyRepository.save(company);
		}

		eventPublisher.publish("User", id.toString(), "UserUnblocked",
				Map.of("userId", id, "unblockedAt", LocalDateTime.now().toString()));

		notificationService.createNotification(id,
				"Аккаунт разблокирован",
				"Ваш аккаунт был разблокирован. Вы снова можете пользоваться платформой.",
				Notification.NotificationType.USER_UNBLOCKED,
				"User", id);

		return ResponseEntity.ok(Map.of("message", "User unblocked"));
	}

	@PutMapping("/users/{id}")
@Operation(summary = "Edit user data")
	public ResponseEntity<Map<String, String>> editUser(
			@PathVariable Long id,
			@RequestBody AdminUserUpdateRequest request) {
		User user = userRepository.findById(id).orElse(null);
		if (user == null) {
			return ResponseEntity.notFound().build();
		}
		if (request.getEmail() != null && !request.getEmail().isEmpty()
				&& !request.getEmail().equals(user.getEmail())) {
			if (userRepository.existsByEmail(request.getEmail())) {
				return ResponseEntity.badRequest().body(Map.of("error", "Email already in use"));
			}
			user.setEmail(request.getEmail());
		}

		Company company = user.getCompany();
		if (company != null) {
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
		}

		user.setUpdatedAt(LocalDateTime.now());
		userRepository.save(user);

		eventPublisher.publish("User", id.toString(), "UserEditedByAdmin",
				Map.of("userId", id, "editedAt", LocalDateTime.now().toString()));

		return ResponseEntity.ok(Map.of("message", "User updated successfully"));
	}

	@DeleteMapping("/users/{id}")
	@Operation(summary = "Delete user account (soft delete)")
	public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
		User user = userRepository.findById(id).orElse(null);
		if (user == null) {
			return ResponseEntity.notFound().build();
		}

		user.setStatus("DELETED");
		user.setIsActive(false);
		user.setUpdatedAt(LocalDateTime.now());
		userRepository.save(user);

		eventPublisher.publish("User", id.toString(), "UserDeleted",
				Map.of("userId", id, "deletedAt", LocalDateTime.now().toString()));

		return ResponseEntity.ok(Map.of("message", "User deleted"));
	}

	private Map<String, Object> mapUserToResponse(User user) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("id", user.getId());
		map.put("email", user.getEmail());
		map.put("role", user.getRole().name());
		map.put("status", user.getStatus() != null ? user.getStatus() : "ACTIVE");
		map.put("isActive", user.getIsActive());
		map.put("createdAt", user.getCreatedAt());
		if (user.getCompany() != null) {
			Map<String, Object> company = new LinkedHashMap<>();
			company.put("id", user.getCompany().getId());
			company.put("legalName", user.getCompany().getLegalName());
			company.put("taxId", user.getCompany().getTaxId());
			company.put("status", user.getCompany().getStatus().name());
			company.put("legalForm", user.getCompany().getLegalForm().name());
			company.put("registrationDate", user.getCompany().getRegistrationDate());
			map.put("company", company);
		}
		return map;
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
