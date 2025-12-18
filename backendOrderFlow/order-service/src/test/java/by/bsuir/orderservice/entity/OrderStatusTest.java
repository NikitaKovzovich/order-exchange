package by.bsuir.orderservice.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusTest {

	@Nested
	@DisplayName("canTransition Tests")
	class CanTransitionTests {

		@ParameterizedTest
		@CsvSource({
				"CREATED, PENDING_CONFIRMATION, true",
				"CREATED, CONFIRMED, false",
				"PENDING_CONFIRMATION, CONFIRMED, true",
				"PENDING_CONFIRMATION, REJECTED, true",
				"PENDING_CONFIRMATION, SHIPPED, false",
				"CONFIRMED, AWAITING_PAYMENT, true",
				"CONFIRMED, CANCELLED, true",
				"CONFIRMED, SHIPPED, false",
				"AWAITING_PAYMENT, PENDING_PAYMENT_VERIFICATION, true",
				"AWAITING_PAYMENT, CANCELLED, true",
				"PENDING_PAYMENT_VERIFICATION, PAID, true",
				"PENDING_PAYMENT_VERIFICATION, AWAITING_PAYMENT, true",
				"PAID, AWAITING_SHIPMENT, true",
				"PAID, SHIPPED, true",
				"PAID, DELIVERED, false",
				"AWAITING_SHIPMENT, SHIPPED, true",
				"SHIPPED, DELIVERED, true",
				"SHIPPED, AWAITING_CORRECTION, true",
				"SHIPPED, CLOSED, false",
				"AWAITING_CORRECTION, DELIVERED, true",
				"DELIVERED, CLOSED, true",
				"DELIVERED, SHIPPED, false",
				"CLOSED, DELIVERED, false"
		})
		@DisplayName("Should validate transitions correctly")
		void shouldValidateTransitionsCorrectly(String from, String to, boolean expected) {
			boolean result = OrderStatus.canTransition(from, to);
			assertThat(result).isEqualTo(expected);
		}

		@Test
		@DisplayName("Should return false for unknown status")
		void shouldReturnFalseForUnknownStatus() {
			assertThat(OrderStatus.canTransition("UNKNOWN", "CONFIRMED")).isFalse();
		}
	}

	@Nested
	@DisplayName("getDisplayName Tests")
	class GetDisplayNameTests {

		@Test
		@DisplayName("Should return display name for PENDING_CONFIRMATION")
		void shouldReturnDisplayNameForPendingConfirmation() {
			String displayName = OrderStatus.getDisplayName(OrderStatus.Codes.PENDING_CONFIRMATION);
			assertThat(displayName).isEqualTo("Ожидает подтверждения");
		}

		@Test
		@DisplayName("Should return display name for CONFIRMED")
		void shouldReturnDisplayNameForConfirmed() {
			String displayName = OrderStatus.getDisplayName(OrderStatus.Codes.CONFIRMED);
			assertThat(displayName).isEqualTo("Подтвержден");
		}

		@Test
		@DisplayName("Should return display name for REJECTED")
		void shouldReturnDisplayNameForRejected() {
			String displayName = OrderStatus.getDisplayName(OrderStatus.Codes.REJECTED);
			assertThat(displayName).isEqualTo("Отклонен");
		}

		@Test
		@DisplayName("Should return display name for SHIPPED")
		void shouldReturnDisplayNameForShipped() {
			String displayName = OrderStatus.getDisplayName(OrderStatus.Codes.SHIPPED);
			assertThat(displayName).isEqualTo("Отгружен");
		}

		@Test
		@DisplayName("Should return display name for DELIVERED")
		void shouldReturnDisplayNameForDelivered() {
			String displayName = OrderStatus.getDisplayName(OrderStatus.Codes.DELIVERED);
			assertThat(displayName).isEqualTo("Доставлен");
		}

		@Test
		@DisplayName("Should return display name for CLOSED")
		void shouldReturnDisplayNameForClosed() {
			String displayName = OrderStatus.getDisplayName(OrderStatus.Codes.CLOSED);
			assertThat(displayName).isEqualTo("Закрыт");
		}

		@Test
		@DisplayName("Should return code for unrecognized code")
		void shouldReturnCodeForUnrecognizedCode() {
			String displayName = OrderStatus.getDisplayName("UNKNOWN_STATUS");
			assertThat(displayName).isEqualTo("UNKNOWN_STATUS");
		}
	}

	@Nested
	@DisplayName("Codes Constants Tests")
	class CodesConstantsTests {

		@Test
		@DisplayName("Should have all required status codes")
		void shouldHaveAllRequiredStatusCodes() {
			assertThat(OrderStatus.Codes.CREATED).isEqualTo("CREATED");
			assertThat(OrderStatus.Codes.PENDING_CONFIRMATION).isEqualTo("PENDING_CONFIRMATION");
			assertThat(OrderStatus.Codes.CONFIRMED).isEqualTo("CONFIRMED");
			assertThat(OrderStatus.Codes.REJECTED).isEqualTo("REJECTED");
			assertThat(OrderStatus.Codes.AWAITING_PAYMENT).isEqualTo("AWAITING_PAYMENT");
			assertThat(OrderStatus.Codes.PENDING_PAYMENT_VERIFICATION).isEqualTo("PENDING_PAYMENT_VERIFICATION");
			assertThat(OrderStatus.Codes.PAID).isEqualTo("PAID");
			assertThat(OrderStatus.Codes.AWAITING_SHIPMENT).isEqualTo("AWAITING_SHIPMENT");
			assertThat(OrderStatus.Codes.SHIPPED).isEqualTo("SHIPPED");
			assertThat(OrderStatus.Codes.DELIVERED).isEqualTo("DELIVERED");
			assertThat(OrderStatus.Codes.AWAITING_CORRECTION).isEqualTo("AWAITING_CORRECTION");
			assertThat(OrderStatus.Codes.CLOSED).isEqualTo("CLOSED");
			assertThat(OrderStatus.Codes.CANCELLED).isEqualTo("CANCELLED");
		}
	}
}
