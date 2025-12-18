package by.bsuir.catalogservice.service;

import by.bsuir.catalogservice.dto.ProductImageResponse;
import by.bsuir.catalogservice.entity.Product;
import by.bsuir.catalogservice.entity.ProductImage;
import by.bsuir.catalogservice.exception.InvalidOperationException;
import by.bsuir.catalogservice.exception.ResourceNotFoundException;
import by.bsuir.catalogservice.repository.ProductImageRepository;
import by.bsuir.catalogservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductImageServiceTest {

	@Mock
	private ProductImageRepository imageRepository;
	@Mock
	private ProductRepository productRepository;

	@InjectMocks
	private ProductImageService productImageService;

	private Product testProduct;
	private ProductImage testImage;

	@BeforeEach
	void setUp() {
		testProduct = Product.builder()
				.id(1L)
				.supplierId(100L)
				.name("Test Product")
				.build();

		testImage = ProductImage.builder()
				.id(1L)
				.product(testProduct)
				.fileName("test.jpg")
				.mimeType("image/jpeg")
				.imageData(new byte[]{1, 2, 3})
				.isPrimary(true)
				.build();
	}

	@Nested
	@DisplayName("Get Images Tests")
	class GetImagesTests {

		@Test
		@DisplayName("Should return product images")
		void shouldReturnProductImages() {
			when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
			when(imageRepository.findByProductId(1L)).thenReturn(List.of(testImage));

			List<ProductImageResponse> images = productImageService.getProductImages(1L);

			assertThat(images).hasSize(1);
			assertThat(images.getFirst().fileName()).isEqualTo("test.jpg");
		}

		@Test
		@DisplayName("Should throw exception when product not found")
		void shouldThrowExceptionWhenProductNotFound() {
			when(productRepository.findById(999L)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> productImageService.getProductImages(999L))
					.isInstanceOf(ResourceNotFoundException.class);
		}

		@Test
		@DisplayName("Should return image by ID")
		void shouldReturnImageById() {
			when(imageRepository.findById(1L)).thenReturn(Optional.of(testImage));

			ProductImageResponse response = productImageService.getImage(1L);

			assertThat(response).isNotNull();
			assertThat(response.fileName()).isEqualTo("test.jpg");
		}

		@Test
		@DisplayName("Should return image data")
		void shouldReturnImageData() {
			when(imageRepository.findById(1L)).thenReturn(Optional.of(testImage));

			byte[] data = productImageService.getImageData(1L);

			assertThat(data).isEqualTo(new byte[]{1, 2, 3});
		}

		@Test
		@DisplayName("Should return image mime type")
		void shouldReturnImageMimeType() {
			when(imageRepository.findById(1L)).thenReturn(Optional.of(testImage));

			String mimeType = productImageService.getImageMimeType(1L);

			assertThat(mimeType).isEqualTo("image/jpeg");
		}
	}

	@Nested
	@DisplayName("Upload Image Tests")
	class UploadImageTests {

		@Test
		@DisplayName("Should upload image successfully")
		void shouldUploadImageSuccessfully() {
			MockMultipartFile file = new MockMultipartFile(
					"file", "test.jpg", "image/jpeg", new byte[]{1, 2, 3}
			);
			when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
			when(imageRepository.save(any(ProductImage.class))).thenReturn(testImage);

			ProductImageResponse response = productImageService.uploadImage(1L, 100L, file, false);

			assertThat(response).isNotNull();
			verify(imageRepository).save(any(ProductImage.class));
		}

		@Test
		@DisplayName("Should throw exception for wrong supplier")
		void shouldThrowExceptionForWrongSupplier() {
			MockMultipartFile file = new MockMultipartFile(
					"file", "test.jpg", "image/jpeg", new byte[]{1, 2, 3}
			);
			when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

			assertThatThrownBy(() -> productImageService.uploadImage(1L, 999L, file, false))
					.isInstanceOf(InvalidOperationException.class)
					.hasMessageContaining("does not belong");
		}

		@Test
		@DisplayName("Should throw exception for empty file")
		void shouldThrowExceptionForEmptyFile() {
			MockMultipartFile file = new MockMultipartFile(
					"file", "test.jpg", "image/jpeg", new byte[]{}
			);
			when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

			assertThatThrownBy(() -> productImageService.uploadImage(1L, 100L, file, false))
					.isInstanceOf(InvalidOperationException.class)
					.hasMessageContaining("empty");
		}

		@Test
		@DisplayName("Should throw exception for invalid file type")
		void shouldThrowExceptionForInvalidFileType() {
			MockMultipartFile file = new MockMultipartFile(
					"file", "test.pdf", "application/pdf", new byte[]{1, 2, 3}
			);
			when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

			assertThatThrownBy(() -> productImageService.uploadImage(1L, 100L, file, false))
					.isInstanceOf(InvalidOperationException.class)
					.hasMessageContaining("not allowed");
		}

		@Test
		@DisplayName("Should unset previous primary when uploading new primary")
		void shouldUnsetPreviousPrimaryWhenUploadingNewPrimary() {
			MockMultipartFile file = new MockMultipartFile(
					"file", "test.jpg", "image/jpeg", new byte[]{1, 2, 3}
			);
			ProductImage existingPrimary = ProductImage.builder()
					.id(2L)
					.product(testProduct)
					.isPrimary(true)
					.build();

			when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
			when(imageRepository.findByProductIdAndIsPrimaryTrue(1L)).thenReturn(Optional.of(existingPrimary));
			when(imageRepository.save(any(ProductImage.class))).thenReturn(testImage);

			productImageService.uploadImage(1L, 100L, file, true);

			verify(imageRepository, times(2)).save(any(ProductImage.class));
		}
	}

	@Nested
	@DisplayName("Set Primary Image Tests")
	class SetPrimaryImageTests {

		@Test
		@DisplayName("Should set primary image successfully")
		void shouldSetPrimaryImageSuccessfully() {
			ProductImage nonPrimaryImage = ProductImage.builder()
					.id(2L)
					.product(testProduct)
					.fileName("test2.jpg")
					.mimeType("image/jpeg")
					.isPrimary(false)
					.build();

			when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
			when(imageRepository.findById(2L)).thenReturn(Optional.of(nonPrimaryImage));
			when(imageRepository.findByProductIdAndIsPrimaryTrue(1L)).thenReturn(Optional.of(testImage));
			when(imageRepository.save(any(ProductImage.class))).thenReturn(nonPrimaryImage);

			ProductImageResponse response = productImageService.setPrimaryImage(1L, 2L, 100L);

			assertThat(response).isNotNull();
			verify(imageRepository, times(2)).save(any(ProductImage.class));
		}

		@Test
		@DisplayName("Should throw exception when image not for product")
		void shouldThrowExceptionWhenImageNotForProduct() {
			Product otherProduct = Product.builder().id(2L).supplierId(100L).build();
			ProductImage otherImage = ProductImage.builder()
					.id(2L)
					.product(otherProduct)
					.build();

			when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
			when(imageRepository.findById(2L)).thenReturn(Optional.of(otherImage));

			assertThatThrownBy(() -> productImageService.setPrimaryImage(1L, 2L, 100L))
					.isInstanceOf(InvalidOperationException.class)
					.hasMessageContaining("does not belong to this product");
		}
	}

	@Nested
	@DisplayName("Delete Image Tests")
	class DeleteImageTests {

		@Test
		@DisplayName("Should delete image successfully")
		void shouldDeleteImageSuccessfully() {
			when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
			when(imageRepository.findById(1L)).thenReturn(Optional.of(testImage));

			productImageService.deleteImage(1L, 1L, 100L);

			verify(imageRepository).delete(testImage);
		}

		@Test
		@DisplayName("Should throw exception for wrong supplier")
		void shouldThrowExceptionForWrongSupplier() {
			when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

			assertThatThrownBy(() -> productImageService.deleteImage(1L, 1L, 999L))
					.isInstanceOf(InvalidOperationException.class);
		}
	}
}
