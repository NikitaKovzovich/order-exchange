package by.bsuir.orderservice.controller;

import by.bsuir.orderservice.dto.*;
import by.bsuir.orderservice.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

@WebMvcTest(CartController.class)
class CartControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CartService cartService;

	private ObjectMapper objectMapper;
	private CartResponse testCartResponse;
	private CartItemResponse testItemResponse;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());

		testItemResponse = new CartItemResponse(
				1L, 100L, 10L, "Test Product", "SKU-001",
				5, new BigDecimal("100.00"), new BigDecimal("20"),
				new BigDecimal("500.00"), new BigDecimal("100.00"),
				LocalDateTime.now()
		);

		testCartResponse = new CartResponse(
				1L, 1L, List.of(testItemResponse), 1,
				new BigDecimal("500.00"), new BigDecimal("100.00"),
				List.of(10L), LocalDateTime.now(), LocalDateTime.now()
		);
	}

	@Test
	@DisplayName("Should get cart")
	void shouldGetCart() throws Exception {
		when(cartService.getCart(1L)).thenReturn(testCartResponse);

		mockMvc.perform(get("/api/cart")
						.header("X-User-Company-Id", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.customerId").value(1))
				.andExpect(jsonPath("$.data.itemCount").value(1));
	}

	@Test
	@DisplayName("Should add item to cart")
	void shouldAddItemToCart() throws Exception {
		AddToCartRequest request = new AddToCartRequest(
				200L, 20L, "New Product", "SKU-002",
				3, new BigDecimal("50.00"), new BigDecimal("20")
		);

		when(cartService.addItem(eq(1L), any(AddToCartRequest.class))).thenReturn(testCartResponse);

		mockMvc.perform(post("/api/cart/items")
						.header("X-User-Company-Id", "1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true));

		verify(cartService).addItem(eq(1L), any(AddToCartRequest.class));
	}

	@Test
	@DisplayName("Should update item quantity")
	void shouldUpdateItemQuantity() throws Exception {
		UpdateCartItemRequest request = new UpdateCartItemRequest(10);

		when(cartService.updateItemQuantity(eq(1L), eq(100L), any(UpdateCartItemRequest.class)))
				.thenReturn(testCartResponse);

		mockMvc.perform(put("/api/cart/items/100")
						.header("X-User-Company-Id", "1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));

		verify(cartService).updateItemQuantity(eq(1L), eq(100L), any(UpdateCartItemRequest.class));
	}

	@Test
	@DisplayName("Should remove item from cart")
	void shouldRemoveItemFromCart() throws Exception {
		when(cartService.removeItem(1L, 100L)).thenReturn(testCartResponse);

		mockMvc.perform(delete("/api/cart/items/100")
						.header("X-User-Company-Id", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));

		verify(cartService).removeItem(1L, 100L);
	}

	@Test
	@DisplayName("Should clear cart")
	void shouldClearCart() throws Exception {
		doNothing().when(cartService).clearCart(1L);

		mockMvc.perform(delete("/api/cart")
						.header("X-User-Company-Id", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Cart cleared"));

		verify(cartService).clearCart(1L);
	}

	@Test
	@DisplayName("Should checkout cart")
	void shouldCheckoutCart() throws Exception {
		CheckoutRequest request = new CheckoutRequest("Test Address", LocalDate.now().plusDays(3), null);

		OrderResponse orderResponse = new OrderResponse(
				1L, "ORD-12345678", 10L, 1L,
				"PENDING_CONFIRMATION", "Ожидает подтверждения",
				"Test Address", LocalDate.now().plusDays(3),
				new BigDecimal("500.00"), new BigDecimal("100.00"),
				List.of(), LocalDateTime.now(), null
		);

		CheckoutResponse checkoutResponse = new CheckoutResponse(
				List.of(orderResponse), 1, "Successfully created 1 order(s)"
		);

		when(cartService.checkout(eq(1L), any(CheckoutRequest.class))).thenReturn(checkoutResponse);

		mockMvc.perform(post("/api/cart/checkout")
						.header("X-User-Company-Id", "1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.orderCount").value(1));

		verify(cartService).checkout(eq(1L), any(CheckoutRequest.class));
	}

	@Test
	@DisplayName("Should return 400 for invalid add request")
	void shouldReturn400ForInvalidAddRequest() throws Exception {
		AddToCartRequest invalidRequest = new AddToCartRequest(
				null, null, null, null, null, null, null
		);

		mockMvc.perform(post("/api/cart/items")
						.header("X-User-Company-Id", "1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(invalidRequest)))
				.andExpect(status().isBadRequest());
	}
}
