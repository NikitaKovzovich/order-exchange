package by.bsuir.catalogservice.service;

import by.bsuir.catalogservice.dto.ProductImageResponse;
import by.bsuir.catalogservice.entity.Product;
import by.bsuir.catalogservice.entity.ProductImage;
import by.bsuir.catalogservice.exception.InvalidOperationException;
import by.bsuir.catalogservice.exception.ResourceNotFoundException;
import by.bsuir.catalogservice.repository.ProductImageRepository;
import by.bsuir.catalogservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductImageService {
	private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
	private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/webp");

	private final ProductImageRepository imageRepository;
	private final ProductRepository productRepository;

	public List<ProductImageResponse> getProductImages(Long productId) {
		productRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
		return imageRepository.findByProductId(productId).stream()
				.map(this::mapToResponse)
				.toList();
	}

	public ProductImageResponse getImage(Long imageId) {
		ProductImage image = imageRepository.findById(imageId)
				.orElseThrow(() -> new ResourceNotFoundException("Image", "id", imageId));
		return mapToResponse(image);
	}

	public byte[] getImageData(Long imageId) {
		ProductImage image = imageRepository.findById(imageId)
				.orElseThrow(() -> new ResourceNotFoundException("Image", "id", imageId));
		return image.getImageData();
	}

	public String getImageMimeType(Long imageId) {
		ProductImage image = imageRepository.findById(imageId)
				.orElseThrow(() -> new ResourceNotFoundException("Image", "id", imageId));
		return image.getMimeType();
	}

	@Transactional
	public ProductImageResponse uploadImage(Long productId, Long supplierId, MultipartFile file, boolean isPrimary) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

		if (!product.getSupplierId().equals(supplierId)) {
			throw new InvalidOperationException("upload", "Product does not belong to this supplier");
		}

		validateImage(file);

		try {
			if (isPrimary) {
				imageRepository.findByProductIdAndIsPrimaryTrue(productId)
						.ifPresent(img -> {
							img.unsetPrimary();
							imageRepository.save(img);
						});
			}

			ProductImage image = ProductImage.builder()
					.product(product)
					.imageData(file.getBytes())
					.mimeType(file.getContentType())
					.fileName(file.getOriginalFilename())
					.isPrimary(isPrimary)
					.build();

			image = imageRepository.save(image);
			return mapToResponse(image);
		} catch (IOException e) {
			throw new InvalidOperationException("upload", "Failed to process image file");
		}
	}

	@Transactional
	public ProductImageResponse setPrimaryImage(Long productId, Long imageId, Long supplierId) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

		if (!product.getSupplierId().equals(supplierId)) {
			throw new InvalidOperationException("update", "Product does not belong to this supplier");
		}

		ProductImage image = imageRepository.findById(imageId)
				.orElseThrow(() -> new ResourceNotFoundException("Image", "id", imageId));

		if (!image.getProduct().getId().equals(productId)) {
			throw new InvalidOperationException("update", "Image does not belong to this product");
		}

		imageRepository.findByProductIdAndIsPrimaryTrue(productId)
				.ifPresent(img -> {
					img.unsetPrimary();
					imageRepository.save(img);
				});

		image.setPrimary();
		image = imageRepository.save(image);
		return mapToResponse(image);
	}

	@Transactional
	public void deleteImage(Long productId, Long imageId, Long supplierId) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

		if (!product.getSupplierId().equals(supplierId)) {
			throw new InvalidOperationException("delete", "Product does not belong to this supplier");
		}

		ProductImage image = imageRepository.findById(imageId)
				.orElseThrow(() -> new ResourceNotFoundException("Image", "id", imageId));

		if (!image.getProduct().getId().equals(productId)) {
			throw new InvalidOperationException("delete", "Image does not belong to this product");
		}

		imageRepository.delete(image);
	}

	private void validateImage(MultipartFile file) {
		if (file.isEmpty()) {
			throw new InvalidOperationException("upload", "File is empty");
		}
		if (file.getSize() > MAX_IMAGE_SIZE) {
			throw new InvalidOperationException("upload", "File size exceeds maximum allowed (5MB)");
		}
		if (!ALLOWED_TYPES.contains(file.getContentType())) {
			throw new InvalidOperationException("upload", "File type not allowed. Allowed: JPEG, PNG, WebP");
		}
	}

	private ProductImageResponse mapToResponse(ProductImage image) {
		return new ProductImageResponse(
				image.getId(),
				image.getProduct().getId(),
				image.getFileName(),
				image.getMimeType(),
				image.getSizeKb(),
				image.getIsPrimary(),
				"/api/products/" + image.getProduct().getId() + "/images/" + image.getId() + "/data"
		);
	}
}
