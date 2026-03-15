package by.bsuir.orderservice.controller;

import by.bsuir.orderservice.dto.*;
import by.bsuir.orderservice.service.CartService;
import by.bsuir.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management API")
public class OrderController {
	private final OrderService orderService;
	private final CartService cartService;

	@GetMapping("/{id}")
	@Operation(summary = "Get order by ID")
	public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long id) {
		OrderResponse order = orderService.getOrderById(id);
		return ResponseEntity.ok(ApiResponse.success(order));
	}

	@GetMapping("/number/{orderNumber}")
	@Operation(summary = "Get order by order number")
	public ResponseEntity<ApiResponse<OrderResponse>> getOrderByNumber(@PathVariable String orderNumber) {
		OrderResponse order = orderService.getOrderByNumber(orderNumber);
		return ResponseEntity.ok(ApiResponse.success(order));
	}

	@GetMapping("/supplier")
	@Operation(summary = "Get orders for supplier with search & filters")
	public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getSupplierOrders(
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long supplierId,
			@RequestParam(required = false) String status,
			@RequestParam(required = false) @Parameter(description = "Search by order number") String search,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		PageResponse<OrderResponse> orders = orderService.getSupplierOrders(supplierId, status, search, dateFrom, dateTo, page, size);
		return ResponseEntity.ok(ApiResponse.success(orders));
	}

	@GetMapping("/customer")
	@Operation(summary = "Get orders for customer (retail chain) with search & filters")
	public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getCustomerOrders(
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long customerId,
			@RequestParam(required = false) String status,
			@RequestParam(required = false) @Parameter(description = "Search by order number") String search,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		PageResponse<OrderResponse> orders = orderService.getCustomerOrders(customerId, status, search, dateFrom, dateTo, page, size);
		return ResponseEntity.ok(ApiResponse.success(orders));
	}

	@GetMapping("/supplier/summary")
	@Operation(summary = "Get supplier order summary counts by status")
	public ResponseEntity<ApiResponse<OrderSummaryResponse>> getSupplierOrdersSummary(
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long supplierId) {
		OrderSummaryResponse summary = orderService.getSupplierOrdersSummary(supplierId);
		return ResponseEntity.ok(ApiResponse.success(summary));
	}

	@GetMapping("/customer/summary")
	@Operation(summary = "Get customer order summary counts by status")
	public ResponseEntity<ApiResponse<OrderSummaryResponse>> getCustomerOrdersSummary(
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long customerId) {
		OrderSummaryResponse summary = orderService.getCustomerOrdersSummary(customerId);
		return ResponseEntity.ok(ApiResponse.success(summary));
	}

	@PostMapping
	@Operation(summary = "Create new order")
	public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long customerId,
			@Valid @RequestBody CreateOrderRequest request) {
		OrderResponse order = orderService.createOrder(customerId, request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(order, "Order created successfully"));
	}

	@PostMapping("/{id}/confirm")
	@Operation(summary = "Confirm order (supplier) — auto-transitions to AWAITING_PAYMENT")
	public ResponseEntity<ApiResponse<OrderResponse>> confirmOrder(
			@PathVariable Long id,
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long supplierId) {
		OrderResponse order = orderService.confirmOrder(id, supplierId);
		return ResponseEntity.ok(ApiResponse.success(order, "Order confirmed"));
	}

	@PostMapping("/{id}/reject")
	@Operation(summary = "Reject order (supplier)")
	public ResponseEntity<ApiResponse<OrderResponse>> rejectOrder(
			@PathVariable Long id,
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long supplierId,
			@Valid @RequestBody OrderActionRequest request) {
		OrderResponse order = orderService.rejectOrder(id, supplierId, request.reason());
		return ResponseEntity.ok(ApiResponse.success(order, "Order rejected"));
	}

	@PostMapping("/{id}/confirm-payment")
	@Operation(summary = "Confirm payment (supplier) — auto-transitions to AWAITING_SHIPMENT")
	public ResponseEntity<ApiResponse<OrderResponse>> confirmPayment(
			@PathVariable Long id,
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long supplierId) {
		OrderResponse order = orderService.confirmPayment(id, supplierId);
		return ResponseEntity.ok(ApiResponse.success(order, "Payment confirmed"));
	}

	@PostMapping("/{id}/reject-payment")
	@Operation(summary = "Reject payment (supplier) → PAYMENT_PROBLEM")
	public ResponseEntity<ApiResponse<OrderResponse>> rejectPayment(
			@PathVariable Long id,
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long supplierId,
			@RequestBody(required = false) OrderActionRequest request) {
		String reason = request != null ? request.reason() : null;
		OrderResponse order = orderService.rejectPayment(id, supplierId, reason);
		return ResponseEntity.ok(ApiResponse.success(order, "Payment rejected"));
	}

	@PostMapping("/{id}/generate-ttn")
	@Operation(summary = "Generate TTN document (supplier, in AWAITING_SHIPMENT)")
	public ResponseEntity<ApiResponse<OrderResponse>> generateTtn(
			@PathVariable Long id,
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long supplierId) {
		OrderResponse order = orderService.generateTtn(id, supplierId);
		return ResponseEntity.ok(ApiResponse.success(order, "TTN generated"));
	}

	@PostMapping("/{id}/ship")
	@Operation(summary = "Mark order as shipped (supplier, TTN must be generated first)")
	public ResponseEntity<ApiResponse<OrderResponse>> shipOrder(
			@PathVariable Long id,
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long supplierId) {
		OrderResponse order = orderService.shipOrder(id, supplierId);
		return ResponseEntity.ok(ApiResponse.success(order, "Order shipped"));
	}

	@PostMapping("/{id}/deliver")
	@Operation(summary = "Confirm delivery without discrepancy (customer)")
	public ResponseEntity<ApiResponse<OrderResponse>> deliverOrder(
			@PathVariable Long id,
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long customerId) {
		OrderResponse order = orderService.deliverOrder(id, customerId);
		return ResponseEntity.ok(ApiResponse.success(order, "Order delivered"));
	}

	@PostMapping("/{id}/payment-proof")
	@Operation(summary = "Upload payment proof (customer)")
	public ResponseEntity<ApiResponse<OrderResponse>> uploadPaymentProof(
			@PathVariable Long id,
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long customerId,
			@Valid @RequestBody PaymentProofRequest request) {
		OrderResponse order = orderService.uploadPaymentProof(id, customerId, request);
		return ResponseEntity.ok(ApiResponse.success(order, "Payment proof uploaded"));
	}

	@PostMapping(value = "/{id}/payment-proof", consumes = "multipart/form-data")
	@Operation(summary = "Upload payment proof file (customer, multipart)")
	public ResponseEntity<ApiResponse<OrderResponse>> uploadPaymentProofMultipart(
			@PathVariable Long id,
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long customerId,
			@RequestParam("file") MultipartFile file,
			@RequestParam(required = false) String paymentReference,
			@RequestParam(required = false) String notes) {
		OrderResponse order = orderService.uploadPaymentProof(id, customerId, file, paymentReference, notes);
		return ResponseEntity.ok(ApiResponse.success(order, "Payment proof uploaded"));
	}

	@PostMapping("/{id}/close")
	@Operation(summary = "Close order (supplier, from DELIVERED)")
	public ResponseEntity<ApiResponse<OrderResponse>> closeOrder(
			@PathVariable Long id,
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long supplierId) {
		OrderResponse order = orderService.closeOrder(id, supplierId);
		return ResponseEntity.ok(ApiResponse.success(order, "Order closed"));
	}

	@PostMapping("/{id}/cancel")
	@Operation(summary = "Cancel order")
	public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
			@PathVariable Long id,
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long userId,
			@RequestBody(required = false) OrderActionRequest request) {
		String reason = request != null ? request.reason() : null;
		OrderResponse order = orderService.cancelOrder(id, userId, reason);
		return ResponseEntity.ok(ApiResponse.success(order, "Order cancelled"));
	}

	@PostMapping("/{id}/correct")
	@Operation(summary = "Generate correction TTN (supplier, from AWAITING_CORRECTION) → SHIPPED")
	public ResponseEntity<ApiResponse<OrderResponse>> correctOrder(
			@PathVariable Long id,
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long supplierId) {
		OrderResponse order = orderService.correctOrder(id, supplierId);
		return ResponseEntity.ok(ApiResponse.success(order, "Correction TTN generated, order back to shipped"));
	}

	@PostMapping("/{id}/repeat")
	@Operation(summary = "Repeat order — add items to cart (ТЗ 6.3)")
	public ResponseEntity<ApiResponse<CartResponse>> repeatOrder(
			@PathVariable Long id,
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long customerId) {
		CartResponse cart = orderService.repeatOrder(id, customerId, cartService);
		return ResponseEntity.ok(ApiResponse.success(cart, "Items added to cart"));
	}

	@PostMapping("/{id}/discrepancy")
	@Operation(summary = "Create discrepancy report (customer)")
	public ResponseEntity<ApiResponse<DiscrepancyResponse>> createDiscrepancy(
			@PathVariable Long id,
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long customerId,
			@Valid @RequestBody DiscrepancyRequest request) {
		DiscrepancyResponse discrepancy = orderService.createDiscrepancy(id, customerId, request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(discrepancy, "Discrepancy report created"));
	}

	@GetMapping("/{id}/discrepancies")
	@Operation(summary = "Get order discrepancies")
	public ResponseEntity<ApiResponse<List<DiscrepancyResponse>>> getOrderDiscrepancies(@PathVariable Long id) {
		List<DiscrepancyResponse> discrepancies = orderService.getOrderDiscrepancies(id);
		return ResponseEntity.ok(ApiResponse.success(discrepancies));
	}

	@GetMapping("/{id}/history")
	@Operation(summary = "Get order history (event log)")
	public ResponseEntity<ApiResponse<List<OrderHistoryResponse>>> getOrderHistory(@PathVariable Long id) {
		List<OrderHistoryResponse> history = orderService.getOrderHistory(id);
		return ResponseEntity.ok(ApiResponse.success(history));
	}

	@GetMapping("/{id}/documents")
	@Operation(summary = "Get order documents")
	public ResponseEntity<ApiResponse<List<OrderDocumentResponse>>> getOrderDocuments(@PathVariable Long id) {
		List<OrderDocumentResponse> documents = orderService.getOrderDocuments(id);
		return ResponseEntity.ok(ApiResponse.success(documents));
	}
}
