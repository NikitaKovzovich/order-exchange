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
	@Column(nullable = false, length = 50)
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

	public enum NotificationType {
		REGISTRATION_SUBMITTED,
		VERIFICATION_APPROVED,
		VERIFICATION_REJECTED,
		USER_BLOCKED,
		USER_UNBLOCKED,
		PROFILE_UPDATED,
		SYSTEM
	}
}

