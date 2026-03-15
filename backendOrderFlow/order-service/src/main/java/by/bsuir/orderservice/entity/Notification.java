package by.bsuir.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;





@Entity
@Table(name = "notification", indexes = {
	@Index(name = "idx_notification_recipient", columnList = "recipient_id"),
	@Index(name = "idx_notification_read", columnList = "recipient_id, is_read"),
	@Index(name = "idx_notification_order", columnList = "order_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;




	@Column(name = "recipient_id", nullable = false)
	private Long recipientId;




	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 60)
	private NotificationType type;




	@Column(name = "title", nullable = false, length = 255)
	private String title;




	@Column(name = "message", nullable = false, columnDefinition = "TEXT")
	private String message;




	@Column(name = "order_id")
	private Long orderId;




	@Column(name = "order_number", length = 50)
	private String orderNumber;




	@Column(name = "is_read", nullable = false)
	@Builder.Default
	private boolean read = false;

	@Column(name = "created_at")
	@Builder.Default
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(name = "read_at")
	private LocalDateTime readAt;








	public enum NotificationType {

		PARTNERSHIP_REQUEST("Запрос на сотрудничество"),
		NEW_ORDER("Новый заказ"),
		PAYMENT_PROOF_UPLOADED("Загрузка подтверждения оплаты"),
		PAYMENT_CONFIRMED_NEXT_STEP("Оплата подтверждена"),
		DOCUMENTS_FORMED("Документы сформированы"),
		DELIVERY_CONFIRMED("Подтверждение получения товара"),
		ACCEPTANCE_PROBLEM("Проблема при приёмке (Акт расхождений)"),
		CORRECTION_TTN_FORMED("Корректировочная ТТН сформирована"),
		CORRECTION_DELIVERY_CONFIRMED("Подтверждение получения по корректировочной ТТН"),


		CONTRACT_CONFIRMED("Подтверждение договора"),
		ORDER_CONFIRMED("Заказ подтверждён"),
		ORDER_REJECTED("Заказ отклонён"),
		INVOICE_ISSUED("Выставлен счёт"),
		PAYMENT_CONFIRMED_RETAIL("Оплата подтверждена"),
		PAYMENT_REJECTED("Оплата отклонена"),
		ORDER_SHIPPED("Заказ отгружен (В пути)"),
		TTN_FORMED("ТТН сформирована"),
		CORRECTION_RESPONSE("Корректировка (ответ на Акт)"),
		ORDER_CLOSED("Заказ закрыт");

		private final String displayName;

		NotificationType(String displayName) {
			this.displayName = displayName;
		}

		public String getDisplayName() {
			return displayName;
		}
	}



	public void markAsRead() {
		this.read = true;
		this.readAt = LocalDateTime.now();
	}
}
