package by.bsuir.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Событие (Event Sourcing)
 * Хранит все изменения состояния как неизменяемые события
 * Является Single Source of Truth для восстановления состояния заказов
 */
@Entity
@Table(name = "events", indexes = {
	@Index(name = "idx_aggregate_id", columnList = "aggregate_id"),
	@Index(name = "idx_aggregate_type", columnList = "aggregate_type"),
	@Index(name = "idx_event_type", columnList = "event_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "aggregate_id", nullable = false)
	private String aggregateId;

	@Column(name = "aggregate_type", nullable = false, length = 100)
	private String aggregateType;

	@Column(nullable = false)
	private Long version;

	@Column(name = "event_type", nullable = false, length = 100)
	private String eventType;

	@Column(nullable = false, columnDefinition = "JSON")
	private String payload;

	@Column(name = "created_at", nullable = false)
	@Builder.Default
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(name = "user_id")
	private Long userId;

	// ========== Типы событий заказов ==========

	public static class EventTypes {
		// Order lifecycle events
		public static final String ORDER_CREATED = "OrderCreated";
		public static final String ORDER_SUBMITTED = "OrderSubmitted";
		public static final String ORDER_CONFIRMED = "OrderConfirmed";
		public static final String ORDER_REJECTED = "OrderRejected";
		public static final String ORDER_CANCELLED = "OrderCancelled";

		// Payment events
		public static final String INVOICE_GENERATED = "InvoiceGenerated";
		public static final String PAYMENT_PROOF_UPLOADED = "PaymentProofUploaded";
		public static final String PAYMENT_VERIFIED = "PaymentVerified";
		public static final String PAYMENT_REJECTED = "PaymentRejected";

		// Shipping events
		public static final String ORDER_SHIPPED = "OrderShipped";
		public static final String SHIPMENT_DOCUMENTS_UPLOADED = "ShipmentDocumentsUploaded";

		// Delivery events
		public static final String ORDER_DELIVERED = "OrderDelivered";
		public static final String DISCREPANCY_REPORTED = "DiscrepancyReported";
		public static final String DISCREPANCY_RESOLVED = "DiscrepancyResolved";

		// Completion events
		public static final String ORDER_CLOSED = "OrderClosed";

		// Item events
		public static final String ITEM_ADDED = "ItemAdded";
		public static final String ITEM_REMOVED = "ItemRemoved";
		public static final String ITEM_QUANTITY_CHANGED = "ItemQuantityChanged";
	}

	// ========== Типы агрегатов ==========

	public static class AggregateTypes {
		public static final String ORDER = "Order";
		public static final String CART = "Cart";
	}
}
