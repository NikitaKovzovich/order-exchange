package by.bsuir.authservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(nullable = false)
	private String title;

	@Lob
	@Column(nullable = false)
	private String message;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 60)
	private NotificationType type;

	@Column(name = "is_read", nullable = false)
	@Builder.Default
	private Boolean isRead = false;

	@Column(name = "created_at", nullable = false)
	@Builder.Default
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(name = "related_entity_type", length = 50)
	private String relatedEntityType;

	@Column(name = "related_entity_id")
	private Long relatedEntityId;

	@Column(name = "order_id")
	private Long orderId;

	@Column(name = "order_number", length = 50)
	private String orderNumber;

	public enum NotificationType {
		REGISTRATION_SUBMITTED,
		VERIFICATION_APPROVED,
		VERIFICATION_REJECTED,
		USER_BLOCKED,
		USER_UNBLOCKED,
		PROFILE_UPDATED,
		TICKET_CREATED,
		TICKET_USER_REPLIED,
		SYSTEM,

		NEW_ORDER,
		ORDER_CONFIRMED,
		ORDER_REJECTED,
		INVOICE_ISSUED,
		PAYMENT_PROOF_UPLOADED,
		PAYMENT_CONFIRMED_RETAIL,
		PAYMENT_REJECTED,
		TTN_FORMED,
		ORDER_SHIPPED,
		DELIVERY_CONFIRMED,
		ACCEPTANCE_PROBLEM,
		CORRECTION_DELIVERY_CONFIRMED,
		CORRECTION_RESPONSE,
		CORRECTION_TTN_FORMED,
		ORDER_CLOSED
	}
}
