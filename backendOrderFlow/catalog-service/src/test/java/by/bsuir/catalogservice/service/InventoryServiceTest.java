package by.bsuir.catalogservice.service;

import by.bsuir.catalogservice.dto.InventoryResponse;
import by.bsuir.catalogservice.dto.InventoryUpdateRequest;
import by.bsuir.catalogservice.entity.Inventory;
import by.bsuir.catalogservice.entity.Product;
import by.bsuir.catalogservice.exception.ResourceNotFoundException;
import by.bsuir.catalogservice.repository.InventoryRepository;
import by.bsuir.catalogservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

	@Mock
	private InventoryRepository inventoryRepository;
	@Mock
	private ProductRepository productRepository;
	@Mock
	private EventPublisher eventPublisher;

	@InjectMocks
	private InventoryService inventoryService;

	private Product testProduct;
	private Inventory testInventory;

	@BeforeEach
	void setUp() {
		testProduct = Product.builder()
				.id(1L)
				.supplierId(100L)
				.name("Test Product")
				.sku("SKU-001")
				.pricePerUnit(new BigDecimal("99.99"))
				.build();

		testInventory = Inventory.builder()
				.product(testProduct)
				.quantityAvailable(50)
				.reservedQuantity(10)
				.build();
		testInventory.setProductId(1L);
	}

	@Nested
	@DisplayName("Get Inventory Tests")
	class GetInventoryTests {

		@Test
		@DisplayName("Should return inventory for product")
		void shouldReturnInventoryForProduct() {
			when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));

			InventoryResponse response = inventoryService.getInventory(1L);

			assertThat(response).isNotNull();
			assertThat(response.quantityAvailable()).isEqualTo(50);
			assertThat(response.reservedQuantity()).isEqualTo(10);
		}

		@Test
		@DisplayName("Should throw exception when inventory not found")
		void shouldThrowExceptionWhenInventoryNotFound() {
			when(inventoryRepository.findById(999L)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> inventoryService.getInventory(999L))
					.isInstanceOf(ResourceNotFoundException.class);
		}
	}

	@Nested
	@DisplayName("Update Inventory Tests")
	class UpdateInventoryTests {

		@Test
		@DisplayName("Should update inventory quantity")
		void shouldUpdateInventoryQuantity() {
			InventoryUpdateRequest request = new InventoryUpdateRequest(100, "Restocking");
			when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
			when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));
			when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

			InventoryResponse response = inventoryService.updateQuantity(1L, 100L, request);

			assertThat(response).isNotNull();
			verify(inventoryRepository).save(any(Inventory.class));
		}

		@Test
		@DisplayName("Should throw exception when product not owned by supplier")
		void shouldThrowExceptionWhenProductNotOwned() {
			InventoryUpdateRequest request = new InventoryUpdateRequest(100, "Restocking");
			when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

			assertThatThrownBy(() -> inventoryService.updateQuantity(1L, 999L, request))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("does not belong");
		}
	}

	@Nested
	@DisplayName("Add Stock Tests")
	class AddStockTests {

		@Test
		@DisplayName("Should add stock successfully")
		void shouldAddStockSuccessfully() {
			when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
			when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));
			when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

			InventoryResponse response = inventoryService.addStock(1L, 100L, 20, "Delivery received");

			assertThat(response).isNotNull();
			verify(inventoryRepository).save(any(Inventory.class));
		}

		@Test
		@DisplayName("Should throw exception for negative quantity")
		void shouldThrowExceptionForNegativeQuantity() {
			when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
			when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));

			assertThatThrownBy(() -> inventoryService.addStock(1L, 100L, -10, "Error"))
					.isInstanceOf(IllegalArgumentException.class);
		}
	}

	@Nested
	@DisplayName("Low Stock Tests")
	class LowStockTests {

		@Test
		@DisplayName("Should return low stock products")
		void shouldReturnLowStockProducts() {
			Inventory lowStockInventory = Inventory.builder()
					.product(testProduct)
					.quantityAvailable(5)
					.reservedQuantity(0)
					.build();
			lowStockInventory.setProductId(2L);

			when(inventoryRepository.findLowStock(10)).thenReturn(List.of(lowStockInventory));

			List<InventoryResponse> result = inventoryService.getLowStockProducts(10);

			assertThat(result).hasSize(1);
			assertThat(result.getFirst().quantityAvailable()).isEqualTo(5);
		}

		@Test
		@DisplayName("Should return out of stock products")
		void shouldReturnOutOfStockProducts() {
			Inventory outOfStockInventory = Inventory.builder()
					.product(testProduct)
					.quantityAvailable(0)
					.reservedQuantity(0)
					.build();
			outOfStockInventory.setProductId(2L);

			when(inventoryRepository.findOutOfStock()).thenReturn(List.of(outOfStockInventory));

			List<InventoryResponse> result = inventoryService.getOutOfStockProducts();

			assertThat(result).hasSize(1);
			assertThat(result.getFirst().quantityAvailable()).isEqualTo(0);
		}
	}
}
