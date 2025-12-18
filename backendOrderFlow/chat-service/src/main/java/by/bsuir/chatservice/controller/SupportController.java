package by.bsuir.chatservice.controller;

import by.bsuir.chatservice.dto.*;
import by.bsuir.chatservice.service.SupportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/support/tickets")
@RequiredArgsConstructor
@Tag(name = "Support Tickets", description = "API для работы с тикетами поддержки")
public class SupportController {

	private final SupportService supportService;

	@PostMapping
	@Operation(summary = "Создать тикет поддержки")
	public ResponseEntity<ApiResponse<TicketResponse>> createTicket(
			@RequestHeader("X-User-Company-Id") Long companyId,
			@RequestHeader("X-User-Id") Long userId,
			@Valid @RequestBody CreateTicketRequest request) {
		TicketResponse response = supportService.createTicket(companyId, userId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
	}

	@GetMapping("/{ticketId}")
	@Operation(summary = "Получить тикет по ID")
	public ResponseEntity<ApiResponse<TicketResponse>> getTicket(
			@PathVariable Long ticketId,
			@RequestHeader("X-User-Id") Long userId,
			@RequestHeader(value = "X-User-Role", defaultValue = "USER") String role) {
		boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
		TicketResponse response = supportService.getTicket(ticketId, userId, isAdmin);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping
	@Operation(summary = "Получить тикеты пользователя")
	public ResponseEntity<ApiResponse<PageResponse<TicketResponse>>> getUserTickets(
			@RequestHeader("X-User-Company-Id") Long companyId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		PageResponse<TicketResponse> response = supportService.getUserTickets(companyId, page, size);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/admin")
	@Operation(summary = "Получить все тикеты (для администратора)")
	public ResponseEntity<ApiResponse<PageResponse<TicketResponse>>> getAllTickets(
			@RequestParam(required = false) String status,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		PageResponse<TicketResponse> response = supportService.getAllTickets(status, page, size);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/{ticketId}/messages")
	@Operation(summary = "Получить сообщения тикета")
	public ResponseEntity<ApiResponse<List<TicketMessageResponse>>> getTicketMessages(
			@PathVariable Long ticketId,
			@RequestHeader("X-User-Id") Long userId,
			@RequestHeader(value = "X-User-Role", defaultValue = "USER") String role) {
		boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
		List<TicketMessageResponse> response = supportService.getTicketMessages(ticketId, userId, isAdmin);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PostMapping("/{ticketId}/messages")
	@Operation(summary = "Добавить сообщение в тикет")
	public ResponseEntity<ApiResponse<TicketMessageResponse>> addMessage(
			@PathVariable Long ticketId,
			@RequestHeader("X-User-Id") Long userId,
			@RequestHeader(value = "X-User-Role", defaultValue = "USER") String role,
			@Valid @RequestBody TicketMessageRequest request) {
		boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
		TicketMessageResponse response = supportService.addMessage(ticketId, userId, isAdmin, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
	}

	@PostMapping("/{ticketId}/assign")
	@Operation(summary = "Назначить тикет на администратора")
	public ResponseEntity<ApiResponse<TicketResponse>> assignTicket(
			@PathVariable Long ticketId,
			@RequestHeader("X-User-Id") Long adminId) {
		TicketResponse response = supportService.assignTicket(ticketId, adminId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PostMapping("/{ticketId}/resolve")
	@Operation(summary = "Решить тикет")
	public ResponseEntity<ApiResponse<TicketResponse>> resolveTicket(@PathVariable Long ticketId) {
		TicketResponse response = supportService.resolveTicket(ticketId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PostMapping("/{ticketId}/close")
	@Operation(summary = "Закрыть тикет")
	public ResponseEntity<ApiResponse<TicketResponse>> closeTicket(@PathVariable Long ticketId) {
		TicketResponse response = supportService.closeTicket(ticketId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PostMapping("/{ticketId}/reopen")
	@Operation(summary = "Переоткрыть тикет")
	public ResponseEntity<ApiResponse<TicketResponse>> reopenTicket(@PathVariable Long ticketId) {
		TicketResponse response = supportService.reopenTicket(ticketId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
