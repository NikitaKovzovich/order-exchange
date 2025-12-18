package by.bsuir.catalogservice.controller;

import by.bsuir.catalogservice.dto.ApiResponse;
import by.bsuir.catalogservice.dto.ProductImageResponse;
import by.bsuir.catalogservice.service.ProductImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products/{productId}/images")
@RequiredArgsConstructor
@Tag(name = "Product Images", description = "Product image management API")
public class ProductImageController {
	private final ProductImageService imageService;

	@GetMapping
	@Operation(summary = "Get all images for a product")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Images retrieved successfully"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found")
	})
	public ResponseEntity<ApiResponse<List<ProductImageResponse>>> getProductImages(
			@PathVariable Long productId) {
		List<ProductImageResponse> images = imageService.getProductImages(productId);
		return ResponseEntity.ok(ApiResponse.success(images));
	}

	@GetMapping("/{imageId}")
	@Operation(summary = "Get image metadata by ID")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Image metadata retrieved"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Image not found")
	})
	public ResponseEntity<ApiResponse<ProductImageResponse>> getImage(
			@PathVariable Long productId,
			@PathVariable Long imageId) {
		ProductImageResponse image = imageService.getImage(imageId);
		return ResponseEntity.ok(ApiResponse.success(image));
	}

	@GetMapping("/{imageId}/data")
	@Operation(summary = "Get image binary data")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "Image data",
			content = @Content(mediaType = "image/*")
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Image not found")
	})
	public ResponseEntity<byte[]> getImageData(
			@PathVariable Long productId,
			@PathVariable Long imageId) {
		byte[] data = imageService.getImageData(imageId);
		String mimeType = imageService.getImageMimeType(imageId);
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(mimeType))
				.body(data);
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "Upload a new image for a product (Supplier only)")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Image uploaded successfully"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid image file"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Product does not belong to supplier"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found")
	})
	public ResponseEntity<ApiResponse<ProductImageResponse>> uploadImage(
			@PathVariable Long productId,
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long supplierId,
			@RequestParam("file") @Parameter(description = "Image file (JPEG, PNG, WebP, max 5MB)") MultipartFile file,
			@RequestParam(value = "primary", defaultValue = "false") @Parameter(description = "Set as primary image") boolean isPrimary) {
		ProductImageResponse image = imageService.uploadImage(productId, supplierId, file, isPrimary);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(image, "Image uploaded successfully"));
	}

	@PutMapping("/{imageId}/primary")
	@Operation(summary = "Set image as primary (Supplier only)")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Primary image set"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Product does not belong to supplier"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Image or product not found")
	})
	public ResponseEntity<ApiResponse<ProductImageResponse>> setPrimaryImage(
			@PathVariable Long productId,
			@PathVariable Long imageId,
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long supplierId) {
		ProductImageResponse image = imageService.setPrimaryImage(productId, imageId, supplierId);
		return ResponseEntity.ok(ApiResponse.success(image, "Primary image updated"));
	}

	@DeleteMapping("/{imageId}")
	@Operation(summary = "Delete an image (Supplier only)")
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Image deleted"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Product does not belong to supplier"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Image or product not found")
	})
	public ResponseEntity<ApiResponse<Void>> deleteImage(
			@PathVariable Long productId,
			@PathVariable Long imageId,
			@RequestHeader("X-User-Company-Id") @Parameter(hidden = true) Long supplierId) {
		imageService.deleteImage(productId, imageId, supplierId);
		return ResponseEntity.ok(ApiResponse.success(null, "Image deleted successfully"));
	}
}
