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

		PageResponse<ProductResponse> products = productService.searchProducts(request);
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
			@RequestParam(defaultValue = "0") @Parameter(description = "Page number") int page,
			@RequestParam(defaultValue = "20") @Parameter(description = "Page size") int size) {
		PageResponse<ProductResponse> products = productService.getSupplierProducts(supplierId, page, size);
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
}
