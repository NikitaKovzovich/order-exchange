package by.bsuir.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * История изменений заказа (Read Model для CQRS)
 * Хранит человекочитаемую историю событий
 */
@Entity
@Table(name = "order_history", indexes = {
	@Index(name = "idx_order_id", columnList = "order_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@Column(nullable = false)
	@Builder.Default
	private LocalDateTime timestamp = LocalDateTime.now();

	@Column(name = "event_description", nullable = false)
	private String eventDescription;

	/**
	 * ID пользователя, совершившего действие
	 */
	@Column(name = "user_id")
	private Long userId;

	/**
	 * Предыдущий статус
	 */
	@Column(name = "previous_status", length = 50)
	private String previousStatus;

	/**
	 * Новый статус
	 */
	@Column(name = "new_status", length = 50)
	private String newStatus;

	/**
	 * Дополнительные данные (JSON)
	 */
	@Column(name = "metadata", columnDefinition = "JSON")
	private String metadata;

	// ========== Фабричные методы ==========

	public static OrderHistory createStatusChange(Order order, String previousStatus,
												String newStatus, Long userId, String description) {
		return OrderHistory.builder()
				.order(order)
				.previousStatus(previousStatus)
				.newStatus(newStatus)
				.userId(userId)
				.eventDescription(description)
				.build();
	}

	public static OrderHistory createNote(Order order, Long userId, String note) {
		return OrderHistory.builder()
				.order(order)
				.userId(userId)
				.eventDescription(note)
				.build();
	}
}
