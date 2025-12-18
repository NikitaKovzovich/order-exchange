package by.bsuir.catalogservice.controller;

import by.bsuir.catalogservice.dto.ApiResponse;
import by.bsuir.catalogservice.dto.CategoryRequest;
import by.bsuir.catalogservice.dto.CategoryResponse;
import by.bsuir.catalogservice.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management API for product classification")
public class CategoryController {
	private final CategoryService categoryService;

	@GetMapping
	@Operation(summary = "Get all categories", description = "Retrieve flat list of all categories")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
	})
	public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
		List<CategoryResponse> categories = categoryService.getAllCategories();
		return ResponseEntity.ok(ApiResponse.success(categories));
	}

	@GetMapping("/tree")
	@Operation(summary = "Get category tree", description = "Retrieve hierarchical category structure with root categories and their children")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category tree retrieved successfully")
	})
	public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategoryTree() {
		List<CategoryResponse> tree = categoryService.getRootCategories();
		return ResponseEntity.ok(ApiResponse.success(tree));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get category by ID", description = "Retrieve detailed category information")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category found"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found")
	})
	public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(
			@PathVariable @Parameter(description = "Category ID") Long id) {
		CategoryResponse category = categoryService.getCategoryById(id);
		return ResponseEntity.ok(ApiResponse.success(category));
	}

	@GetMapping("/{id}/subcategories")
	@Operation(summary = "Get subcategories", description = "Retrieve direct children of a category")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Subcategories retrieved successfully"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Parent category not found")
	})
	public ResponseEntity<ApiResponse<List<CategoryResponse>>> getSubcategories(
			@PathVariable @Parameter(description = "Parent category ID") Long id) {
		List<CategoryResponse> subcategories = categoryService.getSubcategories(id);
		return ResponseEntity.ok(ApiResponse.success(subcategories));
	}

	@PostMapping
	@Operation(summary = "Create a new category", description = "Create a new category. Admin only.")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Category created successfully"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Parent category not found")
	})
	public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
			@Valid @RequestBody CategoryRequest request) {
		CategoryResponse category = categoryService.createCategory(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(category, "Category created successfully"));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a category", description = "Update category details. Admin only.")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category updated successfully"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found")
	})
	public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
			@PathVariable @Parameter(description = "Category ID") Long id,
			@Valid @RequestBody CategoryRequest request) {
		CategoryResponse category = categoryService.updateCategory(id, request);
		return ResponseEntity.ok(ApiResponse.success(category, "Category updated successfully"));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a category", description = "Delete a category. Admin only. Category must have no products.")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category deleted successfully"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Category has products or subcategories"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found")
	})
	public ResponseEntity<ApiResponse<Void>> deleteCategory(
			@PathVariable @Parameter(description = "Category ID") Long id) {
		categoryService.deleteCategory(id);
		return ResponseEntity.ok(ApiResponse.success(null, "Category deleted successfully"));
	}
}
