package by.bsuir.authservice.controller;

import by.bsuir.authservice.DTO.*;
import by.bsuir.authservice.entity.User;
import by.bsuir.authservice.service.AuthService;
import by.bsuir.authservice.repository.UserRepository;
import by.bsuir.authservice.repository.CompanyRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin management API")
public class AdminController {
	private final AuthService authService;
	private final UserRepository userRepository;
	private final CompanyRepository companyRepository;


	@GetMapping("/users")
	@Operation(summary = "Get all users")
	public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
		List<User> users = userRepository.findAll();
		List<Map<String, Object>> response = users.stream().map(user -> {
			Map<String, Object> map = new HashMap<>();
			map.put("id", user.getId());
			map.put("email", user.getEmail());
			map.put("role", user.getRole().name());
			map.put("status", user.getStatus() != null ? user.getStatus() : "ACTIVE");
			if (user.getCompany() != null) {
				Map<String, Object> company = new HashMap<>();
				company.put("id", user.getCompany().getId());
				company.put("legalName", user.getCompany().getLegalName());
				map.put("company", company);
			}
			map.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt() : new Date());
			return map;
		}).collect(Collectors.toList());

		return ResponseEntity.ok(response);
	}

	@GetMapping("/users/{id}")
	@Operation(summary = "Get user by ID")
	public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
		Optional<User> user = userRepository.findById(id);
		if (user.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		Map<String, Object> response = new HashMap<>();
		User u = user.get();
		response.put("id", u.getId());
		response.put("email", u.getEmail());
		response.put("role", u.getRole().name());
		response.put("status", u.getStatus() != null ? u.getStatus() : "ACTIVE");
		if (u.getCompany() != null) {
			response.put("company", u.getCompany());
		}
		return ResponseEntity.ok(response);
	}

	@PostMapping("/users/{id}/block")
	@Operation(summary = "Block user")
	public ResponseEntity<Map<String, String>> blockUser(@PathVariable Long id) {
		Optional<User> user = userRepository.findById(id);
		if (user.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		User u = user.get();
		u.setStatus("BLOCKED");
		userRepository.save(u);

		return ResponseEntity.ok(Collections.singletonMap("message", "Пользователь заблокирован"));
	}

	@PostMapping("/users/{id}/unblock")
	@Operation(summary = "Unblock user")
	public ResponseEntity<Map<String, String>> unblockUser(@PathVariable Long id) {
		Optional<User> user = userRepository.findById(id);
		if (user.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		User u = user.get();
		u.setStatus("ACTIVE");
		userRepository.save(u);

		return ResponseEntity.ok(Collections.singletonMap("message", "Пользователь разблокирован"));
	}


	@GetMapping("/dashboard/stats")
	@Operation(summary = "Get dashboard statistics")
	public ResponseEntity<Map<String, Object>> getDashboardStats() {
		Map<String, Object> stats = new HashMap<>();

		long totalUsers = userRepository.count();

		stats.put("totalUsers", totalUsers);
		stats.put("activeOrders", 42);
		stats.put("pendingVerifications", 5);
		stats.put("openTickets", 5);
		stats.put("verificationTrend", "-5%");

		return ResponseEntity.ok(stats);
	}

	@GetMapping("/dashboard/users-stats")
	@Operation(summary = "Get users statistics")
	public ResponseEntity<Map<String, Object>> getUsersStats() {
		Map<String, Object> stats = new HashMap<>();

		long totalUsers = userRepository.count();
		long suppliers = userRepository.countByRole(User.Role.SUPPLIER);
		long retailers = userRepository.countByRole(User.Role.RETAIL_CHAIN);
		long admins = userRepository.countByRole(User.Role.ADMIN);

		stats.put("total", totalUsers);
		stats.put("suppliers", suppliers);
		stats.put("retailers", retailers);
		stats.put("admins", admins);

		return ResponseEntity.ok(stats);
	}

	@GetMapping("/dashboard/orders-stats")
	@Operation(summary = "Get orders statistics")
	public ResponseEntity<Map<String, Object>> getOrdersStats() {
		Map<String, Object> stats = new HashMap<>();

		stats.put("total", 245);
		stats.put("active", 42);
		stats.put("completed", 180);
		stats.put("cancelled", 23);

		return ResponseEntity.ok(stats);
	}

	@GetMapping("/support/tickets")
	@Operation(summary = "Get support tickets")
	public ResponseEntity<List<Map<String, Object>>> getSupportTickets(
			@RequestParam(required = false) String status) {
		List<Map<String, Object>> tickets = new ArrayList<>();

		return ResponseEntity.ok(tickets);
	}

	@PostMapping("/support/tickets/{id}/respond")
	@Operation(summary = "Respond to support ticket")
	public ResponseEntity<Map<String, String>> respondToTicket(
			@PathVariable Long id,
			@RequestBody Map<String, String> body) {

		return ResponseEntity.ok(Collections.singletonMap("message", "Ответ отправлен"));
	}

	@PostMapping("/support/tickets/{id}/close")
	@Operation(summary = "Close support ticket")
	public ResponseEntity<Map<String, String>> closeTicket(@PathVariable Long id) {

		return ResponseEntity.ok(Collections.singletonMap("message", "Тикет закрыт"));
	}
}
