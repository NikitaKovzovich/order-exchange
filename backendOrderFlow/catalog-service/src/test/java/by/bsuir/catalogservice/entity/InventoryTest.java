package by.bsuir.catalogservice.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryTest {

	private Inventory inventory;

	@BeforeEach
	void setUp() {
		inventory = Inventory.builder()
				.quantityAvailable(100)
				.reservedQuantity(20)
				.build();
	}

	@Nested
	@DisplayName("Add Stock Tests")
	class AddStockTests {

		@Test
		@DisplayName("Should add stock successfully")
		void shouldAddStockSuccessfully() {
			inventory.addStock(50);
			assertThat(inventory.getQuantityAvailable()).isEqualTo(150);
		}

		@Test
		@DisplayName("Should throw exception for negative quantity")
		void shouldThrowExceptionForNegativeQuantity() {
			assertThatThrownBy(() -> inventory.addStock(-10))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("positive");
		}
	}

	@Nested
	@DisplayName("Reserve Stock Tests")
	class ReserveStockTests {

		@Test
		@DisplayName("Should reserve stock successfully")
		void shouldReserveStockSuccessfully() {
			inventory.reserve(30);
			assertThat(inventory.getReservedQuantity()).isEqualTo(50);
		}

		@Test
		@DisplayName("Should throw exception when insufficient stock")
		void shouldThrowExceptionWhenInsufficientStock() {
			assertThatThrownBy(() -> inventory.reserve(100))
					.isInstanceOf(IllegalStateException.class)
					.hasMessageContaining("Insufficient");
		}
	}

	@Nested
	@DisplayName("Cancel Reservation Tests")
	class CancelReservationTests {

		@Test
		@DisplayName("Should cancel reservation successfully")
		void shouldCancelReservationSuccessfully() {
			inventory.cancelReservation(10);
			assertThat(inventory.getReservedQuantity()).isEqualTo(10);
		}

		@Test
		@DisplayName("Should not go negative when canceling more than reserved")
		void shouldNotGoNegativeWhenCancelingMoreThanReserved() {
			inventory.cancelReservation(30);
			assertThat(inventory.getReservedQuantity()).isZero();
		}
	}

	@Nested
	@DisplayName("Ship Reserved Tests")
	class ShipReservedTests {

		@Test
		@DisplayName("Should ship reserved successfully")
		void shouldShipReservedSuccessfully() {
			inventory.shipReserved(10);
			assertThat(inventory.getQuantityAvailable()).isEqualTo(90);
			assertThat(inventory.getReservedQuantity()).isEqualTo(10);
		}

		@Test
		@DisplayName("Should throw exception when shipping more than reserved")
		void shouldThrowExceptionWhenShippingMoreThanReserved() {
			assertThatThrownBy(() -> inventory.shipReserved(30))
					.isInstanceOf(IllegalStateException.class);
		}
	}

	@Nested
	@DisplayName("Status Check Tests")
	class StatusCheckTests {

		@Test
		@DisplayName("Should return actual available quantity")
		void shouldReturnActualAvailable() {
			assertThat(inventory.getActualAvailable()).isEqualTo(80);
		}

		@Test
		@DisplayName("Should detect low stock")
		void shouldDetectLowStock() {
			Inventory lowStock = Inventory.builder()
					.quantityAvailable(5)
					.reservedQuantity(0)
					.build();
			assertThat(lowStock.isLowStock()).isTrue();
		}

		@Test
		@DisplayName("Should detect out of stock")
		void shouldDetectOutOfStock() {
			Inventory outOfStock = Inventory.builder()
					.quantityAvailable(10)
					.reservedQuantity(10)
					.build();
			assertThat(outOfStock.isOutOfStock()).isTrue();
		}

		@Test
		@DisplayName("Should check if has enough stock")
		void shouldCheckIfHasEnoughStock() {
			assertThat(inventory.hasEnough(50)).isTrue();
			assertThat(inventory.hasEnough(100)).isFalse();
		}
	}
}
