package by.bsuir.catalogservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;






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
	private Integer version;




	@Column(name = "event_type", nullable = false, length = 100)
	private String eventType;




	@Column(nullable = false, columnDefinition = "JSON")
	private String payload;




	@Column(name = "created_at", nullable = false)
	@Builder.Default
	private LocalDateTime createdAt = LocalDateTime.now();




	@Column(name = "user_id")
	private Long userId;



	public static class EventTypes {

		public static final String PRODUCT_CREATED = "ProductCreated";
		public static final String PRODUCT_UPDATED = "ProductUpdated";
		public static final String PRODUCT_PUBLISHED = "ProductPublished";
		public static final String PRODUCT_ARCHIVED = "ProductArchived";
		public static final String PRODUCT_PRICE_CHANGED = "ProductPriceChanged";


		public static final String CATEGORY_CREATED = "CategoryCreated";
		public static final String CATEGORY_UPDATED = "CategoryUpdated";
		public static final String CATEGORY_DELETED = "CategoryDeleted";


		public static final String INVENTORY_UPDATED = "InventoryUpdated";
		public static final String STOCK_RESERVED = "StockReserved";
		public static final String STOCK_RELEASED = "StockReleased";
		public static final String STOCK_SHIPPED = "StockShipped";
	}



	public static class AggregateTypes {
		public static final String PRODUCT = "Product";
		public static final String CATEGORY = "Category";
		public static final String INVENTORY = "Inventory";
	}
}
