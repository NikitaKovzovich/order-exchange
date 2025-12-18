package by.bsuir.catalogservice.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

	private Product product;
	private Inventory inventory;

	@BeforeEach
	void setUp() {
		product = Product.builder()
				.id(1L)
				.name("Test Product")
				.pricePerUnit(new BigDecimal("100.00"))
				.status(Product.ProductStatus.DRAFT)
				.build();

		inventory = Inventory.builder()
				.quantityAvailable(50)
				.reservedQuantity(0)
				.build();
		product.setInventory(inventory);
	}

	@Test
	@DisplayName("Should publish product from draft")
	void shouldPublishProductFromDraft() {
		product.publish();
		assertThat(product.getStatus()).isEqualTo(Product.ProductStatus.PUBLISHED);
	}

	@Test
	@DisplayName("Should throw exception when publishing archived product")
	void shouldThrowExceptionWhenPublishingArchivedProduct() {
		product.setStatus(Product.ProductStatus.ARCHIVED);
		assertThatThrownBy(() -> product.publish())
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("archived");
	}

	@Test
	@DisplayName("Should move product to draft from published")
	void shouldMoveProductToDraftFromPublished() {
		product.setStatus(Product.ProductStatus.PUBLISHED);
		product.toDraft();
		assertThat(product.getStatus()).isEqualTo(Product.ProductStatus.DRAFT);
	}

	@Test
	@DisplayName("Should throw exception when moving archived to draft")
	void shouldThrowExceptionWhenMovingArchivedToDraft() {
		product.setStatus(Product.ProductStatus.ARCHIVED);
		assertThatThrownBy(() -> product.toDraft())
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("archived");
	}

	@Test
	@DisplayName("Should archive product")
	void shouldArchiveProduct() {
		product.archive();
		assertThat(product.getStatus()).isEqualTo(Product.ProductStatus.ARCHIVED);
	}

	@Test
	@DisplayName("Should check if product is available")
	void shouldCheckIfProductIsAvailable() {
		product.setStatus(Product.ProductStatus.PUBLISHED);
		assertThat(product.isAvailable()).isTrue();
	}

	@Test
	@DisplayName("Should return not available when draft")
	void shouldReturnNotAvailableWhenDraft() {
		assertThat(product.isAvailable()).isFalse();
	}

	@Test
	@DisplayName("Should return not available when out of stock")
	void shouldReturnNotAvailableWhenOutOfStock() {
		product.setStatus(Product.ProductStatus.PUBLISHED);
		inventory.setQuantityAvailable(0);
		assertThat(product.isAvailable()).isFalse();
	}

	@Test
	@DisplayName("Should get available quantity")
	void shouldGetAvailableQuantity() {
		assertThat(product.getAvailableQuantity()).isEqualTo(50);
	}

	@Test
	@DisplayName("Should return zero when no inventory")
	void shouldReturnZeroWhenNoInventory() {
		product.setInventory(null);
		assertThat(product.getAvailableQuantity()).isZero();
	}

	@Test
	@DisplayName("Should calculate price with VAT")
	void shouldCalculatePriceWithVat() {
		VatRate vatRate = VatRate.builder()
				.ratePercentage(new BigDecimal("20.00"))
				.build();
		product.setVatRate(vatRate);

		BigDecimal priceWithVat = product.getPriceWithVat();
		assertThat(priceWithVat).isEqualByComparingTo(new BigDecimal("120.00"));
	}

	@Test
	@DisplayName("Should return base price when no VAT")
	void shouldReturnBasePriceWhenNoVat() {
		assertThat(product.getPriceWithVat()).isEqualByComparingTo(new BigDecimal("100.00"));
	}
}
