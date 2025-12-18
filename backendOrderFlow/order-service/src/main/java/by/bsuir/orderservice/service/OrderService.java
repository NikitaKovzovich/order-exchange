package by.bsuir.orderservice.service;

import by.bsuir.orderservice.dto.*;
import by.bsuir.orderservice.entity.*;
import by.bsuir.orderservice.exception.InvalidOperationException;
import by.bsuir.orderservice.exception.ResourceNotFoundException;
import by.bsuir.orderservice.repository.OrderDiscrepancyRepository;
import by.bsuir.orderservice.repository.OrderRepository;
import by.bsuir.orderservice.repository.OrderStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
	private final OrderRepository orderRepository;
	private final OrderStatusRepository statusRepository;
	private final OrderDiscrepancyRepository discrepancyRepository;
	private final EventPublisher eventPublisher;

	public OrderResponse getOrderById(Long id) {
		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
		return mapToResponse(order);
	}

	public OrderResponse getOrderByNumber(String orderNumber) {
		Order order = orderRepository.findByOrderNumber(orderNumber)
				.orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));
		return mapToResponse(order);
	}

	public PageResponse<OrderResponse> getSupplierOrders(Long supplierId, String statusCode, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		Page<Order> orders;

		if (statusCode != null && !statusCode.isEmpty()) {
			OrderStatus status = statusRepository.findByCode(statusCode)
					.orElseThrow(() -> new ResourceNotFoundException("OrderStatus", "code", statusCode));
			orders = orderRepository.findBySupplierIdAndStatus(supplierId, status, pageable);
		} else {
			orders = orderRepository.findBySupplierId(supplierId, pageable);
		}

		return toPageResponse(orders);
	}

	public PageResponse<OrderResponse> getCustomerOrders(Long customerId, String statusCode, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		Page<Order> orders;

		if (statusCode != null && !statusCode.isEmpty()) {
			OrderStatus status = statusRepository.findByCode(statusCode)
					.orElseThrow(() -> new ResourceNotFoundException("OrderStatus", "code", statusCode));
			orders = orderRepository.findByCustomerIdAndStatus(customerId, status, pageable);
		} else {
			orders = orderRepository.findByCustomerId(customerId, pageable);
		}

		return toPageResponse(orders);
	}

	@Transactional
	public OrderResponse createOrder(Long customerId, CreateOrderRequest request) {
		OrderStatus pendingStatus = statusRepository.findByCode(OrderStatus.Codes.PENDING_CONFIRMATION)
				.orElseThrow(() -> new ResourceNotFoundException("OrderStatus", "code", OrderStatus.Codes.PENDING_CONFIRMATION));

		Order order = Order.builder()
				.orderNumber(generateOrderNumber())
				.supplierId(request.supplierId())
				.customerId(customerId)
				.status(pendingStatus)
				.deliveryAddress(request.deliveryAddress())
				.desiredDeliveryDate(request.desiredDeliveryDate())
				.totalAmount(BigDecimal.ZERO)
				.vatAmount(BigDecimal.ZERO)
				.build();

		BigDecimal totalAmount = BigDecimal.ZERO;
		BigDecimal vatAmount = BigDecimal.ZERO;

		for (OrderItemRequest itemReq : request.items()) {
			OrderItem item = OrderItem.builder()
					.order(order)
					.productId(itemReq.productId())
					.quantity(itemReq.quantity())
					.unitPrice(itemReq.unitPrice())
					.vatRate(itemReq.vatRate() != null ? itemReq.vatRate() : BigDecimal.ZERO)
					.build();

			item.calculateTotals();
			order.getItems().add(item);

			totalAmount = totalAmount.add(item.getLineTotal());
			vatAmount = vatAmount.add(item.getLineVat());
		}

		order.setTotalAmount(totalAmount);
		order.setVatAmount(vatAmount);

		order = orderRepository.save(order);
		eventPublisher.publishOrderCreated(order);

		return mapToResponse(order);
	}

	@Transactional
	public OrderResponse confirmOrder(Long orderId, Long supplierId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

		if (!order.getSupplierId().equals(supplierId)) {
			throw new InvalidOperationException("confirm", "Order does not belong to this supplier");
		}

		OrderStatus confirmedStatus = statusRepository.findByCode(OrderStatus.Codes.CONFIRMED)
				.orElseThrow(() -> new ResourceNotFoundException("OrderStatus", "code", OrderStatus.Codes.CONFIRMED));

		order.confirm(confirmedStatus);
		order = orderRepository.save(order);
		eventPublisher.publishOrderConfirmed(order);

		return mapToResponse(order);
	}

	@Transactional
	public OrderResponse rejectOrder(Long orderId, Long supplierId, String reason) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

		if (!order.getSupplierId().equals(supplierId)) {
			throw new InvalidOperationException("reject", "Order does not belong to this supplier");
		}

		OrderStatus rejectedStatus = statusRepository.findByCode(OrderStatus.Codes.REJECTED)
				.orElseThrow(() -> new ResourceNotFoundException("OrderStatus", "code", OrderStatus.Codes.REJECTED));

		order.reject(rejectedStatus, reason);
		order = orderRepository.save(order);
		eventPublisher.publishOrderRejected(order, reason);

		return mapToResponse(order);
	}

	@Transactional
	public OrderResponse confirmPayment(Long orderId, Long supplierId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

		if (!order.getSupplierId().equals(supplierId)) {
			throw new InvalidOperationException("confirmPayment", "Order does not belong to this supplier");
		}

		OrderStatus paidStatus = statusRepository.findByCode(OrderStatus.Codes.PAID)
				.orElseThrow(() -> new ResourceNotFoundException("OrderStatus", "code", OrderStatus.Codes.PAID));

		order.confirmPayment(paidStatus);
		order = orderRepository.save(order);
		eventPublisher.publishOrderPaid(order);

		return mapToResponse(order);
	}

	@Transactional
	public OrderResponse shipOrder(Long orderId, Long supplierId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

		if (!order.getSupplierId().equals(supplierId)) {
			throw new InvalidOperationException("ship", "Order does not belong to this supplier");
		}

		OrderStatus shippedStatus = statusRepository.findByCode(OrderStatus.Codes.SHIPPED)
				.orElseThrow(() -> new ResourceNotFoundException("OrderStatus", "code", OrderStatus.Codes.SHIPPED));

		order.ship(shippedStatus);
		order = orderRepository.save(order);
		eventPublisher.publishOrderShipped(order);

		return mapToResponse(order);
	}

	@Transactional
	public OrderResponse deliverOrder(Long orderId, Long customerId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

		if (!order.getCustomerId().equals(customerId)) {
			throw new InvalidOperationException("deliver", "Order does not belong to this customer");
		}

		OrderStatus deliveredStatus = statusRepository.findByCode(OrderStatus.Codes.DELIVERED)
				.orElseThrow(() -> new ResourceNotFoundException("OrderStatus", "code", OrderStatus.Codes.DELIVERED));

		order.deliver(deliveredStatus);
		order = orderRepository.save(order);
		eventPublisher.publishOrderDelivered(order);

		return mapToResponse(order);
	}

	@Transactional
	public OrderResponse uploadPaymentProof(Long orderId, Long customerId, PaymentProofRequest request) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

		if (!order.getCustomerId().equals(customerId)) {
			throw new InvalidOperationException("uploadPaymentProof", "Order does not belong to this customer");
		}

		String currentStatus = order.getStatus().getCode();
		if (!OrderStatus.Codes.AWAITING_PAYMENT.equals(currentStatus) &&
				!OrderStatus.Codes.CONFIRMED.equals(currentStatus)) {
			throw new InvalidOperationException("uploadPaymentProof",
					"Payment proof can only be uploaded for orders awaiting payment");
		}

		order.setPaymentProofKey(request.documentKey());
		order.setPaymentReference(request.paymentReference());
		order.setPaymentNotes(request.notes());
		order.setUpdatedAt(LocalDateTime.now());

		OrderStatus pendingVerification = statusRepository.findByCode(OrderStatus.Codes.PENDING_PAYMENT_VERIFICATION)
				.orElseThrow(() -> new ResourceNotFoundException("OrderStatus", "code", OrderStatus.Codes.PENDING_PAYMENT_VERIFICATION));
		order.setStatus(pendingVerification);

		order = orderRepository.save(order);
		return mapToResponse(order);
	}

	@Transactional
	public DiscrepancyResponse createDiscrepancy(Long orderId, Long customerId, DiscrepancyRequest request) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

		if (!order.getCustomerId().equals(customerId)) {
			throw new InvalidOperationException("createDiscrepancy", "Order does not belong to this customer");
		}

		String currentStatus = order.getStatus().getCode();
		if (!OrderStatus.Codes.DELIVERED.equals(currentStatus) &&
				!OrderStatus.Codes.SHIPPED.equals(currentStatus)) {
			throw new InvalidOperationException("createDiscrepancy",
					"Discrepancy can only be created for delivered or shipped orders");
		}

		OrderDiscrepancy discrepancy = OrderDiscrepancy.builder()
				.order(order)
				.notes(request.notes())
				.createdBy(customerId)
				.build();

		for (DiscrepancyItemRequest itemReq : request.items()) {
			OrderItem orderItem = order.getItems().stream()
					.filter(i -> i.getId().equals(itemReq.orderItemId()))
					.findFirst()
					.orElseThrow(() -> new ResourceNotFoundException("OrderItem", "id", itemReq.orderItemId()));

			DiscrepancyItem item = DiscrepancyItem.builder()
					.orderItem(orderItem)
					.expectedQuantity(orderItem.getQuantity())
					.actualQuantity(itemReq.actualQuantity())
					.unitPrice(orderItem.getUnitPrice())
					.reason(itemReq.reason())
					.build();
			item.calculate();
			discrepancy.addItem(item);
		}

		discrepancy.calculateTotal();

		OrderStatus awaitingCorrection = statusRepository.findByCode(OrderStatus.Codes.AWAITING_CORRECTION)
				.orElse(null);
		if (awaitingCorrection != null) {
			order.setStatus(awaitingCorrection);
			order.setUpdatedAt(LocalDateTime.now());
			orderRepository.save(order);
		}

		discrepancy = discrepancyRepository.save(discrepancy);

		return mapDiscrepancyToResponse(discrepancy);
	}

	public List<DiscrepancyResponse> getOrderDiscrepancies(Long orderId) {
		return discrepancyRepository.findByOrderId(orderId).stream()
				.map(this::mapDiscrepancyToResponse)
				.toList();
	}

	private String generateOrderNumber() {
		return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
	}

	private OrderResponse mapToResponse(Order order) {
		List<OrderItemResponse> items = order.getItems().stream()
				.map(this::mapItemToResponse)
				.toList();

		return new OrderResponse(
				order.getId(),
				order.getOrderNumber(),
				order.getSupplierId(),
				order.getCustomerId(),
				order.getStatus().getCode(),
				order.getStatus().getName(),
				order.getDeliveryAddress(),
				order.getDesiredDeliveryDate(),
				order.getTotalAmount(),
				order.getVatAmount(),
				items,
				order.getCreatedAt(),
				order.getUpdatedAt()
		);
	}

	private OrderItemResponse mapItemToResponse(OrderItem item) {
		return new OrderItemResponse(
				item.getId(),
				item.getProductId(),
				item.getProductName(),
				item.getProductSku(),
				item.getQuantity(),
				item.getUnitPrice(),
				item.getVatRate(),
				item.getLineTotal(),
				item.getLineVat()
		);
	}

	private PageResponse<OrderResponse> toPageResponse(Page<Order> page) {
		return new PageResponse<>(
				page.getContent().stream().map(this::mapToResponse).toList(),
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages(),
				page.isFirst(),
				page.isLast()
		);
	}

	private DiscrepancyResponse mapDiscrepancyToResponse(OrderDiscrepancy discrepancy) {
		List<DiscrepancyItemResponse> items = discrepancy.getItems().stream()
				.map(item -> new DiscrepancyItemResponse(
						item.getOrderItem().getId(),
						item.getOrderItem().getProductName(),
						item.getOrderItem().getProductSku(),
						item.getExpectedQuantity(),
						item.getActualQuantity(),
						item.getDiscrepancyQuantity(),
						item.getUnitPrice(),
						item.getDiscrepancyAmount(),
						item.getReason()
				))
				.toList();

		return new DiscrepancyResponse(
				discrepancy.getId(),
				discrepancy.getOrder().getId(),
				discrepancy.getOrder().getOrderNumber(),
				items,
				discrepancy.getTotalDiscrepancyAmount(),
				discrepancy.getStatus().name(),
				discrepancy.getNotes(),
				discrepancy.getCreatedAt(),
				discrepancy.getCreatedBy()
		);
	}
}
