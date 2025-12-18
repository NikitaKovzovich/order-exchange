package by.bsuir.orderservice.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderItemTest {

	private OrderItem orderItem;

	@BeforeEach
	void setUp() {
		orderItem = OrderItem.builder()
				.id(1L)
				.productId(100L)
				.productName("Test Product")
				.productSku("SKU-001")
				.unitPrice(new BigDecimal("10.00"))
				.vatRate(new BigDecimal("20"))
				.quantity(5)
				.build();
	}

	@Nested
	@DisplayName("Calculate Totals Tests")
	class CalculateTotalsTests {

		@Test
		@DisplayName("Should calculate line total correctly")
		void shouldCalculateLineTotalCorrectly() {
			orderItem.calculateTotals();

			assertThat(orderItem.getLineTotal()).isEqualByComparingTo("50.00");
			assertThat(orderItem.getTotalPrice()).isEqualByComparingTo("50.00");
		}

		@Test
		@DisplayName("Should calculate VAT correctly")
		void shouldCalculateVatCorrectly() {
			orderItem.calculateTotals();

			assertThat(orderItem.getLineVat()).isEqualByComparingTo("10.00");
		}

		@Test
		@DisplayName("Should handle zero VAT rate")
		void shouldHandleZeroVatRate() {
			orderItem.setVatRate(BigDecimal.ZERO);
			orderItem.calculateTotals();

			assertThat(orderItem.getLineVat()).isEqualByComparingTo("0");
		}

		@Test
		@DisplayName("Should handle null values gracefully")
		void shouldHandleNullValuesGracefully() {
			OrderItem emptyItem = new OrderItem();
			emptyItem.calculateTotals();

			assertThat(emptyItem.getLineTotal()).isNull();
		}
	}

	@Nested
	@DisplayName("Calculate Total Price Tests")
	class CalculateTotalPriceTests {

		@Test
		@DisplayName("Should calculate total price")
		void shouldCalculateTotalPrice() {
			orderItem.calculateTotalPrice();

			assertThat(orderItem.getTotalPrice()).isEqualByComparingTo("50.00");
		}
	}

	@Nested
	@DisplayName("Set Quantity And Recalculate Tests")
	class SetQuantityAndRecalculateTests {

		@Test
		@DisplayName("Should update quantity and recalculate")
		void shouldUpdateQuantityAndRecalculate() {
			orderItem.setQuantityAndRecalculate(10);

			assertThat(orderItem.getQuantity()).isEqualTo(10);
			assertThat(orderItem.getTotalPrice()).isEqualByComparingTo("100.00");
		}
	}

	@Nested
	@DisplayName("Receipt Tests")
	class ReceiptTests {

		@Test
		@DisplayName("Should confirm full receipt")
		void shouldConfirmFullReceipt() {
			orderItem.confirmFullReceipt();

			assertThat(orderItem.getReceivedQuantity()).isEqualTo(5);
			assertThat(orderItem.hasDiscrepancy()).isFalse();
		}

		@Test
		@DisplayName("Should confirm partial receipt")
		void shouldConfirmPartialReceipt() {
			orderItem.confirmPartialReceipt(3);

			assertThat(orderItem.getReceivedQuantity()).isEqualTo(3);
			assertThat(orderItem.hasDiscrepancy()).isTrue();
			assertThat(orderItem.getQuantityDifference()).isEqualTo(2);
		}

		@Test
		@DisplayName("Should throw exception for invalid received quantity")
		void shouldThrowExceptionForInvalidReceivedQuantity() {
			assertThatThrownBy(() -> orderItem.confirmPartialReceipt(10))
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		@DisplayName("Should throw exception for negative received quantity")
		void shouldThrowExceptionForNegativeReceivedQuantity() {
			assertThatThrownBy(() -> orderItem.confirmPartialReceipt(-1))
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		@DisplayName("Should return zero difference when received is null")
		void shouldReturnZeroDifferenceWhenReceivedIsNull() {
			assertThat(orderItem.getQuantityDifference()).isZero();
		}

		@Test
		@DisplayName("Should not have discrepancy when received is null")
		void shouldNotHaveDiscrepancyWhenReceivedIsNull() {
			assertThat(orderItem.hasDiscrepancy()).isFalse();
		}
	}
}
