package by.bsuir.authservice.controller;

import by.bsuir.authservice.entity.Notification;
import by.bsuir.authservice.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management API")
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping
	@Operation(summary = "Get user notifications with pagination")
	public ResponseEntity<Map<String, Object>> getNotifications(
			@RequestHeader("X-User-Id") Long userId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {

		Page<Notification> notificationsPage = notificationService.getUserNotifications(userId, page, size);

		List<Map<String, Object>> content = notificationsPage.getContent().stream()
				.map(this::mapNotification)
				.collect(Collectors.toList());

		Map<String, Object> response = new LinkedHashMap<>();
		response.put("content", content);
		response.put("page", notificationsPage.getNumber());
		response.put("size", notificationsPage.getSize());
		response.put("totalElements", notificationsPage.getTotalElements());
		response.put("totalPages", notificationsPage.getTotalPages());

		return ResponseEntity.ok(response);
	}

	@GetMapping("/unread-count")
	@Operation(summary = "Get unread notifications count")
	public ResponseEntity<Map<String, Object>> getUnreadCount(@RequestHeader("X-User-Id") Long userId) {
		long count = notificationService.getUnreadCount(userId);
		return ResponseEntity.ok(Map.of("unreadCount", count));
	}

	@PostMapping("/{id}/read")
	@Operation(summary = "Mark notification as read")
	public ResponseEntity<Map<String, String>> markAsRead(
			@PathVariable Long id,
			@RequestHeader("X-User-Id") Long userId) {
		boolean success = notificationService.markAsRead(id, userId);
		if (success) {
			return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
		}
		return ResponseEntity.notFound().build();
	}

	@PostMapping("/read-all")
	@Operation(summary = "Mark all notifications as read")
	public ResponseEntity<Map<String, Object>> markAllAsRead(@RequestHeader("X-User-Id") Long userId) {
		int updated = notificationService.markAllAsRead(userId);
		return ResponseEntity.ok(Map.of("message", "All notifications marked as read", "updated", updated));
	}

	private Map<String, Object> mapNotification(Notification n) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("id", n.getId());
		map.put("title", n.getTitle());
		map.put("message", n.getMessage());
		map.put("type", n.getType().name());
		map.put("isRead", n.getIsRead());
		map.put("createdAt", n.getCreatedAt());
		map.put("relatedEntityType", n.getRelatedEntityType());
		map.put("relatedEntityId", n.getRelatedEntityId());
		return map;
	}
}

