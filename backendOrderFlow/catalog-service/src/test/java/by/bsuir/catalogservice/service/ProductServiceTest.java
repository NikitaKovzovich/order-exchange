package by.bsuir.catalogservice.service;

import by.bsuir.catalogservice.dto.PageResponse;
import by.bsuir.catalogservice.dto.ProductRequest;
import by.bsuir.catalogservice.dto.ProductResponse;
import by.bsuir.catalogservice.dto.ProductSearchRequest;
import by.bsuir.catalogservice.entity.*;
import by.bsuir.catalogservice.exception.DuplicateResourceException;
import by.bsuir.catalogservice.exception.InvalidOperationException;
import by.bsuir.catalogservice.exception.ResourceNotFoundException;
import by.bsuir.catalogservice.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

	@Mock
	private ProductRepository productRepository;
	@Mock
	private CategoryRepository categoryRepository;
	@Mock
	private UnitOfMeasureRepository unitRepository;
	@Mock
	private VatRateRepository vatRateRepository;
	@Mock
	private InventoryRepository inventoryRepository;
	@Mock
	private EventPublisher eventPublisher;

	@InjectMocks
	private ProductService productService;

	private Category testCategory;
	private UnitOfMeasure testUnit;
	private VatRate testVatRate;
	private Product testProduct;
	private ProductRequest testRequest;

	@BeforeEach
	void setUp() {
		testCategory = Category.builder()
				.id(1L)
				.name("Electronics")
				.build();

		testUnit = UnitOfMeasure.builder()
				.id(1L)
				.name("штука")
				.build();

		testVatRate = VatRate.builder()
				.id(1L)
				.description("20%")
				.ratePercentage(new BigDecimal("20.00"))
				.build();

		testProduct = Product.builder()
				.id(1L)
				.supplierId(100L)
				.sku("SKU-001")
				.name("Test Product")
				.description("Test Description")
				.category(testCategory)
				.pricePerUnit(new BigDecimal("99.99"))
				.unit(testUnit)
				.vatRate(testVatRate)
				.status(Product.ProductStatus.DRAFT)
				.build();

		testRequest = new ProductRequest(
				"SKU-001",
				"Test Product",
				"Test Description",
				1L,
				new BigDecimal("99.99"),
				1L,
				1L,
				new BigDecimal("0.5"),
				"Belarus",
				null,
				null,
				100
		);
	}

	@Nested
	@DisplayName("Create Product Tests")
	class CreateProductTests {

		@Test
		@DisplayName("Should create product successfully")
		void shouldCreateProductSuccessfully() {
			when(productRepository.existsBySupplierIdAndSku(anyLong(), any())).thenReturn(false);
			when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
			when(unitRepository.findById(1L)).thenReturn(Optional.of(testUnit));
			when(vatRateRepository.findById(1L)).thenReturn(Optional.of(testVatRate));
			when(productRepository.save(any(Product.class))).thenReturn(testProduct);
			when(inventoryRepository.save(any(Inventory.class))).thenReturn(new Inventory());

			ProductResponse response = productService.createProduct(100L, testRequest);

			assertThat(response).isNotNull();
			assertThat(response.name()).isEqualTo("Test Product");
			assertThat(response.sku()).isEqualTo("SKU-001");
			verify(productRepository).save(any(Product.class));
			verify(eventPublisher).publishProductCreated(any(Product.class));
		}

		@Test
		@DisplayName("Should throw exception when SKU already exists")
		void shouldThrowExceptionWhenSkuExists() {
			when(productRepository.existsBySupplierIdAndSku(100L, "SKU-001")).thenReturn(true);

			assertThatThrownBy(() -> productService.createProduct(100L, testRequest))
					.isInstanceOf(DuplicateResourceException.class)
					.hasMessageContaining("SKU-001");
		}

		@Test
		@DisplayName("Should throw exception when category not found")
		void shouldThrowExceptionWhenCategoryNotFound() {
			when(productRepository.existsBySupplierIdAndSku(anyLong(), any())).thenReturn(false);
			when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> productService.createProduct(100L, testRequest))
					.isInstanceOf(ResourceNotFoundException.class)
					.hasMessageContaining("Category");
		}
	}

	@Nested
	@DisplayName("Get Product Tests")
	class GetProductTests {

		@Test
		@DisplayName("Should return product by ID")
		void shouldReturnProductById() {
			when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

			ProductResponse response = productService.getProductById(1L);

			assertThat(response).isNotNull();
			assertThat(response.id()).isEqualTo(1L);
			assertThat(response.name()).isEqualTo("Test Product");
		}

		@Test
		@DisplayName("Should throw exception when product not found")
		void shouldThrowExceptionWhenProductNotFound() {
			when(productRepository.findById(999L)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> productService.getProductById(999L))
					.isInstanceOf(ResourceNotFoundException.class)
					.hasMessageContaining("Product");
		}
	}

	@Nested
	@DisplayName("Update Product Tests")
	class UpdateProductTests {

		@Test
		@DisplayName("Should update product successfully")
		void shouldUpdateProductSuccessfully() {
			when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
			when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
			when(unitRepository.findById(1L)).thenReturn(Optional.of(testUnit));
			when(vatRateRepository.findById(1L)).thenReturn(Optional.of(testVatRate));
			when(productRepository.save(any(Product.class))).thenReturn(testProduct);

			ProductResponse response = productService.updateProduct(1L, 100L, testRequest);

			assertThat(response).isNotNull();
			verify(productRepository).save(any(Product.class));
			verify(eventPublisher).publishProductUpdated(any(Product.class));
		}

		@Test
		@DisplayName("Should throw exception when product does not belong to supplier")
		void shouldThrowExceptionWhenProductNotOwnedBySupplier() {
			when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

			assertThatThrownBy(() -> productService.updateProduct(1L, 999L, testRequest))
					.isInstanceOf(InvalidOperationException.class)
					.hasMessageContaining("does not belong");
		}
	}

	@Nested
	@DisplayName("Publish Product Tests")
	class PublishProductTests {

		@Test
		@DisplayName("Should publish product successfully")
		void shouldPublishProductSuccessfully() {
			testProduct.setStatus(Product.ProductStatus.DRAFT);
			when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
			when(productRepository.save(any(Product.class))).thenReturn(testProduct);

			ProductResponse response = productService.publishProduct(1L, 100L);

			assertThat(response).isNotNull();
			verify(eventPublisher).publishProductPublished(any(Product.class));
		}

		@Test
		@DisplayName("Should throw exception when publishing archived product")
		void shouldThrowExceptionWhenPublishingArchivedProduct() {
			testProduct.setStatus(Product.ProductStatus.ARCHIVED);
			when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

			assertThatThrownBy(() -> productService.publishProduct(1L, 100L))
					.isInstanceOf(IllegalStateException.class);
		}
	}

	@Nested
	@DisplayName("Archive Product Tests")
	class ArchiveProductTests {

		@Test
		@DisplayName("Should archive product successfully")
		void shouldArchiveProductSuccessfully() {
			testProduct.setStatus(Product.ProductStatus.PUBLISHED);
			when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
			when(productRepository.save(any(Product.class))).thenReturn(testProduct);

			ProductResponse response = productService.archiveProduct(1L, 100L);

			assertThat(response).isNotNull();
			verify(productRepository).save(any(Product.class));
		}

		@Test
		@DisplayName("Should throw exception when archiving product of another supplier")
		void shouldThrowExceptionWhenArchivingProductOfAnotherSupplier() {
			when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

			assertThatThrownBy(() -> productService.archiveProduct(1L, 999L))
					.isInstanceOf(InvalidOperationException.class);
		}
	}

	@Nested
	@DisplayName("ToDraft Product Tests")
	class ToDraftProductTests {

		@Test
		@DisplayName("Should move product to draft successfully")
		void shouldMoveProductToDraftSuccessfully() {
			testProduct.setStatus(Product.ProductStatus.PUBLISHED);
			when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
			when(productRepository.save(any(Product.class))).thenReturn(testProduct);

			ProductResponse response = productService.toDraft(1L, 100L);

			assertThat(response).isNotNull();
			verify(productRepository).save(any(Product.class));
		}

		@Test
		@DisplayName("Should throw exception when moving archived product to draft")
		void shouldThrowExceptionWhenMovingArchivedProductToDraft() {
			testProduct.setStatus(Product.ProductStatus.ARCHIVED);
			when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

			assertThatThrownBy(() -> productService.toDraft(1L, 100L))
					.isInstanceOf(IllegalStateException.class);
		}

		@Test
		@DisplayName("Should throw exception for wrong supplier")
		void shouldThrowExceptionForWrongSupplier() {
			when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

			assertThatThrownBy(() -> productService.toDraft(1L, 999L))
					.isInstanceOf(InvalidOperationException.class);
		}
	}

	@Nested
	@DisplayName("Search Products Tests")
	class SearchProductsTests {

		@Test
		@DisplayName("Should search products with filters")
		void shouldSearchProductsWithFilters() {
			ProductSearchRequest request = new ProductSearchRequest(
					1L, null, null, null, "test", 0, 10, "name", "asc"
			);
			Page<Product> page = new PageImpl<>(List.of(testProduct));
			when(productRepository.searchProducts(any(), any(), any(), any(), any(), any()))
					.thenReturn(page);

			PageResponse<ProductResponse> response = productService.searchProducts(request);

			assertThat(response.content()).hasSize(1);
			assertThat(response.totalElements()).isEqualTo(1);
		}

		@Test
		@DisplayName("Should search products with descending sort")
		void shouldSearchProductsWithDescendingSort() {
			ProductSearchRequest request = new ProductSearchRequest(
					null, null, null, null, null, 0, 10, "pricePerUnit", "desc"
			);
			Page<Product> page = new PageImpl<>(List.of(testProduct));
			when(productRepository.searchProducts(any(), any(), any(), any(), any(), any()))
					.thenReturn(page);

			PageResponse<ProductResponse> response = productService.searchProducts(request);

			assertThat(response.content()).hasSize(1);
		}
	}

	@Nested
	@DisplayName("Get Supplier Products Tests")
	class GetSupplierProductsTests {

		@Test
		@DisplayName("Should return supplier products")
		void shouldReturnSupplierProducts() {
			Page<Product> page = new PageImpl<>(List.of(testProduct));
			when(productRepository.findBySupplierId(eq(100L), any(Pageable.class))).thenReturn(page);

			PageResponse<ProductResponse> response = productService.getSupplierProducts(100L, 0, 10);

			assertThat(response.content()).hasSize(1);
			assertThat(response.totalElements()).isEqualTo(1);
		}
	}
}
