package by.bsuir.chatservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_message", indexes = {
	@Index(name = "idx_ticket_id", columnList = "ticket_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketMessage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ticket_id", nullable = false)
	private SupportTicket ticket;

	@Column(name = "sender_id", nullable = false)
	private Long senderId;

	@Column(name = "is_admin_reply")
	@Builder.Default
	private Boolean isAdminReply = false;

	@Column(name = "message_text", nullable = false, columnDefinition = "TEXT")
	private String messageText;

	@Column(name = "sent_at")
	@Builder.Default
	private LocalDateTime sentAt = LocalDateTime.now();

	@Column(name = "attachment_key")
	private String attachmentKey;

	@Column(name = "is_internal_note")
	@Builder.Default
	private Boolean isInternalNote = false;

	public boolean isFromAdmin() {
		return Boolean.TRUE.equals(isAdminReply);
	}

	public boolean isInternal() {
		return Boolean.TRUE.equals(isInternalNote);
	}

	public String getPreview() {
		if (messageText == null) return "";
		return messageText.length() > 150 ? messageText.substring(0, 150) + "..." : messageText;
	}
}
