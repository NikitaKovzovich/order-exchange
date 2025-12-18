package by.bsuir.catalogservice.controller;

import by.bsuir.catalogservice.dto.ApiResponse;
import by.bsuir.catalogservice.dto.InventoryResponse;
import by.bsuir.catalogservice.dto.InventoryUpdateRequest;
import by.bsuir.catalogservice.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Inventory and stock management API")
public class InventoryController {
	private final InventoryService inventoryService;

	@GetMapping("/{productId}")
	@Operation(summary = "Get inventory for a product", description = "Retrieve current stock levels for a product")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Inventory retrieved successfully"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found")
	})
	public ResponseEntity<ApiResponse<InventoryResponse>> getInventory(
			@PathVariable @Parameter(description = "Product ID") Long productId) {
		InventoryResponse inventory = inventoryService.getInventory(productId);
		return ResponseEntity.ok(ApiResponse.success(inventory));
	}

	@PutMapping("/{productId}")
	@Operation(summary = "Update inventory quantity", description = "Set absolute quantity. Supplier only.")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Inventory updated successfully"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Product does not belong to supplier"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found")
	})
	public ResponseEntity<ApiResponse<InventoryResponse>> updateQuantity(
			@PathVariable @Parameter(description = "Product ID") Long productId,
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long supplierId,
			@Valid @RequestBody InventoryUpdateRequest request) {
		InventoryResponse inventory = inventoryService.updateQuantity(productId, supplierId, request);
		return ResponseEntity.ok(ApiResponse.success(inventory, "Inventory updated successfully"));
	}

	@PostMapping("/{productId}/add")
	@Operation(summary = "Add stock to inventory", description = "Add quantity to current stock. Supplier only.")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Stock added successfully"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid quantity"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Product does not belong to supplier"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found")
	})
	public ResponseEntity<ApiResponse<InventoryResponse>> addStock(
			@PathVariable @Parameter(description = "Product ID") Long productId,
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long supplierId,
			@RequestParam @Parameter(description = "Quantity to add") int quantity,
			@RequestParam(required = false) @Parameter(description = "Reason for stock addition") String reason) {
		InventoryResponse inventory = inventoryService.addStock(productId, supplierId, quantity, reason);
		return ResponseEntity.ok(ApiResponse.success(inventory, "Stock added successfully"));
	}

	@GetMapping("/low-stock")
	@Operation(summary = "Get low stock products", description = "Retrieve products with stock below threshold")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Low stock products retrieved")
	})
	public ResponseEntity<ApiResponse<List<InventoryResponse>>> getLowStock(
			@RequestParam(defaultValue = "10") @Parameter(description = "Stock threshold") int threshold) {
		List<InventoryResponse> inventory = inventoryService.getLowStockProducts(threshold);
		return ResponseEntity.ok(ApiResponse.success(inventory));
	}

	@GetMapping("/out-of-stock")
	@Operation(summary = "Get out of stock products", description = "Retrieve products with zero available stock")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Out of stock products retrieved")
	})
	public ResponseEntity<ApiResponse<List<InventoryResponse>>> getOutOfStock() {
		List<InventoryResponse> inventory = inventoryService.getOutOfStockProducts();
		return ResponseEntity.ok(ApiResponse.success(inventory));
	}
}
