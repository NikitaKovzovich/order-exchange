package by.bsuir.orderservice;

import by.bsuir.orderservice.dto.*;
import by.bsuir.orderservice.entity.Order;
import by.bsuir.orderservice.entity.OrderStatus;
import by.bsuir.orderservice.repository.OrderRepository;
import by.bsuir.orderservice.repository.OrderStatusRepository;
import by.bsuir.orderservice.service.EventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OrderIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private OrderStatusRepository statusRepository;

	@MockBean
	private EventPublisher eventPublisher;

	@MockBean
	private RabbitTemplate rabbitTemplate;

	private OrderStatus pendingStatus;
	private OrderStatus confirmedStatus;

	@BeforeEach
	void setUp() {
		pendingStatus = statusRepository.findByCode(OrderStatus.Codes.PENDING_CONFIRMATION)
				.orElseGet(() -> statusRepository.save(OrderStatus.builder()
						.code(OrderStatus.Codes.PENDING_CONFIRMATION)
						.name("Ожидает подтверждения")
						.build()));

		confirmedStatus = statusRepository.findByCode(OrderStatus.Codes.CONFIRMED)
				.orElseGet(() -> statusRepository.save(OrderStatus.builder()
						.code(OrderStatus.Codes.CONFIRMED)
						.name("Подтвержден")
						.build()));
	}

	@Test
	@DisplayName("Should create order and retrieve it")
	void shouldCreateOrderAndRetrieveIt() throws Exception {
		CreateOrderRequest request = new CreateOrderRequest(
				100L,
				"Test Delivery Address",
				LocalDate.now().plusDays(7),
				List.of(new OrderItemRequest(1L, 10, new BigDecimal("100.00"), new BigDecimal("20")))
		);

		String responseJson = mockMvc.perform(post("/api/orders")
						.header("X-User-Company-Id", "200")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.orderNumber").exists())
				.andReturn().getResponse().getContentAsString();

		ApiResponse<OrderResponse> response = objectMapper.readValue(
				responseJson,
				objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, OrderResponse.class)
		);

		Long orderId = response.getData().id();
		assertThat(orderId).isNotNull();

		mockMvc.perform(get("/api/orders/" + orderId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.id").value(orderId))
				.andExpect(jsonPath("$.data.statusCode").value("PENDING_CONFIRMATION"));
	}

	@Test
	@DisplayName("Should list supplier orders")
	void shouldListSupplierOrders() throws Exception {
		Order order = Order.builder()
				.orderNumber("TEST-ORD-001")
				.supplierId(100L)
				.customerId(200L)
				.status(pendingStatus)
				.deliveryAddress("Test Address")
				.totalAmount(new BigDecimal("500.00"))
				.vatAmount(new BigDecimal("100.00"))
				.build();
		orderRepository.save(order);

		mockMvc.perform(get("/api/orders/supplier")
						.header("X-User-Company-Id", "100"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.content").isArray());
	}

	@Test
	@DisplayName("Should list customer orders")
	void shouldListCustomerOrders() throws Exception {
		Order order = Order.builder()
				.orderNumber("TEST-ORD-002")
				.supplierId(100L)
				.customerId(200L)
				.status(pendingStatus)
				.deliveryAddress("Test Address")
				.totalAmount(new BigDecimal("500.00"))
				.vatAmount(new BigDecimal("100.00"))
				.build();
		orderRepository.save(order);

		mockMvc.perform(get("/api/orders/customer")
						.header("X-User-Company-Id", "200"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.content").isArray());
	}

	@Test
	@DisplayName("Should confirm order")
	void shouldConfirmOrder() throws Exception {
		Order order = Order.builder()
				.orderNumber("TEST-ORD-003")
				.supplierId(100L)
				.customerId(200L)
				.status(pendingStatus)
				.deliveryAddress("Test Address")
				.totalAmount(new BigDecimal("500.00"))
				.vatAmount(new BigDecimal("100.00"))
				.build();
		order = orderRepository.save(order);

		mockMvc.perform(post("/api/orders/" + order.getId() + "/confirm")
						.header("X-User-Company-Id", "100"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.statusCode").value("CONFIRMED"));

		Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
		assertThat(updatedOrder.getStatus().getCode()).isEqualTo(OrderStatus.Codes.CONFIRMED);
	}

	@Test
	@DisplayName("Should reject order")
	void shouldRejectOrder() throws Exception {
		OrderStatus rejectedStatus = statusRepository.findByCode(OrderStatus.Codes.REJECTED)
				.orElseGet(() -> statusRepository.save(OrderStatus.builder()
						.code(OrderStatus.Codes.REJECTED)
						.name("Отклонен")
						.build()));

		Order order = Order.builder()
				.orderNumber("TEST-ORD-004")
				.supplierId(100L)
				.customerId(200L)
				.status(pendingStatus)
				.deliveryAddress("Test Address")
				.totalAmount(new BigDecimal("500.00"))
				.vatAmount(new BigDecimal("100.00"))
				.build();
		order = orderRepository.save(order);

		OrderActionRequest rejectRequest = new OrderActionRequest("Out of stock");

		mockMvc.perform(post("/api/orders/" + order.getId() + "/reject")
						.header("X-User-Company-Id", "100")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(rejectRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.statusCode").value("REJECTED"));
	}

	@Test
	@DisplayName("Should return 404 for non-existent order")
	void shouldReturn404ForNonExistentOrder() throws Exception {
		mockMvc.perform(get("/api/orders/99999"))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("Should return 400 for wrong supplier on confirm")
	void shouldReturn400ForWrongSupplierOnConfirm() throws Exception {
		Order order = Order.builder()
				.orderNumber("TEST-ORD-005")
				.supplierId(100L)
				.customerId(200L)
				.status(pendingStatus)
				.deliveryAddress("Test Address")
				.totalAmount(new BigDecimal("500.00"))
				.vatAmount(new BigDecimal("100.00"))
				.build();
		order = orderRepository.save(order);

		mockMvc.perform(post("/api/orders/" + order.getId() + "/confirm")
						.header("X-User-Company-Id", "999"))
				.andExpect(status().isBadRequest());
	}
}
