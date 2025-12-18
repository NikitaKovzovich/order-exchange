package by.bsuir.orderservice.service;

import by.bsuir.orderservice.dto.*;
import by.bsuir.orderservice.entity.Cart;
import by.bsuir.orderservice.entity.CartItem;
import by.bsuir.orderservice.entity.Order;
import by.bsuir.orderservice.entity.OrderItem;
import by.bsuir.orderservice.entity.OrderStatus;
import by.bsuir.orderservice.exception.InvalidOperationException;
import by.bsuir.orderservice.exception.ResourceNotFoundException;
import by.bsuir.orderservice.repository.CartRepository;
import by.bsuir.orderservice.repository.OrderRepository;
import by.bsuir.orderservice.repository.OrderStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

	private final CartRepository cartRepository;
	private final OrderRepository orderRepository;
	private final OrderStatusRepository statusRepository;
	private final EventPublisher eventPublisher;

	/**
	 * Получить корзину покупателя
	 */
	@Transactional(readOnly = true)
	public CartResponse getCart(Long customerId) {
		Cart cart = cartRepository.findByCustomerIdWithItems(customerId)
				.orElse(null);

		if (cart == null) {
			// Вернуть пустую корзину
			return new CartResponse(
					null, customerId, List.of(), 0,
					BigDecimal.ZERO, BigDecimal.ZERO, List.of(),
					null, null
			);
		}

		return mapToResponse(cart);
	}

	/**
	 * Добавить товар в корзину
	 */
	@Transactional
	public CartResponse addItem(Long customerId, AddToCartRequest request) {
		Cart cart = getOrCreateCart(customerId);

		CartItem item = CartItem.builder()
				.productId(request.productId())
				.supplierId(request.supplierId())
				.productName(request.productName())
				.productSku(request.productSku())
				.quantity(request.quantity())
				.unitPrice(request.unitPrice())
				.vatRate(request.vatRate() != null ? request.vatRate() : BigDecimal.ZERO)
				.build();
		item.calculateTotal();

		cart.addItem(item);
		cart = cartRepository.save(cart);

		log.info("Added item to cart: customerId={}, productId={}, quantity={}",
				customerId, request.productId(), request.quantity());

		return mapToResponse(cart);
	}

	/**
	 * Обновить количество товара в корзине
	 */
	@Transactional
	public CartResponse updateItemQuantity(Long customerId, Long productId, UpdateCartItemRequest request) {
		Cart cart = cartRepository.findByCustomerIdWithItems(customerId)
				.orElseThrow(() -> new ResourceNotFoundException("Cart", "customerId", customerId));

		if (request.quantity() <= 0) {
			cart.removeItem(productId);
		} else {
			cart.updateItemQuantity(productId, request.quantity());
		}

		cart = cartRepository.save(cart);
		log.info("Updated cart item: customerId={}, productId={}, quantity={}",
				customerId, productId, request.quantity());

		return mapToResponse(cart);
	}

	/**
	 * Удалить товар из корзины
	 */
	@Transactional
	public CartResponse removeItem(Long customerId, Long productId) {
		Cart cart = cartRepository.findByCustomerIdWithItems(customerId)
				.orElseThrow(() -> new ResourceNotFoundException("Cart", "customerId", customerId));

		cart.removeItem(productId);
		cart = cartRepository.save(cart);

		log.info("Removed item from cart: customerId={}, productId={}", customerId, productId);

		return mapToResponse(cart);
	}

	/**
	 * Очистить корзину
	 */
	@Transactional
	public void clearCart(Long customerId) {
		Cart cart = cartRepository.findByCustomerIdWithItems(customerId).orElse(null);
		if (cart != null) {
			cart.clear();
			cartRepository.save(cart);
			log.info("Cleared cart: customerId={}", customerId);
		}
	}

	/**
	 * Оформить заказ (checkout)
	 * Создает отдельные заказы для каждого поставщика
	 */
	@Transactional
	public CheckoutResponse checkout(Long customerId, CheckoutRequest request) {
		Cart cart = cartRepository.findByCustomerIdWithItems(customerId)
				.orElseThrow(() -> new ResourceNotFoundException("Cart", "customerId", customerId));

		if (cart.isEmpty()) {
			throw new InvalidOperationException("checkout", "cart is empty");
		}

		OrderStatus pendingStatus = statusRepository.findByCode(OrderStatus.Codes.PENDING_CONFIRMATION)
				.orElseThrow(() -> new ResourceNotFoundException("OrderStatus", "code", OrderStatus.Codes.PENDING_CONFIRMATION));

		List<Order> createdOrders = new ArrayList<>();
		List<Long> supplierIds = cart.getSupplierIds();

		// Создаем отдельный заказ для каждого поставщика
		for (Long supplierId : supplierIds) {
			List<CartItem> supplierItems = cart.getItemsBySupplierId(supplierId);

			Order order = Order.builder()
					.orderNumber(generateOrderNumber())
					.supplierId(supplierId)
					.customerId(customerId)
					.status(pendingStatus)
					.deliveryAddress(request.deliveryAddress())
					.desiredDeliveryDate(request.desiredDeliveryDate())
					.totalAmount(BigDecimal.ZERO)
					.vatAmount(BigDecimal.ZERO)
					.build();

			BigDecimal totalAmount = BigDecimal.ZERO;
			BigDecimal vatAmount = BigDecimal.ZERO;

			for (CartItem cartItem : supplierItems) {
				OrderItem orderItem = OrderItem.builder()
						.order(order)
						.productId(cartItem.getProductId())
						.productName(cartItem.getProductName())
						.productSku(cartItem.getProductSku())
						.quantity(cartItem.getQuantity())
						.unitPrice(cartItem.getUnitPrice())
						.vatRate(cartItem.getVatRate())
						.build();
				orderItem.calculateTotals();

				order.getItems().add(orderItem);
				totalAmount = totalAmount.add(orderItem.getTotalPrice());
				vatAmount = vatAmount.add(orderItem.getLineVat() != null ? orderItem.getLineVat() : BigDecimal.ZERO);
			}

			order.setTotalAmount(totalAmount);
			order.setVatAmount(vatAmount);

			order = orderRepository.save(order);
			createdOrders.add(order);

			// Публикуем событие создания заказа
			eventPublisher.publishOrderCreated(order);
			log.info("Created order from cart: orderId={}, supplierId={}, customerId={}",
					order.getId(), supplierId, customerId);
		}

		// Очищаем корзину после успешного оформления
		cart.clear();
		cartRepository.save(cart);

		List<OrderResponse> orderResponses = createdOrders.stream()
				.map(this::mapOrderToResponse)
				.toList();

		return new CheckoutResponse(
				orderResponses,
				orderResponses.size(),
				"Successfully created " + orderResponses.size() + " order(s)"
		);
	}

	// ========== Helper Methods ==========

	private Cart getOrCreateCart(Long customerId) {
		return cartRepository.findByCustomerIdWithItems(customerId)
				.orElseGet(() -> {
					Cart newCart = Cart.builder()
							.customerId(customerId)
							.build();
					return cartRepository.save(newCart);
				});
	}

	private String generateOrderNumber() {
		return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
	}

	private CartResponse mapToResponse(Cart cart) {
		List<CartItemResponse> itemResponses = cart.getItems().stream()
				.map(this::mapItemToResponse)
				.toList();

		return new CartResponse(
				cart.getId(),
				cart.getCustomerId(),
				itemResponses,
				cart.getItemCount(),
				cart.getTotalAmount(),
				cart.getTotalVat(),
				cart.getSupplierIds(),
				cart.getCreatedAt(),
				cart.getUpdatedAt()
		);
	}

	private CartItemResponse mapItemToResponse(CartItem item) {
		return new CartItemResponse(
				item.getId(),
				item.getProductId(),
				item.getSupplierId(),
				item.getProductName(),
				item.getProductSku(),
				item.getQuantity(),
				item.getUnitPrice(),
				item.getVatRate(),
				item.getTotalPrice(),
				item.getVatAmount(),
				item.getAddedAt()
		);
	}

	private OrderResponse mapOrderToResponse(Order order) {
		List<OrderItemResponse> items = order.getItems().stream()
				.map(item -> new OrderItemResponse(
						item.getId(),
						item.getProductId(),
						item.getProductName(),
						item.getProductSku(),
						item.getQuantity(),
						item.getUnitPrice(),
						item.getVatRate(),
						item.getTotalPrice(),
						item.getLineVat()
				))
				.toList();

		return new OrderResponse(
				order.getId(),
				order.getOrderNumber(),
				order.getSupplierId(),
				order.getCustomerId(),
				order.getStatus().getCode(),
				OrderStatus.getDisplayName(order.getStatus().getCode()),
				order.getDeliveryAddress(),
				order.getDesiredDeliveryDate(),
				order.getTotalAmount(),
				order.getVatAmount(),
				items,
				order.getCreatedAt(),
				order.getUpdatedAt()
		);
	}
}
