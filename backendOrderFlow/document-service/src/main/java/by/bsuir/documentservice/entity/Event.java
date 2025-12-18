package by.bsuir.documentservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Событие (Event Sourcing)
 * Хранит все изменения состояния как неизменяемые события
 */
@Entity
@Table(name = "events", indexes = {
	@Index(name = "idx_aggregate_id", columnList = "aggregate_id"),
	@Index(name = "idx_aggregate_type", columnList = "aggregate_type")
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

	// ========== Типы событий документов ==========

	public static class EventTypes {
		public static final String DOCUMENT_UPLOADED = "DocumentUploaded";
		public static final String DOCUMENT_DELETED = "DocumentDeleted";
		public static final String DOCUMENT_GENERATED = "DocumentGenerated";
		public static final String INVOICE_GENERATED = "InvoiceGenerated";
		public static final String UPD_GENERATED = "UPDGenerated";
		public static final String TTN_GENERATED = "TTNGenerated";
		public static final String DISCREPANCY_ACT_GENERATED = "DiscrepancyActGenerated";
	}

	// ========== Типы агрегатов ==========

	public static class AggregateTypes {
		public static final String DOCUMENT = "Document";
		public static final String GENERATED_DOCUMENT = "GeneratedDocument";
	}
}
