package by.bsuir.orderservice.controller;

import by.bsuir.orderservice.dto.*;
import by.bsuir.orderservice.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Order notification API")
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping
	@Operation(summary = "Get notifications for company")
	public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getNotifications(
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long companyId,
			@RequestParam(required = false) Boolean unreadOnly,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		PageResponse<NotificationResponse> notifications =
				notificationService.getNotifications(companyId, unreadOnly, page, size);
		return ResponseEntity.ok(ApiResponse.success(notifications));
	}

	@GetMapping("/unread-count")
	@Operation(summary = "Get unread notification count")
	public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount(
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long companyId) {
		long count = notificationService.getUnreadCount(companyId);
		return ResponseEntity.ok(ApiResponse.success(new UnreadCountResponse(count)));
	}

	@PostMapping("/{id}/read")
	@Operation(summary = "Mark notification as read")
	public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
			@PathVariable Long id,
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long companyId) {
		NotificationResponse notification = notificationService.markAsRead(id, companyId);
		return ResponseEntity.ok(ApiResponse.success(notification, "Notification marked as read"));
	}

	@PostMapping("/read-all")
	@Operation(summary = "Mark all notifications as read")
	public ResponseEntity<ApiResponse<Void>> markAllAsRead(
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long companyId) {
		int count = notificationService.markAllAsRead(companyId);
		return ResponseEntity.ok(ApiResponse.success(null, count + " notifications marked as read"));
	}
}
