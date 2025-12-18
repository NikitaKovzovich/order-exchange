package by.bsuir.orderservice.controller;

import by.bsuir.orderservice.dto.*;
import by.bsuir.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management API")
public class OrderController {
	private final OrderService orderService;

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
	@Operation(summary = "Get orders for supplier")
	public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getSupplierOrders(
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long supplierId,
			@RequestParam(required = false) String status,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		PageResponse<OrderResponse> orders = orderService.getSupplierOrders(supplierId, status, page, size);
		return ResponseEntity.ok(ApiResponse.success(orders));
	}

	@GetMapping("/customer")
	@Operation(summary = "Get orders for customer (retail chain)")
	public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getCustomerOrders(
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long customerId,
			@RequestParam(required = false) String status,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		PageResponse<OrderResponse> orders = orderService.getCustomerOrders(customerId, status, page, size);
		return ResponseEntity.ok(ApiResponse.success(orders));
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
	@Operation(summary = "Confirm order (supplier)")
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
	@Operation(summary = "Confirm payment (supplier)")
	public ResponseEntity<ApiResponse<OrderResponse>> confirmPayment(
			@PathVariable Long id,
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long supplierId) {
		OrderResponse order = orderService.confirmPayment(id, supplierId);
		return ResponseEntity.ok(ApiResponse.success(order, "Payment confirmed"));
	}

	@PostMapping("/{id}/ship")
	@Operation(summary = "Mark order as shipped (supplier)")
	public ResponseEntity<ApiResponse<OrderResponse>> shipOrder(
			@PathVariable Long id,
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long supplierId) {
		OrderResponse order = orderService.shipOrder(id, supplierId);
		return ResponseEntity.ok(ApiResponse.success(order, "Order shipped"));
	}

	@PostMapping("/{id}/deliver")
	@Operation(summary = "Confirm delivery (customer)")
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
	public ResponseEntity<ApiResponse<java.util.List<DiscrepancyResponse>>> getOrderDiscrepancies(
			@PathVariable Long id) {
		java.util.List<DiscrepancyResponse> discrepancies = orderService.getOrderDiscrepancies(id);
		return ResponseEntity.ok(ApiResponse.success(discrepancies));
	}
}
