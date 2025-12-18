package by.bsuir.chatservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_channel", indexes = {
	@Index(name = "idx_order_id", columnList = "order_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatChannel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "order_id", nullable = false, unique = true)
	private Long orderId;

	@Column(name = "supplier_user_id", nullable = false)
	private Long supplierUserId;

	@Column(name = "customer_user_id", nullable = false)
	private Long customerUserId;

	@Column(name = "channel_name")
	private String channelName;

	@Column(name = "created_at")
	@Builder.Default
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(name = "is_active")
	@Builder.Default
	private Boolean isActive = true;

	@OneToMany(mappedBy = "channel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@Builder.Default
	private List<Message> messages = new ArrayList<>();

	public boolean isParticipant(Long userId) {
		return supplierUserId.equals(userId) || customerUserId.equals(userId);
	}

	public Long getOtherParticipant(Long userId) {
		if (supplierUserId.equals(userId)) {
			return customerUserId;
		} else if (customerUserId.equals(userId)) {
			return supplierUserId;
		}
		throw new IllegalArgumentException("User is not a participant of this channel");
	}

	public void deactivate() {
		this.isActive = false;
	}

	public int getMessageCount() {
		return messages.size();
	}

	public Message getLastMessage() {
		if (messages.isEmpty()) return null;
		return messages.get(messages.size() - 1);
	}
}
