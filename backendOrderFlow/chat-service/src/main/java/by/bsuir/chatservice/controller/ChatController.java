package by.bsuir.chatservice.controller;

import by.bsuir.chatservice.dto.*;
import by.bsuir.chatservice.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "API для управления чатами по заказам")
public class ChatController {

	private final ChatService chatService;

	@PostMapping
	@Operation(summary = "Создать канал чата для заказа")
	public ResponseEntity<ApiResponse<ChatChannelResponse>> createChannel(
			@Valid @RequestBody CreateChannelRequest request) {
		ChatChannelResponse response = chatService.createChannel(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
	}

	@GetMapping("/order/{orderId}")
	@Operation(summary = "Получить канал чата по ID заказа")
	public ResponseEntity<ApiResponse<ChatChannelResponse>> getChannelByOrderId(
			@PathVariable Long orderId,
			@Parameter(description = "ID пользователя") @RequestHeader("X-User-Id") Long userId) {
		ChatChannelResponse response = chatService.getChannelByOrderId(orderId, userId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping
	@Operation(summary = "Получить список чатов пользователя")
	public ResponseEntity<ApiResponse<List<ChatChannelResponse>>> getUserChannels(
			@Parameter(description = "ID пользователя") @RequestHeader("X-User-Id") Long userId) {
		List<ChatChannelResponse> response = chatService.getUserChannels(userId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/order/{orderId}/messages")
	@Operation(summary = "Получить сообщения чата")
	public ResponseEntity<ApiResponse<PageResponse<MessageResponse>>> getMessages(
			@PathVariable Long orderId,
			@Parameter(description = "ID пользователя") @RequestHeader("X-User-Id") Long userId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size) {
		PageResponse<MessageResponse> response = chatService.getMessages(orderId, userId, page, size);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PostMapping("/order/{orderId}/messages")
	@Operation(summary = "Отправить сообщение в чат")
	public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
			@PathVariable Long orderId,
			@Parameter(description = "ID пользователя") @RequestHeader("X-User-Id") Long userId,
			@Valid @RequestBody SendMessageRequest request) {
		MessageResponse response = chatService.sendMessage(orderId, userId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
	}

	@PostMapping("/order/{orderId}/read")
	@Operation(summary = "Пометить сообщения как прочитанные")
	public ResponseEntity<ApiResponse<Void>> markAsRead(
			@PathVariable Long orderId,
			@Parameter(description = "ID пользователя") @RequestHeader("X-User-Id") Long userId) {
		chatService.markMessagesAsRead(orderId, userId);
		return ResponseEntity.ok(ApiResponse.success(null, "Messages marked as read"));
	}

	@PostMapping("/order/{orderId}/deactivate")
	@Operation(summary = "Деактивировать канал чата (после закрытия заказа)")
	public ResponseEntity<ApiResponse<Void>> deactivateChannel(@PathVariable Long orderId) {
		chatService.deactivateChannel(orderId);
		return ResponseEntity.ok(ApiResponse.success(null, "Channel deactivated"));
	}
}
