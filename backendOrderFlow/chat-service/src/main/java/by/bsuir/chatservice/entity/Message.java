package by.bsuir.chatservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "message", indexes = {
	@Index(name = "idx_channel_id", columnList = "channel_id"),
	@Index(name = "idx_sender_id", columnList = "sender_id"),
	@Index(name = "idx_sent_at", columnList = "sent_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "channel_id", nullable = false)
	private ChatChannel channel;

	@Column(name = "sender_id", nullable = false)
	private Long senderId;

	@Column(name = "message_text", nullable = false, columnDefinition = "TEXT")
	private String messageText;

	@Column(name = "sent_at", nullable = false)
	@Builder.Default
	private LocalDateTime sentAt = LocalDateTime.now();

	@Column(name = "is_read")
	@Builder.Default
	private Boolean isRead = false;

	@Column(name = "read_at")
	private LocalDateTime readAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "message_type")
	@Builder.Default
	private MessageType messageType = MessageType.TEXT;

	@Column(name = "attachment_key")
	private String attachmentKey;

	public enum MessageType {
		TEXT,
		SYSTEM,
		ATTACHMENT,
		STATUS_UPDATE
	}

	public void markAsRead() {
		if (!this.isRead) {
			this.isRead = true;
			this.readAt = LocalDateTime.now();
		}
	}

	public boolean isSentBy(Long userId) {
		return senderId.equals(userId);
	}
}
