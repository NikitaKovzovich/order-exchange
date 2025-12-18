package by.bsuir.orderservice.service;

import by.bsuir.orderservice.dto.*;
import by.bsuir.orderservice.entity.Cart;
import by.bsuir.orderservice.entity.CartItem;
import by.bsuir.orderservice.entity.Order;
import by.bsuir.orderservice.entity.OrderStatus;
import by.bsuir.orderservice.exception.InvalidOperationException;
import by.bsuir.orderservice.exception.ResourceNotFoundException;
import by.bsuir.orderservice.repository.CartRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

	@Mock
	private CartRepository cartRepository;

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private OrderStatusRepository statusRepository;

	@Mock
	private EventPublisher eventPublisher;

	@InjectMocks
	private CartService cartService;

	private Cart testCart;
	private CartItem testCartItem;
	private AddToCartRequest addToCartRequest;
	private OrderStatus pendingStatus;

	@BeforeEach
	void setUp() {
		testCartItem = CartItem.builder()
				.id(1L)
				.productId(100L)
				.supplierId(10L)
				.productName("Test Product")
				.productSku("SKU-001")
				.quantity(5)
				.unitPrice(new BigDecimal("100.00"))
				.vatRate(new BigDecimal("20"))
				.build();
		testCartItem.calculateTotal();

		testCart = Cart.builder()
				.id(1L)
				.customerId(1L)
				.items(new ArrayList<>(List.of(testCartItem)))
				.build();
		testCartItem.setCart(testCart);

		addToCartRequest = new AddToCartRequest(
				200L, 20L, "New Product", "SKU-002",
				3, new BigDecimal("50.00"), new BigDecimal("20")
		);

		pendingStatus = OrderStatus.builder()
				.id(1L)
				.code(OrderStatus.Codes.PENDING_CONFIRMATION)
				.name("Pending Confirmation")
				.build();
	}

	@Nested
	@DisplayName("Get Cart Tests")
	class GetCartTests {

		@Test
		@DisplayName("Should return cart with items")
		void shouldReturnCartWithItems() {
			when(cartRepository.findByCustomerIdWithItems(1L)).thenReturn(Optional.of(testCart));

			CartResponse response = cartService.getCart(1L);

			assertThat(response).isNotNull();
			assertThat(response.customerId()).isEqualTo(1L);
			assertThat(response.items()).hasSize(1);
			assertThat(response.itemCount()).isEqualTo(1);
		}

		@Test
		@DisplayName("Should return empty cart when not exists")
		void shouldReturnEmptyCartWhenNotExists() {
			when(cartRepository.findByCustomerIdWithItems(999L)).thenReturn(Optional.empty());

			CartResponse response = cartService.getCart(999L);

			assertThat(response).isNotNull();
			assertThat(response.customerId()).isEqualTo(999L);
			assertThat(response.items()).isEmpty();
			assertThat(response.itemCount()).isEqualTo(0);
		}
	}

	@Nested
	@DisplayName("Add Item Tests")
	class AddItemTests {

		@Test
		@DisplayName("Should add item to existing cart")
		void shouldAddItemToExistingCart() {
			when(cartRepository.findByCustomerIdWithItems(1L)).thenReturn(Optional.of(testCart));
			when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

			CartResponse response = cartService.addItem(1L, addToCartRequest);

			assertThat(response).isNotNull();
			verify(cartRepository).save(any(Cart.class));
		}

		@Test
		@DisplayName("Should create new cart when not exists")
		void shouldCreateNewCartWhenNotExists() {
			Cart newCart = Cart.builder().id(2L).customerId(999L).items(new ArrayList<>()).build();

			when(cartRepository.findByCustomerIdWithItems(999L)).thenReturn(Optional.empty());
			when(cartRepository.save(any(Cart.class))).thenReturn(newCart);

			CartResponse response = cartService.addItem(999L, addToCartRequest);

			assertThat(response).isNotNull();
			verify(cartRepository, atLeast(1)).save(any(Cart.class));
		}

		@Test
		@DisplayName("Should increase quantity when adding same product")
		void shouldIncreaseQuantityWhenAddingSameProduct() {
			AddToCartRequest sameProductRequest = new AddToCartRequest(
					100L, 10L, "Test Product", "SKU-001",
					2, new BigDecimal("100.00"), new BigDecimal("20")
			);

			when(cartRepository.findByCustomerIdWithItems(1L)).thenReturn(Optional.of(testCart));
			when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

			cartService.addItem(1L, sameProductRequest);

			assertThat(testCart.getItems().get(0).getQuantity()).isEqualTo(7); // 5 + 2
		}
	}

	@Nested
	@DisplayName("Update Item Quantity Tests")
	class UpdateItemQuantityTests {

		@Test
		@DisplayName("Should update item quantity")
		void shouldUpdateItemQuantity() {
			when(cartRepository.findByCustomerIdWithItems(1L)).thenReturn(Optional.of(testCart));
			when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

			UpdateCartItemRequest request = new UpdateCartItemRequest(10);
			CartResponse response = cartService.updateItemQuantity(1L, 100L, request);

			assertThat(response).isNotNull();
			verify(cartRepository).save(any(Cart.class));
		}

		@Test
		@DisplayName("Should remove item when quantity is zero")
		void shouldRemoveItemWhenQuantityIsZero() {
			when(cartRepository.findByCustomerIdWithItems(1L)).thenReturn(Optional.of(testCart));
			when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

			UpdateCartItemRequest request = new UpdateCartItemRequest(0);
			cartService.updateItemQuantity(1L, 100L, request);

			assertThat(testCart.getItems()).isEmpty();
		}

		@Test
		@DisplayName("Should throw when cart not found")
		void shouldThrowWhenCartNotFound() {
			when(cartRepository.findByCustomerIdWithItems(999L)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> cartService.updateItemQuantity(999L, 100L, new UpdateCartItemRequest(5)))
					.isInstanceOf(ResourceNotFoundException.class);
		}
	}

	@Nested
	@DisplayName("Remove Item Tests")
	class RemoveItemTests {

		@Test
		@DisplayName("Should remove item from cart")
		void shouldRemoveItemFromCart() {
			when(cartRepository.findByCustomerIdWithItems(1L)).thenReturn(Optional.of(testCart));
			when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

			CartResponse response = cartService.removeItem(1L, 100L);

			assertThat(response).isNotNull();
			assertThat(testCart.getItems()).isEmpty();
		}

		@Test
		@DisplayName("Should throw when cart not found")
		void shouldThrowWhenCartNotFoundOnRemove() {
			when(cartRepository.findByCustomerIdWithItems(999L)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> cartService.removeItem(999L, 100L))
					.isInstanceOf(ResourceNotFoundException.class);
		}
	}

	@Nested
	@DisplayName("Clear Cart Tests")
	class ClearCartTests {

		@Test
		@DisplayName("Should clear cart")
		void shouldClearCart() {
			when(cartRepository.findByCustomerIdWithItems(1L)).thenReturn(Optional.of(testCart));
			when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

			cartService.clearCart(1L);

			assertThat(testCart.getItems()).isEmpty();
			verify(cartRepository).save(testCart);
		}

		@Test
		@DisplayName("Should do nothing when cart not found")
		void shouldDoNothingWhenCartNotFound() {
			when(cartRepository.findByCustomerIdWithItems(999L)).thenReturn(Optional.empty());

			cartService.clearCart(999L); // Should not throw

			verify(cartRepository, never()).save(any());
		}
	}

	@Nested
	@DisplayName("Checkout Tests")
	class CheckoutTests {

		@Test
		@DisplayName("Should create orders grouped by supplier")
		void shouldCreateOrdersGroupedBySupplier() {
			// Add item from another supplier
			CartItem secondSupplierItem = CartItem.builder()
					.id(2L)
					.productId(200L)
					.supplierId(20L)
					.productName("Another Product")
					.quantity(2)
					.unitPrice(new BigDecimal("50.00"))
					.vatRate(new BigDecimal("20"))
					.build();
			secondSupplierItem.calculateTotal();
			testCart.getItems().add(secondSupplierItem);
			secondSupplierItem.setCart(testCart);

			Order mockOrder = Order.builder()
					.id(1L)
					.orderNumber("ORD-12345678")
					.supplierId(10L)
					.customerId(1L)
					.status(pendingStatus)
					.deliveryAddress("Test Address")
					.totalAmount(new BigDecimal("500.00"))
					.vatAmount(new BigDecimal("100.00"))
					.items(new ArrayList<>())
					.build();

			when(cartRepository.findByCustomerIdWithItems(1L)).thenReturn(Optional.of(testCart));
			when(statusRepository.findByCode(OrderStatus.Codes.PENDING_CONFIRMATION))
					.thenReturn(Optional.of(pendingStatus));
			when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);
			when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

			CheckoutRequest request = new CheckoutRequest("Test Address", LocalDate.now().plusDays(3), null);
			CheckoutResponse response = cartService.checkout(1L, request);

			assertThat(response).isNotNull();
			assertThat(response.orderCount()).isEqualTo(2); // 2 suppliers = 2 orders
			verify(orderRepository, times(2)).save(any(Order.class));
			verify(eventPublisher, times(2)).publishOrderCreated(any(Order.class));
		}

		@Test
		@DisplayName("Should throw when cart is empty")
		void shouldThrowWhenCartIsEmpty() {
			testCart.getItems().clear();
			when(cartRepository.findByCustomerIdWithItems(1L)).thenReturn(Optional.of(testCart));

			CheckoutRequest request = new CheckoutRequest("Test Address", null, null);

			assertThatThrownBy(() -> cartService.checkout(1L, request))
					.isInstanceOf(InvalidOperationException.class)
					.hasMessageContaining("cart is empty");
		}

		@Test
		@DisplayName("Should clear cart after checkout")
		void shouldClearCartAfterCheckout() {
			Order mockOrder = Order.builder()
					.id(1L)
					.orderNumber("ORD-12345678")
					.supplierId(10L)
					.customerId(1L)
					.status(pendingStatus)
					.deliveryAddress("Test Address")
					.totalAmount(new BigDecimal("500.00"))
					.vatAmount(new BigDecimal("100.00"))
					.items(new ArrayList<>())
					.build();

			when(cartRepository.findByCustomerIdWithItems(1L)).thenReturn(Optional.of(testCart));
			when(statusRepository.findByCode(OrderStatus.Codes.PENDING_CONFIRMATION))
					.thenReturn(Optional.of(pendingStatus));
			when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);
			when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

			CheckoutRequest request = new CheckoutRequest("Test Address", null, null);
			cartService.checkout(1L, request);

			assertThat(testCart.getItems()).isEmpty();
		}
	}
}
