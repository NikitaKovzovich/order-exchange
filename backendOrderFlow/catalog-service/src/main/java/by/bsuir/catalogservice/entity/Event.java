package by.bsuir.catalogservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Событие (Event Sourcing)
 * Хранит все изменения состояния как неизменяемые события
 * Является Single Source of Truth для восстановления состояния
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

	/**
	 * ID агрегата (например, product_id)
	 */
	@Column(name = "aggregate_id", nullable = false)
	private String aggregateId;

	/**
	 * Тип агрегата (Product, Category, Inventory)
	 */
	@Column(name = "aggregate_type", nullable = false, length = 100)
	private String aggregateType;

	/**
	 * Версия для оптимистичной блокировки
	 */
	@Column(nullable = false)
	private Integer version;

	/**
	 * Тип события (ProductCreated, ProductPublished, InventoryUpdated)
	 */
	@Column(name = "event_type", nullable = false, length = 100)
	private String eventType;

	/**
	 * Данные события в формате JSON
	 */
	@Column(nullable = false, columnDefinition = "JSON")
	private String payload;

	/**
	 * Время создания события
	 */
	@Column(name = "created_at", nullable = false)
	@Builder.Default
	private LocalDateTime createdAt = LocalDateTime.now();

	/**
	 * ID пользователя, инициировавшего событие
	 */
	@Column(name = "user_id")
	private Long userId;

	// ========== Типы событий каталога ==========

	public static class EventTypes {
		// Product events
		public static final String PRODUCT_CREATED = "ProductCreated";
		public static final String PRODUCT_UPDATED = "ProductUpdated";
		public static final String PRODUCT_PUBLISHED = "ProductPublished";
		public static final String PRODUCT_ARCHIVED = "ProductArchived";
		public static final String PRODUCT_PRICE_CHANGED = "ProductPriceChanged";

		// Category events
		public static final String CATEGORY_CREATED = "CategoryCreated";
		public static final String CATEGORY_UPDATED = "CategoryUpdated";
		public static final String CATEGORY_DELETED = "CategoryDeleted";

		// Inventory events
		public static final String INVENTORY_UPDATED = "InventoryUpdated";
		public static final String STOCK_RESERVED = "StockReserved";
		public static final String STOCK_RELEASED = "StockReleased";
		public static final String STOCK_SHIPPED = "StockShipped";
	}

	// ========== Типы агрегатов ==========

	public static class AggregateTypes {
		public static final String PRODUCT = "Product";
		public static final String CATEGORY = "Category";
		public static final String INVENTORY = "Inventory";
	}
}
