package by.bsuir.orderservice.service;

import by.bsuir.orderservice.client.AuthServiceClient;
import by.bsuir.orderservice.client.DocumentServiceClient;
import by.bsuir.orderservice.dto.*;
import by.bsuir.orderservice.entity.*;
import by.bsuir.orderservice.exception.InvalidOperationException;
import by.bsuir.orderservice.exception.ResourceNotFoundException;
import by.bsuir.orderservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
	private final OrderRepository orderRepository;
	private final OrderStatusRepository statusRepository;
	private final OrderDiscrepancyRepository discrepancyRepository;
	private final OrderHistoryRepository historyRepository;
	private final OrderDocumentRepository documentRepository;
	private final EventPublisher eventPublisher;
	private final AuthServiceClient authServiceClient;
	private final DocumentServiceClient documentServiceClient;
	private final NotificationService notificationService;



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

	public PageResponse<OrderResponse> getSupplierOrders(Long supplierId, String statusCode,
			String search, LocalDate dateFrom, LocalDate dateTo, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		Page<Order> orders;

		if (search != null && !search.isBlank()) {
			orders = orderRepository.findBySupplierIdAndOrderNumberContaining(supplierId, search, pageable);
		} else if (statusCode != null && !statusCode.isEmpty()) {
			OrderStatus status = statusRepository.findByCode(statusCode)
					.orElseThrow(() -> new ResourceNotFoundException("OrderStatus", "code", statusCode));
			orders = orderRepository.findBySupplierIdAndStatus(supplierId, status, pageable);
		} else if (dateFrom != null && dateTo != null) {
			orders = orderRepository.findBySupplierIdAndCreatedAtBetween(
					supplierId, dateFrom.atStartOfDay(), dateTo.plusDays(1).atStartOfDay(), pageable);
		} else {
			orders = orderRepository.findBySupplierId(supplierId, pageable);
		}

		return toPageResponse(orders);
	}

	public PageResponse<OrderResponse> getCustomerOrders(Long customerId, String statusCode,
			String search, LocalDate dateFrom, LocalDate dateTo, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		Page<Order> orders;

		if (search != null && !search.isBlank()) {
			orders = orderRepository.findByCustomerIdAndOrderNumberContaining(customerId, search, pageable);
		} else if (statusCode != null && !statusCode.isEmpty()) {
			OrderStatus status = statusRepository.findByCode(statusCode)
					.orElseThrow(() -> new ResourceNotFoundException("OrderStatus", "code", statusCode));
			orders = orderRepository.findByCustomerIdAndStatus(customerId, status, pageable);
		} else if (dateFrom != null && dateTo != null) {
			orders = orderRepository.findByCustomerIdAndCreatedAtBetween(
					customerId, dateFrom.atStartOfDay(), dateTo.plusDays(1).atStartOfDay(), pageable);
		} else {
			orders = orderRepository.findByCustomerId(customerId, pageable);
		}

		return toPageResponse(orders);
	}




	public OrderSummaryResponse getSupplierOrdersSummary(Long supplierId) {
		List<Order> orders = orderRepository.findAllBySupplierId(supplierId);
		return buildSummary(orders);
	}




	public OrderSummaryResponse getCustomerOrdersSummary(Long customerId) {
		List<Order> orders = orderRepository.findAllByCustomerId(customerId);
		return buildSummary(orders);
	}

	private OrderSummaryResponse buildSummary(List<Order> orders) {
		java.util.Map<String, Long> countByStatus = orders.stream()
				.collect(java.util.stream.Collectors.groupingBy(
						o -> o.getStatus().getCode(),
						java.util.stream.Collectors.counting()
				));
		return new OrderSummaryResponse(orders.size(), countByStatus);
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
					.productName(itemReq.productName())
					.productSku(itemReq.productSku())
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


		recordHistory(order, null, OrderStatus.Codes.PENDING_CONFIRMATION, customerId, "Заказ создан");

		eventPublisher.publishOrderCreated(order);


		notificationService.notifySupplierNewOrder(order);

		return mapToResponse(order);
	}



	@Transactional
	public OrderResponse confirmOrder(Long orderId, Long supplierId) {
		Order order = getOrderForSupplier(orderId, supplierId, "confirm");

		String previousStatus = order.getStatus().getCode();

		OrderStatus confirmedStatus = getStatus(OrderStatus.Codes.CONFIRMED);
		order.confirm(confirmedStatus);
		order = orderRepository.save(order);

		recordHistory(order, previousStatus, OrderStatus.Codes.CONFIRMED, supplierId, "Заказ подтвержден поставщиком");
		eventPublisher.publishOrderConfirmed(order);


		notificationService.notifyCustomerOrderConfirmed(order);


		order = autoTransitionToAwaitingPayment(order, supplierId);


		notificationService.notifyCustomerInvoiceIssued(order);

		return mapToResponse(order);
	}




	private Order autoTransitionToAwaitingPayment(Order order, Long userId) {
		OrderStatus awaitingPaymentStatus = getStatus(OrderStatus.Codes.AWAITING_PAYMENT);
		order.awaitPayment(awaitingPaymentStatus);
		order = orderRepository.save(order);

		recordHistory(order, OrderStatus.Codes.CONFIRMED, OrderStatus.Codes.AWAITING_PAYMENT,
				userId, "Счет на оплату сформирован");


		String invoiceFileKey = "invoice-" + order.getOrderNumber() + "-" + System.currentTimeMillis();
		String invoiceFilename = "Счет на оплату " + order.getOrderNumber() + ".pdf";

		try {
			var invoiceItems = order.getItems().stream()
					.map(item -> new DocumentServiceClient.InvoiceItem(
							item.getProductName(), item.getProductSku(), "шт",
							item.getQuantity(), item.getUnitPrice(), item.getVatRate(),
							item.getLineTotal().subtract(item.getLineVat()),
							item.getLineVat(), item.getLineTotal()))
					.toList();

			var invoiceReq = new DocumentServiceClient.InvoiceRequest(
					order.getId(), order.getOrderNumber(), LocalDate.now(),
					DocumentServiceClient.CompanyInfo.of(authServiceClient.getCompanyName(order.getSupplierId())),
					DocumentServiceClient.CompanyInfo.of(authServiceClient.getCompanyName(order.getCustomerId())),
					invoiceItems,
					order.getTotalAmount().subtract(order.getVatAmount()),
					order.getVatAmount(), order.getTotalAmount(),
					null, null);

			var result = documentServiceClient.generateInvoice(invoiceReq, userId);
			if (result != null && result.fileKey() != null) {
				invoiceFileKey = result.fileKey();
				invoiceFilename = "Счет на оплату №" + result.documentNumber() + ".pdf";
			}
		} catch (Exception e) {
			log.warn("Failed to generate invoice PDF via document-service, using stub: {}", e.getMessage());
		}

		OrderDocument invoice = OrderDocument.builder()
				.order(order)
				.documentType(OrderDocument.DocumentType.INVOICE)
				.fileKey(invoiceFileKey)
				.originalFilename(invoiceFilename)
				.uploadedBy(userId)
				.build();
		documentRepository.save(invoice);

		log.info("Auto-generated invoice for order {}", order.getOrderNumber());
		return order;
	}



	@Transactional
	public OrderResponse rejectOrder(Long orderId, Long supplierId, String reason) {
		Order order = getOrderForSupplier(orderId, supplierId, "reject");
		String previousStatus = order.getStatus().getCode();

		OrderStatus rejectedStatus = getStatus(OrderStatus.Codes.REJECTED);
		order.reject(rejectedStatus, reason);
		order = orderRepository.save(order);

		recordHistory(order, previousStatus, OrderStatus.Codes.REJECTED, supplierId,
				"Заказ отклонен поставщиком. Причина: " + reason);
		eventPublisher.publishOrderRejected(order, reason);


		notificationService.notifyCustomerOrderRejected(order, reason);

		return mapToResponse(order);
	}



	@Transactional
	public OrderResponse confirmPayment(Long orderId, Long supplierId) {
		Order order = getOrderForSupplier(orderId, supplierId, "confirmPayment");
		String previousStatus = order.getStatus().getCode();

		OrderStatus paidStatus = getStatus(OrderStatus.Codes.PAID);
		order.confirmPayment(paidStatus);
		order = orderRepository.save(order);

		recordHistory(order, previousStatus, OrderStatus.Codes.PAID, supplierId, "Оплата подтверждена");
		eventPublisher.publishOrderPaid(order);


		notificationService.notifyCustomerPaymentConfirmed(order);


		order = autoTransitionToAwaitingShipment(order, supplierId);

		return mapToResponse(order);
	}




	private Order autoTransitionToAwaitingShipment(Order order, Long userId) {
		OrderStatus awaitingShipmentStatus = getStatus(OrderStatus.Codes.AWAITING_SHIPMENT);

		String previousStatus = order.getStatus().getCode();
		order.setStatus(awaitingShipmentStatus);
		order.setUpdatedAt(LocalDateTime.now());
		order = orderRepository.save(order);

		recordHistory(order, previousStatus, OrderStatus.Codes.AWAITING_SHIPMENT,
				userId, "Заказ передан на подготовку к отгрузке");

		log.info("Auto-transitioned order {} to AWAITING_SHIPMENT", order.getOrderNumber());
		return order;
	}



	@Transactional
	public OrderResponse rejectPayment(Long orderId, Long supplierId, String reason) {
		Order order = getOrderForSupplier(orderId, supplierId, "rejectPayment");
		String previousStatus = order.getStatus().getCode();

		OrderStatus paymentProblemStatus = getStatus(OrderStatus.Codes.PAYMENT_PROBLEM);
		order.rejectPayment(paymentProblemStatus);
		order = orderRepository.save(order);

		recordHistory(order, previousStatus, OrderStatus.Codes.PAYMENT_PROBLEM, supplierId,
				"Возникли проблемы с оплатой" + (reason != null ? ". Причина: " + reason : ""));
		eventPublisher.publishPaymentRejected(order, reason);


		notificationService.notifyCustomerPaymentRejected(order, reason);

		return mapToResponse(order);
	}



	@Transactional
	public OrderResponse generateTtn(Long orderId, Long supplierId) {
		Order order = getOrderForSupplier(orderId, supplierId, "generateTtn");

		if (!OrderStatus.Codes.AWAITING_SHIPMENT.equals(order.getStatus().getCode())) {
			throw new InvalidOperationException("generateTtn", "TTN can only be generated in AWAITING_SHIPMENT status");
		}

		if (Boolean.TRUE.equals(order.getTtnGenerated())) {
			throw new InvalidOperationException("generateTtn", "TTN has already been generated for this order");
		}


		String ttnFileKey = "ttn-" + order.getOrderNumber() + "-" + System.currentTimeMillis();
		String ttnFilename = "ТТН " + order.getOrderNumber() + ".pdf";

		try {
			var ttnItems = order.getItems().stream()
					.map(item -> new DocumentServiceClient.TtnItem(
							item.getProductName(), "шт", item.getQuantity(),
							item.getUnitPrice(), item.getVatRate(),
							item.getLineTotal().subtract(item.getLineVat()),
							item.getLineVat(), item.getLineTotal(), null))
					.toList();

			String supplierName = authServiceClient.getCompanyName(order.getSupplierId());
			String customerName = authServiceClient.getCompanyName(order.getCustomerId());

			var ttnReq = new DocumentServiceClient.TtnRequest(
					order.getId(), order.getOrderNumber(), LocalDate.now(), null,
					DocumentServiceClient.CompanyInfo.of(supplierName),
					DocumentServiceClient.CompanyInfo.of(customerName),
					null,
					"Склад поставщика",
					order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "Адрес покупателя",
					null, ttnItems,
					order.getTotalAmount().subtract(order.getVatAmount()),
					order.getVatAmount(), order.getTotalAmount(), null, null,
					order.getContractNumber() != null ? "Договор №" + order.getContractNumber() : null,
					null);

			var result = documentServiceClient.generateTtn(ttnReq, supplierId);
			if (result != null && result.fileKey() != null) {
				ttnFileKey = result.fileKey();
				ttnFilename = "ТТН №" + result.documentNumber() + ".pdf";
			}
		} catch (Exception e) {
			log.warn("Failed to generate TTN PDF via document-service, using stub: {}", e.getMessage());
		}

		OrderDocument ttn = OrderDocument.builder()
				.order(order)
				.documentType(OrderDocument.DocumentType.TTN)
				.fileKey(ttnFileKey)
				.originalFilename(ttnFilename)
				.uploadedBy(supplierId)
				.build();
		documentRepository.save(ttn);

		order.setTtnGenerated(true);
		order.setUpdatedAt(LocalDateTime.now());
		order = orderRepository.save(order);

		recordHistory(order, null, null, supplierId, "ТТН сформирована");


		notificationService.notifyCustomerTtnFormed(order);

		log.info("Generated TTN for order {}", order.getOrderNumber());
		return mapToResponse(order);
	}



	@Transactional
	public OrderResponse shipOrder(Long orderId, Long supplierId) {
		Order order = getOrderForSupplier(orderId, supplierId, "ship");


		if (!Boolean.TRUE.equals(order.getTtnGenerated())) {
			throw new InvalidOperationException("ship", "TTN must be generated before shipping");
		}

		String previousStatus = order.getStatus().getCode();

		OrderStatus shippedStatus = getStatus(OrderStatus.Codes.SHIPPED);
		order.ship(shippedStatus);
		order = orderRepository.save(order);

		recordHistory(order, previousStatus, OrderStatus.Codes.SHIPPED, supplierId, "Товар отгружен");
		eventPublisher.publishOrderShipped(order);


		notificationService.notifyCustomerOrderShipped(order);

		return mapToResponse(order);
	}



	@Transactional
	public OrderResponse deliverOrder(Long orderId, Long customerId) {
		Order order = getOrderForCustomer(orderId, customerId, "deliver");
		String previousStatus = order.getStatus().getCode();

		OrderStatus deliveredStatus = getStatus(OrderStatus.Codes.DELIVERED);
		order.deliver(deliveredStatus);
		order = orderRepository.save(order);


		OrderDocument signedUpd = OrderDocument.builder()
				.order(order)
				.documentType(OrderDocument.DocumentType.SIGNED_UPD)
				.fileKey("signed-upd-" + order.getOrderNumber() + "-" + System.currentTimeMillis())
				.originalFilename("Подписанный УПД " + order.getOrderNumber() + ".pdf")
				.uploadedBy(customerId)
				.build();
		documentRepository.save(signedUpd);

		recordHistory(order, previousStatus, OrderStatus.Codes.DELIVERED, customerId,
				"Товар доставлен. Подписанные документы загружены.");
		eventPublisher.publishOrderDelivered(order);


		notificationService.notifySupplierDeliveryConfirmed(order);

		return mapToResponse(order);
	}



	@Transactional
	public OrderResponse uploadPaymentProof(Long orderId, Long customerId, PaymentProofRequest request) {
		return uploadPaymentProof(orderId, customerId, request, null);
	}

	@Transactional
	public OrderResponse uploadPaymentProof(Long orderId, Long customerId, MultipartFile file,
			String paymentReference, String notes) {
		String documentKey = documentServiceClient.uploadFile(
				file,
				"orders/" + orderId + "/payment-proof",
				"order-service",
				orderId,
				"Order");

		PaymentProofRequest request = new PaymentProofRequest(documentKey, paymentReference, notes);
		String originalFilename = file != null ? file.getOriginalFilename() : null;
		return uploadPaymentProof(orderId, customerId, request, originalFilename);
	}

	private OrderResponse uploadPaymentProof(Long orderId, Long customerId, PaymentProofRequest request,
			String originalFilename) {
		Order order = getOrderForCustomer(orderId, customerId, "uploadPaymentProof");
		String previousStatus = order.getStatus().getCode();

		order.setPaymentProofKey(request.documentKey());
		order.setPaymentReference(request.paymentReference());
		order.setPaymentNotes(request.notes());

		OrderStatus pendingVerification = getStatus(OrderStatus.Codes.PENDING_PAYMENT_VERIFICATION);
		order.uploadPaymentProof(pendingVerification);


		OrderDocument paymentDoc = OrderDocument.builder()
				.order(order)
				.documentType(OrderDocument.DocumentType.PAYMENT_PROOF)
				.fileKey(request.documentKey())
				.originalFilename(originalFilename != null && !originalFilename.isBlank()
						? originalFilename
						: "Платежное поручение " + order.getOrderNumber() + ".pdf")
				.uploadedBy(customerId)
				.build();
		documentRepository.save(paymentDoc);

		order = orderRepository.save(order);

		recordHistory(order, previousStatus, OrderStatus.Codes.PENDING_PAYMENT_VERIFICATION,
				customerId, "Торговая сеть загрузила подтверждение оплаты");


		notificationService.notifySupplierPaymentProofUploaded(order);

		return mapToResponse(order);
	}



	@Transactional
	public OrderResponse closeOrder(Long orderId, Long supplierId) {
		Order order = getOrderForSupplier(orderId, supplierId, "close");
		String previousStatus = order.getStatus().getCode();

		OrderStatus closedStatus = getStatus(OrderStatus.Codes.CLOSED);
		order.close(closedStatus);
		order = orderRepository.save(order);

		recordHistory(order, previousStatus, OrderStatus.Codes.CLOSED, supplierId, "Заказ закрыт");
		eventPublisher.publishOrderClosed(order);


		notificationService.notifyCustomerOrderClosed(order);

		return mapToResponse(order);
	}



	@Transactional
	public OrderResponse cancelOrder(Long orderId, Long userId, String reason) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

		String previousStatus = order.getStatus().getCode();

		OrderStatus cancelledStatus = getStatus(OrderStatus.Codes.CANCELLED);
		order.cancel(cancelledStatus);
		order = orderRepository.save(order);

		recordHistory(order, previousStatus, OrderStatus.Codes.CANCELLED, userId,
				"Заказ отменен" + (reason != null ? ". Причина: " + reason : ""));

		eventPublisher.publishOrderCancelled(order);

		return mapToResponse(order);
	}



	@Transactional
	public OrderResponse correctOrder(Long orderId, Long supplierId) {
		Order order = getOrderForSupplier(orderId, supplierId, "correct");

		if (!OrderStatus.Codes.AWAITING_CORRECTION.equals(order.getStatus().getCode())) {
			throw new InvalidOperationException("correct", "Order must be in AWAITING_CORRECTION status");
		}


		String corrTtnFileKey = "correction-ttn-" + order.getOrderNumber() + "-" + System.currentTimeMillis();
		String corrTtnFilename = "Корректировочная ТТН " + order.getOrderNumber() + ".pdf";

		try {
			var ttnItems = order.getItems().stream()
					.map(item -> new DocumentServiceClient.TtnItem(
							item.getProductName(), "шт", item.getQuantity(),
							item.getUnitPrice(), item.getVatRate(),
							item.getLineTotal().subtract(item.getLineVat()),
							item.getLineVat(), item.getLineTotal(), null))
					.toList();

			String supplierName = authServiceClient.getCompanyName(order.getSupplierId());
			String customerName = authServiceClient.getCompanyName(order.getCustomerId());

			var ttnReq = new DocumentServiceClient.TtnRequest(
					order.getId(), order.getOrderNumber(), LocalDate.now(), "КОРР",
					DocumentServiceClient.CompanyInfo.of(supplierName),
					DocumentServiceClient.CompanyInfo.of(customerName),
					null,
					"Склад поставщика",
					order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "Адрес покупателя",
					null, ttnItems,
					order.getTotalAmount().subtract(order.getVatAmount()),
					order.getVatAmount(), order.getTotalAmount(), null, null,
					order.getContractNumber() != null ? "Договор №" + order.getContractNumber() : null,
					"Корректировочная ТТН по Акту о расхождении");

			var result = documentServiceClient.generateTtn(ttnReq, supplierId);
			if (result != null && result.fileKey() != null) {
				corrTtnFileKey = result.fileKey();
				corrTtnFilename = "Корректировочная ТТН №" + result.documentNumber() + ".pdf";
			}
		} catch (Exception e) {
			log.warn("Failed to generate correction TTN PDF via document-service, using stub: {}", e.getMessage());
		}

		OrderDocument correctionTtn = OrderDocument.builder()
				.order(order)
				.documentType(OrderDocument.DocumentType.CORRECTION_TTN)
				.fileKey(corrTtnFileKey)
				.originalFilename(corrTtnFilename)
				.uploadedBy(supplierId)
				.build();
		documentRepository.save(correctionTtn);


		String previousStatus = order.getStatus().getCode();
		OrderStatus shippedStatus = getStatus(OrderStatus.Codes.SHIPPED);
		order.setStatus(shippedStatus);
		order.setUpdatedAt(LocalDateTime.now());
		order = orderRepository.save(order);

		recordHistory(order, previousStatus, OrderStatus.Codes.SHIPPED, supplierId,
				"Сформирована корректировочная ТТН");


		notificationService.notifyCustomerCorrectionResponse(order);
		notificationService.notifySupplierCorrectionTtnFormed(order);

		log.info("Correction TTN generated for order {}, status changed to SHIPPED", order.getOrderNumber());
		return mapToResponse(order);
	}



	@Transactional
	public DiscrepancyResponse createDiscrepancy(Long orderId, Long customerId, DiscrepancyRequest request) {
		Order order = getOrderForCustomer(orderId, customerId, "createDiscrepancy");

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


		String discrepancyFileKey = "discrepancy-act-" + order.getOrderNumber() + "-" + System.currentTimeMillis();
		String discrepancyFilename = "Акт о расхождении " + order.getOrderNumber() + ".pdf";

		try {
			var discrepancyLines = discrepancy.getItems().stream()
					.map(item -> new DocumentServiceClient.DiscrepancyLine(
							item.getOrderItem().getProductName(),
							item.getOrderItem().getProductSku(),
							item.getExpectedQuantity(),
							item.getActualQuantity(),
							item.getDiscrepancyQuantity(),
							item.getUnitPrice(),
							item.getDiscrepancyAmount(),
							item.getReason() != null ? item.getReason().name() : null))
					.toList();

			String supplierName = authServiceClient.getCompanyName(order.getSupplierId());
			String customerName = authServiceClient.getCompanyName(order.getCustomerId());

			var discrepancyActReq = new DocumentServiceClient.DiscrepancyActReq(
					order.getId(), order.getOrderNumber(), LocalDate.now(),
					DocumentServiceClient.CompanyInfo.of(supplierName),
					DocumentServiceClient.CompanyInfo.of(customerName),
					discrepancyLines,
					discrepancy.getTotalDiscrepancyAmount(),
					request.notes());

			var result = documentServiceClient.generateDiscrepancyAct(discrepancyActReq, customerId);
			if (result != null && result.fileKey() != null) {
				discrepancyFileKey = result.fileKey();
				discrepancyFilename = "Акт о расхождении №" + result.documentNumber() + ".pdf";
			}
		} catch (Exception e) {
			log.warn("Failed to generate Discrepancy Act PDF via document-service, using stub: {}", e.getMessage());
		}

		OrderDocument discrepancyAct = OrderDocument.builder()
				.order(order)
				.documentType(OrderDocument.DocumentType.DISCREPANCY_ACT)
				.fileKey(discrepancyFileKey)
				.originalFilename(discrepancyFilename)
				.uploadedBy(customerId)
				.build();
		documentRepository.save(discrepancyAct);

		String previousStatus = order.getStatus().getCode();
		OrderStatus awaitingCorrection = getStatus(OrderStatus.Codes.AWAITING_CORRECTION);
		order.reportDiscrepancy(awaitingCorrection);
		orderRepository.save(order);

		recordHistory(order, previousStatus, OrderStatus.Codes.AWAITING_CORRECTION, customerId,
				"Торговая сеть сформировала Акт о расхождении");


		notificationService.notifySupplierAcceptanceProblem(order);

		discrepancy = discrepancyRepository.save(discrepancy);
		return mapDiscrepancyToResponse(discrepancy);
	}

	public List<DiscrepancyResponse> getOrderDiscrepancies(Long orderId) {
		return discrepancyRepository.findByOrderId(orderId).stream()
				.map(this::mapDiscrepancyToResponse)
				.toList();
	}



	public List<OrderHistoryResponse> getOrderHistory(Long orderId) {
		return historyRepository.findByOrderIdOrderByTimestampDesc(orderId).stream()
				.map(this::mapHistoryToResponse)
				.toList();
	}

	public List<OrderDocumentResponse> getOrderDocuments(Long orderId) {
		return documentRepository.findByOrderIdOrderByUploadedAtDesc(orderId).stream()
				.map(this::mapDocumentToResponse)
				.toList();
	}



	@Transactional
	public CartResponse repeatOrder(Long orderId, Long customerId, CartService cartService) {
		Order originalOrder = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

		if (!originalOrder.getCustomerId().equals(customerId)) {
			throw new InvalidOperationException("repeat", "Order does not belong to this customer");
		}

		CartResponse result = null;
		for (OrderItem item : originalOrder.getItems()) {
			AddToCartRequest cartRequest = new AddToCartRequest(
					item.getProductId(),
					originalOrder.getSupplierId(),
					item.getProductName(),
					item.getProductSku(),
					item.getQuantity(),
					item.getUnitPrice(),
					item.getVatRate()
			);
			result = cartService.addItem(customerId, cartRequest);
		}

		if (result == null) {

			result = cartService.getCart(customerId);
		}

		log.info("Repeated order {} → items added to cart for customer {}", orderId, customerId);
		return result;
	}



	private Order getOrderForSupplier(Long orderId, Long supplierId, String operation) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
		if (!order.getSupplierId().equals(supplierId)) {
			throw new InvalidOperationException(operation, "Order does not belong to this supplier");
		}
		return order;
	}

	private Order getOrderForCustomer(Long orderId, Long customerId, String operation) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
		if (!order.getCustomerId().equals(customerId)) {
			throw new InvalidOperationException(operation, "Order does not belong to this customer");
		}
		return order;
	}

	private OrderStatus getStatus(String code) {
		return statusRepository.findByCode(code)
				.orElseThrow(() -> new ResourceNotFoundException("OrderStatus", "code", code));
	}

	private void recordHistory(Order order, String previousStatus, String newStatus,
							Long userId, String description) {
		OrderHistory history = OrderHistory.createStatusChange(
				order, previousStatus, newStatus, userId, description);
		historyRepository.save(history);
		order.getHistory().add(history);
	}

	private String generateOrderNumber() {
		return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
	}



	private OrderResponse mapToResponse(Order order) {
		List<OrderItemResponse> items = order.getItems().stream()
				.map(this::mapItemToResponse)
				.toList();

		List<OrderHistoryResponse> history = order.getHistory().stream()
				.map(this::mapHistoryToResponse)
				.toList();

		List<OrderDocumentResponse> documents = order.getDocuments().stream()
				.map(this::mapDocumentToResponse)
				.toList();

		List<DiscrepancyResponse> discrepancies = order.getDiscrepancies().stream()
				.map(this::mapDiscrepancyToResponse)
				.toList();

		return new OrderResponse(
				order.getId(),
				order.getOrderNumber(),
				order.getSupplierId(),
				authServiceClient.getCompanyName(order.getSupplierId()),
				order.getCustomerId(),
				authServiceClient.getCompanyName(order.getCustomerId()),
				order.getStatus().getCode(),
				OrderStatus.getDisplayName(order.getStatus().getCode()),
				order.getDeliveryAddress(),
				order.getDesiredDeliveryDate(),
				order.getTotalAmount(),
				order.getVatAmount(),
				order.getContractNumber(),
				order.getContractDate(),
				order.getContractEndDate(),
				order.getTtnGenerated(),
				items,
				history,
				documents,
				discrepancies,
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

	private OrderHistoryResponse mapHistoryToResponse(OrderHistory h) {
		return new OrderHistoryResponse(
				h.getId(),
				h.getEventDescription(),
				h.getPreviousStatus(),
				h.getPreviousStatus() != null ? OrderStatus.getDisplayName(h.getPreviousStatus()) : null,
				h.getNewStatus(),
				h.getNewStatus() != null ? OrderStatus.getDisplayName(h.getNewStatus()) : null,
				h.getUserId(),
				h.getTimestamp()
		);
	}

	private OrderDocumentResponse mapDocumentToResponse(OrderDocument d) {
		return new OrderDocumentResponse(
				d.getId(),
				d.getDocumentType().name(),
				d.getDocumentType().getDisplayName(),
				d.getFileKey(),
				d.getOriginalFilename(),
				d.getUploadedBy(),
				d.getUploadedAt()
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
						item.getReason() != null ? item.getReason().name() : null
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
