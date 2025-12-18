package by.bsuir.orderservice.controller;

import by.bsuir.orderservice.dto.*;
import by.bsuir.orderservice.exception.InvalidOperationException;
import by.bsuir.orderservice.exception.ResourceNotFoundException;
import by.bsuir.orderservice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private OrderService orderService;

	private OrderResponse testOrderResponse;

	@BeforeEach
	void setUp() {
		testOrderResponse = new OrderResponse(
				1L,
				"ORD-123456-ABCD",
				100L,
				200L,
				"PENDING_CONFIRMATION",
				"Ожидает подтверждения",
				"Test Address",
				LocalDate.now().plusDays(7),
				new BigDecimal("1000.00"),
				new BigDecimal("200.00"),
				List.of(),
				LocalDateTime.now(),
				null
		);
	}

	@Nested
	@DisplayName("GET /api/orders/{id}")
	class GetOrderByIdTests {

		@Test
		@DisplayName("Should return order by ID")
		void shouldReturnOrderById() throws Exception {
			when(orderService.getOrderById(1L)).thenReturn(testOrderResponse);

			mockMvc.perform(get("/api/orders/1"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.success").value(true))
					.andExpect(jsonPath("$.data.orderNumber").value("ORD-123456-ABCD"));
		}

		@Test
		@DisplayName("Should return 404 when order not found")
		void shouldReturn404WhenOrderNotFound() throws Exception {
			when(orderService.getOrderById(999L))
					.thenThrow(new ResourceNotFoundException("Order", "id", 999L));

			mockMvc.perform(get("/api/orders/999"))
					.andExpect(status().isNotFound());
		}
	}

	@Nested
	@DisplayName("GET /api/orders/number/{orderNumber}")
	class GetOrderByNumberTests {

		@Test
		@DisplayName("Should return order by number")
		void shouldReturnOrderByNumber() throws Exception {
			when(orderService.getOrderByNumber("ORD-123456-ABCD")).thenReturn(testOrderResponse);

			mockMvc.perform(get("/api/orders/number/ORD-123456-ABCD"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.success").value(true))
					.andExpect(jsonPath("$.data.id").value(1));
		}
	}

	@Nested
	@DisplayName("GET /api/orders/supplier")
	class GetSupplierOrdersTests {

		@Test
		@DisplayName("Should return supplier orders")
		void shouldReturnSupplierOrders() throws Exception {
			PageResponse<OrderResponse> pageResponse = new PageResponse<>(
					List.of(testOrderResponse), 0, 20, 1, 1, true, true
			);
			when(orderService.getSupplierOrders(eq(100L), isNull(), eq(0), eq(20)))
					.thenReturn(pageResponse);

			mockMvc.perform(get("/api/orders/supplier")
							.header("X-User-Company-Id", "100")
							.param("page", "0")
							.param("size", "20"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.success").value(true))
					.andExpect(jsonPath("$.data.content").isArray())
					.andExpect(jsonPath("$.data.totalElements").value(1));
		}

		@Test
		@DisplayName("Should return supplier orders filtered by status")
		void shouldReturnSupplierOrdersFilteredByStatus() throws Exception {
			PageResponse<OrderResponse> pageResponse = new PageResponse<>(
					List.of(testOrderResponse), 0, 20, 1, 1, true, true
			);
			when(orderService.getSupplierOrders(eq(100L), eq("PENDING_CONFIRMATION"), eq(0), eq(20)))
					.thenReturn(pageResponse);

			mockMvc.perform(get("/api/orders/supplier")
							.header("X-User-Company-Id", "100")
							.param("status", "PENDING_CONFIRMATION"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.data.content[0].statusCode").value("PENDING_CONFIRMATION"));
		}
	}

	@Nested
	@DisplayName("GET /api/orders/customer")
	class GetCustomerOrdersTests {

		@Test
		@DisplayName("Should return customer orders")
		void shouldReturnCustomerOrders() throws Exception {
			PageResponse<OrderResponse> pageResponse = new PageResponse<>(
					List.of(testOrderResponse), 0, 20, 1, 1, true, true
			);
			when(orderService.getCustomerOrders(eq(200L), isNull(), eq(0), eq(20)))
					.thenReturn(pageResponse);

			mockMvc.perform(get("/api/orders/customer")
							.header("X-User-Company-Id", "200"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.success").value(true))
					.andExpect(jsonPath("$.data.content").isArray());
		}
	}

	@Nested
	@DisplayName("POST /api/orders")
	class CreateOrderTests {

		@Test
		@DisplayName("Should create order successfully")
		void shouldCreateOrderSuccessfully() throws Exception {
			CreateOrderRequest request = new CreateOrderRequest(
					100L,
					"Delivery Address",
					LocalDate.now().plusDays(7),
					List.of(new OrderItemRequest(1L, 10, new BigDecimal("100.00"), new BigDecimal("20")))
			);

			when(orderService.createOrder(eq(200L), any(CreateOrderRequest.class)))
					.thenReturn(testOrderResponse);

			mockMvc.perform(post("/api/orders")
							.header("X-User-Company-Id", "200")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isCreated())
					.andExpect(jsonPath("$.success").value(true))
					.andExpect(jsonPath("$.data.orderNumber").exists());
		}

		@Test
		@DisplayName("Should return 400 for invalid request")
		void shouldReturn400ForInvalidRequest() throws Exception {
			CreateOrderRequest invalidRequest = new CreateOrderRequest(
					null,
					"",
					null,
					List.of()
			);

			mockMvc.perform(post("/api/orders")
							.header("X-User-Company-Id", "200")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(invalidRequest)))
					.andExpect(status().isBadRequest());
		}
	}

	@Nested
	@DisplayName("POST /api/orders/{id}/confirm")
	class ConfirmOrderTests {

		@Test
		@DisplayName("Should confirm order successfully")
		void shouldConfirmOrderSuccessfully() throws Exception {
			OrderResponse confirmedOrder = new OrderResponse(
					1L, "ORD-123456-ABCD", 100L, 200L,
					"CONFIRMED", "Подтвержден", "Test Address",
					LocalDate.now().plusDays(7), new BigDecimal("1000.00"),
					new BigDecimal("200.00"), List.of(), LocalDateTime.now(), LocalDateTime.now()
			);
			when(orderService.confirmOrder(1L, 100L)).thenReturn(confirmedOrder);

			mockMvc.perform(post("/api/orders/1/confirm")
							.header("X-User-Company-Id", "100"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.success").value(true))
					.andExpect(jsonPath("$.data.statusCode").value("CONFIRMED"));
		}

		@Test
		@DisplayName("Should return 400 for wrong supplier")
		void shouldReturn400ForWrongSupplier() throws Exception {
			when(orderService.confirmOrder(1L, 999L))
					.thenThrow(new InvalidOperationException("confirm", "Order does not belong to this supplier"));

			mockMvc.perform(post("/api/orders/1/confirm")
							.header("X-User-Company-Id", "999"))
					.andExpect(status().isBadRequest());
		}
	}

	@Nested
	@DisplayName("POST /api/orders/{id}/reject")
	class RejectOrderTests {

		@Test
		@DisplayName("Should reject order successfully")
		void shouldRejectOrderSuccessfully() throws Exception {
			OrderResponse rejectedOrder = new OrderResponse(
					1L, "ORD-123456-ABCD", 100L, 200L,
					"REJECTED", "Отклонен", "Test Address",
					LocalDate.now().plusDays(7), new BigDecimal("1000.00"),
					new BigDecimal("200.00"), List.of(), LocalDateTime.now(), LocalDateTime.now()
			);
			when(orderService.rejectOrder(eq(1L), eq(100L), anyString())).thenReturn(rejectedOrder);

			OrderActionRequest request = new OrderActionRequest("Out of stock");

			mockMvc.perform(post("/api/orders/1/reject")
							.header("X-User-Company-Id", "100")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.data.statusCode").value("REJECTED"));
		}
	}

	@Nested
	@DisplayName("POST /api/orders/{id}/confirm-payment")
	class ConfirmPaymentTests {

		@Test
		@DisplayName("Should confirm payment successfully")
		void shouldConfirmPaymentSuccessfully() throws Exception {
			OrderResponse paidOrder = new OrderResponse(
					1L, "ORD-123456-ABCD", 100L, 200L,
					"PAID", "Оплачен", "Test Address",
					LocalDate.now().plusDays(7), new BigDecimal("1000.00"),
					new BigDecimal("200.00"), List.of(), LocalDateTime.now(), LocalDateTime.now()
			);
			when(orderService.confirmPayment(1L, 100L)).thenReturn(paidOrder);

			mockMvc.perform(post("/api/orders/1/confirm-payment")
							.header("X-User-Company-Id", "100"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.data.statusCode").value("PAID"));
		}
	}

	@Nested
	@DisplayName("POST /api/orders/{id}/ship")
	class ShipOrderTests {

		@Test
		@DisplayName("Should ship order successfully")
		void shouldShipOrderSuccessfully() throws Exception {
			OrderResponse shippedOrder = new OrderResponse(
					1L, "ORD-123456-ABCD", 100L, 200L,
					"SHIPPED", "Отгружен", "Test Address",
					LocalDate.now().plusDays(7), new BigDecimal("1000.00"),
					new BigDecimal("200.00"), List.of(), LocalDateTime.now(), LocalDateTime.now()
			);
			when(orderService.shipOrder(1L, 100L)).thenReturn(shippedOrder);

			mockMvc.perform(post("/api/orders/1/ship")
							.header("X-User-Company-Id", "100"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.data.statusCode").value("SHIPPED"));
		}
	}

	@Nested
	@DisplayName("POST /api/orders/{id}/deliver")
	class DeliverOrderTests {

		@Test
		@DisplayName("Should deliver order successfully")
		void shouldDeliverOrderSuccessfully() throws Exception {
			OrderResponse deliveredOrder = new OrderResponse(
					1L, "ORD-123456-ABCD", 100L, 200L,
					"DELIVERED", "Доставлен", "Test Address",
					LocalDate.now().plusDays(7), new BigDecimal("1000.00"),
					new BigDecimal("200.00"), List.of(), LocalDateTime.now(), LocalDateTime.now()
			);
			when(orderService.deliverOrder(1L, 200L)).thenReturn(deliveredOrder);

			mockMvc.perform(post("/api/orders/1/deliver")
							.header("X-User-Company-Id", "200"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.data.statusCode").value("DELIVERED"));
		}

		@Test
		@DisplayName("Should return 400 for wrong customer")
		void shouldReturn400ForWrongCustomer() throws Exception {
			when(orderService.deliverOrder(1L, 999L))
					.thenThrow(new InvalidOperationException("deliver", "Order does not belong to this customer"));

			mockMvc.perform(post("/api/orders/1/deliver")
							.header("X-User-Company-Id", "999"))
					.andExpect(status().isBadRequest());
		}
	}
}
