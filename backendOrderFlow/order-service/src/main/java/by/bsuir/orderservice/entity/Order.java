package by.bsuir.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Заказ (Read Model для CQRS)
 * Основная сущность домена заказов
 * Обновляется асинхронно на основе событий
 */
@Entity
@Table(name = "orders", indexes = {
	@Index(name = "idx_supplier_id", columnList = "supplier_id"),
	@Index(name = "idx_customer_id", columnList = "customer_id"),
	@Index(name = "idx_status_id", columnList = "status_id"),
	@Index(name = "idx_order_number", columnList = "order_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "order_number", nullable = false, unique = true, length = 50)
	private String orderNumber;

	@Column(name = "supplier_id", nullable = false)
	private Long supplierId;

	@Column(name = "customer_id", nullable = false)
	private Long customerId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "status_id", nullable = false)
	private OrderStatus status;

	@Column(name = "delivery_address", nullable = false, columnDefinition = "TEXT")
	private String deliveryAddress;

	@Column(name = "desired_delivery_date")
	private LocalDate desiredDeliveryDate;

	@Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
	private BigDecimal totalAmount;

	@Column(name = "vat_amount", nullable = false, precision = 12, scale = 2)
	private BigDecimal vatAmount;

	@Column(name = "created_at")
	@Builder.Default
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@Builder.Default
	private List<OrderItem> items = new ArrayList<>();

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@Builder.Default
	private List<OrderHistory> history = new ArrayList<>();

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@Builder.Default
	private List<OrderDocument> documents = new ArrayList<>();

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@Builder.Default
	private List<OrderDiscrepancy> discrepancies = new ArrayList<>();

	@Column(name = "payment_proof_key")
	private String paymentProofKey;

	@Column(name = "payment_reference")
	private String paymentReference;

	@Column(name = "payment_notes")
	private String paymentNotes;

	// ========== Бизнес-методы для CQRS ==========

	/**
	 * Подтвердить заказ (поставщик)
	 */
	public void confirm(OrderStatus confirmedStatus) {
		validateTransition(OrderStatus.Codes.CONFIRMED);
		this.status = confirmedStatus;
		this.updatedAt = LocalDateTime.now();
	}

	/**
	 * Отклонить заказ (поставщик)
	 */
	public void reject(OrderStatus rejectedStatus, String reason) {
		validateTransition(OrderStatus.Codes.REJECTED);
		this.status = rejectedStatus;
		this.updatedAt = LocalDateTime.now();
	}

	/**
	 * Перевести в ожидание оплаты
	 */
	public void awaitPayment(OrderStatus awaitingPaymentStatus) {
		validateTransition(OrderStatus.Codes.AWAITING_PAYMENT);
		this.status = awaitingPaymentStatus;
		this.updatedAt = LocalDateTime.now();
	}

	/**
	 * Загрузить платежное поручение
	 */
	public void uploadPaymentProof(OrderStatus pendingVerificationStatus) {
		validateTransition(OrderStatus.Codes.PENDING_PAYMENT_VERIFICATION);
		this.status = pendingVerificationStatus;
		this.updatedAt = LocalDateTime.now();
	}

	/**
	 * Подтвердить оплату
	 */
	public void confirmPayment(OrderStatus paidStatus) {
		validateTransition(OrderStatus.Codes.PAID);
		this.status = paidStatus;
		this.updatedAt = LocalDateTime.now();
	}

	/**
	 * Отгрузить заказ
	 */
	public void ship(OrderStatus shippedStatus) {
		validateTransition(OrderStatus.Codes.SHIPPED);
		this.status = shippedStatus;
		this.updatedAt = LocalDateTime.now();
	}

	/**
	 * Подтвердить получение
	 */
	public void deliver(OrderStatus deliveredStatus) {
		validateTransition(OrderStatus.Codes.DELIVERED);
		this.status = deliveredStatus;
		this.updatedAt = LocalDateTime.now();
	}

	/**
	 * Сообщить о расхождении
	 */
	public void reportDiscrepancy(OrderStatus awaitingCorrectionStatus) {
		validateTransition(OrderStatus.Codes.AWAITING_CORRECTION);
		this.status = awaitingCorrectionStatus;
		this.updatedAt = LocalDateTime.now();
	}

	/**
	 * Закрыть заказ
	 */
	public void close(OrderStatus closedStatus) {
		validateTransition(OrderStatus.Codes.CLOSED);
		this.status = closedStatus;
		this.updatedAt = LocalDateTime.now();
	}

	/**
	 * Отменить заказ
	 */
	public void cancel(OrderStatus cancelledStatus) {
		validateTransition(OrderStatus.Codes.CANCELLED);
		this.status = cancelledStatus;
		this.updatedAt = LocalDateTime.now();
	}

	/**
	 * Проверить возможность перехода статуса
	 */
	private void validateTransition(String targetStatus) {
		String currentStatus = this.status != null ? this.status.getCode() : null;
		if (currentStatus != null && !OrderStatus.canTransition(currentStatus, targetStatus)) {
			throw new IllegalStateException(
				"Cannot transition from " + currentStatus + " to " + targetStatus
			);
		}
	}

	/**
	 * Добавить позицию в заказ
	 */
	public void addItem(OrderItem item) {
		items.add(item);
		item.setOrder(this);
		recalculateTotals();
	}

	/**
	 * Удалить позицию из заказа
	 */
	public void removeItem(OrderItem item) {
		items.remove(item);
		item.setOrder(null);
		recalculateTotals();
	}

	/**
	 * Пересчитать суммы заказа
	 */
	public void recalculateTotals() {
		this.totalAmount = items.stream()
				.map(OrderItem::getTotalPrice)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		// Примерный расчет НДС (20%)
		this.vatAmount = totalAmount.multiply(BigDecimal.valueOf(0.2));
	}

	/**
	 * Получить количество позиций
	 */
	public int getItemsCount() {
		return items.size();
	}

	/**
	 * Проверить, принадлежит ли заказ поставщику
	 */
	public boolean belongsToSupplier(Long supplierId) {
		return this.supplierId.equals(supplierId);
	}

	/**
	 * Проверить, принадлежит ли заказ покупателю
	 */
	public boolean belongsToCustomer(Long customerId) {
		return this.customerId.equals(customerId);
	}

	/**
	 * Сгенерировать номер заказа
	 */
	public static String generateOrderNumber(Long supplierId) {
		return "ORD-" + supplierId + "-" + System.currentTimeMillis();
	}
}
