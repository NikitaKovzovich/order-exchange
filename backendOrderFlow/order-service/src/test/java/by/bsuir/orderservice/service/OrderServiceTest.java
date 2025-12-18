package by.bsuir.orderservice.service;

import by.bsuir.orderservice.dto.*;
import by.bsuir.orderservice.entity.Order;
import by.bsuir.orderservice.entity.OrderStatus;
import by.bsuir.orderservice.exception.InvalidOperationException;
import by.bsuir.orderservice.exception.ResourceNotFoundException;
import by.bsuir.orderservice.repository.OrderRepository;
import by.bsuir.orderservice.repository.OrderStatusRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

	@Mock
	private OrderRepository orderRepository;
	@Mock
	private OrderStatusRepository statusRepository;
	@Mock
	private EventPublisher eventPublisher;

	@InjectMocks
	private OrderService orderService;

	private Order testOrder;
	private OrderStatus pendingStatus;
	private OrderStatus confirmedStatus;

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

		testOrder = Order.builder()
				.id(1L)
				.orderNumber("ORD-123456-ABCD")
				.supplierId(100L)
				.customerId(200L)
				.status(pendingStatus)
				.deliveryAddress("Test Address")
				.totalAmount(new BigDecimal("1000.00"))
				.vatAmount(new BigDecimal("200.00"))
				.items(new ArrayList<>())
				.build();
	}

	@Nested
	@DisplayName("Get Order Tests")
	class GetOrderTests {

		@Test
		@DisplayName("Should return order by ID")
		void shouldReturnOrderById() {
			when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

			OrderResponse response = orderService.getOrderById(1L);

			assertThat(response).isNotNull();
			assertThat(response.orderNumber()).isEqualTo("ORD-123456-ABCD");
		}

		@Test
		@DisplayName("Should throw exception when order not found")
		void shouldThrowExceptionWhenOrderNotFound() {
			when(orderRepository.findById(999L)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> orderService.getOrderById(999L))
					.isInstanceOf(ResourceNotFoundException.class);
		}

		@Test
		@DisplayName("Should return order by number")
		void shouldReturnOrderByNumber() {
			when(orderRepository.findByOrderNumber("ORD-123456-ABCD")).thenReturn(Optional.of(testOrder));

			OrderResponse response = orderService.getOrderByNumber("ORD-123456-ABCD");

			assertThat(response).isNotNull();
			assertThat(response.id()).isEqualTo(1L);
		}
	}

	@Nested
	@DisplayName("Get Orders List Tests")
	class GetOrdersListTests {

		@Test
		@DisplayName("Should return supplier orders")
		void shouldReturnSupplierOrders() {
			Page<Order> page = new PageImpl<>(List.of(testOrder));
			when(orderRepository.findBySupplierId(eq(100L), any(Pageable.class))).thenReturn(page);

			PageResponse<OrderResponse> response = orderService.getSupplierOrders(100L, null, 0, 10);

			assertThat(response.content()).hasSize(1);
			assertThat(response.totalElements()).isEqualTo(1);
		}

		@Test
		@DisplayName("Should return supplier orders by status")
		void shouldReturnSupplierOrdersByStatus() {
			Page<Order> page = new PageImpl<>(List.of(testOrder));
			when(statusRepository.findByCode(OrderStatus.Codes.PENDING_CONFIRMATION))
					.thenReturn(Optional.of(pendingStatus));
			when(orderRepository.findBySupplierIdAndStatus(eq(100L), eq(pendingStatus), any(Pageable.class)))
					.thenReturn(page);

			PageResponse<OrderResponse> response = orderService.getSupplierOrders(100L, "PENDING_CONFIRMATION", 0, 10);

			assertThat(response.content()).hasSize(1);
		}

		@Test
		@DisplayName("Should return customer orders")
		void shouldReturnCustomerOrders() {
			Page<Order> page = new PageImpl<>(List.of(testOrder));
			when(orderRepository.findByCustomerId(eq(200L), any(Pageable.class))).thenReturn(page);

			PageResponse<OrderResponse> response = orderService.getCustomerOrders(200L, null, 0, 10);

			assertThat(response.content()).hasSize(1);
		}
	}

	@Nested
	@DisplayName("Create Order Tests")
	class CreateOrderTests {

		@Test
		@DisplayName("Should create order successfully")
		void shouldCreateOrderSuccessfully() {
			CreateOrderRequest request = new CreateOrderRequest(
					100L,
					"Delivery Address",
					null,
					List.of(new OrderItemRequest(1L, 10, new BigDecimal("100.00"), new BigDecimal("20")))
			);

			when(statusRepository.findByCode(OrderStatus.Codes.PENDING_CONFIRMATION))
					.thenReturn(Optional.of(pendingStatus));
			when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

			OrderResponse response = orderService.createOrder(200L, request);

			assertThat(response).isNotNull();
			verify(eventPublisher).publishOrderCreated(any(Order.class));
		}
	}

	@Nested
	@DisplayName("Confirm Order Tests")
	class ConfirmOrderTests {

		@Test
		@DisplayName("Should confirm order successfully")
		void shouldConfirmOrderSuccessfully() {
			when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
			when(statusRepository.findByCode(OrderStatus.Codes.CONFIRMED))
					.thenReturn(Optional.of(confirmedStatus));
			when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

			OrderResponse response = orderService.confirmOrder(1L, 100L);

			assertThat(response).isNotNull();
			verify(eventPublisher).publishOrderConfirmed(any(Order.class));
		}

		@Test
		@DisplayName("Should throw exception for wrong supplier")
		void shouldThrowExceptionForWrongSupplier() {
			when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

			assertThatThrownBy(() -> orderService.confirmOrder(1L, 999L))
					.isInstanceOf(InvalidOperationException.class)
					.hasMessageContaining("does not belong");
		}
	}

	@Nested
	@DisplayName("Reject Order Tests")
	class RejectOrderTests {

		@Test
		@DisplayName("Should reject order successfully")
		void shouldRejectOrderSuccessfully() {
			OrderStatus rejectedStatus = OrderStatus.builder()
					.id(3L)
					.code(OrderStatus.Codes.REJECTED)
					.name("Отклонен")
					.build();

			when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
			when(statusRepository.findByCode(OrderStatus.Codes.REJECTED))
					.thenReturn(Optional.of(rejectedStatus));
			when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

			OrderResponse response = orderService.rejectOrder(1L, 100L, "Out of stock");

			assertThat(response).isNotNull();
			verify(eventPublisher).publishOrderRejected(any(Order.class), eq("Out of stock"));
		}
	}

	@Nested
	@DisplayName("Ship Order Tests")
	class ShipOrderTests {

		@Test
		@DisplayName("Should ship order successfully")
		void shouldShipOrderSuccessfully() {
			OrderStatus paidStatus = OrderStatus.builder()
					.id(4L)
					.code(OrderStatus.Codes.PAID)
					.name("Оплачен")
					.build();
			testOrder.setStatus(paidStatus);

			OrderStatus shippedStatus = OrderStatus.builder()
					.id(5L)
					.code(OrderStatus.Codes.SHIPPED)
					.name("Отгружен")
					.build();

			when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
			when(statusRepository.findByCode(OrderStatus.Codes.SHIPPED))
					.thenReturn(Optional.of(shippedStatus));
			when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

			OrderResponse response = orderService.shipOrder(1L, 100L);

			assertThat(response).isNotNull();
			verify(eventPublisher).publishOrderShipped(any(Order.class));
		}
	}

	@Nested
	@DisplayName("Deliver Order Tests")
	class DeliverOrderTests {

		@Test
		@DisplayName("Should deliver order successfully")
		void shouldDeliverOrderSuccessfully() {
			OrderStatus shippedStatus = OrderStatus.builder()
					.id(5L)
					.code(OrderStatus.Codes.SHIPPED)
					.name("Отгружен")
					.build();
			testOrder.setStatus(shippedStatus);

			OrderStatus deliveredStatus = OrderStatus.builder()
					.id(6L)
					.code(OrderStatus.Codes.DELIVERED)
					.name("Доставлен")
					.build();

			when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
			when(statusRepository.findByCode(OrderStatus.Codes.DELIVERED))
					.thenReturn(Optional.of(deliveredStatus));
			when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

			OrderResponse response = orderService.deliverOrder(1L, 200L);

			assertThat(response).isNotNull();
			verify(eventPublisher).publishOrderDelivered(any(Order.class));
		}

		@Test
		@DisplayName("Should throw exception for wrong customer")
		void shouldThrowExceptionForWrongCustomer() {
			when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

			assertThatThrownBy(() -> orderService.deliverOrder(1L, 999L))
					.isInstanceOf(InvalidOperationException.class)
					.hasMessageContaining("does not belong");
		}
	}
}
