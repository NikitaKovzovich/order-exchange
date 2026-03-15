package by.bsuir.catalogservice.controller;

import by.bsuir.catalogservice.dto.*;
import by.bsuir.catalogservice.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management API for suppliers and retail chains")
public class ProductController {
	private final ProductService productService;

	@GetMapping("/search")
	@Operation(summary = "Search published products", description = "Search products visible to retail chains with filters and pagination")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Products found successfully")
	})
	public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> searchProducts(
			@RequestHeader(value = "X-User-Company-Id", required = false) @Parameter(hidden = true) Long customerCompanyId,
			@RequestParam(required = false) @Parameter(description = "Filter by category ID") Long categoryId,
			@RequestParam(required = false) @Parameter(description = "Filter by supplier ID") Long supplierId,
			@RequestParam(required = false) @Parameter(description = "Minimum price filter") BigDecimal minPrice,
			@RequestParam(required = false) @Parameter(description = "Maximum price filter") BigDecimal maxPrice,
			@RequestParam(required = false) @Parameter(description = "Search by name or description") String search,
			@RequestParam(defaultValue = "0") @Parameter(description = "Page number (0-based)") int page,
			@RequestParam(defaultValue = "20") @Parameter(description = "Page size") int size,
			@RequestParam(defaultValue = "name") @Parameter(description = "Sort field") String sortBy,
			@RequestParam(defaultValue = "asc") @Parameter(description = "Sort direction (asc/desc)") String sortDir) {

		ProductSearchRequest request = new ProductSearchRequest(
				categoryId,
				supplierId,
				minPrice,
				maxPrice,
				search,
				page,
				size,
				sortBy,
				sortDir
		);

		PageResponse<ProductResponse> products = productService.searchProducts(request, customerCompanyId);
		return ResponseEntity.ok(ApiResponse.success(products));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get product by ID", description = "Retrieve detailed product information")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product found"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found")
	})
	public ResponseEntity<ApiResponse<ProductResponse>> getProductById(
			@PathVariable @Parameter(description = "Product ID") Long id) {
		ProductResponse product = productService.getProductById(id);
		return ResponseEntity.ok(ApiResponse.success(product));
	}

	@GetMapping("/supplier")
	@Operation(summary = "Get supplier's own products", description = "Retrieve all products belonging to the authenticated supplier")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Products retrieved successfully")
	})
	public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getSupplierProducts(
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long supplierId,
			@RequestParam(required = false) @Parameter(description = "Search by product name, SKU or description") String search,
			@RequestParam(defaultValue = "0") @Parameter(description = "Page number") int page,
			@RequestParam(defaultValue = "20") @Parameter(description = "Page size") int size) {
		PageResponse<ProductResponse> products = productService.getSupplierProducts(supplierId, search, page, size);
		return ResponseEntity.ok(ApiResponse.success(products));
	}

	@PostMapping
	@Operation(summary = "Create a new product", description = "Create a new product in DRAFT status. Supplier only.")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Product created successfully"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Product with this SKU already exists")
	})
	public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long supplierId,
			@Valid @RequestBody ProductRequest request) {
		ProductResponse product = productService.createProduct(supplierId, request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(product, "Product created successfully"));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a product", description = "Update product details. Supplier only.")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product updated successfully"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Product does not belong to supplier"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found")
	})
	public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
			@PathVariable @Parameter(description = "Product ID") Long id,
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long supplierId,
			@Valid @RequestBody ProductRequest request) {
		ProductResponse product = productService.updateProduct(id, supplierId, request);
		return ResponseEntity.ok(ApiResponse.success(product, "Product updated successfully"));
	}

	@PostMapping("/{id}/publish")
	@Operation(summary = "Publish a product", description = "Change product status from DRAFT to PUBLISHED. Supplier only.")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product published successfully"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Product cannot be published from current state"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Product does not belong to supplier"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found")
	})
	public ResponseEntity<ApiResponse<ProductResponse>> publishProduct(
			@PathVariable @Parameter(description = "Product ID") Long id,
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long supplierId) {
		ProductResponse product = productService.publishProduct(id, supplierId);
		return ResponseEntity.ok(ApiResponse.success(product, "Product published successfully"));
	}

	@PostMapping("/{id}/archive")
	@Operation(summary = "Archive a product", description = "Change product status to ARCHIVED. Supplier only.")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product archived successfully"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Product cannot be archived from current state"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Product does not belong to supplier"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found")
	})
	public ResponseEntity<ApiResponse<ProductResponse>> archiveProduct(
			@PathVariable @Parameter(description = "Product ID") Long id,
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long supplierId) {
		ProductResponse product = productService.archiveProduct(id, supplierId);
		return ResponseEntity.ok(ApiResponse.success(product, "Product archived successfully"));
	}

	@PostMapping("/{id}/draft")
	@Operation(summary = "Move product back to draft", description = "Return product to DRAFT status for editing. Supplier only.")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product moved to draft"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Product cannot be moved to draft from current state"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Product does not belong to supplier"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found")
	})
	public ResponseEntity<ApiResponse<ProductResponse>> toDraft(
			@PathVariable @Parameter(description = "Product ID") Long id,
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long supplierId) {
		ProductResponse product = productService.toDraft(id, supplierId);
		return ResponseEntity.ok(ApiResponse.success(product, "Product moved to draft"));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a product", description = "Permanently delete a product. Supplier only.")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product deleted successfully"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Product does not belong to supplier"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found")
	})
	public ResponseEntity<ApiResponse<Void>> deleteProduct(
			@PathVariable @Parameter(description = "Product ID") Long id,
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long supplierId) {
		productService.deleteProduct(id, supplierId);
		return ResponseEntity.ok(ApiResponse.success(null, "Product deleted successfully"));
	}

	@PostMapping("/publish-catalog")
	@Operation(summary = "Publish all draft products", description = "Publish all DRAFT products for this supplier. (#16)")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Catalog published")
	})
	public ResponseEntity<ApiResponse<Integer>> publishCatalog(
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long supplierId) {
		int count = productService.publishCatalog(supplierId);
		return ResponseEntity.ok(ApiResponse.success(count, count + " products published"));
	}

	@PostMapping("/update-catalog")
	@Operation(summary = "Publish new draft products", description = "Publish only new DRAFT products (added after last publish). (#16)")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Catalog updated")
	})
	public ResponseEntity<ApiResponse<Integer>> updateCatalog(
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long supplierId) {
		int count = productService.updateCatalog(supplierId);
		return ResponseEntity.ok(ApiResponse.success(count, count + " new products published"));
	}



	@PostMapping("/{id}/hide")
	@Operation(summary = "Hide product (admin)", description = "Admin hides product → ARCHIVED (#18)")
	public ResponseEntity<ApiResponse<ProductResponse>> adminHideProduct(
			@PathVariable @Parameter(description = "Product ID") Long id,
			@RequestHeader("X-User-Role") @Parameter(hidden = true) String role) {
		if (!"ADMIN".equalsIgnoreCase(role)) {
			return ResponseEntity.status(403).body(ApiResponse.error("Admin role required"));
		}
		ProductResponse product = productService.adminHideProduct(id);
		return ResponseEntity.ok(ApiResponse.success(product, "Product hidden by admin"));
	}

	@PostMapping("/{id}/show")
	@Operation(summary = "Show product (admin)", description = "Admin shows product → PUBLISHED (#18)")
	public ResponseEntity<ApiResponse<ProductResponse>> adminShowProduct(
			@PathVariable @Parameter(description = "Product ID") Long id,
			@RequestHeader("X-User-Role") @Parameter(hidden = true) String role) {
		if (!"ADMIN".equalsIgnoreCase(role)) {
			return ResponseEntity.status(403).body(ApiResponse.error("Admin role required"));
		}
		ProductResponse product = productService.adminShowProduct(id);
		return ResponseEntity.ok(ApiResponse.success(product, "Product shown by admin"));
	}

	@DeleteMapping("/{id}/admin")
	@Operation(summary = "Delete product (admin)", description = "Admin deletes product without supplier check (#18)")
	public ResponseEntity<ApiResponse<Void>> adminDeleteProduct(
			@PathVariable @Parameter(description = "Product ID") Long id,
			@RequestHeader("X-User-Role") @Parameter(hidden = true) String role) {
		if (!"ADMIN".equalsIgnoreCase(role)) {
			return ResponseEntity.status(403).body(ApiResponse.error("Admin role required"));
		}
		productService.adminDeleteProduct(id);
		return ResponseEntity.ok(ApiResponse.success(null, "Product deleted by admin"));
	}

	@GetMapping("/admin/all")
	@Operation(summary = "Get all products for admin moderation (#P6)",
			description = "Returns all products across all suppliers with optional filters")
	public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getAdminProducts(
			@RequestHeader("X-User-Role") @Parameter(hidden = true) String role,
			@RequestParam(required = false) @Parameter(description = "Filter by supplier ID") Long supplierId,
			@RequestParam(required = false) @Parameter(description = "Filter by category ID") Long categoryId,
			@RequestParam(required = false) @Parameter(description = "Filter by status") String status,
			@RequestParam(required = false) @Parameter(description = "Search by name") String search,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		if (!"ADMIN".equalsIgnoreCase(role)) {
			return ResponseEntity.status(403).body(ApiResponse.error("Admin role required"));
		}
		PageResponse<ProductResponse> products = productService.getAdminProducts(supplierId, categoryId, status, search, page, size);
		return ResponseEntity.ok(ApiResponse.success(products));
	}
}
