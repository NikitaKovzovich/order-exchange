package by.bsuir.orderservice.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

	private Order order;
	private OrderStatus pendingStatus;
	private OrderStatus confirmedStatus;
	private OrderStatus rejectedStatus;
	private OrderStatus shippedStatus;
	private OrderStatus deliveredStatus;

	@BeforeEach
	void setUp() {
		pendingStatus = OrderStatus.builder()
				.id(1L)
				.code(OrderStatus.Codes.PENDING_CONFIRMATION)
				.name("Ожидает подтверждения")
				.build();

		confirmedStatus = OrderStatus.builder()
				.id(2L)
				.code(OrderStatus.Codes.CONFIRMED)
				.name("Подтвержден")
				.build();

		rejectedStatus = OrderStatus.builder()
				.id(3L)
				.code(OrderStatus.Codes.REJECTED)
				.name("Отклонен")
				.build();

		shippedStatus = OrderStatus.builder()
				.id(4L)
				.code(OrderStatus.Codes.SHIPPED)
				.name("Отгружен")
				.build();

		deliveredStatus = OrderStatus.builder()
				.id(5L)
				.code(OrderStatus.Codes.DELIVERED)
				.name("Доставлен")
				.build();

		order = Order.builder()
				.id(1L)
				.orderNumber("ORD-123")
				.supplierId(100L)
				.customerId(200L)
				.status(pendingStatus)
				.deliveryAddress("Test Address")
				.totalAmount(new BigDecimal("1000.00"))
				.vatAmount(new BigDecimal("200.00"))
				.build();
	}

	@Nested
	@DisplayName("Order State Transitions")
	class StateTransitionTests {

		@Test
		@DisplayName("Should confirm order from pending")
		void shouldConfirmOrderFromPending() {
			order.confirm(confirmedStatus);
			assertThat(order.getStatus().getCode()).isEqualTo(OrderStatus.Codes.CONFIRMED);
		}

		@Test
		@DisplayName("Should reject order from pending")
		void shouldRejectOrderFromPending() {
			order.reject(rejectedStatus, "Out of stock");
			assertThat(order.getStatus().getCode()).isEqualTo(OrderStatus.Codes.REJECTED);
		}

		@Test
		@DisplayName("Should ship order")
		void shouldShipOrder() {
			OrderStatus paidStatus = OrderStatus.builder()
					.id(6L)
					.code(OrderStatus.Codes.PAID)
					.name("Оплачен")
					.build();
			order.setStatus(paidStatus);

			order.ship(shippedStatus);
			assertThat(order.getStatus().getCode()).isEqualTo(OrderStatus.Codes.SHIPPED);
		}

		@Test
		@DisplayName("Should deliver order")
		void shouldDeliverOrder() {
			order.setStatus(shippedStatus);

			order.deliver(deliveredStatus);
			assertThat(order.getStatus().getCode()).isEqualTo(OrderStatus.Codes.DELIVERED);
		}

		@Test
		@DisplayName("Should throw exception for invalid transition")
		void shouldThrowExceptionForInvalidTransition() {
			assertThatThrownBy(() -> order.ship(shippedStatus))
					.isInstanceOf(IllegalStateException.class)
					.hasMessageContaining("Cannot transition");
		}
	}

	@Nested
	@DisplayName("OrderStatus Transition Rules")
	class OrderStatusTransitionTests {

		@Test
		@DisplayName("Should allow pending to confirmed")
		void shouldAllowPendingToConfirmed() {
			assertThat(OrderStatus.canTransition(
					OrderStatus.Codes.PENDING_CONFIRMATION,
					OrderStatus.Codes.CONFIRMED
			)).isTrue();
		}

		@Test
		@DisplayName("Should allow pending to rejected")
		void shouldAllowPendingToRejected() {
			assertThat(OrderStatus.canTransition(
					OrderStatus.Codes.PENDING_CONFIRMATION,
					OrderStatus.Codes.REJECTED
			)).isTrue();
		}

		@Test
		@DisplayName("Should not allow pending to shipped")
		void shouldNotAllowPendingToShipped() {
			assertThat(OrderStatus.canTransition(
					OrderStatus.Codes.PENDING_CONFIRMATION,
					OrderStatus.Codes.SHIPPED
			)).isFalse();
		}

		@Test
		@DisplayName("Should allow shipped to delivered")
		void shouldAllowShippedToDelivered() {
			assertThat(OrderStatus.canTransition(
					OrderStatus.Codes.SHIPPED,
					OrderStatus.Codes.DELIVERED
			)).isTrue();
		}

		@Test
		@DisplayName("Should allow shipped to awaiting correction")
		void shouldAllowShippedToAwaitingCorrection() {
			assertThat(OrderStatus.canTransition(
					OrderStatus.Codes.SHIPPED,
					OrderStatus.Codes.AWAITING_CORRECTION
			)).isTrue();
		}
	}
}
