package by.bsuir.orderservice.controller;

import by.bsuir.orderservice.dto.*;
import by.bsuir.orderservice.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер для работы с корзиной покупателя
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "API для управления корзиной покупателя")
public class CartController {

	private final CartService cartService;

	@GetMapping
	@Operation(summary = "Получить корзину", description = "Возвращает текущую корзину покупателя")
	public ResponseEntity<ApiResponse<CartResponse>> getCart(
			@Parameter(description = "ID покупателя (компании)")
			@RequestHeader("X-User-Company-Id") Long customerId) {

		CartResponse response = cartService.getCart(customerId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PostMapping("/items")
	@Operation(summary = "Добавить товар в корзину")
	public ResponseEntity<ApiResponse<CartResponse>> addItem(
			@RequestHeader("X-User-Company-Id") Long customerId,
			@Valid @RequestBody AddToCartRequest request) {

		CartResponse response = cartService.addItem(customerId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
	}

	@PutMapping("/items/{productId}")
	@Operation(summary = "Обновить количество товара в корзине")
	public ResponseEntity<ApiResponse<CartResponse>> updateItemQuantity(
			@RequestHeader("X-User-Company-Id") Long customerId,
			@PathVariable Long productId,
			@Valid @RequestBody UpdateCartItemRequest request) {

		CartResponse response = cartService.updateItemQuantity(customerId, productId, request);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@DeleteMapping("/items/{productId}")
	@Operation(summary = "Удалить товар из корзины")
	public ResponseEntity<ApiResponse<CartResponse>> removeItem(
			@RequestHeader("X-User-Company-Id") Long customerId,
			@PathVariable Long productId) {

		CartResponse response = cartService.removeItem(customerId, productId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@DeleteMapping
	@Operation(summary = "Очистить корзину")
	public ResponseEntity<ApiResponse<Void>> clearCart(
			@RequestHeader("X-User-Company-Id") Long customerId) {

		cartService.clearCart(customerId);
		return ResponseEntity.ok(ApiResponse.success(null, "Cart cleared"));
	}

	@PostMapping("/checkout")
	@Operation(summary = "Оформить заказ",
			description = "Создает отдельные заказы для каждого поставщика из корзины")
	public ResponseEntity<ApiResponse<CheckoutResponse>> checkout(
			@RequestHeader("X-User-Company-Id") Long customerId,
			@Valid @RequestBody CheckoutRequest request) {

		CheckoutResponse response = cartService.checkout(customerId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
	}
}
